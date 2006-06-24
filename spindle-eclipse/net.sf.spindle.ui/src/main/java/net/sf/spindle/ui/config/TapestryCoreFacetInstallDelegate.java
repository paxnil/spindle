package net.sf.spindle.ui.config;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

public final class TapestryCoreFacetInstallDelegate implements IDelegate
{
    public void execute( final IProject pj,
                         final IProjectFacetVersion fv,
                         final Object config,
                         final IProgressMonitor monitor )

        throws CoreException

    {
        monitor.beginTask( "", 2 );

        try
        {
        final TapestryCoreFacetInstallConfig cfg
            = (TapestryCoreFacetInstallConfig) config;
        
            final IFolder webInfLib = Utils.getWebInfLibDir( pj );

            Utils.copyFromPlugin( new Path( "libs/formgen-core.jar" ),
                                  webInfLib.getFile( "formgen-core.jar" ) );

            monitor.worked( 1 );

            Utils.registerFormGenServlet( pj, cfg.getUrlPattern() );

            monitor.worked( 1 );
        }
        finally
        {
            monitor.done();
        }
    }
}
