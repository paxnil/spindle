package com.iw.plugins.spindle.core.resources;

import java.util.Properties;

import org.apache.hivemind.Resource;

import com.iw.plugins.spindle.core.TapestryCoreException;
import com.iw.plugins.spindle.core.resources.search.ISearch;

public interface ResourceExtension
{

    Resource getRelativeResource(String path);
    
    boolean exists();

    void lookup(IResourceAcceptor requestor);

    ISearch getSearch() throws TapestryCoreException;
    
}