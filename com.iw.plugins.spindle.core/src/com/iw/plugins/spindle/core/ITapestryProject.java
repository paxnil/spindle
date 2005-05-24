/*
 * Created on Apr 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.iw.plugins.spindle.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

import com.iw.plugins.spindle.core.resources.eclipse.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.eclipse.ContextRootLocation;

/**
 * @author gwl
 */
public interface ITapestryProject extends IJavaTypeFinder
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#getProject()
     */
    public IProject getProject();

    public boolean isOnOutputPath(IPath candidate);

    public boolean isOnSourcePath(IPath candidate);

    public IJavaProject getJavaProject() throws CoreException;

    public boolean isValidatingWebXML();

    public ClasspathRootLocation getClasspathRoot() ;

    public IFolder getWebContextFolder();

    public ContextRootLocation getWebContextLocation();
        
}