/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.ContextResourceWorkspaceLocation;

public class BuilderContextVisitor implements IResourceVisitor
{

    static private List knownExtensions = Arrays.asList(TapestryBuilder.KnownExtensions);

    ArrayList collector;
    FullBuild build;
    private IFolder contextLocation;
    private IFolder outputLocation;
    private TapestryProject tapestryProject;
    private boolean contextSeen;

    public BuilderContextVisitor(FullBuild build, ArrayList collector)
    {
        this.collector = collector;
        this.build = build;
        this.tapestryProject = build.tapestryBuilder.tapestryProject;
        this.contextLocation = build.tapestryBuilder.contextRoot;
        try
        {
            IPath out = build.javaProject.getOutputLocation();
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            outputLocation = root.getFolder(out);
        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        contextSeen = false;
    }

    private boolean isOnContextPath(IResource resource)
    {
        IResource temp = resource;
        while (temp != tapestryProject.getProject() && !temp.equals(contextLocation))
        {
            if (temp.equals(outputLocation)) {
                return false;
            }
            temp = temp.getParent();
        }
        return temp.equals(contextLocation);
    }

    private boolean isOnClasspath(IFolder folder)
    {
        return JavaCore.create(folder) != null;
    }

    public boolean visit(IResource resource) throws CoreException
    {
        if (resource instanceof IProject)
        {
            return true;
        }
        if (resource instanceof IFolder)
        {
            if (resource.equals(contextLocation)) {
                return true;
            }
            if (isOnContextPath(resource)) {
                return true;
            }
            if (isOnClasspath((IFolder)resource)) {
                return false;
            }
            if (resource.equals(outputLocation)) {
                return false;
            }
            IFolder folder = (IFolder)resource;
            if (folder.getFullPath().segmentCount() >= contextLocation.getFullPath().segmentCount()){
                return false;
            }
            
  
        } else if (resource instanceof IFile)
        {
            if (!isOnContextPath(resource)) {
                return false;
            }
            String extension = resource.getFileExtension();
            if (knownExtensions.contains(extension))
            {
                IResourceLocation location = new ContextResourceWorkspaceLocation(contextLocation, resource);
                collector.add(location);
                debug(location, true);
            }
        }
        return true;
    }

    protected void debug(IResourceLocation location, boolean included)
    {
        if (TapestryBuilder.DEBUG)
        {
            System.out.println(location);
        }
    }

}
