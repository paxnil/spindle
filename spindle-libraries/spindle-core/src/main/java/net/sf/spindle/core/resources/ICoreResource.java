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
import java.io.InputStream;

import org.apache.hivemind.Resource;

/**
 * Extends <code>org.apache.tapestry.IResourceLocation<code> to record additional
 * bits of information describing a Tapestry artifact found in the workspace.
 * 
 * @author glongman@gmail.com
 * 
 * @see org.apache.tapestry.IResourceLocation
 */

public interface ICoreResource extends Resource, ResourceExtension
{

    /**
     * @return true iff this resource is located under a classpath root.
     */
    public boolean isClasspathResource();

    /**
     * @return true iff the resource represents an entry in a jar file
     */
    public boolean isBinaryResource();

    /**
     * Returns an open input stream on the contents of this descriptor. The caller is responsible
     * for closing the stream when finished.
     */
    public InputStream getContents();

    /**
     * Does the path of the passed resource clash (overlap) with my path
     * <p>
     * The name of either resource is not considered in the check.
     * <p>
     * a classpath resource never clashes with a context resource and vice versa.
     * <p>
     * Recommend that implementors defer this in thier implentations with a call to
     * {@link net.sf.spindle.core.build.ClashDetector#clashesWith(ICoreResource, ICoreResource).
     * 
     * @param resource
     *            ther resource to check
     * @return true if the path's clash
     */
    public boolean clashesWith(ICoreResource resource);

    /**
     * @return true if this resource has only a path part and no name.
     */
    public boolean isFolder();

    /**
     * Return the underlying IDE platform specific object that this resource refers to.
     * <p>
     * ie. in Eclipse it might be an IFile or a JarEntryFile but in another implementation if might
     * be a {@link java.io.File} or a {@link java.util.jar.JarEntry}
     * 
     * @return the IDE platform specific object
     */
    public Object getUnderlier();

}