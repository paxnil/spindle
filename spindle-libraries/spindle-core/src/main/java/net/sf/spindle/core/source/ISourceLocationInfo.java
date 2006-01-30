package net.sf.spindle.core.source;
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
import java.util.Set;

import org.apache.hivemind.Location;
import org.apache.hivemind.Resource;

/**
 * Records all the line and offset information for a chunk of markup.
 * 
 * @author glongman@gmail.com
 */
public interface ISourceLocationInfo extends Location
{

    public abstract boolean hasAttributes();

    public abstract boolean isEmptyTag();

    /** return a location for the element - includes all wrapped by it* */
    public abstract ISourceLocation getSourceLocation();

    /** return a location for all wrapped by the element* */
    public abstract ISourceLocation getContentSourceLocation();

    public abstract ISourceLocation getTagNameLocation();

    public abstract ISourceLocation getStartTagSourceLocation();

    public abstract ISourceLocation getEndTagSourceLocation();

    public abstract ISourceLocation getAttributeSourceLocation(String rawname);

    /** return a new set containing the names * */
    public Set getAttributeNames();

    public abstract void setResource(Resource location);

    /** useful in Eclipse documents */
    public int getOffset();

    public int getLength();
}