/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.scanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hivemind.Resource;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.IJavaType;
import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.PicassoMigration;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.I18NResourceAcceptor;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.IResourceRoot;
import com.iw.plugins.spindle.core.resources.ResourceExtension;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.messages.DefaultTapestryMessages;
import com.iw.plugins.spindle.messages.PageloadMessages;

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
     * @see com.iw.plugins.spindle.core.scanning.BaseValidator#findType(java.lang.String)
     */
    public Object findType(Resource dependant, String fullyQualifiedName)
    {
        IJavaType result = getJavaTypeFinder().findType(fullyQualifiedName);
        fireTypeDependency(dependant, fullyQualifiedName, result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification,
     *      org.apache.tapestry.spec.IContainedComponent,
     *      com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
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

        if (use_namespace == null)
        {
            addProblem(
                    IProblem.ERROR,
                    info.getAttributeSourceLocation("type"),
                    DefaultTapestryMessages.format(
                            "Namespace.no-such-component-type",
                            type,
                            "unknown"),
                    true,
                    IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);

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
                            "Unable to resolve "
                                    + DefaultTapestryMessages.format(
                                            "Namespace.nested-namespace",
                                            namespaceId),
                            true,
                            IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);

                }
                else
                {
                    addProblem(
                            IProblem.ERROR,
                            info.getAttributeSourceLocation("type"),
                            DefaultTapestryMessages.format(
                                    "Namespace.no-such-component-type",
                                    type,
                                    namespaceId),
                            true,
                            IProblem.COMPONENT_TYPE_DOES_NOT_EXIST);
                }

            }
            else
            {

                addProblem(
                        IProblem.ERROR,
                        info.getAttributeSourceLocation("type"),
                        DefaultTapestryMessages.format(
                                "Namespace.no-such-component-type",
                                type,
                                use_namespace.getNamespaceId()),
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
        List required = findRequiredParameterNames(containedSpecification);
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

        String containerName = containerSpecification.getSpecificationLocation().getName();
        String containedName = containedSpecification.getSpecificationLocation().getName();

        // if (contained.getInheritInformalParameters())
        // {
        // if (formalOnly)
        // {
        //
        // reportProblem(
        // IProblem.ERROR,
        // location,
        // DefaultTapestryMessages.format(
        // "PageLoader.inherit-informal-invalid-component-formal-only",
        // containedName));
        // return false;
        // }
        //
        // if (containerFormalOnly)
        // {
        // reportProblem(
        // IProblem.ERROR,
        // location,
        // DefaultTapestryMessages.format(
        // "PageLoader.inherit-informal-invalid-container-formal-only",
        // containerName,
        // containedName));
        // return false;
        // }
        // }

        Iterator i = bindingNames.iterator();

        while (i.hasNext())
        {
            String name = (String) i.next();

            boolean isFormal = containedSpecification.getParameter(name) != null;

            IBindingSpecification bspec = component.getBinding(name);

            ISourceLocationInfo bindingSrcInfo = (ISourceLocationInfo) bspec.getLocation();
            ISourceLocation location = name.startsWith(getDummyStringPrefix()) ? bindingSrcInfo
                    .getTagNameLocation() : bindingSrcInfo.getAttributeSourceLocation("name");

            name = name.startsWith(getDummyStringPrefix()) ? "'name not found'" : name;

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
                        "ignoring binding '" + name + "'. trying to bind to reserved parameter.",
                        true,
                        IProblem.NOT_QUICK_FIXABLE);

                continue;
            }

        }

    }

    private List findRequiredParameterNames(IComponentSpecification spec)
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
        ArrayList result = new ArrayList();
        result.addAll(((PluginComponentSpecification) spec).getRequiredParameterNames());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification,
     *      org.apache.tapestry.spec.IAssetSpecification,
     *      com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
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
            {
                root = specLoc;
            }
            else
            {
                root = fClasspathRoot;
            }
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

        if (PicassoMigration.TEMPLATE_ASSET_NAME.equals(pAsset.getIdentifier()))
            return checkTemplateAsset(specification, asset, prefix, truePath);

        ICoreResource relative = (ICoreResource) root.getRelativeResource(truePath);
        String fileName = relative.getName();

        if (!relative.exists())
        {
            ICoreResource[] I18NEquivalents = getI18NAssetEquivalents(relative, fileName);

            if (I18NEquivalents.length > 0)
            {
                int handleI18NPriority = TapestryCore.getDefault().getHandleAssetProblemPriority();
                if (handleI18NPriority >= 0)
                {
                    addProblem(handleI18NPriority, errorLoc, CoreMessages.format(
                            "scan-component-missing-asset-but-has-i18n",
                            assetSpecName.startsWith(getDummyStringPrefix()) ? "not specified"
                                    : assetSpecName,
                            relative.toString()), true, IProblem.NOT_QUICK_FIXABLE);
                }
            }
            else
            {
                addProblem(IProblem.ERROR, errorLoc, CoreMessages.format(
                        "scan-component-missing-asset",
                        assetSpecName.startsWith(getDummyStringPrefix()) ? "not specified"
                                : assetSpecName,
                        relative.toString()), true, IProblem.COMPONENT_MISSING_ASSET);

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
            baseLocation.lookup(fI18NAcceptor);
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
                int handleI18NPriority = TapestryCore.getDefault().getHandleAssetProblemPriority();
                if (handleI18NPriority >= 0)
                {
                    addProblem(handleI18NPriority, errorLoc, CoreMessages.format(
                            "scan-component-missing-asset-but-has-i18n",
                            assetSpecName.startsWith(getDummyStringPrefix()) ? "not specified"
                                    : assetSpecName,
                            templateLocation.toString()), true, IProblem.NOT_QUICK_FIXABLE);
                }
            }
            else
            {
                addProblem(IProblem.ERROR, errorLoc, CoreMessages.format(
                        "scan-component-missing-asset",
                        assetSpecName.startsWith(getDummyStringPrefix()) ? "not specified"
                                : assetSpecName,
                        templateLocation.toString()), true, IProblem.COMPONENT_MISSING_ASSET);

            }
            return false;
        }
        return true;
    }

    public boolean validateLibraryResource(Resource specLocation, String path, String errorKey,
            ISourceLocation source) throws ScannerException
    {
        Resource useLocation = specLocation;

        if (!((ICoreResource)useLocation).isClasspathResource())
            useLocation = fClasspathRoot.getRelativeResource("/");

        return validateResource(useLocation, path, errorKey, source);
    }
}