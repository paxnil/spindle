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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.hivemind.Locatable;
import org.apache.hivemind.Location;
import org.apache.hivemind.LocationHolder;

/**
 * Base class for all Spec classes.
 * 
 * @author glongman@gmail.com
 */
public abstract class BaseSpecification implements IIdentifiable, Locatable, LocationHolder
{

    private static ThreadLocal INTERNAL_CALL_TRACKER = new ThreadLocal();

    protected static void beginInternalCall(String debugMessage)
    {

        Stack internalStack = (Stack) INTERNAL_CALL_TRACKER.get();

        if (internalStack == null)
        {
            internalStack = new Stack();
            INTERNAL_CALL_TRACKER.set(internalStack);
        }

        internalStack.push(debugMessage);

    }

    protected static void endInternalCall()
    {

        Stack internalStack = (Stack) INTERNAL_CALL_TRACKER.get();

        if (internalStack == null)
            throw new IllegalStateException("stack is null ");

        internalStack.pop();

    }

    protected static void checkInternalCall(String errorMessage)
    {
        Stack internalStack = (Stack) INTERNAL_CALL_TRACKER.get();
        if (internalStack == null || internalStack.isEmpty())
        {
            throw new IllegalStateException(errorMessage);
        }

    }

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

    /** @deprecated */
    public static final int EXPRESSION_BEAN_INIT = 20;

    /** @deprecated */
    public static final int FIELD_BEAN_INIT = 21;

    /** @deprecated */
    public static final int STATIC_BEAN_INIT = 22;

    /** @deprecated */
    public static final int STRING_BEAN_INIT = 23;

    public static final int BINDING_BEAN_INIT = 24;

    public static final int PROPERTY_DECLARATION = 50;

    public static final int PAGE_DECLARATION = 51;

    public static final int COMPONENT_TYPE_DECLARATION = 52;

    public static final int DESCRIPTION_DECLARATION = 53;

    public static final int RESERVED_PARAMETER_DECLARATION = 54;

    public static final int ENGINE_SERVICE_DECLARATION = 55;

    public static final int LIBRARY_DECLARATION = 56;

    public static final int CONFIGURE_DECLARATION = 57;

    public static final int INJECT = 58;

    private String fIdentifier;

    private Object fParent;

    protected Location fILocation;

    private boolean fDirty;

    private boolean fPlaceholderMarker; //a place holder spec.

    private int fSpecificationType = -1;

    public BaseSpecification(int type)
    {
        super();
        fSpecificationType = type;
    }

    public void makePlaceHolder()
    {
        fPlaceholderMarker = true;
    }

    public boolean isPlaceholder()
    {
        return fPlaceholderMarker;
    }

    protected Object get(Map map, Object key)
    {
        if (map == null)
            return null;

        return map.get(key);
    }

    protected List safeList(List list)
    {
        if (list == null)
            return Collections.EMPTY_LIST;

        return Collections.unmodifiableList(list);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.spec.IIdentifiable#getIdentifier()
     */
    public String getIdentifier()
    {
        return fIdentifier;
    }

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setIdentifier(java.lang.String)
     */
    public void setIdentifier(String id)
    {
        this.fIdentifier = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.spec.IIdentifiable#setParent(java.lang.Object)
     */
    public void setParent(Object parent)
    {
        this.fParent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.Locatable#getLocation()
     */
    public Location getLocation()
    {
        return fILocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hivemind.LocationHolder#setLocation(org.apache.hivemind.Location)
     */
    public void setLocation(Location location)
    {
        this.fILocation = location;
    }

    /**
     * @return
     */
    public boolean isDirty()
    {
        return fDirty;
    }

    /**
     * @param b
     */
    public void setDirty(boolean flag)
    {
        fDirty = flag;
    }

}