package net.sf.spindle.core.parser.template;
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
import org.apache.hivemind.Location;
import org.apache.tapestry.parse.OpenToken;

/**
 * Token representing the open tag for a component. Extends the superclass by recording source
 * location info provided by the parser.
 * 
 * @see org.apache.tapestry.parse.OpenToken
 * @author glongman@gmail.com
 */

public class CoreOpenToken extends OpenToken
{

    private TagEventInfo fEventInfo;

    /**
     * Creates a new token with the given tag, id and type, and source location info from the parser
     */

    public CoreOpenToken(String tag, String id, String componentType, Location location,
            TagEventInfo eventInfo)
    {
        super(tag, id, componentType, location);

        fEventInfo = eventInfo;
    }

    public TagEventInfo getEventInfo()
    {
        return fEventInfo;
    }

}