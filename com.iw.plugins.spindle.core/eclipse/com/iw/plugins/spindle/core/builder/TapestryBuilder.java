package com.iw.plugins.spindle.core.builder;

import java.util.Date;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;

import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;
import com.iw.plugins.spindle.xerces.parser.XercesDOMModelSource;

import core.builder.AbstractBuildInfrastructure;

/**
 * @author gwl
 */
public class TapestryBuilder extends IncrementalProjectBuilder
{
    private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi");

    private EclipseBuildInfrastructure fInfrastructure;

    public TapestryBuilder()
    {
        super();
    }

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
    {
        if (systemBundle.getState() == Bundle.STOPPING)
            throw new OperationCanceledException();

        IWorkbench workbench = TapestryCorePlugin.getDefault().getWorkbench();
        if (workbench.isClosing())
            throw new OperationCanceledException();

        IProject project = getProject();
        if (project == null || !project.isAccessible())
            return new IProject[0];
        long start = System.currentTimeMillis();
        if (AbstractBuildInfrastructure.DEBUG)
            System.out.println("\nStarting build of " + project.getName() + " @ "
                    + new Date(System.currentTimeMillis()));

        fInfrastructure = new EclipseBuildInfrastructure(project, monitor, getDelta(project),
                new XercesDOMModelSource());

        fInfrastructure.build(kind != FULL_BUILD, args);

        long stop = System.currentTimeMillis();
        if (AbstractBuildInfrastructure.DEBUG)
            System.out.println("Finished build of " + project.getName() + " @ " + new Date(stop));
        System.out.println("elapsed (ms) = " + (stop - start));
        IProject[] requiredProjects = getRequiredProjects(true);

        return requiredProjects;
    }

    private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
    {
        return fInfrastructure.getRequiredProjects(includeBinaryPrerequisites);

    }

}
