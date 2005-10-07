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

package core.source;

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