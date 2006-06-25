package net.sf.spindle.ui.config;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

public class TapestryCoreFacetUninstallDelegate implements IDelegate
{
    public void execute(IProject project, IProjectFacetVersion version, Object config,
            IProgressMonitor monitor)

    throws CoreException
    {
        TapestryCoreFacetInstallConfig cfg = (TapestryCoreFacetInstallConfig) config;
        monitor.beginTask("", 2);
        try
        {
            ConfigFacetUtils.uninstallJars(
                    project,
                    ConfigFacetUtils.CORE_FILES,
                    new SubProgressMonitor(monitor, 1));

            monitor.worked(1);

            // TODO unhook the Tapestry project nature

        }
        finally
        {
            monitor.done();
        }
    }
}
