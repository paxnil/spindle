package net.sf.spindle.core.resources;

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
import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;

public interface ResourceExtension
{

    /**
     * this is here for the benefit of {@link IResourceRoot}
     */
    Resource getRelativeResource(String path);

    /**
     * @return true iff this resource points to a real file.
     */
    boolean exists();

    /**
     * pass all the non java resources (non .class files) in the same package/folder as this
     * resource to the requestor.
     * 
     * @param requestor
     */
    void lookup(IResourceAcceptor requestor);

    /**
     * @return an {@link ISearch} a modified visitor that will search over all the resources in this
     *         resources root.
     * @throws TapestryCoreException
     *             if the search can't be configured properly
     */
    ISearch getSearch() throws TapestryCoreException;

}