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

import java.util.Iterator;

import com.iw.plugins.spindle.core.spec.IIdentifiable;

/**
 *  A map that hooks/unhooks its contents from/to its parents and identifiers 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class IIdentifiableMap extends PropertyFiringMap
{

    /**
     * @param parent
     * @param propertyName
     */
    public IIdentifiableMap(Object parent, String propertyName)
    {
        super(parent, propertyName);
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value)
    {
        Assert.isNotNull(value);
        if (!(value instanceof IIdentifiable))
        {
            throw new IllegalStateException();
        }
        hookIdentifiable((String) key, (IIdentifiable) value);
        int index = getKeyIndex(key);
        Object result = super.get(key);
        if (result != null)
        {
            unhookIdentifiable((IIdentifiable) result);
        }
        return super.put(key, value);

    }

    private void hookIdentifiable(String identifier, IIdentifiable identifiable)
    {
        identifiable.setParent(parent);
        identifiable.setIdentifier(identifier);
    }

    private void unhookIdentifiable(IIdentifiable identifiable)
    {
        identifiable.setParent(null);
        identifiable.setIdentifier(null);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        Object result = get(key);
        if (result != null)
        {
            unhookIdentifiable((IIdentifiable) result);
        }
        return super.remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        for (Iterator iter = values.iterator(); iter.hasNext();)
        {
            unhookIdentifiable((IIdentifiable) iter.next());
        }
        super.clear();
    }

}
