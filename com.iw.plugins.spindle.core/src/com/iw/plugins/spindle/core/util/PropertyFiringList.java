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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A List that fires property change events to its parent
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PropertyFiringList implements List
{

    private ArrayList store = new ArrayList();

    protected Object parent;
    private String propertyName;

    private PropertyChangeSupport propertySupport;

    public PropertyFiringList(Object parent, String propertyName) {
         super();
         Assert.isNotNull(parent);
         Assert.isNotNull(propertyName);        
         propertySupport = new PropertyChangeSupport(this);
         this.propertyName = propertyName;
         this.parent = parent;       
     }

     public PropertyFiringList(PropertyChangeListener parent, String propertyName)
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
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int arg0, Object arg1)
    {
        store.add(arg0, arg1);
        firePropertyChange(null, arg1);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object arg0)
    {
        boolean result = store.add(arg0);
        if (result)
        {
            firePropertyChange(null, arg0);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection arg0)
    {
        boolean result = store.addAll(arg0);
        if (result)
        {
            firePropertyChange(null, this);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int arg0, Collection arg1)
    {
        boolean result = store.addAll(arg0, arg1);
        if (result)
        {
            firePropertyChange(null, this);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear()
    {
        List old = Collections.unmodifiableList((List)store.clone());
        boolean fire = !store.isEmpty();
        store.clear();
        if (fire)
        {
            firePropertyChange(old, Collections.unmodifiableList(store));
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object arg0)
    {
        return store.contains(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection arg0)
    {
        return store.containsAll(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    public Object get(int arg0)
    {
        return store.get(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object arg0)
    {
        return store.indexOf(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty()
    {
        return store.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator()
    {
        return store.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object arg0)
    {
        return store.lastIndexOf(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator()
    {
        return store.listIterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int arg0)
    {
        return store.listIterator(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    public Object remove(int arg0)
    {
        Object result = store.remove(arg0);
        if (result != null)
        {
            firePropertyChange(result, null);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object arg0)
    {
        boolean result = store.remove(arg0);
        if (result)
        {
            firePropertyChange(arg0, null);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection arg0)
    {
        boolean result = store.removeAll(arg0);
        if (result)
        {
            firePropertyChange(null, this);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection arg0)
    {
        boolean result = store.retainAll(arg0);
        if (result)
        {
            firePropertyChange(null, this);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int arg0, Object arg1)
    {
        Object result = store.set(arg0,arg1);
        if (result != null)
        {
            firePropertyChange(result, arg1);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size()
    {
        return store.size();
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    public List subList(int arg0, int arg1)
    {
        return store.subList(arg0,arg1);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray()
    {
        return store.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] arg0)
    {
        return store.toArray(arg0);
    }

}
