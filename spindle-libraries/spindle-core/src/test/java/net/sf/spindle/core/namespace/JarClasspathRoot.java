package net.sf.spindle.core.namespace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;

import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.IResourceAcceptor;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.resources.PathUtils;
import net.sf.spindle.core.resources.search.ISearch;
import net.sf.spindle.core.util.Assert;

import org.apache.hivemind.Resource;

public class JarClasspathRoot implements IResourceRoot
{

    private URL jarUrl;

    public JarClasspathRoot(String path) throws IOException, URISyntaxException
    {
        PathUtils jarPath = new PathUtils(System.getProperty("basedir")).append("testData").append(
                path);

        File jarFile = jarPath.toFile();

        Assert.isLegal(jarFile != null);
        Assert.isLegal(jarFile.exists());
        Assert.isLegal(jarFile.isFile());
        new JarFile(jarFile); // is it indeed a jar?

        this.jarUrl = new URL("jar:" + jarFile.toURI().toURL() + "!/");
    }

    /* package */URL getResourceURL(JarClasspathResource resource)
    {
        try
        {
            return new URL(jarUrl, resource.getPath());
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    public Resource getRelativeResource(String path)
    {
        return new JarClasspathResource(this, path);
    }

    public boolean exists()
    {
        return true;
    }

    public void lookup(IResourceAcceptor requestor)
    {
        // TODO implement when needed

    }

    public ISearch getSearch() throws TapestryCoreException
    {
        // TODO implement search when needed
        return null;
    }
}
