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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.scanning;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.Direction;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IListenerBindingSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.IPropertySpecification;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.core.spec.bean.PluginMessageBeanInitializer;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Scanner that turns a node tree into a IComponentSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ComponentScanner extends SpecificationScanner
{
    boolean fIsPageSpec;
    boolean fSeenTemplateAsset;
    protected INamespace fNamespace;
    /* Don't need to throw an exception or add a problem here, the Parser will already have caught this
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(
     */
    protected Object beforeScan(Node rootNode) throws ScannerException
    {
        fIsPageSpec = isElement(rootNode, "page-specification");
        fSeenTemplateAsset = false;
        if (!(fIsPageSpec || isElement(rootNode, "component-specification")))
        {
            return null;
        }
        return specificationFactory.createComponentSpecification();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.processing.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan(Object resultObject, Node rootNode) throws ScannerException
    {
        IComponentSpecification specification = (IComponentSpecification) resultObject;

        specification.setPublicId(fParser.getPublicId());
        specification.setSpecificationLocation(fResourceLocation);
        ((PluginComponentSpecification) specification).setNamespace(fNamespace);

        // Only components specify these two attributes.

        specification.setAllowBody(getBooleanAttribute(rootNode, "allow-body"));
        specification.setAllowInformalParameters(getBooleanAttribute(rootNode, "allow-informal-parameters"));

        scanComponentSpecification(rootNode, specification, fIsPageSpec);
        scanForTemplates(specification);
    }

    public void setNamespace(INamespace namespace)
    {
        fNamespace = namespace;
    }

    protected void scanAsset(IComponentSpecification specification, Node node, AssetType type, String attributeName)
        throws ScannerException
    {
        String name = getAttribute(node, "name", true);
        String validateName = (name.startsWith(getDummyStringPrefix()) ? "" : name);

        if (specification.getAsset(name) != null)
        {

            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString(
                    "ComponentSpecification.duplicate-asset",
                    specification.getSpecificationLocation().getName(),
                    name));

        } else
        {

            if (!validateName.equals(ITemplateSource.TEMPLATE_ASSET_NAME))
                validatePattern(
                    validateName,
                    SpecificationParser.ASSET_NAME_PATTERN,
                    "SpecificationParser.invalid-asset-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"));

            String value = getAttribute(node, attributeName);
            IAssetSpecification asset = specificationFactory.createAssetSpecification();

            asset.setType(type);
            asset.setPath(value);

            ISourceLocationInfo location = getSourceLocationInfo(node);
            location.setResourceLocation(specification.getSpecificationLocation());
            asset.setLocation(location);

            specification.addAsset(name, asset);

            validateAsset(specification, asset, getSourceLocationInfo(node));

            scanPropertiesInNode(asset, node);
        }
    }

    /**
     *  @since 1.0.4
     *
     **/

    protected void scanBean(IComponentSpecification specification, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);
        if (specification.getBeanSpecification(name) != null)
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString(
                    "ComponentSpecification.duplicate-bean",
                    specification.getSpecificationLocation().getName(),
                    name));
        } else
        {
            validatePattern(
                name,
                SpecificationParser.BEAN_NAME_PATTERN,
                "SpecificationParser.invalid-bean-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));

            String className = getAttribute(node, "class", true);

            validateTypeName(className, IProblem.ERROR, getAttributeSourceLocation(node, "class"));

            String lifecycleString = getAttribute(node, "lifecycle");

            BeanLifecycle lifecycle = (BeanLifecycle) SpecificationScanner.conversionMap.get(lifecycleString);

            IBeanSpecification bspec = specificationFactory.createBeanSpecification();

            bspec.setClassName(className);
            bspec.setLifecycle(lifecycle);

            ISourceLocationInfo location = getSourceLocationInfo(node);
            location.setResourceLocation(specification.getSpecificationLocation());
            bspec.setLocation(location);

            specification.addBeanSpecification(name, bspec);

            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
            {
                if (isElement(child, "description"))
                {
                    bspec.setDescription(getValue(child));
                    continue;
                }

                if (isElement(child, "property"))
                {
                    scanProperty(bspec, child);
                    continue;
                }

                if (isElement(child, "set-property"))
                {
                    scanSetProperty(bspec, child);
                    continue;
                }

                if (isElement(child, "set-string-property"))
                {
                    scanSetMessageProperty(bspec, child);
                    continue;
                }

                if (isElement(child, "set-message-property"))
                {
                    scanSetMessageProperty(bspec, child);
                    continue;
                }

            }
        }
    }

    protected void scanBinding(IContainedComponent component, Node node, BindingType type, String attributeName)
        throws ScannerException
    {
        String name = getAttribute(node, "name", true, true);

        //        if (name.startsWith(getDummyStringPrefix()))
        //        {
        //
        //            addProblem(IProblem.ERROR, 
        //            getAttributeSourceLocation(node, "name"), 
        //            TapestryCore.getTapestryString());
        //        }

        String value = null;
        boolean fromAttribute = false;
        try
        {
            ExtendedAttributeResult result = getExtendedAttribute(node, attributeName, true);
            value = result.value;
            fromAttribute = result.fromAttribute;

        } catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), e.getMessage());
            value = getNextDummyString();
        }

        IBindingSpecification binding = specificationFactory.createBindingSpecification();
        binding.setType(type);
        binding.setValue(value);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResourceLocation(component.getLocation().getResourceLocation());
        binding.setLocation(location);

        if (type == BindingType.DYNAMIC)
        {
            ISourceLocation src =
                fromAttribute
                    ? getAttributeSourceLocation(node, attributeName)
                    : getBestGuessSourceLocation(node, true);
            validateExpression(value, IProblem.ERROR, src);
        }

        component.setBinding(name, binding);
    }

    protected void scanComponent(IComponentSpecification specification, Node node) throws ScannerException
    {
        String id = getAttribute(node, "id", true);

        if (specification.getComponent(id) != null)
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "id"),
                TapestryCore.getTapestryString(
                    "ComponentSpecification.duplicate-component",
                    specification.getSpecificationLocation().getName(),
                    id));
        } else
        {
            validatePattern(
                id,
                SpecificationParser.COMPONENT_ID_PATTERN,
                "SpecificationParser.invalid-component-id",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "id"));

            String type = getAttribute(node, "type");
            String copyOf = getAttribute(node, "copy-of");
            IContainedComponent c = null;

            if (type != null && copyOf != null)
            {

                addProblem(
                    IProblem.ERROR,
                    getNodeStartSourceLocation(node),
                    TapestryCore.getTapestryString("SpecificationParser.both-type-and-copy-of", id));
            } else
            {
                if (copyOf != null)
                {

                    c = specificationFactory.createContainedComponent();
                    IContainedComponent parent = specification.getComponent(copyOf);
                    if (parent == null)
                    {
                        c.setType(getNextDummyString());
                        c.setCopyOf(copyOf);
                        addProblem(
                            IProblem.ERROR,
                            getAttributeSourceLocation(node, "copy-of"),
                            TapestryCore.getTapestryString("SpecificationParser.unable-to-copy", copyOf));

                    } else
                    {
                        c.setType(parent.getType());
                        c.setCopyOf(copyOf);
                    }

                } else
                {
                    if (type == null)
                    {
                        addProblem(
                            IProblem.ERROR,
                            getNodeStartSourceLocation(node),
                            TapestryCore.getTapestryString("SpecificationParser.missing-type-or-copy-of", id));
                    } else
                    {

                        // In prior versions, its more free-form, because you can specify the path to
                        // a component as well.  In version 3, you must use an alias and define it
                        // in a library.

                        validatePattern(
                            type,
                            SpecificationParser.COMPONENT_TYPE_PATTERN,
                            "SpecificationParser.invalid-component-type",
                            IProblem.ERROR,
                            getAttributeSourceLocation(node, "type"));

                        c = specificationFactory.createContainedComponent();
                        c.setType(type);
                    }
                }

                if (c == null)
                {
                    c = specificationFactory.createContainedComponent();
                    c.setType(getNextDummyString());
                }

                ISourceLocationInfo location = getSourceLocationInfo(node);
                location.setResourceLocation(specification.getSpecificationLocation());
                c.setLocation(location);

                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
                {
                    if (isElement(child, "binding"))
                    {
                        scanBinding(c, child, BindingType.DYNAMIC, "expression");
                        continue;
                    }

                    // Field binding is in 1.3 DTD, but removed from 1.4

                    if (isElement(child, "field-binding"))
                    {
                        if (XMLUtil.getDTDVersion(fParser.getPublicId()) < XMLUtil.DTD_1_4)
                        {
                            scanBinding(c, child, BindingType.FIELD, "field-name");
                        } else
                        {
                            addProblem(
                                IProblem.ERROR,
                                getNodeStartSourceLocation(child),
                                "field-binding not supported in DTD 1.4 and up.");
                        }
                        continue;
                    }

                    if (isElement(child, "listener-binding"))
                    {
                        scanListenerBinding(c, child);
                        continue;
                    }

                    if (isElement(child, "inherited-binding"))
                    {
                        scanBinding(c, child, BindingType.INHERITED, "parameter-name");
                        continue;
                    }

                    if (isElement(child, "static-binding"))
                    {
                        scanBinding(c, child, BindingType.STATIC, "value");
                        continue;
                    }

                    // <string-binding> added in release 2.0.4

                    if (isElement(child, "string-binding"))
                    {
                        scanBinding(c, child, BindingType.STRING, "key");
                        continue;
                    }

                    if (isElement(child, "message-binding"))
                    {
                        scanBinding(c, child, BindingType.STRING, "key");
                        continue;
                    }

                    if (isElement(child, "property"))
                    {
                        scanProperty(c, child);
                        continue;
                    }
                }

                specification.addComponent(id, c);

                if (c.getCopyOf() == null)
                    validateContainedComponent(specification, c, getSourceLocationInfo(node));

            }
        }

    }

    protected void scanComponentSpecification(Node rootNode, IComponentSpecification specification, boolean isPage)
        throws ScannerException
    {

        String componentClassname = getAttribute(rootNode, "class");

        if (componentClassname == null)
        {
            if (fIsPageSpec)
            {

                specification.setComponentClassName("org.apache.tapestry.html.BasePage");
            } else
            {
                specification.setComponentClassName("org.apache.tapestry.BaseComponent");
            }

        } else
        {
            specification.setComponentClassName(componentClassname);
            validateTypeName(componentClassname, IProblem.ERROR, getAttributeSourceLocation(rootNode, "class"));
        }

        for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "parameter"))
            {
                scanParameter(specification, node);
                continue;
            }

            if (isElement(node, "reserved-parameter"))
            {
                if (isPage)
                {
                    addProblem(
                        IProblem.ERROR,
                        getNodeStartSourceLocation(node),
                        TapestryCore.getTapestryString(
                            "SpecificationParser.not-allowed-for-page",
                            "reserved-parameter"));
                } else
                {
                    scanReservedParameter(specification, node);
                }
                continue;
            }

            if (isElement(node, "bean"))
            {
                scanBean(specification, node);
                continue;
            }

            if (isElement(node, "component"))
            {
                scanComponent(specification, node);
                continue;
            }

            if (isElement(node, "external-asset"))
            {
                scanAsset(specification, node, AssetType.EXTERNAL, "URL");
                continue;
            }

            if (isElement(node, "context-asset"))
            {
                scanAsset(specification, node, AssetType.CONTEXT, "path");
                continue;
            }

            if (isElement(node, "private-asset"))
            {
                scanAsset(specification, node, AssetType.PRIVATE, "resource-path");
                continue;
            }

            if (isElement(node, "property"))
            {
                scanProperty(specification, node);
                continue;
            }

            if (isElement(node, "description"))
            {
                specification.setDescription(getValue(node));
                continue;
            }

            if (isElement(node, "property-specification"))
            {
                scanPropertySpecification(specification, node);
                continue;
            }
        }
    }

    protected void scanListenerBinding(IContainedComponent component, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        String language = getAttribute(node, "language");

        // The script itself is the character data wrapped by the element.

        String script = getValue(node);

        validateExpression(script, IProblem.ERROR, getBestGuessSourceLocation(node, true));

        IListenerBindingSpecification binding = specificationFactory.createListenerBindingSpecification();

        component.setBinding(name, binding);
        binding.setType(BindingType.LISTENER);
        binding.setLanguage(language);
        binding.setValue(script);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResourceLocation(component.getLocation().getResourceLocation());
        binding.setLocation(location);

        component.setBinding(name, binding);
    }

    protected void scanParameter(IComponentSpecification specification, Node node) throws ScannerException
    {

        String name = getAttribute(node, "name", true);

        if (specification.getParameter(name) != null)
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString(
                    "ComponentSpecification.duplicate-parameter",
                    specification.getSpecificationLocation().getName(),
                    name));
        } else
        {

            IParameterSpecification param = specificationFactory.createParameterSpecification();

            ISourceLocationInfo location = getSourceLocationInfo(node);
            location.setResourceLocation(specification.getSpecificationLocation());
            param.setLocation(location);

            validatePattern(
                name,
                SpecificationParser.PARAMETER_NAME_PATTERN,
                "SpecificationParser.invalid-parameter-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));

            String type = getAttribute(node, "type");

            // The attribute was called "java-type" in the 1.3 and earlier DTD

            if (type == null)
                type = getAttribute(node, "java-type");

            if (type == null)
                type = "java.lang.Object";

            param.setType(type);

            param.setRequired(getBooleanAttribute(node, "required"));

            String propertyName = getAttribute(node, "property-name");

            // If not specified, use the name of the parameter.

            if (propertyName == null)
            {
                propertyName = name;

                validatePattern(
                    propertyName,
                    SpecificationParser.PROPERTY_NAME_PATTERN,
                    "SpecificationParser.invalid-property-name",
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, name));

            }

            param.setPropertyName(propertyName);

            String direction = getAttribute(node, "direction");

            if (direction != null)
                param.setDirection((Direction) conversionMap.get(direction));

            specification.addParameter(name, param);

            Node child = node.getFirstChild();
            if (child != null && isElement(child, "description"))
                param.setDescription(getValue(child));

        }
    }

    /** @since 2.4 **/

    protected void scanPropertySpecification(IComponentSpecification spec, Node node) throws ScannerException
    {

        String name = getAttribute(node, "name", true);

        if (spec.getPropertySpecification(name) != null)
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString(
                    "ComponentSpecification.duplicate-property-specification",
                    spec.getSpecificationLocation().getName(),
                    name));
        } else
        {
            IPropertySpecification ps = specificationFactory.createPropertySpecification();

            ISourceLocationInfo location = getSourceLocationInfo(node);
            location.setResourceLocation(spec.getSpecificationLocation());
            ps.setLocation(location);

            validatePattern(
                name,
                SpecificationParser.PROPERTY_NAME_PATTERN,
                "SpecificationParser.invalid-property-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));

            ps.setName(name);

            String type = null;

            type = getAttribute(node, "type", true).trim();

            if (!typeList.contains(type))
            {
                validateTypeName(type, IProblem.ERROR, getAttributeSourceLocation(node, "type"));
            }

            ps.setType(type);

            boolean persistent = getBooleanAttribute(node, "persistent");

            ps.setPersistent(persistent);

            String initialValue = null;

            try
            {
                initialValue = getExtendedAttribute(node, "initial-value", false).value;
            } catch (ScannerException e1)
            {
                addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), e1.getMessage());
            }

            ps.setInitialValue(initialValue);

            spec.addPropertySpecification(ps);
        }
    }

    /**
     *  @since 1.0.5
     *
     **/

    protected void scanReservedParameter(IComponentSpecification spec, Node node)
    {
        String name = getAttribute(node, "name", true);

        if (!name.startsWith(getDummyStringPrefix()) && spec.isReservedParameterName(name))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                "duplicate reserved paramter name: " + name);
        }
        spec.addReservedParameterName(name);
    }

    /**
     *  This is a new simplified version structured around OGNL, in the 1.3 DTD.
     * 
     *  @since 2.2
     * 
     **/

    protected void scanSetProperty(IBeanSpecification spec, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        if (name.startsWith(getDummyStringPrefix()))
        {
            addProblem(
                IProblem.ERROR,
                getNodeStartSourceLocation(node),
                TapestryCore.getTapestryString("SpecificationParser.invalid-property-name", "not specified"));
        } else
        {

            validatePattern(
                name,
                SpecificationParser.PROPERTY_NAME_PATTERN,
                "SpecificationParser.invalid-property-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));
        }

        String expression = null;
        try
        {
            ExtendedAttributeResult result = getExtendedAttribute(node, "expression", true);
            expression = result.value;
            ISourceLocation src =
                result.fromAttribute
                    ? getAttributeSourceLocation(node, "expression")
                    : getBestGuessSourceLocation(node, true);
            validateExpression(expression, IProblem.ERROR, src);

        } catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), e.getMessage());
        }

        PluginExpressionBeanInitializer iz =
            (PluginExpressionBeanInitializer) specificationFactory.createExpressionBeanInitializer();
        iz.setPropertyName(name);
        iz.setExpression(expression);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResourceLocation(spec.getLocation().getResourceLocation());
        iz.setLocation(location);

        spec.addInitializer(iz);
    }

    /**
     *  String properties in the 1.3 DTD are handled a little differently.
     * 
     *  @since 2.2
     * 
     **/

    protected void scanSetMessageProperty(IBeanSpecification spec, Node node)
    {
        String name = getAttribute(node, "name");
        String key = getAttribute(node, "key");

        PluginMessageBeanInitializer iz =
            (PluginMessageBeanInitializer) specificationFactory.createMessageBeanInitializer();
        iz.setPropertyName(name);
        iz.setKey(key);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResourceLocation(spec.getLocation().getResourceLocation());
        iz.setLocation(location);

        spec.addInitializer(iz);
    }

    /**
     * @param IComponentSpecification the spec we are looking up templates on behalf of
     */
    public void scanForTemplates(IComponentSpecification specification)
    {
        IResourceWorkspaceLocation[] locations = new IResourceWorkspaceLocation[0];
        TemplateFinder finder = new TemplateFinder();
        try
        {
            locations = finder.getTemplates(specification, this);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        ((PluginComponentSpecification) specification).setTemplateLocations(locations);
    }

}
