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

import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;

/**
 *  Spindle's implementation of IAssetSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginAssetSpecification extends BasePropertyHolder implements IAssetSpecification
{

    private String path;
    private AssetType type;

    /**
     * @param type
     */
    public PluginAssetSpecification()
    {
        super(BaseSpecification.ASSET_SPEC);
    }

   
    public PluginAssetSpecification(AssetType type, String path)
    {
        this();
        this.type = type;
        this.path = path;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#getPath()
     */
    public String getPath()
    {
        return path;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#getType()
     */
    public AssetType getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#setPath(java.lang.String)
     */
    public void setPath(String path)
    {
        this.path = path;
        firePropertyChange("path", null, path);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#setType(org.apache.tapestry.spec.AssetType)
     */
    public void setType(AssetType type)
    {
        this.type = type;
        firePropertyChange("type", null, path);
    }

}
