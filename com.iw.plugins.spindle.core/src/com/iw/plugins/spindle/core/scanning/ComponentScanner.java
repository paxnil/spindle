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

import org.apache.tapestry.INamespace;
import org.apache.tapestry.binding.BindingConstants;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.parse.TapestryParseMessages;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IListenerBindingSpecification;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.PicassoMigration;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.IPluginDescribable;
import com.iw.plugins.spindle.core.spec.IPluginPropertyHolder;
import com.iw.plugins.spindle.core.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.core.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginContainedComponent;
import com.iw.plugins.spindle.core.spec.PluginDescriptionDeclaration;
import com.iw.plugins.spindle.core.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.core.spec.PluginPropertySpecification;
import com.iw.plugins.spindle.core.spec.PluginReservedParameterDeclaration;
import com.iw.plugins.spindle.core.spec.bean.PluginBindingBeanInitializer;

/**
 * Scanner that turns a node tree into a IComponentSpecification
 * 
 * @author glongman@gmail.com
 */
public class ComponentScanner extends SpecificationScanner
{
    boolean fIsPageSpec;

    boolean fSeenTemplateAsset;

    protected INamespace fNamespace;

    /*
     * Don't need to throw an exception or add a problem here, the Parser will already have caught
     * this
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        if (super.beforeScan(source) == null)
            return null;

        String extension = fStorage.getFullPath().getFileExtension();
        if (!extension.equals("jwc") && !extension.equals("page"))
            return null;

        fIsPageSpec = extension.equals("page");
        fSeenTemplateAsset = false;
        return fSpecificationFactory.createComponentSpecification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.processing.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {
        super.doScan(source, resultObject);
        IComponentSpecification specification = (IComponentSpecification) resultObject;

        specification.setPublicId(fPublicId);
        specification.setSpecificationLocation(fResourceLocation);
        specification.setPageSpecification(fIsPageSpec);
        specification.setLocation(getSourceLocationInfo(fRootNode));
        ((PluginComponentSpecification) specification).setNamespace(fNamespace);

        // Only components specify these two attributes.

        specification.setAllowBody(getBooleanAttribute(fRootNode, "allow-body"));
        specification.setAllowInformalParameters(getBooleanAttribute(
                fRootNode,
                "allow-informal-parameters"));

        verifyRootElement();

        scanComponentSpecification(specification);

    }

    private void verifyRootElement() throws ScannerException
    {
        String rootName = fRootNode.getNodeName();
        if (fIsPageSpec)
        {
            if (!rootName.equals("page-specification"))

                throw new ScannerException(TapestryCore.getTapestryString(
                        "AbstractDocumentParser.incorrect-document-type",
                        "page-specification",
                        rootName), getBestGuessSourceLocation(fRootNode, false), false,
                        IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_PAGE_SPECIFICATION);

        }
        else if (!rootName.equals("component-specification"))
        {
            throw new ScannerException(TapestryCore.getTapestryString(
                    "AbstractDocumentParser.incorrect-document-type",
                    "component-specification",
                    rootName), getBestGuessSourceLocation(fRootNode, false), false,
                    IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_COMPONENT_SPECIFICATION);
        }
    }

    public void setNamespace(INamespace namespace)
    {
        fNamespace = namespace;
    }

    protected boolean scanAsset(IComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (!fIsTapestry_4_0)
            return scanAsset_3_0(specification, node);

        if (!isElement(node, "asset"))
            return scanAsset(specification, node, "path", null);

        return true;
    }

    protected boolean scanAsset_3_0(IComponentSpecification specification, Node node)
            throws ScannerException
    {
        boolean result = false;
        if (isElement(node, "external-asset"))
            result = scanAsset(specification, node, "URL", null);

        if (isElement(node, "context-asset"))
            result = scanAsset(specification, node, "path", "context:");

        if (isElement(node, "private-asset"))
            result = scanAsset(specification, node, "resource-path", "classpath:");

        return result;
    }

    protected boolean scanAsset(IComponentSpecification specification, Node node,
            String pathAttributeName, String prefix) throws ScannerException
    {
        String name = getAttribute(node, "name", false);
        String value = getAttribute(node, pathAttributeName);
        String propertyName = getAttribute(node, "property", false);

        if (specification.getAsset(name) != null)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    TapestryCore.getTapestryString(
                            "ComponentSpecification.duplicate-asset",
                            specification.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_ASSET_ID);
        }

        if (name != null && !name.equals(PicassoMigration.TEMPLATE_ASSET_NAME))
            fValidator.validatePattern(
                    name,
                    SpecificationParser.ASSET_NAME_PATTERN,
                    "SpecificationParser.invalid-asset-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    IProblem.COMPONENT_INVALID_ASSET_NAME);

        if (propertyName != null)
            fValidator.validatePattern(
                    propertyName,
                    SpecificationParser.PROPERTY_NAME_PATTERN,
                    "SpecificationParser.invalid-property-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "property"),
                    IProblem.COMPONENT_INVALID_ASSET_PROPERTY_NAME);

        PluginAssetSpecification asset = (PluginAssetSpecification) fSpecificationFactory
                .createAssetSpecification();

        asset.setPath(value);
        asset.setPropertyName(propertyName);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        asset.setLocation(location);

        specification.addAsset(name, asset);

        asset.validate(specification, fValidator);

        scanPropertiesInNode(asset, node);

        return true;

    }

    /**
     * @since 1.0.4
     */

    protected boolean scanBean(IComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (isElement(node, "bean"))
            return false;

        String name = getAttribute(node, "name", true);

        // not revalidatable - error state would only change if the file changed!
        if (specification.getBeanSpecification(name) != null)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    TapestryCore.getTapestryString(
                            "ComponentSpecification.duplicate-bean",
                            specification.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_BEAN_ID);
        }

        fValidator.validatePattern(
                name,
                SpecificationParser.BEAN_NAME_PATTERN,
                "SpecificationParser.invalid-bean-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                IProblem.COMPONENT_INVALID_BEAN_NAME);

        // className is revalidatable (see below)
        String className = getAttribute(node, "class", false);

        String lifecycleString = getAttribute(node, "lifecycle");

        BeanLifecycle lifecycle = (BeanLifecycle) SpecificationScanner.TYPE_CONVERSION_MAP
                .get(lifecycleString);

        PluginBeanSpecification bspec = (PluginBeanSpecification) fSpecificationFactory
                .createBeanSpecification();

        bspec.setClassName(className);
        bspec.setLifecycle(lifecycle);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        bspec.setLocation(location);

        specification.addBeanSpecification(name, bspec);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "description"))
            {
                bspec.setDescription(getValue(child));
                //TODO record description declaration
                continue;
            }

            if (scanSet(bspec, child))
                continue;

            if (scanMeta((IPluginPropertyHolder) bspec, child))
                continue;
        }

        bspec.validate(specification, fValidator);
        return true;
    }

    private String getBindingName(IContainedComponent component, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true, true);

        if (name != null && component.getBinding(name) != null)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    "duplicate binding name '" + name + "'",
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_BINDING_NAME);
            // TODO I18N
        }

        if (name == null)
            name = "";

        fValidator.validatePattern(
                name,
                SpecificationParser.PARAMETER_NAME_PATTERN,
                "invalid-parameter-name",
                IProblem.ERROR,
                IProblem.NOT_QUICK_FIXABLE);
        return name;
    }

    protected boolean scanComponent(IComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (!isElement(node, "component"))
            return false;

        String id = getAttribute(node, "id", false);

        ISourceLocation idLoc = getAttributeSourceLocation(node, "id");

        // not revalidatable - error state would only change if the file changed!
        if (specification.getComponent(id) != null)
        {
            addProblem(IProblem.ERROR, idLoc, TapestryCore.getTapestryString(

            "ComponentSpecification.duplicate-component", specification.getSpecificationLocation()
                    .getName(), id), false, IProblem.COMPONENT_SPEC_DUPLICATE_COMPONENT_NAME);
        }

        fValidator.validatePattern(
                id,
                SpecificationParser.COMPONENT_ID_PATTERN,
                "invalid-component-id",
                IProblem.ERROR,
                idLoc,
                IProblem.COMPONENT_SPEC_INVALID_COMPONENT_ID);

        String type = getAttribute(node, "type");
        String copyOf = getAttribute(node, "copy-of");

        // all below is not revalidatable - error state would only change if the
        // file changed!
        if (type == null && copyOf == null)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), TapestryParseMessages
                    .missingTypeOrCopyOf(id), false, IProblem.COMPONENT_SPEC_MISSING_TYPE_COPY_OF);

        }
        else if (type != null && copyOf != null)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), TapestryParseMessages
                    .bothTypeAndCopyOf(id), false, IProblem.COMPONENT_SPEC_BOTH_TYPE_COPY_OF);
        }
        else if (copyOf != null)
        {
            IContainedComponent parentComponent = specification.getComponent(copyOf);
            if (parentComponent == null)
            {
                addProblem(
                        IProblem.ERROR,
                        getAttributeSourceLocation(node, "copy-of"),
                        TapestryParseMessages.unableToCopy(copyOf),
                        false,
                        IProblem.COMPONENT_SPEC_COPY_OF_MISSING);
            }
        }
        else
        {
            fValidator.validatePattern(
                    type,
                    SpecificationParser.COMPONENT_TYPE_PATTERN,
                    "invalid-component-type",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "type"),
                    IProblem.COMPONENT_SPEC_INVALID_COMPONENT_TYPE);
        }

        PluginContainedComponent component = (PluginContainedComponent) fSpecificationFactory
                .createContainedComponent();
        component.setType(type);
        component.setCopyOf(copyOf);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        component.setLocation(location);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            boolean consumed = false;
            if (!fIsTapestry_4_0)
            {
                if (isElement(node, "binding") && scanBinding_3_0(component, child))
                    continue;

                if (isElement(node, "listener-binding")
                        && scanListenerBinding_3_0(component, child))
                    continue;

                if (isElement(node, "message-binding") && scanMessageBinding_3_0(component, child))
                    continue;

                if (isElement(node, "static-binding") && scanStaticBinding_3_0(component, child))
                    continue;

                if (isElement(node, "inherited-binding")
                        && scanInhertiedBinding_3_0(component, child))
                    continue;
            }
            else if (isElement(node, "binding") && scanBinding_4_0(component, child))
                continue;

            if (scanMeta((IPluginPropertyHolder) component, child))
                continue;

        }

        specification.addComponent(id, component);

        // the revalidatable stuff
        component.validate(specification, fValidator);

        return true;

    }

    private boolean scanBinding_4_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String value = null;
        boolean fromAttribute = true;
        try
        {
            ExtendedAttributeResult result = getExtendedAttribute(node, "value", true);
            value = result.value;
            fromAttribute = result.fromAttribute;

        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
            value = "";
        }

        IBindingSpecification spec = fSpecificationFactory.createBindingSpecification();
        spec.setType(BindingType.PREFIXED);
        spec.setValue(value);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

        component.setBinding(name, spec);
        return true;
    }

    private boolean scanInhertiedBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String parameterName = getAttribute(node, "parameter-name");

        IBindingSpecification spec = fSpecificationFactory.createBindingSpecification();
        spec.setType(BindingType.INHERITED);
        spec.setValue(parameterName);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

        component.setBinding(name, spec);
        return true;
    }

    private boolean scanStaticBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String value = null;
        boolean fromAttribute = true;
        // must be done here - never revalidatable
        try
        {
            ExtendedAttributeResult result = getExtendedAttribute(node, "value", true);
            value = result.value;
            fromAttribute = result.fromAttribute;

        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
            value = "";
        }

        IBindingSpecification spec = fSpecificationFactory.createBindingSpecification();
        spec.setType(BindingType.PREFIXED);
        spec.setValue(BindingConstants.LITERAL_PREFIX + ":" + value);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

        ISourceLocation src = fromAttribute ? getAttributeSourceLocation(node, "value")
                : getBestGuessSourceLocation(node, true);

        component.setBinding(name, spec);
        return true;
    }

    private boolean scanMessageBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String key = getAttribute(node, "key");

        IBindingSpecification spec = fSpecificationFactory.createBindingSpecification();
        spec.setType(BindingType.PREFIXED);
        spec.setValue(BindingConstants.MESSAGE_PREFIX + ":" + key);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

        component.setBinding(name, spec);
        return true;
    }

    private boolean scanBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String expression = null;
        boolean fromAttribute = true;
        // must be done here - never revalidatable
        try
        {
            ExtendedAttributeResult result = getExtendedAttribute(node, "expression", true);
            expression = result.value;
            fromAttribute = result.fromAttribute;

        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
            expression = "";
        }

        IBindingSpecification spec = fSpecificationFactory.createBindingSpecification();
        spec.setType(BindingType.PREFIXED);
        spec.setValue(BindingConstants.OGNL_PREFIX + ":" + expression);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

        ISourceLocation src = fromAttribute ? getAttributeSourceLocation(node, "expression")
                : getBestGuessSourceLocation(node, true);
        //TODO validate bindings of all different types!
        fValidator.validateExpression(expression, IProblem.ERROR, src);

        component.setBinding(name, spec);
        return true;
    }

    protected void scanComponentSpecification(IComponentSpecification specification)
            throws ScannerException
    {

        String componentClassname = getAttribute(fRootNode, "class", false);

        PluginComponentSpecification pluginSpec = (PluginComponentSpecification) specification;

        if (componentClassname == null)
        {
            if (fIsPageSpec)

                specification.setComponentClassName(fPropertySource
                        .getPropertyValue("org.apache.tapestry.default-page-class"));

            else

                //TODO it appears that there is no default for components!
                specification.setComponentClassName("org.apache.tapestry.BaseComponent");

        }
        else
        {
            specification.setComponentClassName(componentClassname);
        }

        pluginSpec.validateSelf(fValidator);

        for (Node node = fRootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (scanParameter(specification, node))
                continue;

            if (scanReservedParameter(specification, node))
                continue;

            if (scanBean(specification, node))
                continue;

            if (scanComponent(specification, node))
                continue;

            if (scanMeta((IPluginPropertyHolder) specification, node))
                continue;

            if (scanDescription((IPluginDescribable) specification, node))
                continue;

            if (scanPropertySpecification(specification, node))
                continue;
        }
    }

    protected boolean scanDescription(IPluginDescribable describable, Node node)
    {
        if (!isElement(node, "description"))
            return false;

        String value = getValue(node);
        describable.setDescription(value);
        PluginDescriptionDeclaration declaration = new PluginDescriptionDeclaration(null, value,
                getSourceLocationInfo(node));
        describable.addDescriptionDeclaration(declaration);

        return true;
    }

    protected boolean scanListenerBinding_3_0(IContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);

        String language = getAttribute(node, "language");

        // The script itself is the character data wrapped by the element.

        String script = getValue(node);

        IListenerBindingSpecification binding = fSpecificationFactory
                .createListenerBindingSpecification();

        binding.setLanguage(language);
        binding.setValue(script);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        binding.setLocation(location);

        component.setBinding(name, binding);

        return true;
    }

    protected boolean scanParameter(IComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (isElement(node, "parameter"))
            return false;

        String name = getAttribute(node, "name", true);

        if (specification.getParameter(name) != null)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    TapestryCore.getTapestryString(
                            "ComponentSpecification.duplicate-parameter",
                            specification.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_PARAMETER_NAME);
        }

        PluginParameterSpecification param = (PluginParameterSpecification) fSpecificationFactory
                .createParameterSpecification();

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        param.setLocation(location);

        fValidator.validatePattern(
                name,
                SpecificationParser.PARAMETER_NAME_PATTERN,
                "SpecificationParser.invalid-parameter-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                IProblem.COMPONENT_INVALID_PARAMETER_NAME);

        String type = getAttribute(node, "type");
        param.setType(type == null ? "java.lang.Object" : type);

        param.setRequired(getBooleanAttribute(node, "required"));

        // In the 3.0 DTD, default-value was always an OGNL expression.
        // Starting with 4.0, it's like a binding (prefixed). For a 3.0
        // DTD, we supply the "ognl:" prefix.

        String defaultValue = getAttribute(node, "default-value");

        String prefix = fIsTapestry_4_0 ? null : BindingConstants.OGNL_PREFIX + ":";
        if (prefix != null)
            defaultValue = defaultValue == null ? prefix : prefix + defaultValue;

        fValidator.validateBindingReference(IProblem.ERROR, getAttributeSourceLocation(
                node,
                "default-value"), defaultValue);

        param.setDefaultValue(defaultValue);

        if (param.isRequired() && defaultValue != null)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "default-value"),
                    TapestryCore
                            .getTapestryString(
                                    "EstablishDefaultParameterValuesVisitor.parameter-must-have-no-default-value",
                                    specification.getSpecificationLocation().getName(),
                                    name),
                    false,
                    IProblem.COMPONENT_SPEC_REQUIRED_PARAMTER_MAY_NOT_HAVE_DEFAULT);
        }

        String propertyName = getAttribute(node, "property-name");

        // If not specified, use the name of the parameter.

        if (TapestryCore.isNull(propertyName))
        {
            propertyName = name;

        }
        else
        {
            fValidator.validatePattern(
                    propertyName,
                    SpecificationParser.PROPERTY_NAME_PATTERN,
                    "SpecificationParser.invalid-property-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, name),
                    IProblem.SPINDLE_INVALID_PROPERTY_ID);
        }

        param.setPropertyName(propertyName);

        String defaultBindingType = getAttribute(node, "default-binding");
        param.setDefaultBindingType(defaultBindingType);

        if (!fIsTapestry_4_0)
        {
            String direction = getAttribute(node, "direction");
            param.setCache(!"auto".equals(direction));

        }
        else
        {
            param.setCache(getBooleanAttribute(node, "cache"));
        }

        specification.addParameter(name, param);

        param.validate(specification, fValidator);

        Node child = node.getFirstChild();
        if (child != null)
            scanDescription(param, child);

        return true;

    }

    protected boolean scanPropertySpecification(IComponentSpecification spec, Node node)
            throws ScannerException
    {
        if (!fIsTapestry_4_0)
            return scanPropertySpecification_3_0(spec, node);

        if (!isElement(node, "property"))
            return false;
        return scanPropertySpecification(spec, node, "persist");
    }

    protected boolean scanPropertySpecification_3_0(IComponentSpecification spec, Node node)
            throws ScannerException
    {
        if (!isElement(node, "property-specification"))
            return false;

        return scanPropertySpecification(spec, node, "persistence");
    }

    protected boolean scanPropertySpecification(IComponentSpecification spec, Node node,
            String persistAttribute) throws ScannerException
    {

        if (!fIsTapestry_4_0 && !isElement(node, "property-specification"))
            return false;

        if (!isElement(node, "property"))
            return false;

        String name = getAttribute(node, "name", true);

        if (spec.getPropertySpecification(name) != null)
        {
            addProblem(IProblem.ERROR, getAttributeSourceLocation(node, "name"), TapestryCore
                    .getTapestryString(
                            "ComponentSpecification.duplicate-property-specification",
                            spec.getSpecificationLocation().getName(),
                            name), false, IProblem.SPINDLE_DUPLICATE_PROPERTY_ID);
        }

        PluginPropertySpecification ps = (PluginPropertySpecification) fSpecificationFactory
                .createPropertySpecification();

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(spec.getSpecificationLocation());
        ps.setLocation(location);

        fValidator.validatePattern(
                name,
                SpecificationParser.PROPERTY_NAME_PATTERN,
                "SpecificationParser.invalid-property-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                IProblem.SPINDLE_INVALID_PROPERTY_ID);

        ps.setName(name);

        String type = null;

        type = getAttribute(node, "type");

        if (type == null || type.trim().length() == 0)
            type = "java.lang.Object";

        ps.setType(type);

        String persistence = null;

        if (fIsTapestry_4_0)

            persistence = getAttribute(node, "persist");

        else
            persistence = getBooleanAttribute(node, "persistent", false) ? "session" : null;

        ps.setPersistence(persistence);

        //   must be done now - not revalidatable
        ExtendedAttributeResult result = null;
        String initialValue = null;
        try
        {
            result = getExtendedAttribute(node, "initial-value", false);
            initialValue = result.value;
        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
        }

        if (initialValue != null && !fIsTapestry_4_0)
            initialValue = BindingConstants.OGNL_PREFIX + ":" + initialValue;

        fValidator.validateBindingReference(
                IProblem.ERROR,
                result.fromAttribute ? getAttributeSourceLocation(node, "initial-value")
                        : getBestGuessSourceLocation(node, true),
                initialValue);

        ps.setInitialValue(initialValue);

        spec.addPropertySpecification(ps);

        ps.validate(spec, fValidator);

        return true;

    }

    /**
     * @since 1.0.5
     */

    protected boolean scanReservedParameter(IComponentSpecification spec, Node node)
    {
        if (!isElement(node, "reserved-parameter"))
            return false;

        if (fIsPageSpec)
        {
            addProblem(
                    IProblem.ERROR,
                    getNodeStartSourceLocation(node),
                    TapestryCore.getTapestryString(
                            "SpecificationParser.not-allowed-for-page",
                            "reserved-parameter"),
                    false,
                    IProblem.COMPONENT_RESERVED_PARAMETER_NOT_ALLOWED);
            return true;
        }

        String name = getAttribute(node, "name", false);

        //   not revalidatable - error state would only change if the file changed!
        if (name != null && spec.isReservedParameterName(name))

            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    "duplicate reserved parameter name: " + name,
                    false,
                    IProblem.COMPONENT_DUPLICATE_RESERVED_PARAMETER_NAME);

        PluginReservedParameterDeclaration declaration = new PluginReservedParameterDeclaration(
                name, getSourceLocationInfo(node));

        ((PluginComponentSpecification) spec).addReservedParameterDeclaration(declaration);

        spec.addReservedParameterName(name);
        return true;
    }

    protected boolean scanSet(IBeanSpecification bspec, Node childNode) throws ScannerException
    {
        if (!fIsTapestry_4_0)
        {
            if (isElement(childNode, "set-property"))
                return scanSet(bspec, childNode, "expression");

            if (isElement(childNode, "set-message"))
                return scanSet(bspec, childNode, "key");
        }
        else if (isElement(childNode, "set"))
        {
            return scanSet(bspec, childNode, "value");
        }
        return false;
    }

    protected boolean scanSet(IBeanSpecification bspec, Node childNode,
            String referenceAttributeName) throws ScannerException
    {
        String name = getAttribute(childNode, "name", false);

        checkPropertyName(childNode, name);

        ExtendedAttributeResult result = null;
        String value = null;
        try
        {
            result = getExtendedAttribute(childNode, referenceAttributeName, true);
            value = result.value;

            String prefix = null;
            if ("expression".equals(referenceAttributeName))

                prefix = BindingConstants.OGNL_PREFIX + ":";

            else if ("key".equals(referenceAttributeName))

                prefix = BindingConstants.MESSAGE_PREFIX + ":";

            if (prefix != null)
                value = value == null ? prefix : prefix + value;
        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
        }

        fValidator
                .validateBindingReference(
                        IProblem.ERROR,
                        result.fromAttribute ? getAttributeSourceLocation(
                                childNode,
                                referenceAttributeName) : getBestGuessSourceLocation(
                                childNode,
                                true),
                        value);

        PluginBindingBeanInitializer bi = new PluginBindingBeanInitializer();
        bi.setPropertyName(name);
        bi.setBindingReference(value);

        ISourceLocationInfo location = getSourceLocationInfo(childNode);
        location.setResource(bspec.getLocation().getResource());
        bi.setLocation(location);

        bspec.addInitializer(bi);

        return true;
    }

    private void checkPropertyName(Node node, String name) throws ScannerException
    {
        if (name == null || name.trim().length() == 0)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    TapestryCore.getTapestryString(
                            "SpecificationParser.invalid-property-name",
                            "not specified"),
                    false,
                    IProblem.NOT_QUICK_FIXABLE);
        }
        else
        {
            fValidator.validatePattern(
                    name,
                    SpecificationParser.PROPERTY_NAME_PATTERN,
                    "SpecificationParser.invalid-property-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    IProblem.SPINDLE_INVALID_PROPERTY_ID);
        }
    }

    public IType validateTypeSpecial(IResourceWorkspaceLocation dependant, String typeName,
            int severity, ISourceLocation location) throws ScannerException
    {
        String useName = typeName;
        if (useName.indexOf(".") < 0)
            useName = "java.lang." + useName;

        return fValidator.validateTypeName(dependant, useName, severity, location);

    }

    class TapestryDTD_3_0_Scanner
    {

        void doScan()
        {

        }

    }

    //  /**
    //  * This is a new simplified version structured around OGNL, in the 1.3 DTD.
    //  *
    //  * @since 2.2
    //  */
    //
    // protected boolean scanSetProperty_3_0(IBeanSpecification spec, Node node)
    //         throws ScannerException
    // {
    //     if (!isElement(node, "set-property"))
    //         return false;
    //
    //     String name = getAttribute(node, "name", false);
    //
    //     checkPropertyName(node, name);
    //
    //     ExtendedAttributeResult result = null;
    //     String expression = null;
    //     try
    //     {
    //         result = getExtendedAttribute(node, "expression", true);
    //         expression = result.value;
    //     }
    //     catch (ScannerException e)
    //     {
    //         addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
    //     }
    //
    //     // not revalidatable - error state would only change if the file changed!
    //     if (expression != null)
    //     {
    //         fValidator.validateExpression(
    //                 expression,
    //                 IProblem.ERROR,
    //                 result.fromAttribute ? getAttributeSourceLocation(node, "expression")
    //                         : getBestGuessSourceLocation(node, true));
    //     }
    //
    //     iz = (PluginExpressionBeanInitializer) fSpecificationFactory
    //             .createBindingBeanInitializer(null);
    //
    //     iz.setPropertyName(name);
    //     iz.setExpression(expression);
    //
    //     ISourceLocationInfo location = getSourceLocationInfo(node);
    //     location.setResource(spec.getLocation().getResourceLocation());
    //     iz.setLocation(location);
    //
    //     spec.addInitializer(iz);
    //
    //     return true;
    //
    // }

    // protected boolean scanSetMessageProperty_3_0(IBeanSpecification spec, Node node)
    //         throws ScannerException
    // {
    //     if (!isElement(node, "set-message-property"))
    //         return false;
    //
    //     String name = getAttribute(node, "name");
    //     String key = getAttribute(node, "key");
    //
    //     // not revalidatable - error state would only change if the file changed!
    //     checkPropertyName(node, name);
    //
    //     // not revalidatable - error state would only change if the file changed!
    //     if (key != null)
    //     {
    //         if (key.trim().length() == 0)
    //         {
    //             addProblem(
    //                     IProblem.ERROR,
    //                     getAttributeSourceLocation(node, "key"),
    //                     "key must not be empty",
    //                     false,
    //                     -1);
    //         }
    //     }
    //
    //     PluginMessageBeanInitializer iz = (PluginMessageBeanInitializer) fSpecificationFactory
    //             .createMessageBeanInitializer();
    //
    //     iz.setPropertyName(name);
    //     iz.setKey(key);
    //
    //     ISourceLocationInfo location = getSourceLocationInfo(node);
    //     location.setResource(spec.getLocation().getResource());
    //     iz.setLocation(location);
    //
    //     spec.addInitializer(iz);
    //
    //     return true;
    //
    // }

    //  protected void scanBinding(IContainedComponent component, Node node, BindingType type,
    //  String attributeName) throws ScannerException
    //{
    //String name = getAttribute(node, "name", true, true);
    //
    //if (name != null && component.getBinding(name) != null)
    //{
    //  addProblem(
    //          IProblem.ERROR,
    //          getAttributeSourceLocation(node, "name"),
    //          "duplicate binding name '" + name + "'",
    //          false,
    //          IProblem.COMPONENT_SPEC_DUPLICATE_BINDING_NAME);
    //  // TODO I18N
    //}
    //
    //if (name == null)
    //  name = "";
    //
    //String value = null;
    //boolean fromAttribute = true;
    //
    //if (type == BindingType.INHERITED || type == BindingType.STRING)
    //{
    //  value = getAttribute(node, attributeName);
    //}
    //else
    //{
    //  // must be done here - never revalidatable
    //  try
    //  {
    //      ExtendedAttributeResult result = getExtendedAttribute(node, attributeName, true);
    //      value = result.value;
    //      fromAttribute = result.fromAttribute;
    //
    //  }
    //  catch (ScannerException e)
    //  {
    //      int severity = IProblem.ERROR;
    //
    //      if (type == BindingType.STATIC)
    //          severity = IProblem.WARNING;
    //
    //      addProblem(IProblem.WARNING, e.getLocation(), e.getMessage(), false, e.getCode());
    //
    //      value = getNextDummyString();
    //  }
    //}
    //
    //PluginBindingSpecification binding = (PluginBindingSpecification) fSpecificationFactory
    //      .createBindingSpecification();
    //
    //binding.setType(type);
    //binding.setValue(value);
    //
    //ISourceLocationInfo location = getSourceLocationInfo(node);
    //location.setResource(component.getLocation().getResource());
    //binding.setLocation(location);
    //
    //// no point in making revalidatable - error state would only change if the
    //// file changed!
    //if (type == BindingType.DYNAMIC)
    //{
    //  ISourceLocation src = fromAttribute ? getAttributeSourceLocation(node, attributeName)
    //          : getBestGuessSourceLocation(node, true);
    //  fValidator.validateExpression(value, IProblem.ERROR, src);
    //}
    //
    //component.setBinding(name, binding);
    //}

}