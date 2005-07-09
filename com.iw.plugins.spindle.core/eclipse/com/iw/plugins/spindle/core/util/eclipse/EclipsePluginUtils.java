package com.iw.plugins.spindle.core.util.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

/**
 * @author gwl
 */
public class EclipsePluginUtils
{

    public static IWorkbench getWorkbench()
    {
        return PlatformUI.getWorkbench();
    }

    public static Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null)
            return window.getShell();

        return null;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        IWorkbench workbench = getWorkbench();
        if (workbench != null)
            return workbench.getActiveWorkbenchWindow();

        return null;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public static void addNatureToProject(IProject project, String natureId, boolean forceOrder)
            throws CoreException
    {
        IProjectDescription description = project.getDescription();
        List natures = new ArrayList(Arrays.asList(description.getNatureIds()));
        if (!natures.contains(natureId))
        {
            if (forceOrder)
            {
                //changes so that the project overlay icon shows up!
                natures.add(0, natureId);
            }
            else
            {
                natures.add(natureId);
            }
            description.setNatureIds((String[]) natures.toArray(new String[natures.size()]));
            project.setDescription(description, null);
        }
    
        TapestryCore.getDefault().getCoreListeners().fireCoreListenerEvent();
    }

    public static boolean hasTapestryNature(IProject project)
    {
        try
        {
            return project.hasNature(TapestryCorePlugin.NATURE_ID);
        }
        catch (CoreException e)
        {
            //eat it
        }
        return false;
    }

    public static void removeNatureFromProject(IProject project, String natureId)
            throws CoreException
    {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
    
        int natureIndex = -1;
        for (int i = 0; i < prevNatures.length; i++)
        {
            if (prevNatures[i].equals(natureId))
            {
                natureIndex = i;
                i = prevNatures.length;
            }
        }
    
        // Remove nature only if it exists...
        if (natureIndex != -1)
        {
            String[] newNatures = new String[prevNatures.length - 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, natureIndex);
            System.arraycopy(
                    prevNatures,
                    natureIndex + 1,
                    newNatures,
                    natureIndex,
                    prevNatures.length - (natureIndex + 1));
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
        }
        TapestryCore.getDefault().getCoreListeners().fireCoreListenerEvent();
    }
}
