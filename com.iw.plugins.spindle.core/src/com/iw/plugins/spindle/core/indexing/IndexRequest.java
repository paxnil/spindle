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

package com.iw.plugins.spindle.core.indexing;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  Fired by scanners when an indexable artifact is found.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class IndexRequest
{
    /**
      * @param resource
      */
    private static String  IRWLtoPlatformUrlString(IResourceWorkspaceLocation resource)
    {
        Assert.isLegal(resource.isWorkspaceResource());

    }

    /** the request source **/
    private Object fSource;

    /** the request type **/
    private int fType;

    /** the  string  indexable by Lucene **/
    private String fValue;

    /** the location in the source where the indexable string occured **/
    private ISourceLocation fLocation;

    /**
     *  Construct a new IndexRequest
     * 
     *  Type must be one of:
     * <ul>
     *  <li>{@link com.iw.plugins.spindle.core.indexing.IndexConstants#FILE_REF}</li>
     *  <li>{@link com.iw.plugins.spindle.core.indexing.IndexConstants#TYPE_REF}</li>
     * </ul>
     * 
     * @param source the object making the request
     * @param type the type of thing we are indexing.
     * @param value the string to be put in the appropriate field of the Lucene document 
     * @param location the source location
     */
    public IndexRequest(Object source, int type, String value, ISourceLocation location)
    {
        fSource = source;
        fType = type;
        fValue = value;
        fLocation = location;
    }

    public IndexRequest(Object source, int type, IResourceWorkspaceLocation resource, ISourceLocation location)
    {
        this(source, IRWLtoPlatformUrlString(resource), location);
    }

}
