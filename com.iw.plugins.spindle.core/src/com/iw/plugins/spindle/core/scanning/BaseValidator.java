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

import java.util.HashMap;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BaseValidator implements IScannerValidator
{

    static class SLocation implements ISourceLocation
    { /* (non-Javadoc)
             * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getCharEnd()
             */
        public int getCharEnd()
        {
            return 1;
        }
        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getCharStart()
        */
        public int getCharStart()
        {
            return 0;
        }
        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getLineNumber()
        */
        public int getLineNumber()
        {
            return 1;
        }
    }

    public static final String DefaultDummyString = "1~dummy<>";

    private static final ISourceLocation DefaultLocation = new SLocation();

    /** 
     * 
     *  Map of compiled Patterns, keyed on pattern
     *  string.  Patterns are lazily compiled as needed.
     * 
     **/

    protected Map compiledPatterns;

    protected String dummyString = DefaultDummyString;

    /** 
     * 
     *  Matcher used to match patterns against input strings.
     * 
     **/

    protected PatternMatcher matcher;

    /** 
     * 
     *  Compiler used to convert pattern strings into Patterns
     *  instances.
     * 
     * 
     **/

    protected PatternCompiler patternCompiler;
    protected IProblemCollector problemCollector;

    /**
     * 
     */
    public BaseValidator()
    {
        super();

    }

    /** 
     * 
     *  Returns a pattern compiled for single line matching
     * 
     **/

    protected Pattern compilePattern(String pattern)
    {
        if (patternCompiler == null)
            patternCompiler = new Perl5Compiler();

        try
        {
            return patternCompiler.compile(pattern, Perl5Compiler.SINGLELINE_MASK);
        } catch (MalformedPatternException ex)
        {

            throw new ApplicationRuntimeException(ex);
        }
    }

    /**
     * Base Implementation always passes!
     * @param fullyQualifiedName
     * @return
     */
    protected Object findType(String fullyQualifiedName)
    {
        return this;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getDummyStringPrefix()
     */
    public String getDummyStringPrefix()
    {
        return dummyString;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getNextDummyString()
     */
    public String getNextDummyString()
    {
        return dummyString + System.currentTimeMillis();
    }

    public IProblemCollector getProblemCollector()
    {
        return problemCollector;
    }

    public void setProblemCollector(IProblemCollector collector)
    {
        problemCollector = collector;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.IAsset, com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
     */
    public void validateAsset(IComponentSpecification specification, IAssetSpecification asset, ISourceLocationInfo sourceLocation)
        throws ScannerException
    {
        return;

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.spec.IContainedComponent)
     */
    public void validateContainedComponent(
        IComponentSpecification specification,
        IContainedComponent component,
        ISourceLocationInfo info)
        throws ScannerException
    {
        return;

    }

    public void validateExpression(String expression, int severity) throws ScannerException
    {
        validateExpression(expression, severity, DefaultLocation);
    }

    public void validateExpression(String expression, int severity, ISourceLocation location) throws ScannerException
    {
        if (!expression.startsWith(dummyString))
        {
            try
            {
                Ognl.parseExpression(expression);
            } catch (OgnlException e)
            {
                if (problemCollector == null)
                {
                    throw new ScannerException(e.getMessage());
                } else
                {
                    problemCollector.addProblem(severity, (location == null ? DefaultLocation : location), e.getMessage());
                }

            }
        }
    }

    public void validatePattern(String value, String pattern, String errorKey, int severity) throws ScannerException
    {
        validatePattern(value, pattern, errorKey, severity, DefaultLocation);
    }

    public void validatePattern(String value, String pattern, String errorKey, int severity, ISourceLocation location)
        throws ScannerException
    {
        if (value.startsWith(dummyString))
        {
            return;
        }
        if (compiledPatterns == null)
            compiledPatterns = new HashMap();

        Pattern compiled = (Pattern) compiledPatterns.get(pattern);

        if (compiled == null)
        {
            compiled = compilePattern(pattern);

            compiledPatterns.put(pattern, compiled);
        }

        if (matcher == null)
            matcher = new Perl5Matcher();

        if (matcher.matches(value, compiled))
            return;

        String message = TapestryCore.getTapestryString(errorKey, value);
        if (problemCollector == null)
        {
            throw new ScannerException(message);
        } else
        {
            problemCollector.addProblem(severity, location, message);
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateResourceLocation(org.apache.tapestry.IResourceLocation, java.lang.String)
     */
    public void validateResourceLocation(IResourceLocation location, String relativePath, String errorKey, ISourceLocation source)
        throws ScannerException
    {
        IResourceWorkspaceLocation real = (IResourceWorkspaceLocation) location;
        IResourceWorkspaceLocation relative = (IResourceWorkspaceLocation) real.getRelativeLocation(relativePath);

        if (!relative.exists())
        {
            String message = TapestryCore.getString("validate-could-not-find-resource", relative.toString());
            if (problemCollector == null)
            {
                throw new ScannerException(message);
            } else
            {
                problemCollector.addProblem(IProblem.ERROR, source, message);
            }
        }
    }
    
    public void validateTypeName(String fullyQualifiedType, int severity) throws ScannerException
    {
        validateTypeName(fullyQualifiedType, severity, DefaultLocation);
    }
    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateTypeName(java.lang.String)
     */
    public void validateTypeName(String fullyQualifiedType, int severity, ISourceLocation location) throws ScannerException
    {
        Object type = findType(fullyQualifiedType);
        if (type == null)
        {
            String message = TapestryCore.getTapestryString("unable-to-resolve-class", fullyQualifiedType);
            if (problemCollector == null)
            {
                throw new ScannerException(message);
            } else
            {
                problemCollector.addProblem(severity, location, message);
            }
        }
    }

}
