/*
 * Created on Apr 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.iw.plugins.spindle.core.adapters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.ClasspathSearch;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * @author Administrator TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class SpindleProjectAdapterFactory implements IAdapterFactory
{
    public Class[] getAdapterList()
    {
        return new Class[]
        { IJavaElement.class, IResource.class, IStorage.class, IFileEditorInput.class };

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object obj, Class adapterType)
    {
        if (adapterType.isInstance(obj))
            return obj;

        if (adapterType == IProject.class)
            return adaptToProject(obj);

        if (adapterType == IJavaProject.class)
            return adaptToJavaProject(obj);

        if (adapterType == ITapestryProject.class)
        {
            ITapestryProject result = null;

            IProject project = (IProject) adaptToProject(obj);

            if (project != null)
            {
                result = TapestryProject.create(project);
                if (result == null)
                {
                    IProject[] potentials = Markers.getHomeProjects(project);
                    for (int i = 0; i < potentials.length; i++)
                    {
                        result = TapestryProject.create(potentials[i]);
                        if (result != null)
                            break;
                    }
                }
                return result;
            }
        }

        return null;
    }

    private Object adaptToProject(Object obj)
    {
        if (obj instanceof IJavaElement)
        {

            IJavaProject project = (IJavaProject) adaptToJavaProject(obj);
            return project == null ? null : project.getProject();

        }
        else if (obj instanceof IResource)
        {

            return ((IResource) obj).getProject();

        }       
        else if (obj instanceof IJarEntryResource)
        {
            return getProjectFor((IJarEntryResource) obj);
        }
        else if (obj instanceof IFileEditorInput)
        {

            IFile file = ((IFileEditorInput) obj).getFile();
            return file == null || !file.exists() ? null : file.getProject();
        }
        return null;

    }

    private Object adaptToJavaProject(Object obj)
    {
        IJavaProject jproject = null;
        if (obj instanceof IJavaElement)
        {
            jproject = (IJavaProject) ((IJavaElement) obj).getAncestor(IJavaElement.JAVA_PROJECT);
        }
        else
        {
            IProject project = (IProject) adaptToProject(obj);
            jproject = project == null ? null : JavaCore.create(project);
        }

        if (jproject == null || !jproject.exists())
            return null;

        return jproject;

    }    

    private IProject getProjectFor(IJarEntryResource jarFile)
    {
        ClasspathSearch lookup = null;

        try
        {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject[] projects = workspace.getRoot().getProjects();
            for (int i = 0; i < projects.length; i++)
            {
                if (!projects[i].isAccessible())
                    continue;

                if (lookup == null)

                    lookup = new ClasspathSearch();

                IJavaProject jproject = JavaCore.create(projects[i]);
                if (jproject == null || !jproject.exists())
                    continue;

                lookup.configure(jproject);
                if (lookup.projectContainsJarEntry(jarFile))
                    return projects[i];

            }
        }
        catch (CoreException jmex)
        {
            jmex.printStackTrace();
        }
        return null;
    }

}