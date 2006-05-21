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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.messages.DefaultTapestryMessages;
import net.sf.spindle.core.messages.ImplMessages;
import net.sf.spindle.core.messages.PageloadMessages;
import net.sf.spindle.core.namespace.ComponentSpecificationResolver;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.parser.template.CoreOpenToken;
import net.sf.spindle.core.parser.template.CoreTemplateParser;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.spec.PluginBindingSpecification;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.spec.PluginContainedComponent;
import net.sf.spindle.core.util.Assert;
import net.sf.spindle.core.util.Files;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.Location;
import org.apache.hivemind.Resource;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.binding.BindingConstants;
import org.apache.tapestry.parse.ITemplateParserDelegate;
import org.apache.tapestry.parse.OpenToken;
import org.apache.tapestry.parse.TemplateParseException;
import org.apache.tapestry.parse.TemplateToken;
import org.apache.tapestry.parse.TokenType;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;

/**
 * Scanner for Tapestry templates. It is assumed that the component we are scanning templates on
 * behalf of has its namepsace already.
 * 
 * @author glongman@gmail.com
 */
public class TemplateScanner extends AbstractScanner
{

    // private static final int IMPLICIT_ID_PATTERN_ID_GROUP = 1;
    //
    // private static final int IMPLICIT_ID_PATTERN_TYPE_GROUP = 2;
    //
    // private static final int IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP = 4;
    //
    // private static final int IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP = 5;
    //
    // private Pattern _simpleIdPattern;
    //
    // private Pattern _implicitIdPattern;
    //
    // private PatternMatcher _patternMatcher;

    private PluginComponentSpecification fComponentSpec;

    private String fComponentAttributeName;

    private ICoreNamespace fNamespace;

    private CoreTemplateParser fParser;

    private ITemplateParserDelegate fParserDelegate = new ScannerDelegate();

    private List<String> fSeenIds = new ArrayList<String>();

    private ICoreResource fTemplateLocation;

    private String fContents;

    private boolean fPerformDeferredValidations = true;

    private String fEncoding;

    private boolean fContainsImplicitComponents; // true if template contains

    // implicit components

    public void scanTemplate(PluginComponentSpecification spec, Resource templateLocation,
            String componentAttributeName, SpecificationValidator validator)
            throws ScannerException
    {
        Assert.isNotNull(spec);
        Assert.isNotNull(spec.getNamespace());
        fTemplateLocation = (ICoreResource) templateLocation;
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
            SpecificationValidator validator) throws ScannerException
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
     * @see core.scanning.AbstractScanner#doScan(java.lang.Object, java.lang.Object)
     */
    protected void doScan() throws ScannerException
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
            // catch (CoreException e)
            // {
            // TapestryCore.log(e);
            // }
            catch (IOException e)
            {
                TapestryCore.log(e);
            }
        }
        if (data == null)
            throw new ScannerException("null data!", false, IProblem.NOT_QUICK_FIXABLE);

        List result = (List) fResultObject;

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

        PluginContainedComponent result = new PluginContainedComponent();
        result.setIdentifier(id);
        result.setType(componentType);

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

    private PluginBindingSpecification createImplicitBinding(String name, String reference)
    {
        PluginBindingSpecification result = new PluginBindingSpecification();

        result.setType(BindingType.PREFIXED);

        String prefix = BindingConstants.LITERAL_PREFIX;

        int colonx = reference.indexOf(':');

        if (colonx > 1)
        {
            prefix = reference.substring(0, colonx - 1);
            result.setPrefix(prefix);
            result.setValue(reference);

        }
        else
        {
            result.setPrefix(prefix);
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
        List<String> required = findRequiredParameterNames(containedSpecification);
        required.removeAll(bindingNames);
        if (!required.isEmpty())
        {
            addProblem(
                    IProblem.ERROR,
                    token.getEventInfo().getStartTagLocation(),
                    PageloadMessages.requiredParameterNotBound(required.toString(), token
                            .getComponentType()),
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

            IParameterSpecification parameter = containedSpecification.getParameter(name);

            boolean isFormal = parameter != null;

            boolean isAllowInformalParameters = containedSpecification.getAllowInformalParameters();

            if (!isFormal && !isAllowInformalParameters)
            {

                addProblem(
                        IProblem.ERROR,
                        location,
                        PageloadMessages.formalParametersOnly(containedSpecification
                                .getSpecificationLocation().getName(), bspec.getIdentifier()),
                        false,
                        IProblem.TEMPLATE_SCANNER_NO_INFORMALS_ALLOWED);
            }

            ((SpecificationValidator) fValidator).doValidateBinding(containedSpecification, bspec, location);
        }

        // TODO deferred validations are to be revisited

        // if (fPerformDeferredValidations)
        // FrameworkComponentValidator.validateImplictComponent(
        // (ICoreResource) fComponentSpec.getSpecificationLocation(),
        // fTemplateLocation,
        // fComponentSpec.getNamespace(),
        // token.getComponentType(),
        // containedSpecification,
        // contained,
        // token.getEventInfo(),
        // containedSpecification.getPublicId());

    }

    

    private IComponentSpecification resolveComponentType(String type)
    {
        ICoreNamespace namespace = (ICoreNamespace) fComponentSpec.getNamespace();
        ComponentSpecificationResolver resolver = namespace.getComponentResolver();
        IComponentSpecification containedSpecification = resolver.resolve(type);
        return containedSpecification;
    }

    private List<String> findRequiredParameterNames(IComponentSpecification spec)
    {
        ArrayList<String> result = new ArrayList<String>();
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

            PluginBindingSpecification bspec = createImplicitBinding(name, value);

            if ((spec.getParameter(name) != null)) // is a formal parameter
            {
                if (component.getBinding(name) != null)
                    addProblem(
                            IProblem.ERROR,
                            location,
                            DefaultTapestryMessages.format(
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
                            DefaultTapestryMessages.format(
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
                            DefaultTapestryMessages.format(
                                    "BaseComponent.template-expression-for-reserved-parameter",
                                    name,
                                    spec.getSpecificationLocation().getName(),
                                    fComponentSpec.getSpecificationLocation().getName()),
                            false,
                            IProblem.TEMPLATE_SCANNER_TEMPLATE_EXPRESSION_FOR_RESERVED_PARM);

            }

            ((SpecificationValidator) fValidator).doValidateBinding(spec, bspec, location);
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
     * @see core.scanning.AbstractScanner#beforeScan(java.lang.Object)
     */
    protected Object beforeScan() throws ScannerException
    {
        return new ArrayList();
    }

    public void setEncoding(String encoding)
    {
        fEncoding = encoding;
    }

    public String getEncoding()
    {
        return fEncoding;
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
                        libraryId == null ? CoreMessages.projectNamespace() : libraryId));

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