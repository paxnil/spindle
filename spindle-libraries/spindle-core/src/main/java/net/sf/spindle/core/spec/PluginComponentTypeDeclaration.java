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

import org.apache.hivemind.Location;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * Record <component>tags in a document
 * 
 * @author glongman@gmail.com
 */
public class PluginComponentTypeDeclaration extends DescribableSpecification
{

    String fResourcePath;

    public PluginComponentTypeDeclaration(String id, String resourcePath, Location location)
    {
        super(BaseSpecification.COMPONENT_TYPE_DECLARATION);
        setIdentifier(id);
        fResourcePath = resourcePath;
        setLocation(location);
    }

    public String getId()
    {
        return getIdentifier();
    }

    public String getResourcePath()
    {
        return fResourcePath;
    }

    /**
     * Revalidate this declaration. Note that some validations, like duplicate ids, are only
     * possible during a parse/scan cycle. But that's ok 'cuz those kinds of problems would have
     * already been caught.
     * 
     * @param parent
     *            the object holding this
     * @param validator
     *            a validator helper
     */
    public void validate(Object parent, IScannerValidator validator)
    {
        ISourceLocationInfo info = (ISourceLocationInfo) getLocation();

        try
        {

            ILibrarySpecification parentLib = (ILibrarySpecification) parent;

            validator.validateResource(
                    parentLib.getSpecificationLocation(),
                    fResourcePath,
                    "scan-library-missing-component",
                    info.getAttributeSourceLocation("specification-path"));

        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
        }

    }

}