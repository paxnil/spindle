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

    private ArrayList fStore = new ArrayList();

    protected Object fParent;
    private String fPropertyName;

    private PropertyChangeSupport fPropertySupport;

    public PropertyFiringList(Object parent, String propertyName) {
         super();
         Assert.isNotNull(parent);
         Assert.isNotNull(propertyName);        
         fPropertySupport = new PropertyChangeSupport(this);
         this.fPropertyName = propertyName;
         this.fParent = parent;       
     }

     public PropertyFiringList(PropertyChangeListener parent, String propertyName)
     {
         this((Object)parent, propertyName);
         fPropertySupport.addPropertyChangeListener(parent);
     }

     private void firePropertyChange(Object oldValue, Object newValue)
     {
         fPropertySupport.firePropertyChange(new PropertyChangeEvent(fParent, fPropertyName, oldValue, newValue));
     }
    
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         fPropertySupport.addPropertyChangeListener(listener);        
     }
    
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         fPropertySupport.removePropertyChangeListener(listener);
     }

    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int arg0, Object arg1)
    {
        fStore.add(arg0, arg1);
        firePropertyChange(null, arg1);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object arg0)
    {
        boolean result = fStore.add(arg0);
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
        boolean result = fStore.addAll(arg0);
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
        boolean result = fStore.addAll(arg0, arg1);
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
        List old = Collections.unmodifiableList((List)fStore.clone());
        boolean fire = !fStore.isEmpty();
        fStore.clear();
        if (fire)
        {
            firePropertyChange(old, Collections.unmodifiableList(fStore));
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object arg0)
    {
        return fStore.contains(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection arg0)
    {
        return fStore.containsAll(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    public Object get(int arg0)
    {
        return fStore.get(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object arg0)
    {
        return fStore.indexOf(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty()
    {
        return fStore.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator()
    {
        return fStore.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object arg0)
    {
        return fStore.lastIndexOf(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator()
    {
        return fStore.listIterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int arg0)
    {
        return fStore.listIterator(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    public Object remove(int arg0)
    {
        Object result = fStore.remove(arg0);
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
        boolean result = fStore.remove(arg0);
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
        boolean result = fStore.removeAll(arg0);
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
        boolean result = fStore.retainAll(arg0);
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
        Object result = fStore.set(arg0,arg1);
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
        return fStore.size();
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    public List subList(int arg0, int arg1)
    {
        return fStore.subList(arg0,arg1);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray()
    {
        return fStore.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] arg0)
    {
        return fStore.toArray(arg0);
    }

}
