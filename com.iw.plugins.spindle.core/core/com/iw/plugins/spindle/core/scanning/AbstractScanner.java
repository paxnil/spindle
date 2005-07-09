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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.source.SourceLocation;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * Base Class for Node processors
 * <p>
 * Node Processors can find problems, but these problems do not represent a list of *all* the
 * problems with this document.
 * </p>
 * <p>
 * i.e.The Parser will hold problems for things like well-formedness and dtd validation!
 * </p>
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractScanner implements IProblemCollector
{

    protected IProblemCollector fExternalProblemCollector;

    protected List fProblems = new ArrayList();

    protected IScannerValidator fValidator;

    protected IJavaTypeFinder fJavaTypeFinder;

    protected boolean isCachingJavaTypes = false;

    public Object scan(Object source, IScannerValidator validator) throws ScannerException
    {
        Assert.isNotNull(source);
        Assert.isNotNull(validator);
        Object resultObject = null;
        beginCollecting();
        try
        {

            fValidator = validator;
            fValidator.setProblemCollector(this);
            resultObject = beforeScan(source);
            if (resultObject == null)
                return null;

            doScan(source, resultObject);
            return afterScan(resultObject);
        }
        catch (ScannerException scex)
        {

            if (scex.getLocation() != null)
            {
                addProblem(IProblem.ERROR, scex.getLocation(), scex.getMessage(), scex
                        .isTemporary(), scex.getCode());
            }
            else
            {
                addProblem(new DefaultProblem(IProblem.ERROR,
                        scex.getMessage(), SourceLocation.FILE_LOCATION, false, scex.getCode()));
            }
            return null;
        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
            throw e;

        }
        finally
        {
            cleanup();
            endCollecting();
        }

    }

    protected abstract void doScan(Object source, Object resultObject) throws ScannerException;

    protected abstract Object beforeScan(Object source) throws ScannerException;

    protected abstract void cleanup();

    protected Object afterScan(Object scanResults) throws ScannerException
    {
        return scanResults;
    }

    public void beginCollecting()
    {
        if (fExternalProblemCollector != null)
            fExternalProblemCollector.beginCollecting();

        fProblems.clear();
    }

    public void endCollecting()
    {
        if (fExternalProblemCollector != null)
            fExternalProblemCollector.endCollecting();
    }

    public void addProblem(IProblem problem)
    {
        if (fExternalProblemCollector != null)
        {
            fExternalProblemCollector.addProblem(problem);
        }
        else
        {
            fProblems.add(problem);
        }
    }

    public void addProblem(int severity, ISourceLocation location, String message,
            boolean isTemporary, int code)
    {
        addProblem(new DefaultProblem(severity, message, location, isTemporary, code));
    }

    public void addProblems(IProblem[] problems)
    {
        if (problems != null)
            for (int i = 0; i < problems.length; i++)
            {
                addProblem(problems[i]);
            }
    }

    public IProblem[] getProblems()
    {
        if (fExternalProblemCollector != null)
            return fExternalProblemCollector.getProblems();
        return (IProblem[]) fProblems.toArray(new IProblem[fProblems.size()]);
    }

    public boolean isElement(Node node, String elementName)
    {
        return W3CAccess.isElement(node, elementName);
    }

    public String getValue(Node node)
    {
        return W3CAccess.getValue(node);
    }

    protected boolean isDummyString(String value)
    {
        if (value != null)
            return value.startsWith(fValidator.getDummyStringPrefix());

        return false;
    }

    protected String getAttribute(Node node, String attributeName)
    {
        return getAttribute(node, attributeName, false);
    }

    protected boolean getBooleanAttribute(Node node, String attributeName, boolean defaultValue)
    {
        return W3CAccess.getBooleanAttribute(node, attributeName, defaultValue);
    }

    protected boolean getBooleanAttribute(Node node, String attributeName)
    {
        return W3CAccess.getBooleanAttribute(node, attributeName);
    }

    protected String getAttribute(Node node, String attributeName, boolean returnDummyIfNull)
    {
        String result = W3CAccess.getAttribute(node, attributeName);
        if (TapestryCore.isNull(result) && returnDummyIfNull)
            result = getNextDummyString();

        return result;
    }

    protected String getAttribute(Node node, String attributeName, boolean returnDummyIfNull,
            boolean warnIfNull)
    {
        String result = W3CAccess.getAttribute(node, attributeName);
        if (TapestryCore.isNull(result) && returnDummyIfNull)
        {
            result = getNextDummyString();
            if (warnIfNull)
                addProblem(
                        IProblem.WARNING,
                        getAttributeSourceLocation(node, attributeName),
                        "warning, attribute value is null!",
                        false,
                        -1);
        }

        return result;
    }

    protected ISourceLocationInfo getSourceLocationInfo(Node node)
    {
        return W3CAccess.getSourceLocationInfo(node);
    }

    protected ISourceLocation getBestGuessSourceLocation(Node node, boolean forNodeContent)
    {
        ISourceLocationInfo info = getSourceLocationInfo(node);

        if (info != null)
        {
            if (forNodeContent)
            {
                if (!info.isEmptyTag())
                {
                    return info.getContentSourceLocation();
                }
                else
                {
                    return info.getTagNameLocation();
                }
            }
            else
            {
                return info.getTagNameLocation();
            }
        }
        return null;
    }

    protected ISourceLocation getNodeStartSourceLocation(Node node)
    {
        ISourceLocationInfo info = getSourceLocationInfo(node);
        ISourceLocation result = null;
        if (info != null)
            result = info.getTagNameLocation();

        return result;
    }

    protected ISourceLocation getNodeEndSourceLocation(Node node)
    {
        ISourceLocationInfo info = getSourceLocationInfo(node);
        ISourceLocation result = null;
        if (info != null)
        {
            result = info.getEndTagSourceLocation();
            if (result == null)
            {
                result = info.getTagNameLocation();
            }
        }
        return result;
    }

    protected ISourceLocation getNodeBodySourceLocation(Node node)
    {
        ISourceLocationInfo info = getSourceLocationInfo(node);
        ISourceLocation result = null;

        if (info != null)
        {
            result = info.getContentSourceLocation();
        }
        return result;
    }

    protected ISourceLocation getAttributeSourceLocation(Node node, String rawname)
    {
        ISourceLocationInfo info = getSourceLocationInfo(node);
        ISourceLocation result = null;
        if (info != null)
        {
            result = info.getAttributeSourceLocation(rawname);
            if (result == null)
            {
                result = info.getTagNameLocation();
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getNextDummyString()
     */
    protected String getNextDummyString()
    {
        return fValidator.getDummyStringPrefix();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getDummyStringPrefix()
     */
    protected String getDummyStringPrefix()
    {
        return fValidator.getDummyStringPrefix();
    }

    /**
     * @return
     */
    public IProblemCollector getExternalProblemCollector()
    {
        return fExternalProblemCollector;
    }

    /**
     * @param collector
     */
    public void setExternalProblemCollector(IProblemCollector collector)
    {
        fExternalProblemCollector = collector;
    }

}