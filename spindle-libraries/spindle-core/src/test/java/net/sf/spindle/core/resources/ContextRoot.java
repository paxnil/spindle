package net.sf.spindle.core.resources;

import net.sf.spindle.core.resources.search.ISearch;

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
/**
 * An implementation of a {@link ParentRoot} for the classpath. You can only add folders to this root.
 * <p>
 * If you require projects to be in exploded war format then you would add only one folder to this root.
 * <p>
 * However you can add as many folders as you like. Lookups and Searches occur in the order you add the folders.
 * <p>
 * In Eclipse everyone wants the Project to be the root of the exploded war *and* the root of the classpath.
 * This can't be done with this impl, but I could see it being possible if the ContextRoot had a reference to the
 * ClasspathRoot. Then the ContextRoot could exclude paths into the ClasspathRoot's source folders.
 *
 */
public class ContextRoot extends ParentRoot
{

    public ContextRoot()
    {
        super(ParentRoot.CONTEXT);        
    }

    /* (non-Javadoc)
     * @see net.sf.spindle.core.resources.ParentRoot#createSearch()
     */
    @Override
    ISearch createSearch()
    {        
        return null; //TODO implement when needed.
    }

    /* (non-Javadoc)
     * @see net.sf.spindle.core.resources.AbstractRoot#isBinaryResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    @Override
    boolean isBinaryResource(ResourceImpl resource)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.spindle.core.resources.AbstractRoot#isClasspathResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    @Override
    boolean isClasspathResource(ResourceImpl resource)
    {
        return false;
    }
}
