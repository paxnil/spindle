/*
 * Created on Apr 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sf.spindle.core;

import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.types.IJavaTypeFinder;


/**
 * @author gwl
 */
public interface ITapestryProject extends IJavaTypeFinder
{
    public boolean isValidatingWebXML();

    public IResourceRoot getClasspathRoot() ;

    public IResourceRoot getWebContextLocation();
        
}