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
import java.util.Map;

/**
 *  An Order preserving map that fires property change events to its parent
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PropertyFiringMap extends OrderPreservingMap
{

    protected PropertyChangeSupport propertySupport;
    private String propertyName;
    protected Object parent;

    public PropertyFiringMap(Object parent, String propertyName) {
        super();
        Assert.isNotNull(parent);
        Assert.isNotNull(propertyName);        
        propertySupport = new PropertyChangeSupport(this);
        this.propertyName = propertyName;
        this.parent = parent;       
    }

    public PropertyFiringMap(PropertyChangeListener parent, String propertyName)
    {
        this((Object)parent, propertyName);
        propertySupport.addPropertyChangeListener(parent);
    }

    private void firePropertyChange(Object oldValue, Object newValue)
    {
        propertySupport.firePropertyChange(new PropertyChangeEvent(parent, propertyName, oldValue, newValue));
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);        
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value)
    {
        Object old = super.get(key);
        Object result = super.put(key, value);
        firePropertyChange(old, result);
        return result;

    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map aMap)
    {
        super.putAll(aMap);
        firePropertyChange(null, this);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        Object result = super.remove(key);
        firePropertyChange(result, null);
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear()
    {        
        super.clear();
        firePropertyChange(null, this);
    }

}
