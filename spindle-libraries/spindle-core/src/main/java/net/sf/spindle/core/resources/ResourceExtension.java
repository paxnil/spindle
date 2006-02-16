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
import org.apache.hivemind.Resource;

public interface ResourceExtension
{
    /**
     * this is here for the benefit of {@link IResourceRoot}
     */
    Resource getRelativeResource(String path);

    /**
     * @return true iff this resource points to a real file or folder.
     */
    boolean exists();

    /**
     * pass all the non java resources (non .class files) in the same package/folder as this
     * resource to the requestor.
     * <p>
     * Callers must indicate if this is a folder or tree search by specifing the depth.
     * <ul>
     * <li>(@link DEPTH#ZERO} - look in this folder only</li>
     * <li>(@link DEPTH#INFINITE} - look in this folder and all subfolders of this folder</li>
     * </ul>
     * 
     * @param requestor
     *            the requestor
     * @param depth
     *            one of (@link DEPTH#ZERO} or (@link DEPTH#INFINITE}
     */
    void lookup(IResourceAcceptor requestor, LookupDepth depth);

}