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

package com.iw.plugins.spindle.core.spec;

import org.apache.tapestry.ILocation;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 *  Record <property> tags in a document
 * 
 *  These can only be validated at the time the document is parsed/scanned.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginPropertyDeclaration extends BaseSpecification
{
    String fKey;
    String fValue;
    boolean fValueIsFromAttribute;

    public PluginPropertyDeclaration(String key, String value)
    {
        super(BaseSpecification.PROPERTY_DECLARATION);
        setKey(key);
        fValue = value;
    }

    public String getKey()
    {
        return getIdentifier();
    }

    public void setKey(String key)
    {
        setIdentifier(key);
    }

    public String getValue()
    {
        return fValue;
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        BasePropertyHolder spec = (BasePropertyHolder) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        if (sourceInfo == null) //TODO is this the right thing to do?
            return;

        String key = getKey();

        try
        {
            if (spec.getPropertyDeclaration(key) != this)
            {
                validator.addProblem(
                    IProblem.WARNING,
                    sourceInfo.getAttributeSourceLocation("name"),
                    "duplicate definition of property: " + key);
            }

            String value = getValue();

            if (value != null && value.trim().length() == 0)
            {
                validator.addProblem(
                    IProblem.WARNING,
                    fValueIsFromAttribute
                        ? sourceInfo.getAttributeSourceLocation("value")
                        : sourceInfo.getContentSourceLocation(),
                    "missing value of property: " + key);
            }
        } catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }
    /**
     * @return
     */
    public boolean isValueIsFromAttribute()
    {
        return fValueIsFromAttribute;
    }

    /**
     * @param b
     */
    public void setValueIsFromAttribute(boolean flag)
    {
        fValueIsFromAttribute = flag;
    }

}
