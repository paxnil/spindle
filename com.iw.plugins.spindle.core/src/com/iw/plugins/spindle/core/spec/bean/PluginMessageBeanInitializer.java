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

package com.iw.plugins.spindle.core.spec.bean;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.BaseSpecification;

/**
 *  Spindle implementation of a StringBeanInitializer
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginMessageBeanInitializer extends AbstractPluginBeanInitializer
{

    public PluginMessageBeanInitializer()
    {
        super(BaseSpecification.STRING_BEAN_INIT);
    }

    public String getKey()
    {
        return getValue();
    }

    public void setKey(String value)
    {
        setValue(value);
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();
        try
        {
            super.validate(parent, validator);

            String key = getKey();
            if (key != null)
            {
                if (key.trim().length() == 0)
                {
                    validator.addProblem(
                        IProblem.ERROR,
                        sourceInfo.getAttributeSourceLocation("key"),
                        "key must not be empty");
                }
            }
        } catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }

}
