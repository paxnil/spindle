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
import java.io.InputStream;
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
import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;
import net.sf.spindle.core.util.Assert;

import org.apache.hivemind.Resource;

/**
 * An implementation of a classpath root for one jar only. Some note to self: To be a true
 * implementation in an IDE environment it would, of course, have to handle more than one jar file
 * and also source folders in a project.
 * <p>
 * I can see this root actually being useful though. If an IDE implementor were to create an
 * {@link net.sf.spindle.core.resources.IResourceRoot} that contained for the ClasspathRoot
 * containing one of these for each jar on the classpath..
 * <p>
 * But, this approach would break if the project classpath changed and the jar removed from the
 * classpath!
 * <p>
 * A way to work around this is to have the IDE impementation check for classpath changes and remove
 * any sub-roots that no longer exist on the classpath.
 * <p>
 * The mechanism for doing this is not Spindle core API and the impementation would be left up to
 * clients.
 * <p>
 * TODO Note to self: it's starting to look like in the eclipse case, a implementation of this kind
 * of root that does not use JDT api might be a LOT faster! The trick is to handle the
 * IEclipseResource#getStorage() function without the JDT api.
 */
public class JarClasspathRoot implements IResourceRoot
{

    private ICoreResource[] EMPTY = new ICoreResource[] {};

    private URL jarUrl;

    private File jarFile;

    private Map<String, ICoreResource[]> nonJavaResources;

    public JarClasspathRoot(String path) throws IOException, URISyntaxException
    {
        PathUtils jarPath = new PathUtils(System.getProperty("basedir")).append("testData").append(
                path);

        jarFile = jarPath.toFile();

        Assert.isLegal(jarFile != null);
        Assert.isLegal(jarFile.exists());
        Assert.isLegal(jarFile.isFile());
        new JarFile(jarFile); // is it indeed a jar? - we don't hang on to it as creating is the
        // same as opening

        this.jarUrl = new URL("jar:" + jarFile.toURI().toURL() + "!/");
    }

    /* package */Resource newResource(String path)
    {
        return new JarClasspathResource(this, path);
    }

    /* package */URL getResourceURL(JarClasspathResource resource)
    {
        if (!exists(resource))
            return null;

        try
        {
            return new URL(jarUrl, resource.getPath());
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    private void populateNonJavaResourceInfo()
    {
        if (nonJavaResources != null)
            return;

        ZipFile jar = null;
        try
        {
            jar = new ZipFile(jarFile);
        }
        catch (Exception e)
        {
            TapestryCore.log(e);
        }
        nonJavaResources = new HashMap<String, ICoreResource[]>();
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

                JarClasspathResource newResource = new JarClasspathResource(this, "/" + entryName);
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

    private ICoreResource[] growAndAddToArray(ICoreResource[] array, ICoreResource addition)
    {
        ICoreResource[] old = array;
        array = new ICoreResource[old.length + 1];
        System.arraycopy(old, 0, array, 0, old.length);
        array[old.length] = addition;
        return array;
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

    private ICoreResource[] getNonJavaResources(JarClasspathResource resource)
    {
        populateNonJavaResourceInfo();
        ICoreResource[] result = nonJavaResources.get(getLookupPath(resource));
        if (result == null)
            result = EMPTY;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getRelativeResource(java.lang.String)
     */
    public Resource getRelativeResource(String path)
    {
        return new JarClasspathResource(this, new PathUtils(path).makeAbsolute().toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#exists()
     */
    public boolean exists()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#lookup(net.sf.spindle.core.resources.IResourceAcceptor)
     */
    public void lookup(IResourceAcceptor requestor)
    {
        // TODO implement when needed - the eclipse impl doesn't need or implement this
        // as it would be a lookup on the default package - which is discouraged in Eclipse.
        populateNonJavaResourceInfo();
        ICoreResource[] defaultPackageResources = nonJavaResources.get("");
        for (int i = 0; i < defaultPackageResources.length; i++)
        {
            if (!requestor.accept(defaultPackageResources[i]))
                break;
        }
    }

    /* package */void lookup(JarClasspathResource resource, IResourceAcceptor requestor)
    {
        populateNonJavaResourceInfo();
        ICoreResource[] nonJavaResources = getNonJavaResources(resource);
        for (int i = 0; i < nonJavaResources.length; i++)
        {
            if (!requestor.accept(nonJavaResources[i]))
                break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.resources.ResourceExtension#getSearch()
     */
    public ISearch getSearch() throws TapestryCoreException
    {
        // TODO implement search when needed
        return null;
    }

    /* package */boolean isClasspathResource(JarClasspathResource resource)
    {
        return true;
    }

    /* package */boolean isBinaryResource(JarClasspathResource resource)
    {
        return true;
    }

    /* package */InputStream getContents(JarClasspathResource resource)
    {
        try
        {
            URL resourceURL = getResourceURL(resource);
            if (resourceURL == null)
                return null;
            return resourceURL.openStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /* package */boolean clashCkeck(JarClasspathResource resource, ICoreResource resource2)
    {
        return false;

    }

    /* package */boolean isFolder(JarClasspathResource resource)
    {
        return TapestryCore.isNull(resource.getName());

    }

    private String getLookupPath(JarClasspathResource resource)
    {
        String candidatePath = resource.getPath();
        String path = candidatePath;
        PathUtils pathUtil = new PathUtils(path).makeRelative();
        if (!isFolder(resource))
        {
            path = pathUtil.removeLastSegments(1).toString();
        }
        else
        {
            path = pathUtil.toString();
        }
        return path;
    }

    /* package */boolean exists(JarClasspathResource resource)
    {
        populateNonJavaResourceInfo();

        String lookupPath = getLookupPath(resource);

        if (!nonJavaResources.containsKey(lookupPath))
            return false;

        ICoreResource[] resources = nonJavaResources.get(lookupPath);

        String candidatePath = resource.getPath();
        for (int i = 0; i < resources.length; i++)
        {

            String knownResourcePath = resources[i].getPath();
            if (knownResourcePath.equals(candidatePath))
                return true;
        }
        return false;
    }

    /* package */Resource getLocalization(JarClasspathResource resource, Object localer)
    {
        // this could easily be implemented. But I have not encountered a single case where
        // it is needed in Spindle. YAGNI
        return null;
    }
}
