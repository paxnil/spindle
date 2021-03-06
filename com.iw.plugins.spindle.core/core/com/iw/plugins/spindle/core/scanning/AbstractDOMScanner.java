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

import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.dom.IDOMModel;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.util.Assert;

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