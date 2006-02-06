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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.util.Assert;

/**
 * An implementation of a classpath root for one jar file.
 */
/* package */class JarRoot extends ChildRoot
{

    private Map<String, ResourceImpl[]> nonJavaResources;

    JarRoot(ParentRoot parentRoot, File jarFile) throws IOException,
            URISyntaxException
    {
        super(ChildRoot.BINARY, parentRoot, jarFile);

        Assert.isLegal(parentRoot.getType() == ParentRoot.CLASSPATH);
        Assert.isLegal(jarFile != null);
        Assert.isLegal(jarFile.exists());
        Assert.isLegal(jarFile.isFile());
        new JarFile(jarFile); // is it indeed a jar? - we don't hang on to it as creating is the
        // same as opening

    }

    @Override
    protected URL intitializeUrl(File file) throws MalformedURLException
    {
        return new URL("jar:" + file.toURI().toURL() + "!/");

    }

    private void populateNonJavaResourceInfo()
    {
        if (nonJavaResources != null)
            return;

        ZipFile jar = null;
        try
        {
            jar = new ZipFile(rootFile);
        }
        catch (Exception e)
        {
            TapestryCore.log(e);
        }
        nonJavaResources = new HashMap<String, ResourceImpl[]>();
        if (jar == null)
            return;

        // the default package
        initPackage("", -1);

        for (Enumeration e = jar.entries(); e.hasMoreElements();)
        {
            ZipEntry member = (ZipEntry) e.nextElement();
            String entryName = member.getName();

            if (member.isDirectory())
            {

                initPackage(entryName, entryName.length() - 1);
            }
            else
            {
                int lastSeparator = entryName.lastIndexOf('/');
                String fileName = entryName.substring(lastSeparator + 1);
                // skip classfiles
                if (fileName.endsWith(".class"))
                    continue;
                String packagepath = initPackage(entryName, lastSeparator);

                ResourceImpl newResource = new ResourceImpl(this, "/" + entryName);
                nonJavaResources.put(packagepath, growAndAddToArray(nonJavaResources
                        .get(packagepath), newResource));
            }
        }

        // we can ditch any entries that have no nonjavachildren
        // will make searching quicker
        for (Iterator iter = nonJavaResources.keySet().iterator(); iter.hasNext();)
        {
            String path = (String) iter.next();
            if (((ICoreResource[]) nonJavaResources.get(path)).length == 0)
                iter.remove();
        }
    }

    private String initPackage(String entryName, int lastSeparator)
    {
        String packagepath = null;
        if (lastSeparator >= 0)
            packagepath = entryName.substring(0, lastSeparator);
        else
            packagepath = "";

        if (nonJavaResources.get(packagepath) == null)
            nonJavaResources.put(packagepath, EMPTY);

        return packagepath;
    }

    private String getLookupPath(String candidatePath)
    {
        String path = candidatePath;
        PathUtils pathUtil = new PathUtils(path).makeRelative();
        if (!isFolder(candidatePath))
        {
            path = pathUtil.removeLastSegments(1).toString();
        }
        else
        {
            path = pathUtil.toString();
        }
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ChildClasspathRoot#getNonJavaResources(net.sf.spindle.core.resources.ICoreResource)
     */
    @Override
    ICoreResource[] getNonJavaResources(ResourceImpl resource)
    {
        populateNonJavaResourceInfo();
        ICoreResource[] result = nonJavaResources.get(getLookupPath(resource.getPath()));
        if (result == null)
            result = EMPTY;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.AbstractClasspathRoot#getResourceURL(net.sf.spindle.core.resources.ClasspathResource)
     */
    @Override
    URL getResourceURL(ResourceImpl resource)
    {
        if (!existsInThisRoot(resource.getPath()))
            return parentRoot.getResourceURL(resource);

        return buildResourceURL(resource);
    }

   
    /* (non-Javadoc)
     * @see net.sf.spindle.core.resources.AbstractRoot#isBinaryResource(net.sf.spindle.core.resources.ResourceImpl)
     */
    @Override
    boolean isBinaryResource(ResourceImpl resource)
    {
        if (existsInThisRoot(resource.getPath()))
            return true;
        return parentRoot.isBinaryResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ChildClasspathRoot#existsInThisRoot(java.lang.String)
     */
    @Override
    boolean existsInThisRoot(String path)
    {
        populateNonJavaResourceInfo();

        String lookupPath = getLookupPath(path);

        if (!nonJavaResources.containsKey(lookupPath))
            return false;

        ICoreResource[] resources = nonJavaResources.get(lookupPath);

        for (int i = 0; i < resources.length; i++)
        {

            String knownResourcePath = resources[i].getPath();
            if (knownResourcePath.equals(path))
                return true;
        }
        return false;
    }
}
