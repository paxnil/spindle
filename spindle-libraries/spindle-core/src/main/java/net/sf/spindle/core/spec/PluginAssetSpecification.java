package net.sf.spindle.core.spec;

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
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;

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
        super(SpecType.ASSET_SPEC);
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