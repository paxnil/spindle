package net.sf.spindle.core.namespace;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceAcceptor;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;
import org.apache.hivemind.util.AbstractResource;

public class JarClasspathResource extends AbstractResource implements ICoreResource
{

    JarClasspathRoot root;

    /* package */JarClasspathResource(JarClasspathRoot root, String path)
    {
        super(path);
        this.root = root;
    }

    @Override
    protected Resource newResource(String path)
    {
        return new JarClasspathResource(root, path);
    }

    public boolean isClasspathResource()
    {
        return true;
    }

    public boolean isBinaryResource()
    {
        return true;
    }

    public InputStream getContents()
    {
        try
        {
            URL resourceURL = getResourceURL();
            if (resourceURL == null)
                return null;
            return resourceURL.openStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public boolean clashesWith(ICoreResource resource)
    {
        return false;
    }

    public boolean isFolder()
    {
        return getName() == null;
    }

    public URL getResourceURL()
    {
        return root.getResourceURL(this);
    }

    public Resource getLocalization(Locale arg0)
    {
        return null;
    }

    // not a good example, how many times are we going to open that stream?!
    // but it will suffice for the tests
    public boolean exists()
    {
        try
        {
            InputStream contents = getContents();
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public void lookup(IResourceAcceptor requestor)
    {
        // TODO implement later if required for tests

    }

    public ISearch getSearch() throws TapestryCoreException
    {
        // TODO implement later if required for tests
        return null;
    }

}
