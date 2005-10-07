/*
 * Created on Apr 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package core;

import core.resources.IResourceRoot;

/**
 * @author gwl
 */
public interface ITapestryProject extends IJavaTypeFinder
{
    public boolean isValidatingWebXML();

    public IResourceRoot getClasspathRoot() ;

    public IResourceRoot getWebContextLocation();
        
}