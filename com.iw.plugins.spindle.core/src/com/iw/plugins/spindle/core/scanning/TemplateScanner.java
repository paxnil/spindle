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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.ILocation;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.parse.AttributeType;
import org.apache.tapestry.parse.ITemplateParserDelegate;
import org.apache.tapestry.parse.TemplateAttribute;
import org.apache.tapestry.parse.TemplateParseException;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.parse.TemplateToken;
import org.apache.tapestry.parse.TokenType;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.SpecFactory;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.FrameworkComponentValidator;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.template.CoreOpenToken;
import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.spec.PluginBindingSpecfication;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginContainedComponent;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.Files;

/**
 *  Scanner for Tapestry templates. 
 * 
 *  It is assumed that the component we are scanning templates on behalf of
 *  has its namepsace already.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TemplateScanner extends AbstractScanner
{

    private static final int IMPLICIT_ID_PATTERN_ID_GROUP = 1;
    private static final int IMPLICIT_ID_PATTERN_TYPE_GROUP = 2;
    private static final int IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP = 4;
    private static final int IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP = 5;

    private Pattern _simpleIdPattern;
    private Pattern _implicitIdPattern;
    private PatternMatcher _patternMatcher;

    private PluginComponentSpecification fComponentSpec;
    private ICoreNamespace fNamespace;
    private CoreTemplateParser fParser;
    private ITemplateParserDelegate fParserDelegate = new ScannerDelegate();
    private List fSeenIds = new ArrayList();
    private SpecFactory fSpecificationFactory;
    private IResourceWorkspaceLocation fTemplateLocation;
    private String fContents;
    private boolean fPerformDeferredValidations = true;
    private String fEncoding;

    public void scanTemplate(
        PluginComponentSpecification spec,
        IResourceLocation templateLocation,
        IScannerValidator validator)
        throws ScannerException
    {
        Assert.isNotNull(spec);
        Assert.isNotNull(spec.getNamespace());
        fTemplateLocation = (IResourceWorkspaceLocation) templateLocation;
        fComponentSpec = spec;
        fNamespace = (ICoreNamespace) spec.getNamespace();
        fParser = new CoreTemplateParser();
        fParser.setProblemCollector(this);
        fSeenIds.clear();

        scan(templateLocation, validator);

    }

    public void scanTemplate(PluginComponentSpecification spec, String contents, IScannerValidator validator)
        throws ScannerException
    {
        Assert.isNotNull(spec);
        Assert.isNotNull(spec.getNamespace());
        fContents = contents;
        fComponentSpec = spec;
        fNamespace = (ICoreNamespace) spec.getNamespace();
        fParser = new CoreTemplateParser();
        fParser.setProblemCollector(this);
        fSeenIds.clear();

        scan(contents, validator);

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(java.lang.Object, java.lang.Object)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {

        char[] data = null;
        if (fContents != null)
        {
            data = fContents.toCharArray();
        } else
        {

            try
            {
                InputStream in = fTemplateLocation.getContents();
                data = Files.readFileToString(in, fEncoding == null ? "UTF-8" : fEncoding).toCharArray();
            } catch (CoreException e)
            {
                TapestryCore.log(e);
            } catch (IOException e)
            {
                TapestryCore.log(e);
            }
        }
        if (data == null)
            throw new ScannerException("null data!");

        List result = (List) resultObject;

        TemplateToken[] parseResults = null;

        try
        {
            parseResults = fParser.parse(data, fParserDelegate, fTemplateLocation);
        } catch (TemplateParseException e1)
        {
            // should never happen
            TapestryCore.log(e1);
        }

        if (parseResults == null)
            return;

        for (int i = 0; i < parseResults.length; i++)
        {
            if (parseResults[i].getType() == TokenType.OPEN)
            {
                scanOpenToken((CoreOpenToken) parseResults[i], result);
            }
        }
    }

    private void scanOpenToken(CoreOpenToken token, List result) throws ScannerException
    {
        String id = token.getId();
        PluginContainedComponent component = null;
        String componentType = token.getComponentType();

        if (componentType == null)
            component = (PluginContainedComponent) fComponentSpec.getComponent(id);
        else
            component = createImplicitComponent(token, id, componentType);

        // Make sure the template contains each component only once.

        if (fSeenIds.contains(id))
        {
            addProblem(
                IProblem.ERROR,
                getJWCIDLocation(token.getAttributesMap()),
                TapestryCore.getTapestryString(
                    "BaseComponent.multiple-component-references",
                    fComponentSpec.getSpecificationLocation().getName(),
                    id));
            return;
        }
        fSeenIds.add(id);

        if (componentType == null)
        {
            validateExplicitComponent(component, token);
        } else
        {
            validateImplicitComponent(component, token);
        }
    }

    private PluginContainedComponent createImplicitComponent(CoreOpenToken token, String id, String componentType)
    {
        PluginContainedComponent result = (PluginContainedComponent) fSpecificationFactory.createContainedComponent();
        result.setIdentifier(id);
        result.setType(componentType);

        Map attributesMap = token.getAttributesMap();

        if (attributesMap != null)
            for (Iterator iter = attributesMap.keySet().iterator(); iter.hasNext();)
            {
                String attrName = (String) iter.next();
                TemplateAttribute attr = (TemplateAttribute) attributesMap.get(attrName);
                result.setBinding(attrName, createImplicitBinding(attr));
            }

        return result;

    }

    private void validateImplicitComponent(PluginContainedComponent contained, CoreOpenToken token)
        throws ScannerException
    {
        IComponentSpecification containedSpecification = resolveComponentType(token.getComponentType());

        Collection bindingNames = contained.getBindingNames();
        List required = findRequiredParameterNames(containedSpecification);
        required.removeAll(bindingNames);
        if (!required.isEmpty())
        {
            addProblem(
                IProblem.ERROR,
                token.getEventInfo().getStartTagLocation(),
                TapestryCore.getTapestryString(
                    "PageLoader.required-parameter-not-bound",
                    required.toString(),
                    "'"+token.getComponentType()+"'"));
        }

        Iterator i = bindingNames.iterator();

        while (i.hasNext())
        {
            String name = (String) i.next();
            PluginBindingSpecfication bspec = (PluginBindingSpecfication) contained.getBinding(name);
            ISourceLocation location = getAttributeLocation(name, token.getEventInfo().getAttributeMap());

            BindingType bindingType = bspec.getType();

            if (bindingType == BindingType.DYNAMIC)
            {
                validateImplicitExpressionBinding(
                    bspec,
                    containedSpecification,
                    location.getLocationOffset(TemplateParser.OGNL_EXPRESSION_PREFIX.length()));
                continue;
            }

            if (bindingType == BindingType.STRING)
            {
                validateImplicitStringBinding(
                    bspec,
                    containedSpecification,
                    location.getLocationOffset(TemplateParser.LOCALIZATION_KEY_PREFIX.length()));
                continue;
            }

            if (bindingType == BindingType.STATIC)
                validateImplicitStaticBinding(bspec, containedSpecification, location);

        }

        if (fPerformDeferredValidations)
            FrameworkComponentValidator.validate(
                fTemplateLocation,
                fComponentSpec.getNamespace(),
                token.getComponentType(),
                containedSpecification,
                contained,
                token.getEventInfo());

    }

    private void validateImplicitExpressionBinding(
        PluginBindingSpecfication bspec,
        IComponentSpecification containedSpecification,
        ISourceLocation location)
        throws ScannerException
    {

        if (!validateExpression(bspec.getValue(), IProblem.ERROR, location))
            return;

        IParameterSpecification parameter = containedSpecification.getParameter(bspec.getIdentifier());

        boolean isFormal = parameter != null;

        boolean isAllowInformalParameters = containedSpecification.getAllowInformalParameters();

        if (!isFormal && !isAllowInformalParameters)
        {

            addProblem(
                IProblem.ERROR,
                location,
                TapestryCore.getTapestryString(
                    "PageLoader.formal-parameters-only",
                    containedSpecification.getSpecificationLocation().getName(),
                    bspec.getIdentifier()));
        }

    }

    private void validateImplicitStringBinding(
        PluginBindingSpecfication bspec,
        IComponentSpecification containedSpecification,
        ISourceLocation location)
    {
        validateImplicitStaticBinding(bspec, containedSpecification, location);
    }

    private void validateImplicitStaticBinding(
        PluginBindingSpecfication bspec,
        IComponentSpecification containedSpecification,
        ISourceLocation location)
    {
        IParameterSpecification parameter = containedSpecification.getParameter(bspec.getIdentifier());

        boolean isFormal = parameter != null;

        boolean isAllowInformalParameters = containedSpecification.getAllowInformalParameters();

        String value = bspec.getValue();

        if (isFormal)
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
                    } catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                } else if ("short".equals(pType))
                {
                    try
                    {
                        new Short(value);
                    } catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                } else if ("boolean".equals(pType))
                {
                    allowed = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
                } else if ("long".equals(pType))
                {
                    try
                    {
                        new Long(value);
                    } catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }

                } else if ("float".equals(pType))
                {
                    try
                    {
                        new Float(value);
                    } catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                } else if ("double".equals(pType))
                {
                    try
                    {
                        new Double(value);
                    } catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                } else if ("char".equals(pType))
                {} else if ("byte".equals(pType))
                {
                    try
                    {
                        new Byte(value);
                    } catch (NumberFormatException ex)
                    {
                        allowed = false;
                    }
                } else
                {
                    allowed =
                        "String".equalsIgnoreCase(pType)
                            || "java.lang.String".equals(pType)
                            || "Object".equalsIgnoreCase(pType)
                            || "java.lang.Object".equals(pType);
                }

            }
            if (!allowed)
                addProblem(
                    IProblem.WARNING,
                    location,
                    "Parameter '"
                        + bspec.getIdentifier()
                        + "' of '"
                        + containedSpecification.getSpecificationLocation().getName()
                        + "' expects bindings to be of type '"
                        + pType
                        + "'");
        }

        //        else
        //        {
        //            if (!isAllowInformalParameters)
        //                addProblem(
        //                    IProblem.ERROR,
        //                    location,
        //                    TapestryCore.getTapestryString(
        //                        "PageLoader.formal-parameters-only",
        //                        containedSpecification.getSpecificationLocation().getName(),
        //                        bspec.getIdentifier()));
        //        }

    }

    private IComponentSpecification resolveComponentType(String type)
    {
        ICoreNamespace namespace = (ICoreNamespace) fComponentSpec.getNamespace();
        ComponentSpecificationResolver resolver = namespace.getComponentResolver();
        IComponentSpecification containedSpecification = resolver.resolve(type);
        return containedSpecification;
    }

    private List findRequiredParameterNames(IComponentSpecification spec)
    {
        List result = new ArrayList();
        for (Iterator iter = spec.getParameterNames().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IParameterSpecification pspec = spec.getParameter(name);
            if (pspec.isRequired())
                result.add(name);
        }
        return result;
    }

    private IBindingSpecification createImplicitBinding(TemplateAttribute attr)
    {
        PluginBindingSpecfication result =
            (PluginBindingSpecfication) fSpecificationFactory.createBindingSpecification();
        result.setValue(attr.getValue());
        if (attr.getType() == AttributeType.OGNL_EXPRESSION)
        {
            result.setType(BindingType.DYNAMIC);
        } else if (attr.getType() == AttributeType.LOCALIZATION_KEY)
        {
            result.setType(BindingType.STRING);
        } else if (attr.getType() == AttributeType.LITERAL)
        {
            result.setType(BindingType.STATIC);
        }
        return result;
    }

    /**
     *  check bindings based on attributes in the template.
     * 
     *  this is for explicit components only!
     * 
     **/

    private void validateExplicitComponent(PluginContainedComponent component, CoreOpenToken token)
        throws ScannerException
    {
        IComponentSpecification spec = resolveComponentType(component.getType());

        Map attributes = token.getAttributesMap();

        if (attributes == null)
            return;

        Iterator i = attributes.entrySet().iterator();

        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();

            String name = (String) entry.getKey();
            TemplateAttribute attribute = (TemplateAttribute) entry.getValue();
            AttributeType type = attribute.getType();
            ISourceLocation location = getAttributeLocation(name, token.getEventInfo().getAttributeMap());

            if (type == AttributeType.OGNL_EXPRESSION)
            {
                validateExplicitExpressionBinding(component, spec, name, attribute.getValue(), location);
                continue;
            }

            if (type == AttributeType.LOCALIZATION_KEY)
            {
                validateExplicitStringBinding(component, spec, name, attribute.getValue(), location);
                continue;
            }

            if (type == AttributeType.LITERAL)
                validateExplicitStaticBinding(component, spec, name, attribute.getValue(), location);
        }
    }

    /**
     *  Check a template expression binding, look for errors related
     *  to reserved and informal parameters.
     *
     *  <p>It is an error to specify expression 
     *  bindings in both the specification
     *  and the template.
     * 
     **/

    private void validateExplicitExpressionBinding(
        PluginContainedComponent component,
        IComponentSpecification spec,
        String name,
        String expression,
        ISourceLocation location)
        throws ScannerException
    {

        if (!validateExpression(expression, IProblem.ERROR, location))
            return;

        // If matches a formal parameter name, allow it to be set
        // unless there's already a binding.

        boolean isFormal = (spec.getParameter(name) != null);

        if (isFormal)
        {
            if (component.getBinding(name) != null)
                addProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString(
                        "BaseComponent.dupe-template-expression",
                        name,
                        spec.getSpecificationLocation().getName(),
                        fComponentSpec.getSpecificationLocation().getName()));

            //            throw new ApplicationRuntimeException(
            //                Tapestry.format(
            //                    "BaseComponent.dupe-template-expression",
            //                    name,
            //                    component.getExtendedId(),
            //                    _loadComponent.getExtendedId()),
            //                component,
            //                location,
            //                null);
        } else
        {
            if (!spec.getAllowInformalParameters())
                addProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString(
                        "BaseComponent.template-expression-for-informal-parameter",
                        name,
                        spec.getSpecificationLocation().getName(),
                        fComponentSpec.getSpecificationLocation().getName()));

            //            throw new ApplicationRuntimeException(
            //                Tapestry.format(
            //                    "BaseComponent.template-expression-for-informal-parameter",
            //                    name,
            //                    component.getExtendedId(),
            //                    _loadComponent.getExtendedId()),
            //                component,
            //                location,
            //                null);

            // If the name is reserved (matches a formal parameter
            // or reserved name, caselessly), then skip it.

            if (spec.isReservedParameterName(name))
                addProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString(
                        "BaseComponent.template-expression-for-reserved-parameter",
                        name,
                        spec.getSpecificationLocation().getName(),
                        fComponentSpec.getSpecificationLocation().getName()));

            //            throw new ApplicationRuntimeException(
            //                Tapestry.format(
            //                    "BaseComponent.template-expression-for-reserved-parameter",
            //                    name,
            //                    component.getExtendedId(),
            //                    _loadComponent.getExtendedId()),
            //                component,
            //                location,
            //                null);
        }
    }

    /**
     *  Check a string binding, look for errors related
     *  to reserved and informal parameters.
     **/

    private void validateExplicitStringBinding(
        PluginContainedComponent component,
        IComponentSpecification spec,
        String name,
        String localizationKey,
        ISourceLocation location)
    {
        // If matches a formal parameter name, allow it to be set
        // unless there's already a binding.

        boolean isFormal = (spec.getParameter(name) != null);

        if (isFormal)
        {
            if (component.getBinding(name) != null)
                addProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString(
                        "BaseComponent.dupe-string",
                        name,
                        spec.getSpecificationLocation().getName(),
                        fComponentSpec.getSpecificationLocation().getName()));

            //                throw new ApplicationRuntimeException(
            //                    Tapestry.format(
            //                        "BaseComponent.dupe-string",
            //                        name,
            //                        component.getExtendedId(),
            //                        _loadComponent.getExtendedId()),
            //                    component,
            //                    location,
            //                    null);
        } else
        {
            if (!spec.getAllowInformalParameters())
            {

                addProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString(
                        "BaseComponent.template-expression-for-informal-parameter",
                        name,
                        spec.getSpecificationLocation().getName(),
                        fComponentSpec.getSpecificationLocation().getName()));

                return;
            }

            //                throw new ApplicationRuntimeException(
            //                    Tapestry.format(
            //                        "BaseComponent.template-expression-for-informal-parameter",
            //                        name,
            //                        component.getExtendedId(),
            //                        _loadComponent.getExtendedId()),
            //                    component,
            //                    location,
            //                    null);

            // If the name is reserved (matches a formal parameter
            // or reserved name, caselessly), then skip it.

            if (spec.isReservedParameterName(name))
                addProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString(
                        "BaseComponent.template-expression-for-reserved-parameter",
                        name,
                        spec.getSpecificationLocation().getName(),
                        fComponentSpec.getSpecificationLocation().getName()));

            //                throw new ApplicationRuntimeException(
            //                    Tapestry.format(
            //                        "BaseComponent.template-expression-for-reserved-parameter",
            //                        name,
            //                        component.getExtendedId(),
            //                        _loadComponent.getExtendedId()),
            //                    component,
            //                    location,
            //                    null);
        }

    }

    /**
     *  Check a static binding, look for errors related
     *  to reserved and informal parameters.
     * 
     *  <p>
     *  Static bindings that conflict with bindings in the
     *  specification are warned.
     * 
     *
     **/

    private void validateExplicitStaticBinding(
        PluginContainedComponent component,
        IComponentSpecification spec,
        String name,
        String staticValue,
        ISourceLocation location)
    {

        IBindingSpecification existing = component.getBinding(name);

        if (existing != null)
            return;

        // If matches a formal parameter name, allow it to be set
        // unless there's already a binding.

        boolean isFormal = (spec.getParameter(name) != null);

        if (!isFormal)
        {
            // Skip informal parameters if the component doesn't allow them.

            if (!spec.getAllowInformalParameters())
                return;

            // If the name is reserved (matches a formal parameter
            // or reserved name, caselessly), then skip it.

            if (spec.isReservedParameterName(name))
                return;
        }

    }

    private ISourceLocation getJWCIDLocation(Map attributesLocations)
    {
        return getAttributeLocation(TemplateParser.JWCID_ATTRIBUTE_NAME, attributesLocations);
    }

    private ISourceLocation getAttributeLocation(String key, Map attributesLocations)
    {

        ISourceLocation result;
        result = (ISourceLocation) findCaselessly(key, attributesLocations);
        if (result == null)
            result = BaseValidator.DefaultSourceLocation;
        return result;
    }

    private Object findCaselessly(String key, Map map)
    {
        Object result = map.get(key);

        if (result != null)
            return result;

        Iterator i = map.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();

            String entryKey = (String) entry.getKey();

            if (entryKey.equalsIgnoreCase(key))
                return entry.getValue();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#beforeScan(java.lang.Object)
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        return new ArrayList();
    }

    /**
     *  Sets the SpecFactory which instantiates Tapestry spec objects.
     * 
     **/

    public void setFactory(SpecFactory factory)
    {
        fSpecificationFactory = factory;
    }
    
    public void setEncoding(String encoding) {
        fEncoding = encoding;
    }
    
    public String getEncoding() {
        return fEncoding;
    }

    /**
     *  Returns the current SpecFactory which instantiates Tapestry spec objects.
     * 
     *  @since 1.0.9
     * 
     **/

    public SpecFactory getFactory()
    {
        return fSpecificationFactory;
    }

    private class ScannerDelegate implements ITemplateParserDelegate
    {

        /* (non-Javadoc)
        * @see org.apache.tapestry.parse.ITemplateParserDelegate#getAllowBody(java.lang.String, org.apache.tapestry.ILocation)
        */
        public boolean getAllowBody(String componentId, ILocation location)
        {
            IContainedComponent embedded = fComponentSpec.getComponent(componentId);
            if (embedded == null)
                throw new ApplicationRuntimeException(
                    TapestryCore.getTapestryString(
                        "no-such-component",
                        fComponentSpec.getSpecificationLocation(),
                        componentId));

            IComponentSpecification containedSpec = fNamespace.getComponentResolver().resolve(embedded.getType());
            if (containedSpec == null)
                throw new ApplicationRuntimeException(
                    TapestryCore.getTapestryString(
                        "no-such-component",
                        fComponentSpec.getSpecificationLocation(),
                        componentId));

            return containedSpec.getAllowBody();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getAllowBody(java.lang.String, java.lang.String, org.apache.tapestry.ILocation)
         */
        public boolean getAllowBody(String libraryId, String type, ILocation location)
        {
            if (libraryId != null)
            {
                INamespace namespace = fNamespace.getChildNamespace(libraryId);
                if (namespace == null)
                    throw new ApplicationRuntimeException(
                        "Unable to resolve " + TapestryCore.getTapestryString("Namespace.nested-namespace", libraryId));
            }

            IComponentSpecification spec = fNamespace.getComponentResolver().resolve(libraryId, type);
            if (spec == null)
                throw new ApplicationRuntimeException(
                    TapestryCore.getTapestryString(
                        "Namespace.no-such-component-type",
                        type,
                        libraryId == null ? TapestryCore.getString("project-namespace") : libraryId));

            return spec.getAllowBody();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getKnownComponent(java.lang.String)
         */
        public boolean getKnownComponent(String componentId)
        {
            return fComponentSpec.getComponent(componentId) != null;
        }

    }

    protected void cleanup()
    {
        // do nothing

    }

    /**
     * @return
     */
    public boolean getPerformDeferredValidations()
    {
        return fPerformDeferredValidations;
    }

    /**
     * @param b
     */
    public void setPerformDeferredValidations(boolean b)
    {
        fPerformDeferredValidations = b;
    }

}
