/******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package net.sf.spindle.ui;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public final class UIPlugin

    extends AbstractUIPlugin

{
    public static final String PLUGIN_ID = "net.sf.spindle.ui";

    private static UIPlugin plugin;

    public UIPlugin()
    {
        plugin = this;
    }

    public static UIPlugin getInstance()
    {
        return plugin;
    }

    public static void log( final Exception e )
    {
        final String msg = e.getMessage() + "";
        log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e ) );
    }

    public static void log( final IStatus status )
    {
        getInstance().getLog().log( status );
    }

    public static void log( final String msg )
    {
        log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, null ) );
    }

    public static IStatus createErrorStatus( final String msg )
    {
        return createErrorStatus( msg, null );
    }

    public static IStatus createErrorStatus( final String msg,
                                             final Exception e )
    {
        return new Status( IStatus.ERROR, PLUGIN_ID, 0, msg, e );
    }

}
