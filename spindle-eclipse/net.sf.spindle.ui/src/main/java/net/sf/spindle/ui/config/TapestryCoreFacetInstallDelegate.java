package net.sf.spindle.ui.config;

import net.sf.spindle.core.eclipse.TapestryCorePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

public class TapestryCoreFacetInstallDelegate implements IDelegate
{
    public void execute(IProject project, IProjectFacetVersion version, Object config,
            IProgressMonitor monitor)

    throws CoreException
    {
        TapestryCoreFacetInstallConfig cfg = (TapestryCoreFacetInstallConfig) config;
        // TODO generate the home page and hook up the Tapestry project
        monitor.beginTask("", 3);
        try
        {
            ConfigFacetUtils.installJars(
                    project,
                    TapestryCorePlugin.getDefault().getBundle(),
                    ConfigFacetUtils.CORE_FILES,
                    new SubProgressMonitor(monitor, 1));

            ConfigFacetUtils.registerTapestryServlet(project, cfg.getApplicationName(), monitor);

            monitor.worked(1);

            // todo create the application spec

            monitor.worked(1);
        }
        finally
        {
            monitor.done();
        }
    }
}
