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

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.spec.IExtensionSpecification;

import com.iw.plugins.spindle.core.util.IIdentifiableMap;

/**
 *  Tapestry Extensions for Spindle
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginExtensionSpecification extends BasePropertyHolder implements IExtensionSpecification
{

    private String fClassName;
    protected Map fConfiguration;
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
        firePropertyChange("className", null, className);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#addConfiguration(java.lang.String, java.lang.Object)
     */
    public void addConfiguration(String propertyName, Object value)
    {
        if (fConfiguration == null)
            fConfiguration = new IIdentifiableMap(this, "configration");

        PluginExtensionConfiguration newConfig = new PluginExtensionConfiguration(propertyName, value);
        fConfiguration.put(propertyName, newConfig);
    }

    public void removeConfiguration(String propertyName)
    {
        remove(fConfiguration, propertyName);
    }

    public void setConfiguration(String propertyName, PluginExtensionConfiguration config)
    {
        if (fConfiguration == null)
            fConfiguration = new IIdentifiableMap(this, "configuration");

        fConfiguration.put(propertyName, config);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IExtensionSpecification#getConfiguration()
     */
    public Map getConfiguration()
    {
        if (fConfiguration != null)
            return Collections.unmodifiableMap(fConfiguration);

        return Collections.EMPTY_MAP;
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
        boolean old = fImmediate;
        fImmediate = immediate;
        firePropertyChange("immediate", old, immediate);
    }

   
}
