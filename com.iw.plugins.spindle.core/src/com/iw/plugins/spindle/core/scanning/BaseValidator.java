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
import org.apache.tapestry.Tapestry;

import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.parser.ISourceLocation;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BaseValidator implements IScannerValidator
{

    private static final ISourceLocation DefaultLocation = new SLocation();

    public static final String DefaultDummyString = "~dummy<>";

    /** 
     * 
     *  Compiler used to convert pattern strings into Patterns
     *  instances.
     * 
     * 
     **/

    protected PatternCompiler patternCompiler;

    /** 
     * 
     *  Matcher used to match patterns against input strings.
     * 
     **/

    protected PatternMatcher matcher;

    /** 
     * 
     *  Map of compiled Patterns, keyed on pattern
     *  string.  Patterns are lazily compiled as needed.
     * 
     **/

    protected Map compiledPatterns;
    protected String dummyString = DefaultDummyString;
    protected IProblemCollector problemCollector;

    /**
     * 
     */
    public BaseValidator()
    {
        super();

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
        // do nothing

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

        throw new ScannerException(Tapestry.getString(errorKey, value));
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
                throw new ScannerException(e.getMessage());
            }
        }
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

    static class SLocation implements ISourceLocation
    { /* (non-Javadoc)
                   * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getCharEnd()
                   */
        public int getCharEnd()
        {
            // TODO Auto-generated method stub
            return 1;
        }
        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getCharStart()
        */
        public int getCharStart()
        {
            // TODO Auto-generated method stub
            return 0;
        }
        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getLineNumber()
        */
        public int getLineNumber()
        {
            // TODO Auto-generated method stub
            return 0;
        }
    }
}
