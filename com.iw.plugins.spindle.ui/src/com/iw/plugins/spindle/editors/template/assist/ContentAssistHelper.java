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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;

import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ContentAssistHelper
{

    private static Pattern _simpleIdPattern;
    private static Pattern _implicitIdPattern;
    private static PatternMatcher _patternMatcher;

    static {
        Perl5Compiler compiler = new Perl5Compiler();

        try
        {
            _simpleIdPattern = compiler.compile(TemplateParser.SIMPLE_ID_PATTERN);
            _implicitIdPattern = compiler.compile(TemplateParser.IMPLICIT_ID_PATTERN);
        } catch (MalformedPatternException ex)
        {
            throw new Error(ex);
        }

        _patternMatcher = new Perl5Matcher();
    }

    private PluginComponentSpecification fSpecification;
    private ICoreNamespace fNamespace;
    private String fSimpleId = null;
    private String fFullType = null;
    private String fLibraryId = null;
    private String fSimpleType = null;
    private PluginComponentSpecification fContainedComponentSpecification = null;

    public ContentAssistHelper(TemplateEditor editor) throws IllegalArgumentException
    {
        if (_simpleIdPattern == null)
        {}

        fSpecification = (PluginComponentSpecification) editor.getComponent();
        if (fSpecification != null)
        {
            fNamespace = (ICoreNamespace) fSpecification.getNamespace();
        }
        Assert.isLegal(fNamespace != null && fSpecification != null);
    }

    public void setJwcid(String jwcid)
    {
        if (jwcid==null || jwcid.trim().length() == 0)
            return;
            
        if (jwcid.equalsIgnoreCase(CoreTemplateParser.REMOVE_ID)
            || jwcid.equalsIgnoreCase(CoreTemplateParser.CONTENT_ID))
            return;

        if (_patternMatcher.matches(jwcid, _implicitIdPattern))
        {
            MatchResult match = _patternMatcher.getMatch();

            fSimpleId = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_ID_GROUP);
            fFullType = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_TYPE_GROUP);

            String libraryId = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP);
            String simpleType = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP);

        } else
        {
            fSimpleId = jwcid;

        }
        if (fSimpleId != null)
        {
            IContainedComponent contained = fSpecification.getComponent(fSimpleId);
            if (contained == null)
                return;
            String copyOf = contained.getCopyOf();
            if (copyOf != null)
            {
                contained = fSpecification.getComponent(copyOf);
                if (contained == null)
                    return;
            }
            fContainedComponentSpecification = (PluginComponentSpecification) resolveComponentType(contained.getType());
        } else
        {
            fContainedComponentSpecification = (PluginComponentSpecification) resolveComponentType(fFullType);
        }
    }

    private IComponentSpecification resolveComponentType(String type)
    {
        ComponentSpecificationResolver resolver = fNamespace.getComponentResolver();
        IComponentSpecification containedSpecification = resolver.resolve(type);
        return containedSpecification;
    }

    public CAHelperParameterInfo[] findParameters()
    {
        return findParameters(null, null);
    }

    public CAHelperParameterInfo[] findParameters(String match, Set existing)
    {
        if (fContainedComponentSpecification == null)
        {
            return new CAHelperParameterInfo[] {};
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

            if (existing != null && existing.contains(name))
                continue;

            result.add(createParameterInfo(name, parameterSpec, fSpecification.getPublicId()));
        }
        return (CAHelperParameterInfo[]) result.toArray(new CAHelperParameterInfo[result.size()]);

    }

    private Object createParameterInfo(String name, IParameterSpecification parameterSpec, String publicId)
    {
        CAHelperParameterInfo result = new CAHelperParameterInfo();
        result.parameterName = name;
        String description = parameterSpec.getDescription();

        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);
        XMLUtil.writeParameter(name, parameterSpec, pwriter, 0, publicId);

        StringBuffer extraInfo = new StringBuffer(name);
        extraInfo.append("\n\n");
        extraInfo.append(description == null ? "no description available" : description);
        extraInfo.append("\n\n");
        extraInfo.append(swriter.toString());
        result.description = extraInfo.toString();
        return result;
    }

    public static class CAHelperParameterInfo
    {
        public String parameterName;
        public String description;
    }

}
