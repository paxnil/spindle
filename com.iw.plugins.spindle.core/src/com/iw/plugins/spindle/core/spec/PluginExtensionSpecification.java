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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 *  Tapestry Extensions for Spindle
 * 
 *  TODO complete transformation away from strict Tapestry interface adherence.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginExtensionSpecification extends BasePropertyHolder implements IExtensionSpecification
{

    private String fClassName;
    protected Map fConfiguration;
    private List fRawConfigurations;
    private boolean fImmediate;
    /**
     * @param type
     */
    public PluginExtensionSpecification()
    {
        super(BaseSpecification.EXTENSION_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#getClassName()
     */
    public String getClassName()
    {
        return fClassName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#setClassName(java.lang.String)
     */
    public void setClassName(String className)
    {
        this.fClassName = className;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#addConfiguration(java.lang.String, java.lang.Object)
     */
    public void addConfiguration(String propertyName, Object value)
    {
        checkInternalCall("PluginExtensionSpecification.addConfiguration may not be called by external client code");
        if (fConfiguration == null)
            fConfiguration = new HashMap();

        if (!fConfiguration.containsKey(propertyName))
            fConfiguration.put(propertyName, value);
    }

    public void addConfiguration(PluginExtensionConfiguration configuration)
    {
        if (fRawConfigurations == null)
            fRawConfigurations = new ArrayList();

        fRawConfigurations.add(configuration);
        configuration.setParent(this);

        beginInternalCall("calling Tapestry addConfiguration");
        try
        {
            addConfiguration(configuration.getIdentifier(), configuration);
        } finally
        {

            endInternalCall();
        }
    }

    /* (non-Javadoc)
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
            PluginExtensionConfiguration config = (PluginExtensionConfiguration) fRawConfigurations.get(i);
            if (config.getIdentifier().equals(propertyName))
                return true;
        }
        return false;

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#instantiateExtension(org.apache.tapestry.IResourceResolver)
     */
    public Object instantiateExtension(IResourceResolver resolver)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#isImmediate()
     */
    public boolean isImmediate()
    {
        return fImmediate;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#setImmediate(boolean)
     */
    public void setImmediate(boolean immediate)
    {
        fImmediate = immediate;
    }

    /**
      *  Revalidate this declaration. Note that validating the existence of the value is only possible
      *  during a parse/scan cycle. But that's ok 'cuz those kinds of problems
      *  would have already been caught.
      * 
      * @param parent the object holding this
      * @param validator a validator helper
      */

    public void validate(Object parent, IScannerValidator validator)
    {
        ILibrarySpecification library = (ILibrarySpecification) parent;

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
                (IResourceWorkspaceLocation) library.getSpecificationLocation(),
                fClassName,
                IProblem.ERROR,
                sourceInfo.getAttributeSourceLocation("class"));

            validator.validatePattern(
                getIdentifier(),
                SpecificationParser.EXTENSION_NAME_PATTERN,
                "SpecificationParser.invalid-extension-name",
                IProblem.ERROR,
                sourceInfo.getAttributeSourceLocation("name"));

        } catch (ScannerException e)
        {
            // TODO remove
            e.printStackTrace();
            TapestryCore.log(e);
        }

    }

}
