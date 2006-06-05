package net.sf.spindle.core.scanning;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.build.BuilderMessages;
import net.sf.spindle.core.build.templates.TemplateFinder;
import net.sf.spindle.core.messages.DefaultTapestryMessages;
import net.sf.spindle.core.messages.PageloadMessages;
import net.sf.spindle.core.messages.ResolverMessages;
import net.sf.spindle.core.namespace.ComponentSpecificationResolver;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.resources.I18NResourceAcceptor;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.resources.LookupDepth;
import net.sf.spindle.core.resources.PathUtils;
import net.sf.spindle.core.resources.ResourceExtension;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.ISourceLocationInfo;
import net.sf.spindle.core.spec.PluginAssetSpecification;
import net.sf.spindle.core.spec.PluginBindingSpecification;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.spec.PluginContainedComponent;
import net.sf.spindle.core.spec.PluginInjectSpecification;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.types.IJavaTypeFinder;
import net.sf.spindle.core.util.Assert;

import org.apache.hivemind.Resource;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;

/**
 * A completely functional validator for scanning Tapestry specs.
 * 
 * @author glongman@gmail.com
 */
public class SpecificationValidator extends BaseValidator
{
    ITapestryProject fTapestryProject;

    IResourceRoot fContextRoot;

    IResourceRoot fClasspathRoot;

    boolean fPeformDeferredValidations = true;

    Map fTypeCache;

    public SpecificationValidator(IJavaTypeFinder finder, ITapestryProject project)
    {
        super(finder);
        Assert.isNotNull(project);
        fTapestryProject = project;
        fContextRoot = project.getWebContextLocation();
        fClasspathRoot = project.getClasspathRoot();
        Assert.isNotNull(fContextRoot);
        Assert.isNotNull(fClasspathRoot);
    }

    public SpecificationValidator(ITapestryProject project)
    {
        this(project, project);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.BaseValidator#findType(java.lang.String)
     */
    public IJavaType findType(Resource dependant, String fullyQualifiedName)
    {
        IJavaType result = getJavaTypeFinder().findType(fullyQualifiedName);
        fireTypeDependency(dependant, fullyQualifiedName, result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification,
     *      org.apache.tapestry.spec.IContainedComponent, core.parser.ISourceLocationInfo)
     */
    public boolean validateContainedComponent(IComponentSpecification specification,
            IContainedComponent component, ISourceLocationInfo info) throws ScannerException
    {
        ICoreNamespace use_namespace = (ICoreNamespace) ((PluginComponentSpecification) specification)
                .getNamespace();
        String type = component.getType();

        if (TapestryCore.isNull(type))
            // already caught by the scanner
            return true;

        if (type.startsWith(getDummyStringPrefix()))
            return true;

        checkForIncompatibleComponentName(type, info.getAttributeSourceLocation("type"));

        if (use_namespace == null)
        {
            addProblem(IProblem.ERROR, info.getAttributeSourceLocation("type"), ResolverMessages
                    .noSuchComponentTypeUnknown(type), true, IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);

            return false;
        }

        ComponentSpecificationResolver resolver = use_namespace.getComponentResolver();
        IComponentSpecification containedSpecification = resolver.resolve(type);

        if (containedSpecification == null)
        {
            int colonx = type.indexOf(':');
            String namespaceId = null;

            if (colonx > 0)
            {
                namespaceId = type.substring(0, colonx);
                type = type.substring(colonx + 1);
            }

            if (!TapestryCore.isNull(namespaceId))
            {
                ICoreNamespace sub_namespace = (ICoreNamespace) use_namespace
                        .getChildNamespace(namespaceId);
                if (sub_namespace == null)
                {
                    addProblem(
                            IProblem.ERROR,
                            info.getAttributeSourceLocation("type"),
                            DefaultTapestryMessages.unableToResolveNamespace(sub_namespace),
                            true,
                            IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);

                }
                else
                {
                    addProblem(
                            IProblem.ERROR,
                            info.getAttributeSourceLocation("type"),
                            ResolverMessages.noSuchComponentType(type, sub_namespace),
                            true,
                            IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);
                }

            }
            else
            {

                addProblem(
                        IProblem.ERROR,
                        info.getAttributeSourceLocation("type"),
                        ResolverMessages.noSuchComponentTypeUnknown(type),
                        true,
                        IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);

            }
            return false;
        }
        validateContainedComponentBindings(specification, containedSpecification, component, info);

        // if the contained is a framework component, extra validation might
        // occur
        // at the end of the
        // entire build!
        // TODO this will be replaced at some point
        // FrameworkComponentValidator.validateContainedComponent(
        // (ICoreResource) specification.getSpecificationLocation(),
        // ((PluginComponentSpecification) specification).getNamespace(),
        // type,
        // containedSpecification,
        // component,
        // info,
        // containedSpecification.getPublicId());

        return true;
    }

    protected void validateContainedComponentBindings(
            IComponentSpecification containerSpecification,
            IComponentSpecification containedSpecification, IContainedComponent component,
            ISourceLocationInfo sourceInfo) throws ScannerException
    {
        Collection bindingNames = component.getBindingNames();
        List<String> required = findRequiredParameterNames(containedSpecification);
        required.removeAll(bindingNames);
        if (!required.isEmpty())
        {
            addProblem(
                    IProblem.ERROR,
                    sourceInfo.getTagNameLocation(),
                    PageloadMessages.requiredParameterNotBound(
                            required.toString(),
                            containedSpecification.getSpecificationLocation().getName()),
                    true,
                    IProblem.COMPONENT_REQUIRED_PARAMETER_NOT_BOUND);
        }

        boolean formalOnly = !containedSpecification.getAllowInformalParameters();

        boolean containerFormalOnly = !containerSpecification.getAllowInformalParameters();

        String containedName = containedSpecification.getSpecificationLocation().getName();

        PluginContainedComponent useContainedComponent = ((PluginContainedComponent) component);
        ISourceLocation location = ((ISourceLocationInfo) useContainedComponent.getLocation())
                .getStartTagSourceLocation();
        if (useContainedComponent.getInheritInformalParameters())
        {
            if (formalOnly)
            {
                addProblem(
                        IProblem.ERROR,
                        location,
                        PageloadMessages.inheritInformalInvalidComponentFormalOnly(containedName),
                        false,
                        IProblem.NOT_QUICK_FIXABLE);
                return;
            }

            if (containerFormalOnly)
            {
                addProblem(IProblem.ERROR, location, PageloadMessages
                        .inheritInformalInvalidContainerFormalOnly(
                                containerSpecification,
                                containedSpecification)

                , false, IProblem.NOT_QUICK_FIXABLE);
                return;
            }
        }

        Iterator i = bindingNames.iterator();

        while (i.hasNext())
        {
            String name = (String) i.next();

            boolean isFormal = containedSpecification.getParameter(name) != null;

            PluginBindingSpecification bspec = (PluginBindingSpecification) component
                    .getBinding(name);

            ISourceLocationInfo bindingSrcInfo = (ISourceLocationInfo) bspec.getLocation();
            location = name.startsWith(getDummyStringPrefix()) ? bindingSrcInfo
                    .getTagNameLocation() : bindingSrcInfo.getAttributeSourceLocation("name");

            name = name.startsWith(getDummyStringPrefix()) ? null : name;
            // If not allowing informal parameters, check that each binding
            // matches
            // a formal parameter.

            if (formalOnly && !isFormal)
            {
                addProblem(IProblem.ERROR, location, PageloadMessages.formalParametersOnly(
                        containedName,
                        name), true, IProblem.COMPONENT_INFORMALS_NOT_ALLOWED);

                continue;
            }

            // If an informal parameter that conflicts with a reserved name,
            // then
            // skip it.

            if (!isFormal && containedSpecification.isReservedParameterName(name))
            {

                addProblem(
                        IProblem.WARNING,
                        location,
                        "ignoring binding '" + (name == null ? "'name not found'" : name)
                                + "'. trying to bind to reserved parameter.",
                        true,
                        IProblem.NOT_QUICK_FIXABLE);

                continue;
            }

            if (name == null)
                continue;
            // this is for bindings found in XML only!
            doValidateBinding(containerSpecification, containedSpecification, bspec, location, BuiltInBindingType.EXPRESSION);

        }

    }

    public void doValidateBinding(IComponentSpecification containerSpecification,
            IComponentSpecification containedSpecification, PluginBindingSpecification bspec, ISourceLocation location, BuiltInBindingType defaultBindingType) throws ScannerException
    {
        String name = bspec.getIdentifier();
        String value = bspec.getUnprefixedValue();

        BindingType type = bspec.getType();

        if (type == BindingType.PREFIXED)
        {

            BuiltInBindingType bindingType = BuiltInBindingType.get(bspec.getPrefix(), defaultBindingType);

            switch (bindingType)
            {
                case MESSAGE:
                case LISTENER:
                case HIVEMIND:
                case TRANSLATOR:
                case STATE:
                case VALIDATOR:
                case VALIDATORS: // all the above have no true validator yet - use the
                    // static one
                case LITERAL:
                    validateStaticBinding(containerSpecification, name, value, location);
                    break;
                case ASSET:
                    validateAssetBinding(containerSpecification, name, value, location);
                    break;
                case BEAN:
                    validateBeanBinding(containerSpecification, name, value, location);
                    break;
                case COMPONENT:
                    validateComponentBinding(containerSpecification, name, value, location);
                    break;
                case EXPRESSION:
                    validateExpression(value, IProblem.ERROR, location);
                    break;
                case UNKNOWN:
                    // Most often this is a custom prefix a dev has
                    // added via Hivemind. what to do?
                default:
                    break;
            }
        }
    }

    public void validateComponentBinding(IComponentSpecification containedSpecification,
            String bindingName, String value, ISourceLocation location) throws ScannerException
    {
        if (containedSpecification.getComponent(value) == null)
        {
            addProblem(
                    IProblem.ERROR,
                    location,
                    "TODO I18N - implicit contained component" + value,
                    true,
                    IProblem.IMPLICIT_COMPONENT_BINDING_MISSING_COMPONENT);
        }
    }

    public void validateBeanBinding(IComponentSpecification beanSource,
            String bindingName, String value, ISourceLocation location) throws ScannerException
    {
        if (beanSource.getBeanSpecification(value) == null)
        {
            addProblem(
                    IProblem.ERROR,
                    location,
                    "I18N - implicit bean, missing bean" + value,
                    true,
                    IProblem.IMPLICIT_BEAN_BINDING_MISSING_BEAN);
        }
    }

    public void validateAssetBinding(IComponentSpecification assetSource,
            String bindingName, String value, ISourceLocation location) throws ScannerException
    {

        if (assetSource.getAsset(value) == null)
        {
            addProblem(
                    IProblem.ERROR,
                    location,
                    "TODO I18N - implicit asset, missing asset" + value,
                    true,
                    IProblem.IMPLICIT_ASSET_BINDING_MISSING_ASSET);
        }
    }

    public void validateStaticBinding(IComponentSpecification containedSpecification,
            String bindingName, String value, ISourceLocation location) throws ScannerException
    {
        IParameterSpecification parameter = containedSpecification.getParameter(bindingName);

        if (parameter != null) // must be a formal parameter
        {
            String pType = parameter.getType();
            boolean allowed = true;
            if (pType != null)
            {
                if ("int".equals(pType))
                {
                    try
                    {
                        new Integer(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                }
                else if ("short".equals(pType))
                {
                    try
                    {
                        new Short(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                }
                else if ("boolean".equals(pType))
                {
                    allowed = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
                }
                else if ("long".equals(pType))
                {
                    try
                    {
                        new Long(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }

                }
                else if ("float".equals(pType))
                {
                    try
                    {
                        new Float(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                }
                else if ("double".equals(pType))
                {
                    try
                    {
                        new Double(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                }
                else if ("char".equals(pType))
                {
                }
                else if ("byte".equals(pType))
                {
                    try
                    {
                        new Byte(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                }
                else
                {
                    allowed = "String".equalsIgnoreCase(pType) || "java.lang.String".equals(pType)
                            || "Object".equalsIgnoreCase(pType) || "java.lang.Object".equals(pType);
                }

            }
            if (!allowed)
                addProblem(
                        IProblem.WARNING,
                        location,
                        "Parameter '" + bindingName + "' of '"
                                + containedSpecification.getSpecificationLocation().getName()
                                + "' expects bindings to be of type '" + pType + "'",
                        false,
                        IProblem.TEMPLATE_SCANNER_CHANGE_TO_EXPRESSION);
        }
    }

    private List<String> findRequiredParameterNames(IComponentSpecification spec)
    {
        // List result = new ArrayList();
        // for (Iterator iter = spec.getParameterNames().iterator();
        // iter.hasNext();)
        // {
        // String name = (String) iter.next();
        // IParameterSpecification pspec = spec.getParameter(name);
        // if (pspec.isRequired())
        // result.add(name);
        // }
        // return result;
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(((PluginComponentSpecification) spec).getRequiredParameterNames());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification,
     *      org.apache.tapestry.spec.IAssetSpecification, core.parser.ISourceLocationInfo)
     */
    public boolean validateAsset(IComponentSpecification specification, IAssetSpecification asset,
            ISourceLocationInfo sourceLocation) throws ScannerException
    {

        PluginAssetSpecification pAsset = (PluginAssetSpecification) asset;
        ICoreResource specLoc = (ICoreResource) specification.getSpecificationLocation();

        String path = pAsset.getPath();
        if (path == null)
            return true;

        int colonx = path.indexOf(':');

        if (colonx < 0)
            return true;

        String prefix = path.substring(0, colonx);
        String truePath = path.substring(colonx + 1);

        String assetSpecName = pAsset.getIdentifier();

        ResourceExtension root = null;
        ISourceLocation errorLoc = sourceLocation.getStartTagSourceLocation();
        if ("context".equals(prefix))
        {
            root = fContextRoot;
        }
        else if ("classpath".equals(prefix))
        {
            if (specLoc.isClasspathResource())
                root = specLoc;
            else
                root = fClasspathRoot;

        }
        else
        {
            addProblem(
                    IProblem.WARNING,
                    errorLoc,
                    "unrecognized asset prefix: " + prefix,
                    true,
                    IProblem.ASSET_UNRECOGNIZED_PREFIX);
            return true;
        }

        if (root == null)
            return true;

        if (errorLoc == null)
            errorLoc = sourceLocation.getTagNameLocation();

        if (TemplateFinder.TEMPLATE_ASSET_NAME.equals(pAsset.getIdentifier()))
            return checkTemplateAsset(specification, asset, prefix, truePath);

        ICoreResource relative = (ICoreResource) root.getRelativeResource(truePath);
        String fileName = relative.getName();

        if (!relative.exists())
        {
            ICoreResource[] I18NEquivalents = getI18NAssetEquivalents(relative, fileName);

            if (I18NEquivalents.length > 0)
            {
                int handleI18NPriority = TapestryCore.getDefault().getHandleAssetProblemPriority()
                        .getPriority();
                if (handleI18NPriority >= 0)
                {
                    addProblem(handleI18NPriority, errorLoc, BuilderMessages
                            .missingNonLocalizedAsset(assetSpecName
                                    .startsWith(getDummyStringPrefix()) ? "not specified"
                                    : assetSpecName, relative), true, IProblem.NOT_QUICK_FIXABLE);
                }
            }
            else
            {
                addProblem(IProblem.ERROR, errorLoc, BuilderMessages.missingNonLocalizedAsset(
                        assetSpecName.startsWith(getDummyStringPrefix()) ? "not specified"
                                : assetSpecName,
                        relative), true, IProblem.COMPONENT_MISSING_ASSET);

            }
            return false;
        }

        return true;
    }

    private I18NResourceAcceptor fI18NAcceptor = new I18NResourceAcceptor();

    private ICoreResource[] getI18NAssetEquivalents(ICoreResource baseLocation, String name)
    {

        try
        {
            fI18NAcceptor.configure(name);
            baseLocation.lookup(fI18NAcceptor, LookupDepth.INFINITE);
            return fI18NAcceptor.getResults();
        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
        }

        return new ICoreResource[] {};
    }

    private boolean checkTemplateAsset(IComponentSpecification specification,
            IAssetSpecification templateAsset, String prefix, String truePath)
            throws ScannerException
    {
        String templatePath = templateAsset.getPath();

        // set relative to the context by default!
        ICoreResource templateLocation = (ICoreResource) fContextRoot
                .getRelativeResource(templatePath);

        if (prefix == null)
        {
            addProblem(
                    IProblem.WARNING,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getTagNameLocation(),
                    "Spindle can't resolve templates from external assets",
                    true,
                    IProblem.TEMPLATE_FROM_EXTERNAL_ASSET);
            return false;
        }

        if ("classpath".equals(prefix))
            templateLocation = (ICoreResource) fClasspathRoot.getRelativeResource(templatePath);

        if (!templateLocation.exists())
        {
            String fileName = templateLocation.getName();
            // find the attribute source location for the error
            ISourceLocationInfo sourceLocation = (ISourceLocationInfo) templateAsset.getLocation();
            ISourceLocation errorLoc = sourceLocation.getStartTagSourceLocation();

            String assetSpecName = ((PluginAssetSpecification) templateAsset).getIdentifier();
            ICoreResource[] I18NEquivalents = getI18NAssetEquivalents(templateLocation, fileName);

            if (I18NEquivalents.length > 0)
            {
                int handleI18NPriority = TapestryCore.getDefault().getHandleAssetProblemPriority()
                        .getPriority();
                if (handleI18NPriority >= 0)
                {
                    addProblem(
                            handleI18NPriority,
                            errorLoc,
                            BuilderMessages.missingNonLocalizedAsset(assetSpecName
                                    .startsWith(getDummyStringPrefix()) ? "not specified"
                                    : assetSpecName, templateLocation),
                            true,
                            IProblem.NOT_QUICK_FIXABLE);
                }
            }
            else
            {
                addProblem(IProblem.ERROR, errorLoc, BuilderMessages.missingAsset(
                        assetSpecName.startsWith(getDummyStringPrefix()) ? "not specified"
                                : assetSpecName,
                        templateLocation), true, IProblem.COMPONENT_MISSING_ASSET);

            }
            return false;
        }
        return true;
    }

    public boolean validateLibraryResource(Resource specLocation, String path, String errorKey,
            ISourceLocation source) throws ScannerException
    {
        Resource useLocation = specLocation;

        if (!((ICoreResource) useLocation).isClasspathResource())
            useLocation = fClasspathRoot.getRelativeResource("/");

        return validateResource(useLocation, path, errorKey, source);
    }

    @Override
    public void validateXMLInject(PluginComponentSpecification spec,
            PluginInjectSpecification inject, ISourceLocationInfo sourceInfo)
            throws ScannerException
    {
        String objectValue = inject.getObject();
        if (objectValue == null)
            return;

        ISourceLocation objectLocation = sourceInfo.getAttributeSourceLocation("object");
        if (objectLocation == null)
            objectLocation = sourceInfo.getTagNameLocation();

        InjectType type = InjectType.get(inject.getType());
        switch (type)
        {
            case META:
                if (spec.getProperty(objectValue) == null)
                    addProblem(
                            IProblem.WARNING,
                            objectLocation,
                            "TODO ADD MESSAGE - missing mets",
                            true,
                            IProblem.INJECT_MISSING_META);
                break;
            case PAGE:
                ICoreNamespace ns = (ICoreNamespace) spec.getNamespace();
                if (ns.getPageResolver().resolve(objectValue) == null)
                    addProblem(
                            IProblem.ERROR,
                            objectLocation,
                            "TODO ADD MESSAGE - missing page",
                            true,
                            IProblem.INJECT_MISSING_PAGE);

            case SCRIPT:
                validateResourceLocation(
                        spec.getSpecificationLocation(),
                        objectValue,
                        "TODO CREATE MESSAGE",
                        objectLocation,
                        false);
                PathUtils utils = new PathUtils(objectValue);
                if (!"script".equals(utils.getFileExtension()))
                    addProblem(
                            IProblem.ERROR,
                            objectLocation,
                            "TODO ADD MESSAGE - .script",
                            true,
                            IProblem.INJECT_INCORRECT_SCRIPT_NAME);
            case STATE:
            case STATE_FLAG:
            case SPRING:
            case OBJECT:
            case UNKNOWN:               
            default:
                break;
        }
    }
}