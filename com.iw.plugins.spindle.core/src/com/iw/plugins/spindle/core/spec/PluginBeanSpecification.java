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
import java.util.List;

import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.IBeanSpecification;

/**
 *  Spindle aware concrete implementation of IBeanSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginBeanSpecification extends BasePropertyHolder implements IBeanSpecification
{
    protected String fClassName;
    protected BeanLifecycle fLifecycle;

    /** @since 1.0.9 **/
    private String fDescription;

    /**
     *  A List of {@link IBeanInitializer}.
     *
     **/

    protected List fInitializers;
    /**
     * @param type
     */
    public PluginBeanSpecification()
    {
        super(BaseSpecification.BEAN_SPEC);
    }

    public PluginBeanSpecification(String className, BeanLifecycle lifecycle)
    {
        this();
        fClassName = className;
        fLifecycle = lifecycle;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getClassName()
     */
    public String getClassName()
    {
        return fClassName;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getLifecycle()
     */
    public BeanLifecycle getLifecycle()
    {
        return fLifecycle;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#addInitializer(org.apache.tapestry.bean.IBeanInitializer)
     */
    public void addInitializer(IBeanInitializer initializer)
    {
        if (fInitializers == null)
            fInitializers = new ArrayList();

        fInitializers.add(initializer);
    }

    public void removeInitializer(IBeanInitializer initializer)
    {
        remove(fInitializers, initializer);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getInitializers()
     */
    public List getInitializers()
    {
        return fInitializers;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getDescription()
     */
    public String getDescription()
    {
        return fDescription;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#setDescription(java.lang.String)
     */
    public void setDescription(String desc)
    {
        String old = fDescription;
        fDescription = desc;
        firePropertyChange("description", old, this.fDescription);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#setClassName(java.lang.String)
     */
    public void setClassName(String className)
    {
        String old = fClassName;
        this.fClassName = className;
        firePropertyChange("className", old, this.fClassName);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#setLifecycle(org.apache.tapestry.spec.BeanLifecycle)
     */
    public void setLifecycle(BeanLifecycle lifecycle)
    {
        BeanLifecycle old = this.fLifecycle;
        fLifecycle = lifecycle;
        firePropertyChange("lifecycle", old, this.fLifecycle);
    }

}
