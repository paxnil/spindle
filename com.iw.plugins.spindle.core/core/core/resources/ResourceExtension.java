package core.resources;

import org.apache.hivemind.Resource;

import core.TapestryCoreException;
import core.resources.search.ISearch;

public interface ResourceExtension
{

    Resource getRelativeResource(String path);
    
    boolean exists();

    void lookup(IResourceAcceptor requestor);

    ISearch getSearch() throws TapestryCoreException;
    
}