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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  A map that preserves the order things are added
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class OrderPreservingMap implements Map
{

    List keys;
    List values;

    public OrderPreservingMap()
    {
        super();
        keys = new ArrayList();
        values = new ArrayList();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        keys.clear();
        values.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return keys.contains(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value)
    {

        return values.contains(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet()
    {
        // not implemented
        throw new Error("not implemented");
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        int index = getKeyIndex(key);
        if (index >= 0)
        {
            return values.get(index);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return keys.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet()
    {

        return Collections.unmodifiableSet(new OrderPreservingSet(keys));
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value)
    {

        Assert.isNotNull(key);
        int index = getKeyIndex(key);
        if (index >= 0)
        {
            if (value == null)
            {
                return internalRemove(index);
            } else
            {
                values.set(index, value);
            }
        } else
        {
            Assert.isNotNull(value);
            keys.add(key);
            values.add(value);

        }
        return value;
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map aMap)
    {
        Assert.isNotNull(aMap);
        for (Iterator iter = aMap.keySet().iterator(); iter.hasNext();)
        {
            Object key = (Object) iter.next();
            put(key, aMap.get(key));

        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        int index = getKeyIndex(key);
        if (index >= 0)
        {
            return internalRemove(index);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size()
    {
        return keys.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values()
    {
        return Collections.unmodifiableCollection(values);
    }

    private Object internalRemove(int index)
    {
        keys.remove(index);
        return values.remove(index);
    }

    protected int getKeyIndex(Object key)
    {
        return keys.indexOf(key);
    }

}
