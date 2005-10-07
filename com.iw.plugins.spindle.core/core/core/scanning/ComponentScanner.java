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

package core.scanning;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.binding.BindingConstants;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.BindingType;
import org.w3c.dom.Node;


import com.iw.plugins.spindle.messages.DefaultTapestryMessages;
import com.iw.plugins.spindle.messages.PageloadMessages;
import com.iw.plugins.spindle.messages.ParseMessages;

import core.PicassoMigration;
import core.TapestryCore;
import core.source.IProblem;
import core.source.ISourceLocation;
import core.source.ISourceLocationInfo;
import core.spec.PluginAssetSpecification;
import core.spec.PluginBeanSpecification;
import core.spec.PluginBindingSpecification;
import core.spec.PluginComponentSpecification;
import core.spec.PluginContainedComponent;
import core.spec.PluginInjectSpecification;
import core.spec.PluginListenerBindingSpecification;
import core.spec.PluginParameterSpecification;
import core.spec.PluginPropertySpecification;
import core.spec.PluginReservedParameterDeclaration;
import core.spec.bean.PluginBindingBeanInitializer;

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
     * @see core.scanning.AbstractScanner#doScan(
     */
    protected Object beforeScan() throws ScannerException
    {
        if (super.beforeScan() == null)
            return null;

        String name = fResourceLocation.getName();
        String extension = null;
        int index = name.lastIndexOf(".");
        if (index >= 0)
            name.substring(index + 1);
        else
            return null;

        if (!extension.equals("jwc") && !extension.equals("page"))
            return null;

        fIsPageSpec = extension.equals("page");
        fSeenTemplateAsset = false;
        return new PluginComponentSpecification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.processing.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan() throws ScannerException
    {
        super.doScan();
        PluginComponentSpecification specification = (PluginComponentSpecification) fResultObject;

        specification.setPublicId(fPublicId);
        specification.setSpecificationLocation(fResourceLocation);
        specification.setPageSpecification(fIsPageSpec);
        specification.setLocation(getSourceLocationInfo(fRootNode));
        specification.setNamespace(fNamespace);

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

                throw new ScannerException(ParseMessages.incorrectDocumentType(
                        "page-specification",
                        rootName), getBestGuessSourceLocation(fRootNode, false), false,
                        IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_PAGE_SPECIFICATION);

        }
        else if (!rootName.equals("component-specification"))
        {
            throw new ScannerException(ParseMessages.incorrectDocumentType(
                    "component-specification",
                    rootName), getBestGuessSourceLocation(fRootNode, false), false,
                    IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_COMPONENT_SPECIFICATION);
        }
    }

    public void setNamespace(INamespace namespace)
    {
        fNamespace = namespace;
    }

    protected boolean scanAsset(PluginComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (!fIsTapestry_4_0)
            return scanAsset_3_0(specification, node);

        if (!isElement(node, "asset"))
            return scanAsset(specification, node, "path", null);

        return true;
    }

    protected boolean scanAsset_3_0(PluginComponentSpecification specification, Node node)
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

    protected boolean scanAsset(PluginComponentSpecification specification, Node node,
            String pathAttributeName, String prefix) throws ScannerException
    {
        String name = getAttribute(node, "name", false);
        String value = getAttribute(node, pathAttributeName);
        String propertyName = getAttribute(node, "property", false);

        if (specification.getAsset(name) != null)
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    DefaultTapestryMessages.format(
                            "ComponentSpecification.duplicate-asset",
                            specification.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_ASSET_ID);

        if (name != null && !name.equals(PicassoMigration.TEMPLATE_ASSET_NAME))
            fValidator.validatePattern(
                    name,
                    SpecificationParser.ASSET_NAME_PATTERN,
                    "invalid-asset-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    IProblem.COMPONENT_INVALID_ASSET_NAME);

        if (propertyName != null)
            fValidator.validatePattern(
                    propertyName,
                    SpecificationParser.PROPERTY_NAME_PATTERN,
                    "invalid-property-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "property"),
                    IProblem.COMPONENT_INVALID_ASSET_PROPERTY_NAME);

        PluginAssetSpecification asset = new PluginAssetSpecification();

        asset.setPath(value);
        asset.setPropertyName(propertyName);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        asset.setLocation(location);

        specification.addAsset(name, asset);

        allowMeta(asset, node);

        return true;
    }

    /**
     * @since 1.0.4
     */

    protected boolean scanBean(PluginComponentSpecification specification, Node node)
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
                    DefaultTapestryMessages.format(
                            "ComponentSpecification.duplicate-bean",
                            specification.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_BEAN_ID);
        }

        fValidator.validatePattern(
                name,
                SpecificationParser.BEAN_NAME_PATTERN,
                "invalid-bean-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                IProblem.COMPONENT_INVALID_BEAN_NAME);

        // className is revalidatable (see below)
        String className = getAttribute(node, "class", false);

        String lifecycleString = getAttribute(node, "lifecycle");

        BeanLifecycle lifecycle = (BeanLifecycle) SpecificationScanner.TYPE_CONVERSION_MAP
                .get(lifecycleString);

        PluginBeanSpecification bspec = new PluginBeanSpecification();

        bspec.setClassName(className);
        bspec.setLifecycle(lifecycle);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        bspec.setLocation(location);

        specification.addBeanSpecification(name, bspec);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (scanDescription(bspec, child))
                continue;

            if (scanSet(bspec, child))
                continue;

            if (scanMeta(bspec, child))
                continue;
        }

        return true;
    }

    private String getBindingName(PluginContainedComponent component, Node node)
            throws ScannerException
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

    protected boolean scanComponent(PluginComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (!isElement(node, "component"))
            return false;

        String id = getAttribute(node, "id", false);

        ISourceLocation idLoc = getAttributeSourceLocation(node, "id");

        // not revalidatable - error state would only change if the file changed!
        if (specification.getComponent(id) != null)
        {
            addProblem(IProblem.ERROR, idLoc, DefaultTapestryMessages.format(
                    "ComponentSpecification.duplicate-component",
                    specification.getSpecificationLocation().getName(),
                    id), false, IProblem.COMPONENT_SPEC_DUPLICATE_COMPONENT_NAME);
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
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), ParseMessages
                    .missingTypeOrCopyOf(id), false, IProblem.COMPONENT_SPEC_MISSING_TYPE_COPY_OF);

        }
        else if (type != null && copyOf != null)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), ParseMessages
                    .bothTypeAndCopyOf(id), false, IProblem.COMPONENT_SPEC_BOTH_TYPE_COPY_OF);
        }
        else if (copyOf != null)
        {
            PluginContainedComponent parentComponent = (PluginContainedComponent) specification
                    .getComponent(copyOf);
            if (parentComponent == null)
            {
                addProblem(
                        IProblem.ERROR,
                        getAttributeSourceLocation(node, "copy-of"),
                        ParseMessages.unableToCopy(copyOf),
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

        PluginContainedComponent contained = new PluginContainedComponent();
        contained.setType(type);
        contained.setCopyOf(copyOf);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        contained.setLocation(location);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (!fIsTapestry_4_0)
            {
                if (isElement(node, "binding") && scanBinding_3_0(contained, child))
                    continue;

                if (isElement(node, "listener-binding")
                        && scanListenerBinding_3_0(contained, child))
                    continue;

                if (isElement(node, "message-binding") && scanMessageBinding_3_0(contained, child))
                    continue;

                if (isElement(node, "static-binding") && scanStaticBinding_3_0(contained, child))
                    continue;

                if (isElement(node, "inherited-binding")
                        && scanInhertiedBinding_3_0(contained, child))
                    continue;
            }
            else if (isElement(node, "binding") && scanBinding_4_0(contained, child))
                continue;

            if (scanMeta(contained, child))
                continue;

        }

        specification.addComponent(id, contained);

        return true;

    }

    private boolean scanBinding_4_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String value = null;
        try
        {
            ExtendedAttributeResult result = getExtendedAttribute(node, "value", true);
            value = result.value;
        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
            value = "";
        }

        PluginBindingSpecification bindingSpec = new PluginBindingSpecification();
        bindingSpec.setType(BindingType.PREFIXED);
        bindingSpec.setValue(value);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        bindingSpec.setLocation(location);

        component.setBinding(name, bindingSpec);
        return true;
    }

    private boolean scanInhertiedBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String parameterName = getAttribute(node, "parameter-name");

        PluginBindingSpecification spec = new PluginBindingSpecification();
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

        PluginBindingSpecification spec = new PluginBindingSpecification();
        spec.setType(BindingType.PREFIXED);
        spec.setValue(BindingConstants.LITERAL_PREFIX + ":" + value);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

//        ISourceLocation src = fromAttribute ? getAttributeSourceLocation(node, "value")
//                : getBestGuessSourceLocation(node, true);

        component.setBinding(name, spec);
        return true;
    }

    private boolean scanMessageBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);
        String key = getAttribute(node, "key");

        PluginBindingSpecification spec = new PluginBindingSpecification();
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

        PluginBindingSpecification spec = new PluginBindingSpecification();
        spec.setType(BindingType.PREFIXED);
        spec.setValue(BindingConstants.OGNL_PREFIX + ":" + expression);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        spec.setLocation(location);

        ISourceLocation src = fromAttribute ? getAttributeSourceLocation(node, "expression")
                : getBestGuessSourceLocation(node, true);
        // TODO validate bindings of all different types!
        fValidator.validateExpression(expression, IProblem.ERROR, src);

        component.setBinding(name, spec);
        return true;
    }

    protected void scanComponentSpecification(PluginComponentSpecification specification)
            throws ScannerException
    {

        String componentClassname = getAttribute(fRootNode, "class", false);

        if (componentClassname == null)
        {
            if (fIsPageSpec)

                specification.setComponentClassName(fPropertySource
                        .getPropertyValue("org.apache.tapestry.default-page-class"));

            else

                // TODO it appears that there is no default for components!
                specification.setComponentClassName("org.apache.tapestry.BaseComponent");

        }
        else
        {
            specification.setComponentClassName(componentClassname);
        }

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

            if (scanMeta(specification, node))
                continue;

            if (scanDescription(specification, node))
                continue;

            if (scanPropertySpecification(specification, node))
                continue;
        }

        specification.validate(fValidator);
    }

    protected boolean scanListenerBinding_3_0(PluginContainedComponent component, Node node)
            throws ScannerException
    {
        String name = getBindingName(component, node);

        String language = getAttribute(node, "language");

        // The script itself is the character data wrapped by the element.

        String script = getValue(node);

        PluginListenerBindingSpecification binding = new PluginListenerBindingSpecification();

        binding.setLanguage(language);
        binding.setValue(script);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(component.getLocation().getResource());
        binding.setLocation(location);

        component.setBinding(name, binding);

        return true;
    }

    protected boolean scanInjectSpecification(PluginComponentSpecification specification, Node node)
            throws ScannerException
    {
        if (!isElement(node, "inject"))
            return false;

        String property = getAttribute(node, "property", false);

        fValidator.validatePattern(
                property,
                SpecificationParser.PROPERTY_NAME_PATTERN,
                "invalid-property-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "property"),
                IProblem.COMPONENT_INVALID_INJECT_PROPERTY_NAME);

        String type = getAttribute(node, "type", false);
        String objectReference = getAttribute(node, "object", false);

        PluginInjectSpecification inject = new PluginInjectSpecification();
        inject.setProperty(property);
        inject.setType(type);
        inject.setObject(objectReference);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getLocation().getResource());
        inject.setLocation(location);

        specification.addInjectSpecification(inject);

        return true;
    }

    protected boolean scanParameter(PluginComponentSpecification specification, Node node)
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
                    DefaultTapestryMessages.format(
                            "ComponentSpecification.duplicate-parameter",
                            specification.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.COMPONENT_SPEC_DUPLICATE_PARAMETER_NAME);
        }

        PluginParameterSpecification param = new PluginParameterSpecification();

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        param.setLocation(location);

        fValidator.validatePattern(
                name,
                SpecificationParser.PARAMETER_NAME_PATTERN,
                "invalid-parameter-name",
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
                    PageloadMessages.parameterMustHaveNoDefaultValue(specification
                            .getSpecificationLocation().getName(), name),
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
                    "invalid-property-name",
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

        Node child = node.getFirstChild();
        if (child != null)
            scanDescription(param, child);

        return true;

    }

    protected boolean scanPropertySpecification(PluginComponentSpecification spec, Node node)
            throws ScannerException
    {
        if (!fIsTapestry_4_0)
            return scanPropertySpecification_3_0(spec, node);

        if (!isElement(node, "property"))
            return false;
        return scanPropertySpecification(spec, node, "persist");
    }

    protected boolean scanPropertySpecification_3_0(PluginComponentSpecification spec, Node node)
            throws ScannerException
    {
        if (!isElement(node, "property-specification"))
            return false;

        return scanPropertySpecification(spec, node, "persistence");
    }

    protected boolean scanPropertySpecification(PluginComponentSpecification spec, Node node,
            String persistAttribute) throws ScannerException
    {

        if (!fIsTapestry_4_0 && !isElement(node, "property-specification"))
            return false;

        if (!isElement(node, "property"))
            return false;

        String name = getAttribute(node, "name", true);

        if (spec.getPropertySpecification(name) != null)
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    DefaultTapestryMessages.format(
                            "ComponentSpecification.duplicate-property-specification",
                            spec.getSpecificationLocation().getName(),
                            name),
                    false,
                    IProblem.SPINDLE_DUPLICATE_PROPERTY_ID);
        }

        PluginPropertySpecification ps = new PluginPropertySpecification();

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(spec.getSpecificationLocation());
        ps.setLocation(location);

        fValidator.validatePattern(
                name,
                SpecificationParser.PROPERTY_NAME_PATTERN,
                "invalid-property-name",
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

        // must be done now - not revalidatable
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

        return true;

    }

    /**
     * @since 1.0.5
     */

    protected boolean scanReservedParameter(PluginComponentSpecification spec, Node node)
    {
        if (!isElement(node, "reserved-parameter"))
            return false;

        if (fIsPageSpec)
        {
            addProblem(
                    IProblem.ERROR,
                    getNodeStartSourceLocation(node),
                    ParseMessages.invalidAttribute("not-allowed-for-page", "reserved-parameter"),
                    false,
                    IProblem.COMPONENT_RESERVED_PARAMETER_NOT_ALLOWED);
            return true;
        }

        String name = getAttribute(node, "name", false);

        // not revalidatable - error state would only change if the file changed!
        if (name != null && spec.isReservedParameterName(name))

            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    "duplicate reserved parameter name: " + name,
                    false,
                    IProblem.COMPONENT_DUPLICATE_RESERVED_PARAMETER_NAME);

        PluginReservedParameterDeclaration declaration = new PluginReservedParameterDeclaration(
                name, getSourceLocationInfo(node));

        spec.addReservedParameterDeclaration(declaration);

        return true;
    }

    protected boolean scanSet(PluginBeanSpecification bspec, Node childNode)
            throws ScannerException
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

    protected boolean scanSet(PluginBeanSpecification bspec, Node childNode,
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
                    ParseMessages.invalidAttribute("invalid-property-name", "null value"),
                    false,
                    IProblem.NOT_QUICK_FIXABLE);
        }
        else
        {
            fValidator.validatePattern(
                    name,
                    SpecificationParser.PROPERTY_NAME_PATTERN,
                    "invalid-property-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    IProblem.SPINDLE_INVALID_PROPERTY_ID);
        }
    }

    // public IType validateTypeSpecial(ICoreResource dependant, String typeName,
    // int severity, ISourceLocation location) throws ScannerException
    // {
    // String useName = typeName;
    // if (useName.indexOf(".") < 0)
    // useName = "java.lang." + useName;
    //
    // return fValidator.validateTypeName(dependant, useName, severity, location);
    //
    // }
}