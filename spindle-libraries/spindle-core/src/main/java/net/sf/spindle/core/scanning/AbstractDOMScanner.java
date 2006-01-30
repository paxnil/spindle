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
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.ISourceLocationInfo;
import net.sf.spindle.core.util.Assert;
import net.sf.spindle.core.util.W3CAccess;

import org.w3c.dom.Node;

/**
 * Base Class for DOM Node processors
 * <p>
 *
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractDOMScanner extends AbstractScanner
{

    public Object scan(IDOMModel source, IScannerValidator validator) throws ScannerException
    {
        return scan((Object)source, validator);
    }

    public Object scan(Object source, IScannerValidator validator) throws ScannerException
    {
        Assert.isLegal(source instanceof IDOMModel);
        return super.scan(source, validator);
    }

    public boolean isElement(Node node, String elementName)
    {
        return W3CAccess.isElement(node, elementName);
    }

    public String getValue(Node node)
    {
        return W3CAccess.getValue(node);
    }
    
    public IDOMModel getDOMModel() {
        return (IDOMModel)fSource;
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
        return ((IDOMModel) fSource).getSourceLocationInfo(node);
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
}