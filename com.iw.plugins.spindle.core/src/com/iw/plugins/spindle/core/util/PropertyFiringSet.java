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

package com.iw.plugins.spindle.core.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

/**
 *  An Order preserving map that fires property change events to its parent
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PropertyFiringSet extends OrderPreservingSet
{
    protected Object fParent;
    private String fPropertyName;

    private PropertyChangeSupport fPropertySupport;

    public PropertyFiringSet(Object parent, String propertyName)
    {
        super();
        Assert.isNotNull(parent);
        Assert.isNotNull(propertyName);
        fPropertySupport = new PropertyChangeSupport(this);
        this.fPropertyName = propertyName;
        this.fParent = parent;
    }

    public PropertyFiringSet(PropertyChangeListener parent, String propertyName)
    {
        this((Object) parent, propertyName);
        fPropertySupport.addPropertyChangeListener(parent);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        fPropertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        fPropertySupport.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object arg0)
    {
        boolean result = super.add(arg0);
        if (result)
            firePropertyChange(null, arg0);

        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection arg0)
    {
        boolean result = super.addAll(arg0);
        if (result)
            firePropertyChange(this, this);

        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear()
    {
        super.clear();
        firePropertyChange(this, this);
    }

    private void firePropertyChange(Object oldValue, Object newValue)
    {
        fPropertySupport.firePropertyChange(new PropertyChangeEvent(fParent, fPropertyName, oldValue, newValue));
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public boolean remove(Object arg0)
    {
        boolean result = super.remove(arg0);
        if (result)
            firePropertyChange(arg0, null);

        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection arg0)
    {
        boolean result = super.removeAll(arg0);
        if (result)
            firePropertyChange(this, this);

        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection arg0)
    {
        boolean result = super.retainAll(arg0);
        if (result)
            firePropertyChange(this, this);

        return result;
    }

}
