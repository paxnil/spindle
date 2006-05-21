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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.CoreStatus;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.build.AbstractBuild;
import net.sf.spindle.core.build.BuilderMessages;
import net.sf.spindle.core.build.IDependencyListener;
import net.sf.spindle.core.messages.DefaultTapestryMessages;
import net.sf.spindle.core.messages.ParseMessages;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.PathUtils;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.IProblemCollector;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.ISourceLocationInfo;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.spec.PluginInjectSpecification;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.types.IJavaTypeFinder;
import ognl.Ognl;
import ognl.OgnlException;

import org.apache.hivemind.HiveMind;
import org.apache.hivemind.Resource;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.tapestry.binding.BindingConstants;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

/**
 * TODO Add Type comment
 * 
 * @author glongman@gmail.com
 */
public class BaseValidator implements IScannerValidator
{

    static class SLocation implements ISourceLocation
    {
        public int getCharEnd()
        {
            return 1;
        }

        public int getCharStart()
        {
            return 0;
        }

        public int getLineNumber()
        {
            return 1;
        }

        public int getLength()
        {
            return getCharEnd() - getCharStart() + 1;
        }

        public boolean contains(int cursorPosition)
        {
            return cursorPosition == 0 || cursorPosition == 1;
        }

        public ISourceLocation getLocationOffset(int cursorPosition)
        {
            return this;
        }

    }

    public static final String DefaultDummyString = "1~dummy<>";

    public static final ISourceLocation DefaultSourceLocation = new SLocation();

    /**
     * Map of compiled Patterns, keyed on pattern string. Patterns are lazily compiled as needed.
     */

    protected Map<String, Pattern> fCompiledPatterns;

    protected String fDummyString = DefaultDummyString;

    /**
     * Matcher used to match patterns against input strings.
     */

    protected PatternMatcher fMatcher;

    /**
     * Compiler used to convert pattern strings into Patterns instances.
     */

    protected PatternCompiler fPatternCompiler;

    protected IProblemCollector fProblemCollector;

    private List<IScannerValidatorListener> fListeners;

    private IJavaTypeFinder fJavaTypeFinder;

    public static final String NamespaceClassSearchPageClassProvider="org.apache.tapestry.page-class-packages";

    public static final String NamespaceClassSearchComponentClassProvider="org.apache.tapestry.component-class-packages";

    public BaseValidator(IJavaTypeFinder finder)
    {
        super();
        setJavaTypeFinder(finder);
    }

    public void setJavaTypeFinder(IJavaTypeFinder finder)
    {
        if (!finder.isCachingJavaTypes())
            finder = createCachingTypeFinder(finder);
        fJavaTypeFinder = finder;
    }

    private IJavaTypeFinder createCachingTypeFinder(final IJavaTypeFinder finder)
    {
        return new IJavaTypeFinder()
        {
            Map<String, IJavaType> cache;

            public IJavaType findType(String fullyQualifiedName)
            {
                if (cache == null)
                    cache = new HashMap<String, IJavaType>();

                IJavaType result = (IJavaType) cache.get(fullyQualifiedName);
                if (result == null)
                {
                    result = finder.findType(fullyQualifiedName);
                    cache.put(fullyQualifiedName, result);
                }
                return result;
            }

            public boolean isCachingJavaTypes()
            {
                return true;
            }
        };
    }

    public IJavaTypeFinder getJavaTypeFinder()
    {
        return fJavaTypeFinder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#addListener(core.scanning.IScannerValidatorListener)
     */
    public void addListener(IScannerValidatorListener listener)
    {
        if (fListeners == null)
            fListeners = new ArrayList<IScannerValidatorListener>();

        if (!fListeners.contains(listener))
            fListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#removeListener(core.scanning.IScannerValidatorListener)
     */
    public void removeListener(IScannerValidatorListener listener)
    {
        if (fListeners != null)
            fListeners.remove(listener);
    }

    /**
     * Notify listeners that a type check occured.
     * 
     * @param fullyQulaifiedName
     *            the name of the type
     * @param result
     *            the resolved IType, if any
     */
    protected void fireTypeDependency(Resource dependant, String fullyQualifiedName,
            IJavaType result)
    {
        if (fListeners == null)
            return;

        for (Iterator iter = fListeners.iterator(); iter.hasNext();)
        {
            IScannerValidatorListener listener = (IScannerValidatorListener) iter.next();
            listener.typeChecked(fullyQualifiedName, result); // TODO
            // remove
            // eventually
        }

        IDependencyListener depListener = AbstractBuild.getDependencyListener();
        if (depListener != null && fullyQualifiedName != null
                && fullyQualifiedName.trim().length() > 0)
            depListener.foundTypeDependency(dependant, fullyQualifiedName);

    }

    /**
     * Returns a pattern compiled for single line matching
     */
    protected Pattern compilePattern(String pattern)
    {
        if (fPatternCompiler == null)
            fPatternCompiler = new Perl5Compiler();

        try
        {
            return fPatternCompiler.compile(pattern, Perl5Compiler.SINGLELINE_MASK);
        }
        catch (MalformedPatternException ex)
        {

            throw new Error(ex);
        }
    }

    /**
     * Base Implementation always fails!
     * 
     * @param fullyQualifiedName
     * @return
     */
    public IJavaType findType(Resource dependant, String fullyQualifiedName)
    {
        IJavaType result = getJavaTypeFinder().findType(fullyQualifiedName);
        fireTypeDependency(dependant, fullyQualifiedName, result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#getDummyStringPrefix()
     */
    public String getDummyStringPrefix()
    {
        return fDummyString;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#getNextDummyString()
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

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification,
     *      org.apache.tapestry.IAsset, core.parser.ISourceLocationInfo)
     */
    public boolean validateAsset(IComponentSpecification specification, IAssetSpecification asset,
            ISourceLocationInfo sourceLocation) throws ScannerException
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification,
     *      org.apache.tapestry.spec.IContainedComponent)
     */
    public boolean validateContainedComponent(IComponentSpecification specification,
            IContainedComponent component, ISourceLocationInfo info) throws ScannerException
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
            }
            catch (OgnlException e)
            {
                addProblem(
                        severity,
                        location,
                        e.getMessage(),
                        false,
                        IProblem.SPINDLE_MALFORMED_OGNL_EXPRESSION);
                return false;
            }
        }
        return true;
    }

    public void addProblem(int severity, ISourceLocation location, String message,
            boolean isTemporary, int code) throws ScannerException
    {
        if (fProblemCollector == null)
        {
            throw new ScannerException(message, isTemporary, code);
        }
        else
        {
            fProblemCollector.addProblem(severity, (location == null ? DefaultSourceLocation
                    : location), message, isTemporary, code);
        }
    }

    public boolean validatePattern(String value, String pattern, String errorKey, int severity,
            int code) throws ScannerException
    {
        return validatePattern(value, pattern, errorKey, severity, DefaultSourceLocation, code);
    }

    public boolean validatePattern(String value, String pattern, String errorKey, int severity,
            ISourceLocation location, int code) throws ScannerException
    {

        if (value != null && value.startsWith(fDummyString))
            return true;

        if (value != null)
        {
            if (fCompiledPatterns == null)
                fCompiledPatterns = new HashMap<String, Pattern>();

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
                addProblem(
                        severity,
                        location,
                        ParseMessages.invalidAttribute(errorKey, value),
                        false,
                        code);
                return false;
            }
            return true;
        }
        addProblem(
                severity,
                location,
                ParseMessages.invalidAttribute(errorKey, "null value"),
                false,
                -1);
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateResourceLocation(java.lang.String,
     *      java.lang.String, core.parser.ISourceLocation)
     */
    public boolean validateLibraryResource(Resource specLocation, String path, String errorKey,
            ISourceLocation source) throws ScannerException
    {
        return false;
    }

    public boolean validateResource(Resource location, String relativePath, String errorKey,
            ISourceLocation source) throws ScannerException
    {
        return validateResourceLocation(location, relativePath, errorKey, source, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateResourceLocation(org.apache.tapestry.IResourceLocation,
     *      java.lang.String)
     */
    public boolean validateResourceLocation(Resource location, String relativePath,
            String errorKey, ISourceLocation source, boolean accountForI18N)
            throws ScannerException
    {
        if (relativePath == null || relativePath.startsWith(getDummyStringPrefix()))
            return false;

        ICoreResource relative = (ICoreResource) location.getRelativeResource(relativePath);

        if (!relative.exists())
        {

            addProblem(
                    IProblem.ERROR,
                    source,
                    BuilderMessages.format(errorKey, relative.toString()),
                    true,
                    IProblem.SPINDLE_RESOURCE_LOCATION_DOES_NOT_EXIST);

            return false;
        }
        return true;
    }

    public Object validateTypeName(Resource dependant, String fullyQualifiedType, int severity)
            throws ScannerException
    {
        return validateTypeName(dependant, fullyQualifiedType, severity, DefaultSourceLocation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateTypeName(java.lang.String)
     */
    public Object validateTypeName(Resource dependant, String fullyQualifiedType, int severity,
            ISourceLocation location) throws ScannerException
    {

        if (fullyQualifiedType == null)
        {
            addProblem(severity, location, DefaultTapestryMessages.format(
                    "unable-to-resolve-class",
                    "null value"), true, -1);
            return null;
        }

        IJavaType type = findType(dependant, fullyQualifiedType);
        if (!type.exists())
        {
            addProblem(severity, location, DefaultTapestryMessages.format(
                    "unable-to-resolve-class",
                    fullyQualifiedType), true, IProblem.SPINDLE_MISSING_TYPE);
            return null;
        }
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.IScannerValidator#validateBindingReference(int,
     *      core.source.ISourceLocation, java.lang.String, boolean)
     */
    public void validateBindingReference(int severity, ISourceLocation sourceLocation,
            String reference) throws ScannerException
    {
        if (TapestryCore.isNull(reference))
            return;

        if (reference.startsWith(BindingConstants.OGNL_PREFIX))
            validateExpression(
                    reference.substring(BindingConstants.OGNL_PREFIX.length()),
                    severity,
                    sourceLocation);

    }

    public void validateLibraryMetaKey(String key, ISourceLocation location)
            throws ScannerException
    {
        CoreStatus priority = TapestryCore.getDefault().getIncompatabilityPriority();
        if (priority == CoreStatus.IGNORE)
            return;
        if (NamespaceClassSearchComponentClassProvider.equals(key.trim()))
            addProblem(
                    priority.getPriority(),
                    location,
                    CoreMessages.componentClassMetaIncompatability(),
                    false,
                    IProblem.SPINDLE_UNSUPPORTED_COMPONENT_CLASS_META);
        else if (NamespaceClassSearchPageClassProvider.equals(key.trim()))
            addProblem(
                    priority.getPriority(),
                    location,
                    CoreMessages.pageClassMetaIncompatability(),
                    false,
                    IProblem.SPINDLE_UNSUPPORTED_PAGE_CLASS_META);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.scanning.IScannerValidator#checkForIncompatiblePageName(java.lang.String,
     *      net.sf.spindle.core.source.ISourceLocation)
     */
    public void checkForIncompatiblePageName(String name, ISourceLocation location)
            throws ScannerException
    {
        checkNameForPathParts(
                name,
                location,
                CoreMessages.unsupportedPageName(name),
                IProblem.SPINDLE_UNSUPPORTED_PAGE_NAME);
    }

    public void checkForIncompatibleComponentName(String name, ISourceLocation location)
            throws ScannerException
    {
        checkNameForPathParts(
                name,
                location,
                CoreMessages.unsupportedComponentName(name),
                IProblem.SPINDLE_UNSUPPORTED_COMPONENT_NAME);
    }

    private void checkNameForPathParts(String name, ISourceLocation location, String message,
            int problemCode) throws ScannerException
    {
        if (HiveMind.isBlank(name))
            return;
        CoreStatus priority = TapestryCore.getDefault().getIncompatabilityPriority();
        if (priority == CoreStatus.IGNORE)
            return;
        PathUtils path = new PathUtils(name);
        if (path.segmentCount() > 1)
            addProblem(priority.getPriority(), location, message, false, problemCode);
    }

    /* (non-Javadoc)
     * @see net.sf.spindle.core.scanning.IScannerValidator#validateXMLInject(net.sf.spindle.core.spec.PluginComponentSpecification, net.sf.spindle.core.spec.PluginInjectSpecification)
     */
    public void validateXMLInject(PluginComponentSpecification spec, PluginInjectSpecification inject, ISourceLocationInfo sourceInfo) throws ScannerException
    {
        //The base implementation does nothing
        
    }
    
    

}