package net.sf.spindle.core.builder;

import java.util.Date;
import java.util.Map;

import net.sf.spindle.core.build.AbstractBuildInfrastructure;
import net.sf.spindle.core.eclipse.TapestryCorePlugin;
import net.sf.spindle.xerces.parser.XercesDOMModelSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;


/**
 * @author gwl
 */
public class TapestryBuilder extends IncrementalProjectBuilder
{
    private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi");

    private EclipseBuildInfrastructure infrastructure;

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

        infrastructure = new EclipseBuildInfrastructure(project, monitor, getDelta(project),
                new XercesDOMModelSource());

        infrastructure.build(kind != FULL_BUILD, args);

        long stop = System.currentTimeMillis();
        if (AbstractBuildInfrastructure.DEBUG)
            System.out.println("Finished build of " + project.getName() + " @ " + new Date(stop));
        System.out.println("elapsed (ms) = " + (stop - start));
        IProject[] requiredProjects = getRequiredProjects(true);

        return requiredProjects;
    }

    private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites)
    {
        return infrastructure.getRequiredProjects(includeBinaryPrerequisites);

    }

}
