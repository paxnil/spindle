package net.sf.spindle.core.resources;

import net.sf.spindle.core.TapestryCoreException;
import net.sf.spindle.core.resources.search.ISearch;

import org.apache.hivemind.Resource;



public interface ResourceExtension
{

    Resource getRelativeResource(String path);
    
    boolean exists();

    void lookup(IResourceAcceptor requestor);

    ISearch getSearch() throws TapestryCoreException;
    
}