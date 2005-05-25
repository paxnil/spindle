/*
 * Created on Apr 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.iw.plugins.spindle.core;

import org.apache.hivemind.Resource;

/**
 * @author gwl
 */
public interface ITapestryProject extends IJavaTypeFinder
{
    public boolean isValidatingWebXML();

    public Resource getClasspathRoot() ;

    public Resource getWebContextLocation();
        
}