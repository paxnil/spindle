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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * Tapestry Extensions for Spindle
 * 
 * @author glongman@gmail.com
 */
public class PluginExtensionSpecification extends BasePropertyHolder implements
        IExtensionSpecification
{

    private String fClassName;

    protected Map<String, Object> fConfiguration;

    private List<PluginExtensionConfiguration> fRawConfigurations;

    private boolean fImmediate;

    /**
     * @param type
     */
    public PluginExtensionSpecification()
    {
        super(SpecType.EXTENSION_SPEC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#getClassName()
     */
    public String getClassName()
    {
        return fClassName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#setClassName(java.lang.String)
     */
    public void setClassName(String className)
    {
        this.fClassName = className;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#addConfiguration(java.lang.String,
     *      java.lang.Object)
     */
    public void addConfiguration(String propertyName, Object value)
    {
        if (fConfiguration == null)
            fConfiguration = new HashMap<String, Object>();

        if (!fConfiguration.containsKey(propertyName))
            fConfiguration.put(propertyName, value);
    }

    public void addConfiguration(PluginExtensionConfiguration configuration)
    {
        if (fRawConfigurations == null)
            fRawConfigurations = new ArrayList<PluginExtensionConfiguration>();

        fRawConfigurations.add(configuration);
        configuration.setParent(this);

        addConfiguration(configuration.getIdentifier(), configuration);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#getConfiguration()
     */
    public Map getConfiguration()
    {
        if (fConfiguration != null)
            return Collections.unmodifiableMap(fConfiguration);

        return Collections.EMPTY_MAP;

    }

    public List getConfigurationObjects()
    {
        if (fRawConfigurations != null)
            return Collections.unmodifiableList(fRawConfigurations);

        return Collections.EMPTY_LIST;
    }

    public boolean containsConfiguration(String propertyName)
    {
        if (fRawConfigurations == null)
            return false;

        for (int i = 0; i < fRawConfigurations.size(); i++)
        {
            PluginExtensionConfiguration config = (PluginExtensionConfiguration) fRawConfigurations
                    .get(i);
            if (config.getIdentifier().equals(propertyName))
                return true;
        }
        return false;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#addConfiguration(java.lang.String,
     *      java.lang.String)
     */
    public void addConfiguration(String propertyName, String value)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#instantiateExtension()
     */
    public Object instantiateExtension()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#isImmediate()
     */
    public boolean isImmediate()
    {
        return fImmediate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IExtensionSpecification#setImmediate(boolean)
     */
    public void setImmediate(boolean immediate)
    {
        fImmediate = immediate;
    }

    /**
     * Revalidate this declaration. Note that validating the existence of the value is only possible
     * during a parse/scan cycle. But that's ok 'cuz those kinds of problems would have already been
     * caught.
     * 
     * @param parent
     *            the object holding this
     * @param validator
     *            a validator helper
     */

    public void validate(Object parent, IScannerValidator validator)
    {
        validateSelf(parent, validator);

        for (Iterator iter = getConfigurationObjects().iterator(); iter.hasNext();)
        {
            PluginExtensionConfiguration element = (PluginExtensionConfiguration) iter.next();
            element.validate(this, validator);
        }

    }

    public void validateSelf(Object parent, IScannerValidator validator)
    {
        ILibrarySpecification library = (ILibrarySpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        try
        {

            validator.validateTypeName(
                    (ICoreResource) library.getSpecificationLocation(),
                    fClassName,
                    IProblem.ERROR,
                    sourceInfo.getAttributeSourceLocation("class"));

        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
        }

    }

}