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

package com.iw.plugins.spindle.editors.template.assist;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.apache.tapestry.spec.IParameterSpecification;

import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 *  Helper class for ContentAssistProcessors
 * 
 *  Knows how to extract tapestry information.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
/*package*/
class ContentAssistHelper
{

    private static Pattern SIMPLE_ID_PATTERN;
    private static Pattern IMPLICIT_ID_PATTERN;
    private static PatternMatcher PATTERN_MATCHER;

    static {
        Perl5Compiler compiler = new Perl5Compiler();

        try
        {
            SIMPLE_ID_PATTERN = compiler.compile(TemplateParser.SIMPLE_ID_PATTERN);
            IMPLICIT_ID_PATTERN = compiler.compile(TemplateParser.IMPLICIT_ID_PATTERN);
        } catch (MalformedPatternException ex)
        {
            throw new Error(ex);
        }

        PATTERN_MATCHER = new Perl5Matcher();
    }

    private PluginComponentSpecification fSpecification;
    private ICoreNamespace fNamespace;
    private ICoreNamespace fFrameworkNamespace;
    private String fRawJwcid = null;
    /** The id. Implicit or not. Never null*/
    private String fSimpleId = null;
    /** implicit component - the full type including namespace qualifier */
    private String fFullType = null;
    /** implicit component - the namespace qualifier */
    private String fLibraryId = null;
    /** implicit component - the simple type name. Same as fFullType - fLibraryId */
    private String fSimpleType = null;
    /** the spec of the component referred to directly or indirectly by a jwcid */
    private PluginComponentSpecification fContainedComponentSpecification = null;
    /** not null iff fSimpleId is not null**/
    private IContainedComponent fContainedComponent = null;

    ContentAssistHelper(TemplateEditor editor) throws IllegalArgumentException
    {
        if (SIMPLE_ID_PATTERN == null)
        {}

        fSpecification = (PluginComponentSpecification) editor.getComponent();
        if (fSpecification != null)
        {
            fNamespace = (ICoreNamespace) fSpecification.getNamespace();
        }
        Assert.isLegal(fNamespace != null && fSpecification != null);
    }

    void setJwcid(String jwcid)
    {

        if (jwcid == null || jwcid.trim().length() == 0)
            return;

        fRawJwcid = jwcid.trim();
        if (fRawJwcid.equalsIgnoreCase(CoreTemplateParser.REMOVE_ID)
            || fRawJwcid.equalsIgnoreCase(CoreTemplateParser.CONTENT_ID))
            return;

        if (PATTERN_MATCHER.matches(fRawJwcid, IMPLICIT_ID_PATTERN))
        {
            MatchResult match = PATTERN_MATCHER.getMatch();

            fSimpleId = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_ID_GROUP);
            fFullType = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_TYPE_GROUP);

            fLibraryId = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP);
            fSimpleType = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP);

        } else
        {
            if (PATTERN_MATCHER.matches(fRawJwcid, SIMPLE_ID_PATTERN))
                fSimpleId = fRawJwcid;

        }
    }

    void setJwcid(String jwcid, ICoreNamespace frameworkNamespace)
    {
        setJwcid(jwcid);
        fFrameworkNamespace = frameworkNamespace;
    }

    private void resolveContainedComponent()
    {
        if (fSimpleId != null && fFullType == null)
        {
            fContainedComponent = fSpecification.getComponent(fSimpleId);
            if (fContainedComponent == null)
                return;
            String copyOf = fContainedComponent.getCopyOf();
            if (copyOf != null)
            {
                fContainedComponent = fSpecification.getComponent(copyOf);
                if (fContainedComponent == null)
                    return;
            }
            fContainedComponentSpecification =
                (PluginComponentSpecification) resolveComponentType(fContainedComponent.getType());
        } else if (fFullType != null)
        {
            fContainedComponentSpecification = (PluginComponentSpecification) resolveComponentType(fFullType);
        }
    }

    private IComponentSpecification resolveComponentType(String type)
    {
        ComponentSpecificationResolver resolver = fNamespace.getComponentResolver();
        IComponentSpecification containedSpecification = resolver.resolve(type);
        if (fFrameworkNamespace != null && containedSpecification == null)
        {
            resolver = fFrameworkNamespace.getComponentResolver();
            containedSpecification = resolver.resolve(type);
        }
        return containedSpecification;
    }

    CAHelperResult[] findParameters()
    {
        return findParameters(null, null);
    }

    CAHelperResult[] findParameters(String match, HashSet existing)
    {
        resolveContainedComponent();

        if (fContainedComponentSpecification == null)
        {
            return new CAHelperResult[] {};
        }
        if (fContainedComponent != null)
        {
            existing.addAll(fContainedComponent.getBindingNames());
        }
        List result = new ArrayList();
        Map parameterMap = fContainedComponentSpecification.getParameterMap();
        for (Iterator iter = parameterMap.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IParameterSpecification parameterSpec = (IParameterSpecification) parameterMap.get(name);

            if (match != null && match.trim().length() > 0)
            {
                if (!name.startsWith(match))
                    continue;

            }

            if (existing != null && existing.contains(name.toLowerCase()))
                continue;

            result.add(createResult(name, parameterSpec, fSpecification.getPublicId()));
        }

        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);

    }

    private CAHelperResult createResult(String name, IParameterSpecification parameterSpec, String publicId)
    {
        CAHelperResult result = new CAHelperResult(name, null);
        String description = parameterSpec.getDescription();

        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);
        pwriter.println(name);
        pwriter.println();
        pwriter.println(description == null ? "no description available" : description);
        pwriter.println();
        XMLUtil.writeParameter(name, parameterSpec, pwriter, 0, publicId);

        result.description = swriter.toString();

        if (parameterSpec.isRequired())
            result.required = true;
        return result;
    }

    CAHelperResult[] getSimpleIds()
    {
        if (fSpecification == null)
            return new CAHelperResult[] {};

        List result = new ArrayList();
        List ids = fSpecification.getComponentIds();
        for (Iterator iter = ids.iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();

            result.add(new CAHelperResult(id, null));
        }
        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);
    }

    CAHelperResult[] getLibraryIds()
    {
        if (fNamespace == null)
            return new CAHelperResult[] {};

        List result = new ArrayList();
        List libIds = fNamespace.getChildIds();
        for (Iterator iter = libIds.iterator(); iter.hasNext();)
        {
            String libId = (String) iter.next();
            INamespace libNamespace = fNamespace.getChildNamespace(libId);
            ILibrarySpecification libSpec = fNamespace.getSpecification();

            String description = libSpec.getDescription();
            result.add(new CAHelperResult(libId, description == null ? "no description available" : description));
        }
        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);
    }

    /** return resuls for the current namespace **/
    CAHelperResult[] getComponents()
    {
        List result = new ArrayList();

        ILibrarySpecification libSpec = fNamespace.getSpecification();

        List types = libSpec.getComponentTypes();

        Map applicationTypes = new HashMap();

        for (Iterator iter = types.iterator(); iter.hasNext();)
        {
            String typeId = (String) iter.next();
            IComponentSpecification component = fNamespace.getComponentSpecification(typeId);
            applicationTypes.put(typeId, createResult(typeId, typeId, component));
        }

        if (fFrameworkNamespace != null)
        {
            libSpec = fFrameworkNamespace.getSpecification();
            types = libSpec.getComponentTypes();
            for (Iterator iter = types.iterator(); iter.hasNext();)
            {
                String typeId = (String) iter.next();
                if (applicationTypes.containsKey(typeId))
                    continue;
                IComponentSpecification component = fFrameworkNamespace.getComponentSpecification(typeId);
                applicationTypes.put(typeId, createResult(typeId, typeId, component));
            }
        }

        for (Iterator iter = applicationTypes.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entries = (Map.Entry) iter.next();
            result.add(entries.getValue());
        }

        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);
    }

    CAHelperResult[] getChildNamespaceComponents(String libraryId)
    {
        if (fNamespace == null)
            return new CAHelperResult[] {};

        List result = new ArrayList();

        INamespace childNamespace = fNamespace.getChildNamespace(libraryId);
        if (childNamespace == null)
            return new CAHelperResult[] {};

        ILibrarySpecification childLibSpec = fNamespace.getSpecification();
        List types = childLibSpec.getComponentTypes();
        for (Iterator iter = types.iterator(); iter.hasNext();)
        {
            String typeId = (String) iter.next();
            IComponentSpecification component = fNamespace.getComponentSpecification(typeId);
            result.add(createResult(typeId, typeId, component));
        }

        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);

    }

    CAHelperResult[] getChildNamespaceIds()
    {
        if (fNamespace == null)
        {
            return new CAHelperResult[] {};
        }
        List result = new ArrayList();
        for (Iterator iter = fNamespace.getChildIds().iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();

            result.add(createResult(id, fNamespace.getSpecification()));

        }
        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);
    }

    private CAHelperResult createResult(String libId, ILibrarySpecification namespaceSpec)
    {
        CAHelperResult result = new CAHelperResult(libId, null);

        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);

        pwriter.println(libId);
        pwriter.println();
        XMLUtil.writeLibrary(libId, namespaceSpec.getLibrarySpecificationPath(libId), pwriter, 0);

        result.description = swriter.toString();
        return result;
    }

    CAHelperResult[] getAllChildNamespaceComponents()
    {
        if (fNamespace == null)
            return new CAHelperResult[] {};

        List result = new ArrayList();

        List libIds = fNamespace.getChildIds();
        for (Iterator iter = libIds.iterator(); iter.hasNext();)
        {
            String libId = (String) iter.next();
            ICoreNamespace childNamespace = (ICoreNamespace) fNamespace.getChildNamespace(libId);
            List types = childNamespace.getComponentTypes();
            ComponentSpecificationResolver resolver = childNamespace.getComponentResolver();

            for (Iterator iter2 = types.iterator(); iter2.hasNext();)
            {
                String typeId = (String) iter2.next();
                IComponentSpecification component = resolver.resolve(typeId);
                result.add(createResult(libId + ":" + typeId, typeId + " - from '" + libId + "'", component));
            }
        }
        return (CAHelperResult[]) result.toArray(new CAHelperResult[result.size()]);

    }

    CAHelperResult getComponentContextInformation()
    {
        resolveContainedComponent();

        if (fContainedComponentSpecification == null)
        {
            return null;
        }

        return createResult(
            fContainedComponent != null ? fContainedComponent.getType() : fFullType,
            null,
            fContainedComponentSpecification);
    }

    CAHelperResult getParameterContextInformation(String parameterName)
    {
        resolveContainedComponent();
        if (fContainedComponentSpecification == null)
            return null;

        IParameterSpecification parameterSpec = fContainedComponentSpecification.getParameter(parameterName);
        if (parameterSpec == null)
            return null;

        return createResult(parameterName, parameterSpec, fContainedComponentSpecification.getPublicId());

    }

    private CAHelperResult createResult(String name, String displayName, IComponentSpecification componentSpec)
    {
        CAHelperResult result = new CAHelperResult(name, null);
        result.displayName = displayName;
        if (componentSpec != null)
        {
            StringWriter swriter = new StringWriter();
            PrintWriter pwriter = new PrintWriter(swriter);
            pwriter.println(name);
            pwriter.println();
            String description = componentSpec.getDescription();
            pwriter.println(description == null ? "no description available" : description);
            pwriter.println();
            XMLUtil.writeComponentSpecificationHeader(pwriter, componentSpec, 0);
            result.description = swriter.toString();
        }

        return result;
    }

    public static class CAHelperResult
    {
        CAHelperResult()
        {}

        CAHelperResult(String name, String description)
        {
            this.name = name;
            this.displayName = name;
            this.description = description;
        }
        public String name;
        public String displayName;
        public String description;
        public boolean required = false;
    }

    public String toString()
    {
        return "\n\tfull="
            + fFullType
            + "\n\tlib="
            + fLibraryId
            + "\n\tsimple="
            + fSimpleType
            + "\n\tsimpleId="
            + fSimpleId;
    }

}
