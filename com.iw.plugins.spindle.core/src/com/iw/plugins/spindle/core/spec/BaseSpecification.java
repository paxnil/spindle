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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ILocatable;
import org.apache.tapestry.ILocation;
import org.apache.tapestry.ILocationHolder;

/**
 *  Base class for all Spec classes.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BaseSpecification implements IIdentifiable, PropertyChangeListener, ILocatable, ILocationHolder
{

    public static final int APPLICATION_SPEC = 0;
    public static final int ASSET_SPEC = 1;
    public static final int BEAN_SPEC = 2;
    public static final int BINDING_SPEC = 3;
    public static final int COMPONENT_SPEC = 4;
    public static final int CONTAINED_COMPONENT_SPEC = 5;
    public static final int EXTENSION_CONFIGURATION = 6;
    public static final int EXTENSION_SPEC = 7;
    public static final int LIBRARY_SPEC = 8;
    public static final int LISTENER_BINDING_SPEC = 9;
    public static final int PARAMETER_SPEC = 10;
    public static final int PROPERTY_SPEC = 11;

    public static final int EXPRESSION_BEAN_INIT = 20;
    public static final int FIELD_BEAN_INIT = 21;
    public static final int STATIC_BEAN_INIT = 22;
    public static final int STRING_BEAN_INIT = 23;

    private String fIdentifier;
    private Object fParent;
    protected ILocation fILocation;

    private PropertyChangeSupport fPropertySupport;
    private int fSpecificationType = -1;

    public BaseSpecification(int type)
    {
        super();
        fSpecificationType = type;
        fPropertySupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener != this)
            fPropertySupport.addPropertyChangeListener(listener);
    }

    public void firePropertyChange(String property, boolean oldValue, boolean newValue)
    {
        fPropertySupport.firePropertyChange(property, oldValue, newValue);
    }

    public void firePropertyChange(String property, int oldValue, int newValue)
    {
        fPropertySupport.firePropertyChange(property, oldValue, newValue);
    }

    public void firePropertyChange(String property, Object oldValue, Object newValue)
    {
        fPropertySupport.firePropertyChange(property, oldValue, newValue);
    }

    protected Object get(Map map, Object key)
    {
        if (map == null)
            return null;

        return map.get(key);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
     */
    public String getIdentifier()
    {
        return fIdentifier;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getParent()
     */
    public Object getParent()
    {
        return fParent;
    }

    public int getSpecificationType()
    {
        return fSpecificationType;
    }

    protected List keys(Map map)
    {
        if (map == null)
            return Collections.EMPTY_LIST;

        List result = new ArrayList(map.keySet());

        return result;
    }

    /** 
     * Propogate property changes up. Overriders should make sure they maintain this
     * behaviour.
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() == this)
        {
            fPropertySupport.firePropertyChange(evt);
        } else
        {
            fPropertySupport.firePropertyChange(new PropertyChangeEvent(this, getIdentifier(), null, this));
        }

    }

    protected void remove(Map map, Object key)
    {
        if (map != null)
            map.remove(key);
    }

    protected boolean remove(Set set, Object obj)
    {
        if (set != null)
            return set.remove(obj);

        return false;
    }

    protected boolean remove(List list, Object obj)
    {
        if (list != null)
            return list.remove(obj);

        return false;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        fPropertySupport.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(java.lang.String)
     */
    public void setIdentifier(String id)
    {
        this.fIdentifier = id;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(java.lang.Object)
     */
    public void setParent(Object parent)
    {
        this.fParent = parent;
    }

    public ILocation getLocation()
    {
        return fILocation;
    }

    public void setLocation(ILocation location)
    {
        this.fILocation = location;
    }

}
