package net.sf.spindle.core.spec;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private String fIdentifier;

    private Object fParent;

    protected Location fILocation;

    private boolean fDirty;

    private boolean fPlaceholderMarker; // a place holder spec.

    private SpecType fSpecificationType = SpecType.UNKNOWN;

    public BaseSpecification(SpecType type)
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

    protected <K extends Object> K get(Map<? extends Object, K> map, Object key)
    {
        if (map == null)
            return null;

        return map.get(key);
    }

    protected <T extends List<? super T>> List<T> safeList(List<T> list)
    {
        if (list == null)
            return Collections.emptyList();

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

    public SpecType getSpecificationType()
    {
        return fSpecificationType;
    }

    protected <T extends Object>List<T> keys(Map<T, ? extends Object> map)
    {
        if (map == null)
            return Collections.emptyList();

        return new ArrayList<T>(map.keySet());
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

    // isDirty/setDirty are holdovers from a very very old version of spindle.
    // no currently used for anything.
    @Deprecated
    public boolean isDirty()
    {
        return fDirty;
    }

    @Deprecated
    public void setDirty(boolean flag)
    {
        fDirty = flag;
    }
}