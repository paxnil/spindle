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

import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 *  Spindle's implementation of IAssetSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginAssetSpecification extends BasePropertyHolder implements IAssetSpecification
{

    private String fPath;
    private AssetType fAssetType;

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
        this.fAssetType = type;
        this.fPath = path;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#getPath()
     */
    public String getPath()
    {
        return fPath;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#getType()
     */
    public AssetType getType()
    {
        return fAssetType;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#setPath(java.lang.String)
     */
    public void setPath(String path)
    {
        this.fPath = path;
        firePropertyChange("path", null, path);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IAssetSpecification#setType(org.apache.tapestry.spec.AssetType)
     */
    public void setType(AssetType type)
    {
        this.fAssetType = type;
        firePropertyChange("type", null, fPath);
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        IComponentSpecification component = (IComponentSpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        String name = getIdentifier();

        try
        {
            if (component.getAsset(name) != this)
            {
                validator.addProblem(
                    IProblem.ERROR,
                    sourceInfo.getAttributeSourceLocation("name"),
                    TapestryCore.getTapestryString(
                        "ComponentSpecification.duplicate-asset",
                        component.getSpecificationLocation().getName(),
                        name));
            }

            if (!name.equals(ITemplateSource.TEMPLATE_ASSET_NAME))
                validator.validatePattern(
                    name,
                    SpecificationParser.ASSET_NAME_PATTERN,
                    "SpecificationParser.invalid-asset-name",
                    IProblem.ERROR,
                    sourceInfo.getAttributeSourceLocation("name"));

            validator.validateAsset(component, this, sourceInfo);

        } catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
    }
}
