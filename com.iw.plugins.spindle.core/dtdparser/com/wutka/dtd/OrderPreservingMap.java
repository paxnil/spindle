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

package com.wutka.dtd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iw.plugins.spindle.core.util.Assert;

/**
 *  A map that preserves the order things are added
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class OrderPreservingMap implements Map
{

    List fKeys;
    List fValues;

    public OrderPreservingMap()
    {
        super();
        fKeys = new ArrayList();
        fValues = new ArrayList();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        fKeys.clear();
        fValues.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return fKeys.contains(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value)
    {

        return fValues.contains(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet()
    {
        // not implemented
        throw new RuntimeException("not implemented");
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        int index = getKeyIndex(key);
        if (index >= 0)
            return fValues.get(index);

        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return fKeys.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet()
    {

        return Collections.unmodifiableSet(new OrderPreservingSet(fKeys));
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
                fValues.set(index, value);
            }
        } else
        {
            Assert.isNotNull(value);
            fKeys.add(key);
            fValues.add(value);

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
            return internalRemove(index);

        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size()
    {
        return fKeys.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values()
    {
        return Collections.unmodifiableCollection(fValues);
    }

    private Object internalRemove(int index)
    {
        fKeys.remove(index);
        return fValues.remove(index);
    }

    protected int getKeyIndex(Object key)
    {
        return fKeys.indexOf(key);
    }

}
