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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.spindle.core.util.Assert;

/**
 * An implementation of a classpath root for one source folder.
 */
/* package */class FolderRoot extends ChildRoot
{
    FolderRoot(ParentRoot parentRoot, File rootFolder) throws IOException, URISyntaxException
    {
        super(ChildRoot.SOURCE, parentRoot, rootFolder);

        Assert.isLegal(rootFolder != null);
        Assert.isLegal(rootFolder.exists());
        Assert.isLegal(rootFolder.isDirectory());
    }

    @Override
    protected URL intitializeUrl(File file) throws MalformedURLException
    {
        return file.toURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ChildClasspathRoot#getNonJavaResources(net.sf.spindle.core.resources.ICoreResource)
     */
    @Override
    ICoreResource[] getNonJavaResources(ResourceImpl resource)
    {
        final File root = rootFile;
        File[] children = rootFile.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if (!dir.equals(root))
                    return false;
                if (name.endsWith(".class"))
                    return false;
                return true;
            }
        });
        PathUtils rootpath = new PathUtils(root.getPath());
        ResourceImpl[] result = new ResourceImpl[] {};
        for (int i = 0; i < children.length; i++)
        {
            PathUtils childPath = new PathUtils(children[i].getPath());
            String resourcePath = childPath.removeFirstSegments(
                    rootpath.matchingFirstSegments(childPath)).makeAbsolute().toString();
            growAndAddToArray(result, new ResourceImpl(this, resourcePath));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ChildClasspathRoot#existsInThisRoot(java.lang.String)
     */
    @Override
    boolean existsInThisRoot(String path)
    {
        File check = new File(rootFile, path);
        return check.exists();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof FolderRoot))
            return false;
        FolderRoot other = (FolderRoot) obj;
        return this.rootFile.equals(other.rootFile);
    }

}
