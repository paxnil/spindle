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

import org.apache.tapestry.spec.IListenerBindingSpecification;

/**
 * Spindle implementation of IListenerBindingSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginListenerBindingSpecification extends PluginBindingSpecfication implements IListenerBindingSpecification
{

    private String language;
    private String script;

    public PluginListenerBindingSpecification()
    {
        super(BaseSpecification.LISTENER_BINDING_SPEC);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IListenerBindingSpecification#getLanguage()
     */
    public String getLanguage()
    {
        return language;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IListenerBindingSpecification#getScript()
     */
    public String getScript()
    {
        return getValue();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IListenerBindingSpecification#setLanguage(java.lang.String)
     */
    public void setLanguage(String language)
    {
        String old = this.language;
        this.language = language;
        firePropertyChange("language", old, this.language);
    }

    public void setScript(String value)
    {
        super.setValue(value);
    }

}
