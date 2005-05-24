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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.Location;
import org.apache.hivemind.Resource;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.binding.BindingConstants;
import org.apache.tapestry.parse.ITemplateParserDelegate;
import org.apache.tapestry.parse.OpenToken;
import org.apache.tapestry.parse.TemplateParseException;
import org.apache.tapestry.parse.TemplateToken;
import org.apache.tapestry.parse.TokenType;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.SpecFactory;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.PicassoMigration;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.template.CoreOpenToken;
import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginContainedComponent;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.Files;
import com.iw.plugins.spindle.messages.DefaultTapestryMessages;
import com.iw.plugins.spindle.messages.ImplMessages;
import com.iw.plugins.spindle.messages.PageloadMessages;

/**
 * Scanner for Tapestry templates. It is assumed that the component we are scanning templates on
 * behalf of has its namepsace already.
 * 
 * @author glongman@gmail.com
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

    private String fComponentAttributeName;

    private ICoreNamespace fNamespace;

    private CoreTemplateParser fParser;

    private ITemplateParserDelegate fParserDelegate = new ScannerDelegate();

    private List fSeenIds = new ArrayList();

    /** @dperecated */
    private SpecFactory fSpecificationFactory;

    private IResourceWorkspaceLocation fTemplateLocation;

    private String fContents;

    private boolean fPerformDeferredValidations = true;

    private String fEncoding;

    private boolean fContainsImplicitComponents; // true if template contains

    // implicit components

    public void scanTemplate(PluginComponentSpecification spec, Resource templateLocation,
            String componentAttributeName, IScannerValidator validator) throws ScannerException
    {
        Assert.isNotNull(spec);
        Assert.isNotNull(spec.getNamespace());
        fTemplateLocation = (IResourceWorkspaceLocation) templateLocation;
        fComponentSpec = spec;
        fNamespace = (ICoreNamespace) spec.getNamespace();
        fParser = new CoreTemplateParser();
        fParser.setProblemCollector(this);
        fSeenIds.clear();
        fContainsImplicitComponents = false;
        fComponentAttributeName = componentAttributeName;
        scan(templateLocation, validator);

    }

    public boolean containsImplicitComponents()
    {
        return fContainsImplicitComponents;
    }

    public void scanTemplate(PluginComponentSpecification spec, String contents,
            IScannerValidator validator) throws ScannerException
    {
        Assert.isNotNull(spec);
        Assert.isNotNull(spec.getNamespace());
        fContents = contents;
        fComponentSpec = spec;
        fNamespace = (ICoreNamespace) spec.getNamespace();
        fParser = new CoreTemplateParser();
        fParser.setProblemCollector(this);
        fSeenIds.clear();
        fContainsImplicitComponents = false;

        scan(contents, validator);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(java.lang.Object,
     *      java.lang.Object)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {

        char[] data = null;
        if (fContents != null)
        {
            data = fContents.toCharArray();
        }
        else
        {

            try
            {
                InputStream in = fTemplateLocation.getContents();
                data = Files.readFileToString(in, fEncoding == null ? "UTF-8" : fEncoding)
                        .toCharArray();
            }
//            catch (CoreException e)
//            {
//                TapestryCore.log(e);
//            }
            catch (IOException e)
            {
                TapestryCore.log(e);
            }
        }
        if (data == null)
            throw new ScannerException("null data!", false, IProblem.NOT_QUICK_FIXABLE);

        List result = (List) resultObject;

        TemplateToken[] parseResults = null;

        try
        {
            parseResults = fParser.parse(data, fParserDelegate, fTemplateLocation);
        }
        catch (TemplateParseException e1)
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
            // gork bug [ 996411 ] NullPointerException (3.1.4) - requires a change in the tapestry
            // template parser!
            addProblem(
                    IProblem.ERROR,
                    getJWCIDLocation(token.getEventInfo().getAttributeMap()),
                    ImplMessages.multipleComponentReferences(fComponentSpec
                            .getSpecificationLocation().getName(), id),
                    false,
                    IProblem.TEMPLATE_SCANNER_DUPLICATE_ID);
            return;
        }
        fSeenIds.add(id);

        if (componentType == null)
            validateExplicitComponent(component, token);
        else
            validateImplicitComponent(component, token);

    }

    private PluginContainedComponent createImplicitComponent(CoreOpenToken token, String id,
            String componentType)
    {
        if (!fContainsImplicitComponents)
            fContainsImplicitComponents = true;

        PluginContainedComponent result = (PluginContainedComponent) fSpecificationFactory
                .createContainedComponent();
        result.setIdentifier(id);
        result.setType(componentType);

        Map attributesMap = token.getAttributesMap();

        addTemplateBindings(result, token);

        return result;

    }

    private void addTemplateBindings(PluginContainedComponent component, OpenToken token)
    {
        Map attributes = token.getAttributesMap();

        if (attributes == null)
            return;

        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();

            component.setBinding(name, createImplicitBinding(name, value));
        }
    }

    private String getDefaultBindingType(String parameterName, String metaDefaultBindingType)
    {
        String result = null;

        IParameterSpecification ps = fComponentSpec.getParameter(parameterName);

        if (ps != null)
            result = ps.getDefaultBindingType();

        if (result == null)
            result = metaDefaultBindingType;

        return result;
    }

    private IBindingSpecification createImplicitBinding(String name, String reference)
    {
        PluginBindingSpecification result = (PluginBindingSpecification) fSpecificationFactory
                .createBindingSpecification();

        result.setType(BindingType.PREFIXED);

        String prefix = getDefaultBindingType(name, BindingConstants.LITERAL_PREFIX);

        int colonx = reference.indexOf(':');

        if (colonx > 1)
        {
            result.setValue(reference);

        }
        else
        {

            result.setValue(prefix + ":" + reference);
        }

        return result;
    }

    private void validateImplicitComponent(PluginContainedComponent contained, CoreOpenToken token)
            throws ScannerException
    {
        IComponentSpecification containedSpecification = resolveComponentType(token
                .getComponentType());

        Collection bindingNames = contained.getBindingNames();
        List required = findRequiredParameterNames(containedSpecification);
        required.removeAll(bindingNames);
        if (!required.isEmpty())
        {
            addProblem(
                    IProblem.ERROR,
                    token.getEventInfo().getStartTagLocation(),
                    TapestryPageloadMessages.requiredParameterNotBound(required.toString(), token
                            .getComponentType()),
                    //                    TapestryCore.getTapestryString(
                    //                            "PageLoader.required-parameter-not-bound",
                    //                            required.toString(),
                    //                            "'" + token.getComponentType() + "'"),
                    false,
                    IProblem.TEMPLATE_SCANNER_REQUIRED_PARAMETER_NOT_BOUND);
        }

        Iterator i = bindingNames.iterator();

        while (i.hasNext())
        {
            String name = (String) i.next();
            PluginBindingSpecification bspec = (PluginBindingSpecification) contained
                    .getBinding(name);
            ISourceLocation location = getAttributeLocation(name, token.getEventInfo()
                    .getAttributeMap());

            BindingType bindingType = bspec.getType();

            if (bindingType == BindingType.DYNAMIC)
            {
                validateImplicitExpressionBinding(bspec, containedSpecification, location
                        .getLocationOffset(PicassoMigration.OGNL_EXPRESSION_PREFIX.length()));
                continue;
            }

            if (bindingType == BindingType.STRING)
            {
                validateImplicitStringBinding(bspec, containedSpecification, location
                        .getLocationOffset(PicassoMigration.LOCALIZATION_KEY_PREFIX.length()));
                continue;
            }

            if (bindingType == BindingType.STATIC)
                validateImplicitStaticBinding(bspec, containedSpecification, location);

        }

        //TODO deferred validations are to be revisited

        //        if (fPerformDeferredValidations)
        //            FrameworkComponentValidator.validateImplictComponent(
        //                    (IResourceWorkspaceLocation) fComponentSpec.getSpecificationLocation(),
        //                    fTemplateLocation,
        //                    fComponentSpec.getNamespace(),
        //                    token.getComponentType(),
        //                    containedSpecification,
        //                    contained,
        //                    token.getEventInfo(),
        //                    containedSpecification.getPublicId());

    }

    private void validateImplicitExpressionBinding(PluginBindingSpecification bspec,
            IComponentSpecification containedSpecification, ISourceLocation location)
            throws ScannerException
    {

        if (!fValidator.validateExpression(bspec.getValue(), IProblem.ERROR, location))
            return;

        IParameterSpecification parameter = containedSpecification.getParameter(bspec
                .getIdentifier());

        boolean isFormal = parameter != null;

        boolean isAllowInformalParameters = containedSpecification.getAllowInformalParameters();

        if (!isFormal && !isAllowInformalParameters)
        {

            addProblem(IProblem.ERROR, location, PageloadMessages.formalParametersOnly(
                    containedSpecification.getSpecificationLocation().getName(),
                    bspec.getIdentifier()), false, IProblem.TEMPLATE_SCANNER_NO_INFORMALS_ALLOWED);
        }

    }

    private void validateImplicitStringBinding(PluginBindingSpecification bspec,
            IComponentSpecification containedSpecification, ISourceLocation location)
    {
        validateImplicitStaticBinding(bspec, containedSpecification, location);
    }

    private void validateImplicitStaticBinding(PluginBindingSpecification bspec,
            IComponentSpecification containedSpecification, ISourceLocation location)
    {
        IParameterSpecification parameter = containedSpecification.getParameter(bspec
                .getIdentifier());

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
                        "Parameter '" + bspec.getIdentifier() + "' of '"
                                + containedSpecification.getSpecificationLocation().getName()
                                + "' expects bindings to be of type '" + pType + "'",
                        false,
                        IProblem.TEMPLATE_SCANNER_CHANGE_TO_EXPRESSION);
        }
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
        ArrayList result = new ArrayList();
        result.addAll(((PluginComponentSpecification) spec).getRequiredParameterNames());
        return result;
    }

    /**
     * check bindings based on attributes in the template. this is for explicit components only!
     */

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
            String value = (String) entry.getValue();

            ISourceLocation location = getAttributeLocation(name, token.getEventInfo()
                    .getAttributeMap());

            if (type == AttributeType.OGNL_EXPRESSION)
            {
                validateExplicitExpressionBinding(
                        component,
                        spec,
                        name,
                        attribute.getValue(),
                        location);
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
     * Check a template expression binding, look for errors related to reserved and informal
     * parameters.
     * <p>
     * It is an error to specify expression bindings in both the specification and the template.
     */

    private void validateExplicitExpressionBinding(PluginContainedComponent component,
            IComponentSpecification spec, String name, String expression, ISourceLocation location)
            throws ScannerException
    {

        if (!fValidator.validateExpression(expression, IProblem.ERROR, location))
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
                                fComponentSpec.getSpecificationLocation().getName()),
                        false,
                        IProblem.TEMPLATE_SCANNER_BOUND_IN_BOTH_SPEC_AND_TEMPLATE);

        }
        else
        {
            if (!spec.getAllowInformalParameters())
                addProblem(
                        IProblem.ERROR,
                        location,
                        TapestryCore.getTapestryString(
                                "BaseComponent.template-expression-for-informal-parameter",
                                name,
                                spec.getSpecificationLocation().getName(),
                                fComponentSpec.getSpecificationLocation().getName()),
                        false,
                        IProblem.TEMPLATE_SCANNER_NO_INFORMALS_ALLOWED);

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
                                fComponentSpec.getSpecificationLocation().getName()),
                        false,
                        IProblem.TEMPLATE_SCANNER_TEMPLATE_EXPRESSION_FOR_RESERVED_PARM);

        }
    }

    /**
     * Check a string binding, look for errors related to reserved and informal parameters.
     */

    private void validateExplicitStringBinding(PluginContainedComponent component,
            IComponentSpecification spec, String name, String localizationKey,
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
                        TapestryCore.getTapestryString("BaseComponent.dupe-string", name, spec
                                .getSpecificationLocation().getName(), fComponentSpec
                                .getSpecificationLocation().getName()),
                        false,
                        IProblem.TEMPLATE_SCANNER_FORMAL_STRING_BINDING_ALREADY_BOUND_IN_SPEC);

        }
        else
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
                                fComponentSpec.getSpecificationLocation().getName()),
                        false,
                        IProblem.TEMPLATE_SCANNER_NO_INFORMALS_ALLOWED);

                return;
            }

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
                                fComponentSpec.getSpecificationLocation().getName()),
                        false,
                        IProblem.TEMPLATE_SCANNER_TEMPLATE_EXPRESSION_FOR_RESERVED_PARM);

        }

    }

    /**
     * Check a static binding, look for errors related to reserved and informal parameters.
     * <p>
     * Static bindings that conflict with bindings in the specification are warned.
     */

    private void validateExplicitStaticBinding(PluginContainedComponent component,
            IComponentSpecification spec, String name, String staticValue, ISourceLocation location)
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
        return getAttributeLocation(
                fParserDelegate.getComponentAttributeName(),
                attributesLocations);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#beforeScan(java.lang.Object)
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        return new ArrayList();
    }

    /**
     * @deprecated
     * Sets the SpecFactory which instantiates Tapestry spec objects.
     */

    public void setFactory(SpecFactory factory)
    {
        fSpecificationFactory = factory;
    }

    public void setEncoding(String encoding)
    {
        fEncoding = encoding;
    }

    public String getEncoding()
    {
        return fEncoding;
    }

    /**
     * Returns the current SpecFactory which instantiates Tapestry spec objects.
     * 
     * @since 1.0.9
     */

    public SpecFactory getFactory()
    {
        return fSpecificationFactory;
    }

    private class ScannerDelegate implements ITemplateParserDelegate
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getComponentAttributeName()
         */
        public String getComponentAttributeName()
        {
            return fComponentAttributeName;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getAllowBody(java.lang.String,
         *      org.apache.tapestry.ILocation)
         */
        public boolean getAllowBody(String componentId, Location location)
        {
            IContainedComponent embedded = fComponentSpec.getComponent(componentId);
            if (embedded == null)
                throw new ApplicationRuntimeException(DefaultTapestryMessages.format(
                        "no-such-component",
                        fComponentSpec.getSpecificationLocation(),
                        componentId));

            IComponentSpecification containedSpec = fNamespace.getComponentResolver().resolve(
                    embedded.getType());
            if (containedSpec == null)
                throw new ApplicationRuntimeException(DefaultTapestryMessages.format(
                        "no-such-component",
                        fComponentSpec.getSpecificationLocation(),
                        componentId));

            return containedSpec.getAllowBody();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getAllowBody(java.lang.String,
         *      java.lang.String, org.apache.tapestry.ILocation)
         */
        public boolean getAllowBody(String libraryId, String type, Location location)
        {
            if (libraryId != null)
            {
                INamespace namespace = fNamespace.getChildNamespace(libraryId);
                if (namespace == null)
                    throw new ApplicationRuntimeException("Unable to resolve "
                            + DefaultTapestryMessages.format(
                                    "Namespace.nested-namespace",
                                    libraryId));
            }

            IComponentSpecification spec = fNamespace.getComponentResolver().resolve(
                    libraryId,
                    type);
            if (spec == null)
                throw new ApplicationRuntimeException(DefaultTapestryMessages.format(
                        "Namespace.no-such-component-type",
                        type,
                        libraryId == null ? CoreMessages.format("project-namespace") : libraryId));

            return spec.getAllowBody();
        }

        /*
         * (non-Javadoc)
         * 
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