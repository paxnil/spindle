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

    private static final ISourceLocation DefaultSourceLocation = new SLocation();

    /** 
     * 
     *  Map of compiled Patterns, keyed on pattern
     *  string.  Patterns are lazily compiled as needed.
     * 
     **/

    protected Map fCompiledPatterns;

    protected String fDummyString = DefaultDummyString;

    /** 
     * 
     *  Matcher used to match patterns against input strings.
     * 
     **/

    protected PatternMatcher fMatcher;

    /** 
     * 
     *  Compiler used to convert pattern strings into Patterns
     *  instances.
     * 
     * 
     **/

    protected PatternCompiler fPatternCompiler;
    protected IProblemCollector fProblemCollector;

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
        if (fPatternCompiler == null)
            fPatternCompiler = new Perl5Compiler();

        try
        {
            return fPatternCompiler.compile(pattern, Perl5Compiler.SINGLELINE_MASK);
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
        return fDummyString;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getNextDummyString()
     */
    public String getNextDummyString()
    {
        return fDummyString + System.currentTimeMillis();
    }

    public IProblemCollector getProblemCollector()
    {
        return fProblemCollector;
    }

    public void setProblemCollector(IProblemCollector collector)
    {
        fProblemCollector = collector;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.IAsset, com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
     */
    public boolean validateAsset(
        IComponentSpecification specification,
        IAssetSpecification asset,
        ISourceLocationInfo sourceLocation)
        throws ScannerException
    {
        return true;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.spec.IContainedComponent)
     */
    public boolean validateContainedComponent(
        IComponentSpecification specification,
        IContainedComponent component,
        ISourceLocationInfo info)
        throws ScannerException
    {
        return true;
    }

    public boolean validateExpression(String expression, int severity) throws ScannerException
    {
        return validateExpression(expression, severity, DefaultSourceLocation);
    }

    public boolean validateExpression(String expression, int severity, ISourceLocation location)
        throws ScannerException
    {
        if (!expression.startsWith(fDummyString))
        {
            try
            {
                Ognl.parseExpression(expression);
            } catch (OgnlException e)
            {
                reportProblem(severity, location, e.getMessage());
                return false;
            }
        }
        return true;
    }

    protected void reportProblem(int severity, ISourceLocation location, String message) throws ScannerException
    {
        if (fProblemCollector == null)
        {
            throw new ScannerException(message);
        } else
        {
            fProblemCollector.addProblem(severity, (location == null ? DefaultSourceLocation : location), message);
        }
    }

    public boolean validatePattern(String value, String pattern, String errorKey, int severity) throws ScannerException
    {
        return validatePattern(value, pattern, errorKey, severity, DefaultSourceLocation);
    }

    public boolean validatePattern(
        String value,
        String pattern,
        String errorKey,
        int severity,
        ISourceLocation location)
        throws ScannerException
    {
        if (!value.startsWith(fDummyString))
        {
            if (fCompiledPatterns == null)
                fCompiledPatterns = new HashMap();

            Pattern compiled = (Pattern) fCompiledPatterns.get(pattern);

            if (compiled == null)
            {
                compiled = compilePattern(pattern);

                fCompiledPatterns.put(pattern, compiled);
            }

            if (fMatcher == null)
                fMatcher = new Perl5Matcher();

            if (!fMatcher.matches(value, compiled))
            {
                reportProblem(severity, location, TapestryCore.getTapestryString(errorKey, value));
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateResourceLocation(org.apache.tapestry.IResourceLocation, java.lang.String)
     */
    public boolean validateResourceLocation(
        IResourceLocation location,
        String relativePath,
        String errorKey,
        ISourceLocation source)
        throws ScannerException
    {
        IResourceWorkspaceLocation real = (IResourceWorkspaceLocation) location;
        IResourceWorkspaceLocation relative = (IResourceWorkspaceLocation) real.getRelativeLocation(relativePath);

        if (!relative.exists())
        {
            reportProblem(IProblem.ERROR, source, TapestryCore.getString(errorKey, relative.toString()));
            return false;
        }
        return true;
    }

    public boolean validateTypeName(String fullyQualifiedType, int severity) throws ScannerException
    {
        return validateTypeName(fullyQualifiedType, severity, DefaultSourceLocation);
    }
    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateTypeName(java.lang.String)
     */
    public boolean validateTypeName(String fullyQualifiedType, int severity, ISourceLocation location)
        throws ScannerException
    {
        Object type = findType(fullyQualifiedType);
        if (type == null)
        {
            reportProblem(
                severity,
                location,
                TapestryCore.getTapestryString("unable-to-resolve-class", fullyQualifiedType));
            return false;
        }
        return true;
    }
}
