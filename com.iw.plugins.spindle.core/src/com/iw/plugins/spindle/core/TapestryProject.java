package com.iw.plugins.spindle.core;
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

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.Files;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * The Tapestry project nature. Configures and Deconfigures the builder
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class TapestryProject implements IProjectNature
{

    // Persistence properties of projects
    public static final String PROPERTIES_FILENAME = ".tapestryplugin";
    public static final String KEY_TYPE = "project-type";
    public static final String KEY_CONTEXT = "context-root";
    public static final String KEY_LIBRARY = "library-spec";

    public static final int APPLICATION_PROJECT_TYPE = 0;
    public static final int LIBRARY_PROJECT_TYPE = 1;

    /**
     * The platform project this <code>TapestryProject</code> is based on
     */
    protected IProject fProject;
    protected IJavaProject fJavaProject;

    protected String fWebContext;
    protected IFolder fWebContextFolder;
    
    protected String fLibrarySpecPath;
    protected IFile fLibraryFile;

    protected int fProjectType;

    /** needed for project nature creation **/
    public TapestryProject()
    {
        super();
    }

    /**
     * Gets the project.
     * @return Returns a IProject
     */
    public IProject getProject()
    {
        return fProject;
    }

    /**
     * Sets the project.
     * @param project The project to set
     */
    public void setProject(IProject project)
    {
        this.fProject = project;
    }

    /*
     * @see IProjectNature#configure()
     */
    public void configure() throws CoreException
    {
        addToBuildSpec(TapestryCore.BUILDER_ID);
    }

    /*
     * @see IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException
    {
        removeFromBuildSpec(TapestryCore.BUILDER_ID);
        Markers.removeProblemsForProject(getProject());
        clearProperties();
    }

    /**
     * get rid of the .tapestryplugin file
     */
    private void clearProperties()
    {
        IFile properties = getPropertiesFile();
        if (properties != null && properties.exists())
            try
            {
                properties.delete(true, false, null);
            } catch (CoreException e)
            {
                TapestryCore.log(e);
            }
    }

    public boolean isOnOutputPath(IPath candidate)
    {
        try
        {
            IPath output = getJavaProject().getOutputLocation();
            return pathCheck(output, candidate);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        return false;
    }

    public boolean isOnSourcePath(IPath candidate)
    {
        try
        {
            IPackageFragmentRoot[] roots = getJavaProject().getPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++)
            {
                if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE)
                {
                    IPath rootpath = roots[i].getUnderlyingResource().getFullPath();
                    if (pathCheck(rootpath, candidate))
                        return true;
                }

            }
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        return false;
    }

    private boolean pathCheck(IPath existing, IPath candidate)
    {
        if (existing.equals(candidate))
            return true;

        if (candidate.segmentCount() < existing.segmentCount())
            return false;

        return existing.matchingFirstSegments(candidate) == existing.segmentCount();
    }

    public IJavaProject getJavaProject() throws CoreException
    {
        return (IJavaProject) getProject().getNature(JavaCore.NATURE_ID);
    }

    public void setJavaProject(IJavaProject javaProject)
    {
        this.fJavaProject = javaProject;
        this.setProject(javaProject.getProject());
    }

    static public void addTapestryNature(IJavaProject project)
    {
        try
        {
            TapestryCore.addNatureToProject(project.getProject(), TapestryCore.NATURE_ID);
        } catch (CoreException ex)
        {
            TapestryCore.log(ex.getMessage());
        }
    }

    static public void removeTapestryNature(IJavaProject project)
    {
        try
        {
            TapestryCore.removeNatureFromProject(project.getProject(), TapestryCore.NATURE_ID);

            File properties = project.getProject().getLocation().append(PROPERTIES_FILENAME).toFile();
            if (properties.exists())
                properties.delete();

            Markers.removeProblemsForProject(project.getProject());

        } catch (CoreException ex)
        {
            TapestryCore.log(ex.getMessage());
        }
    }

    /**
     * @return a TapestryProject if this javaProject has the tapestry nature or
     * null if Project has not tapestry nature
     */
    static public TapestryProject create(IJavaProject javaProject)
    {
        TapestryProject result = null;
        try
        {
            result = (TapestryProject) javaProject.getProject().getNature(TapestryCore.NATURE_ID);
        } catch (CoreException ex)
        {
            TapestryCore.log(ex.getMessage());
        }
        return result;
    }

    /**
     * @return a TapestryProject if this Project has the tapestry nature or
     * null if Project doen't have the tapestry nature
     */
    static public TapestryProject create(IProject project)
    {
        IJavaProject javaProject = JavaCore.create(project);
        if (javaProject != null)
        {
            return TapestryProject.create(javaProject);
        } else
        {
            return null;
        }
    }

    private IFile getPropertiesFile()
    {
        return this.getProject().getFile(new Path(PROPERTIES_FILENAME));
    }

    private String readProperty(String key)
    {
        String result = null;
        try
        {
            result = Files.readPropertyInXMLFile(getPropertiesFile(), key);
        } catch (IOException e)
        {
            TapestryCore.log(e);
        }

        if (result == null)
            result = "";

        return result;
    }

    private int readIntProperty(String key)
    {
        String result = readProperty(key);
        if (result == null || result.trim().length() == 0)
            return 0;

        if (result != null)
            return new Integer(result).intValue();

        return -1;
    }

    public void setProjectType(int projectType)
    {
        this.fProjectType = projectType;
    }

    public int getProjectType()
    {
        return this.readIntProperty(KEY_TYPE);
    }

    /**
     * Gets the web context path.
     * @return Returns a String
     */
    public String getWebContext()
    {
        return this.readProperty(KEY_CONTEXT);
    }

    /**
     * Sets the webpath.
     * @param webpath The webpath to set
     */
    public void setWebContext(String context)
    {
        this.fWebContext = context;
        fWebContextFolder = null;
    }

    public String getLibrarySpecPath()
    {
        String path = this.readProperty(KEY_LIBRARY);
        if ("NOT_SPECIFIED".equals(path))
            return "";

        return path;
    }

    public void setLibrarySpecPath(String librarySpecPath)
    {
        this.fLibrarySpecPath = librarySpecPath;
    }
    
    public IResourceWorkspaceLocation getLibraryLocation() throws CoreException {
        return (IResourceWorkspaceLocation)getClasspathRoot().getRelativeLocation(getLibrarySpecPath());  
    }
    
    public ClasspathRootLocation getClasspathRoot() throws CoreException{
       return new ClasspathRootLocation(getJavaProject());      
    }

    public void saveProperties()
    {
        boolean isApplicationProject = APPLICATION_PROJECT_TYPE == fProjectType;
        try
        {
            StringBuffer fileContent = new StringBuffer();
            fileContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fileContent.append("<tapestry-project-properties>\n");
            fileContent.append("    <project-type>" + fProjectType + "</project-type>\n");
            if (isApplicationProject)
            {
                fileContent.append(
                    "    <context-root>" + (fWebContext != null ? fWebContext : "") + "</context-root>\n");
            } else
            {
                fileContent.append(
                    "    <library-spec>"
                        + (fLibrarySpecPath != null ? fLibrarySpecPath : "NOT_SPECIFIED")
                        + "</library-spec>\n");
            }
            fileContent.append("</tapestry-project-properties>\n");
            Files.toTextFile(getPropertiesFile(), fileContent.toString());
        } catch (Exception ex)
        {
            TapestryCore.log(ex.getMessage());
        }
    }

    public IFolder getWebContextFolder()
    {
        if (fWebContextFolder == null)
            return initWebContextFolder();

        return fWebContextFolder;
    }

    public IResourceWorkspaceLocation getWebContextLocation()
    {
        IFolder folder = getWebContextFolder();
        if (folder == null || !folder.exists())
            return null;

        return new ContextRootLocation(folder);
    }

    private IFolder initWebContextFolder()
    {
        IFolder result = null;
        try
        {
            result = initFolder(this.getWebContext(), false);
        } catch (CoreException e)
        {
            this.fWebContext = "/";
        }
        fWebContextFolder = result;
        return result;
    }

    private void createFolder(IFolder folderHandle) throws CoreException
    {
        try
        {
            folderHandle.create(false, true, null);
        } catch (CoreException e)
        {
            // If the folder already existed locally, just refresh to get contents
            if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
            {
                folderHandle.refreshLocal(IResource.DEPTH_INFINITE, null);

            } else
            {
                throw e;
            }
        }
    }

    private IFolder initFolder(String path, boolean create) throws CoreException
    {
        StringTokenizer tokenizer = new StringTokenizer(path, "/\\:");
        IFolder folder = null;
        while (tokenizer.hasMoreTokens())
        {
            String each = tokenizer.nextToken();
            if (folder == null)
            {
                folder = fProject.getFolder(each);
            } else
            {
                folder = folder.getFolder(each);
            }
            if (create)
                this.createFolder(folder);
        }

        return folder;
    }

    protected void addToBuildSpec(String builderID) throws CoreException
    {
        IProjectDescription description = getProject().getDescription();
        ICommand javaCommand = getTapestryCommand(description);

        if (javaCommand == null)
        {
            ICommand command = description.newCommand();
            command.setBuilderName(builderID);
            setTapestryCommand(description, command);
        }
    }

    protected void removeFromBuildSpec(String builderID) throws CoreException
    {
        IProjectDescription description = getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(builderID))
            {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                getProject().setDescription(description, null);
                return;
            }
        }
    }

    private ICommand getTapestryCommand(IProjectDescription description) throws CoreException
    {
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(TapestryCore.BUILDER_ID))
                return commands[i];

        }
        return null;
    }

    private void setTapestryCommand(IProjectDescription description, ICommand newCommand) throws CoreException
    {
        ICommand[] oldCommands = description.getBuildSpec();
        ICommand oldTapestryCommand = getTapestryCommand(description);
        ICommand[] newCommands;

        if (oldTapestryCommand == null)
        {
            // Add a Tapestry build spec to the end of the command list
            newCommands = new ICommand[oldCommands.length + 1];
            System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
            newCommands[newCommands.length - 1] = newCommand;
        } else
        {
            for (int i = 0, max = oldCommands.length; i < max; i++)
            {
                if (oldCommands[i] == oldTapestryCommand)
                {
                    oldCommands[i] = newCommand;
                    break;
                }
            }
            newCommands = oldCommands;
        }

        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
    }

}