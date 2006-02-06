package net.sf.spindle.core.resources;

import java.io.File;

import net.sf.spindle.core.TapestryCore;
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
 * An implementation of a {@link ParentRoot} for the classpath. You can add jarfiles and source
 * folders to this root.
 * <p>
 * The order they are added is == to the order searches and lookups will occur so you should add
 * things in the same order as they appear in the classpath!
 */
public class ClasspathRoot extends ParentRoot
{

    public ClasspathRoot()
    {
        super(ParentRoot.CLASSPATH);

    }

    /**
     * Add a jar to the classpath. Silently discards and invalid jarFile after logging any
     * exception.
     * 
     * @param jarFile
     */
    public void addJar(File jarFile)
    {
        try
        {
            JarRoot newRoot = new JarRoot(this, jarFile);
            if (ChildRoot.arrayContains(roots, newRoot))
                TapestryCore.log("Classpath root already contains: " + jarFile.toString());
            else
                roots = ChildRoot.growAndAddToArray(roots, newRoot);
        }
        catch (Exception e)
        {
            TapestryCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractRoot#isBinaryResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    @Override
    boolean isBinaryResource(ResourceImpl resource)
    {
        ChildRoot childRoot = findResourceRootFor(resource);
        if (childRoot != null)
            return childRoot.getType() == ChildRoot.BINARY;
        return false; // hmm, maybe an exception would be better, the resource does not exist!
    }

    @Override
    ISearch createSearch()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
