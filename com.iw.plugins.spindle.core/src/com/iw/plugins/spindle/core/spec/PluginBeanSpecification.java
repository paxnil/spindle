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

import java.util.List;

import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.IBeanSpecification;

import com.iw.plugins.spindle.core.util.PropertyFiringList;

/**
 *  Spindle aware concrete implementation of IBeanSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PluginBeanSpecification extends BasePropertyHolder implements IBeanSpecification
{
    protected String className;
    protected BeanLifecycle lifecycle;

    /** @since 1.0.9 **/
    private String description;

    /**
     *  A List of {@link IBeanInitializer}.
     *
     **/

    protected List initializers;
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
        this.className = className;
        this.lifecycle = lifecycle;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getClassName()
     */
    public String getClassName()
    {
        return className;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getLifecycle()
     */
    public BeanLifecycle getLifecycle()
    {
        return lifecycle;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#addInitializer(org.apache.tapestry.bean.IBeanInitializer)
     */
    public void addInitializer(IBeanInitializer initializer)
    {
        if (initializers == null)
        {
            initializers = new PropertyFiringList(this, "initializers");
        }

        initializers.add(initializer);
    }
    
    public void removeInitializer(IBeanInitializer initializer){
        remove(initializers, initializer);
    }
    

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getInitializers()
     */
    public List getInitializers()
    {
        return initializers;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#setDescription(java.lang.String)
     */
    public void setDescription(String desc)
    {
        String old = this.description;
        this.description = desc;
        firePropertyChange("description", old, this.description);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#setClassName(java.lang.String)
     */
    public void setClassName(String className)
    {
        String old = this.className;
        this.className = className;
        firePropertyChange("className", old, this.className);
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.IBeanSpecification#setLifecycle(org.apache.tapestry.spec.BeanLifecycle)
     */
    public void setLifecycle(BeanLifecycle lifecycle)
    {
        BeanLifecycle old = this.lifecycle;
        this.lifecycle = lifecycle;
        firePropertyChange("lifecycle", old, this.lifecycle);
    }

}
