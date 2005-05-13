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

package com.iw.plugins.spindle.core.spec;

import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * Spindle's implementation of IAssetSpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginAssetSpecification extends BasePropertyHolder implements IAssetSpecification
{

    private String fPath;

    private String fPropertyName;

    /**
     * @param type
     */
    public PluginAssetSpecification()
    {
        super(BaseSpecification.ASSET_SPEC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IAssetSpecification#getPath()
     */
    public String getPath()
    {
        return fPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IAssetSpecification#setPath(java.lang.String)
     */
    public void setPath(String path)
    {
        this.fPath = path;
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        IComponentSpecification component = (IComponentSpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        try
        {
            validator.validateAsset(component, this, sourceInfo);

        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }

    public String getPropertyName()
    {
        return fPropertyName;
    }

    public void setPropertyName(String propertyName)
    {
        this.fPropertyName = propertyName;
    }
}