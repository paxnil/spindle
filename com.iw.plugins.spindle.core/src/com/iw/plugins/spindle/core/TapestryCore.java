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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com 
 *
 * ***** END LICENSE BLOCK ***** */

import java.util.List;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.iw.plugins.spindle.core.eclipse.EclipseCoreListeners;
import com.iw.plugins.spindle.core.eclipse.EclipsePluginLogger;
import com.iw.plugins.spindle.core.metadata.ProjectExternalMetadataLocator;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author glongman@gmail.com
 */
public class TapestryCore extends AbstractUIPlugin implements PreferenceConstants
{
    public static final String PLUGIN_ID = "com.iw.plugins.spindle.core";

    public static final String NATURE_ID = PLUGIN_ID + ".tapestrynature";

    public static final String BUILDER_ID = PLUGIN_ID + ".tapestrybuilder";

    public static final String CORE_CONTAINER = PLUGIN_ID + ".TAPESTRY_FRAMEWORK";

    public static final String SERVLET_2_2_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";

    public static final String SERVLET_2_3_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";

    public static final String SERVLET_2_4_SCHEMA = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";

    public static boolean IS_RUNNING_IN_ECLIPSE;

    

    //The shared instance.
    private static TapestryCore plugin;

    //Resource bundle.

    private static List CoreListeners;

    private ProjectExternalMetadataLocator externalMetadataLocator;

    private ILogger logger;

    private ICoreListeners coreListeners;

    /**
     * The constructor.
     */
    public TapestryCore()
    {
        super();
        plugin = this;
        logger = new EclipsePluginLogger(InternalPlatform.getDefault().getLog((Bundle) this),
                PLUGIN_ID);
        coreListeners = new EclipseCoreListeners();
        IS_RUNNING_IN_ECLIPSE = true;
    }

    ILogger getLogger()
    {
        return logger;
    }

    public ICoreListeners getCoreListeners()
    {
        return coreListeners;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        if (externalMetadataLocator != null)
        {
            externalMetadataLocator.destroy();
            externalMetadataLocator = null;
        }
    }

    /**
     * Returns the shared instance.
     */
    public static TapestryCore getDefault()
    {
        return plugin;
    }

    static public void log(String msg)
    {
        getDefault().getLogger().log(msg);
    }

    static public void log(Throwable ex)
    {
        getDefault().getLogger().log(null, ex);
    }

    static public void log(String message, Throwable ex)
    {
        getDefault().getLogger().log(message, ex);
    }

    public static void addCoreListener(ICoreListener listener)
    {
        getDefault().getCoreListeners().removeCoreListener(listener);
    }

    public static void removeCoreListener(ICoreListener listener)
    {
        getDefault().getCoreListeners().removeCoreListener(listener);
    }

    public static void buildOccurred()
    {
        getDefault().getCoreListeners().buildOccurred();
    }

    public static boolean isNull(String value)
    {
        if (value == null)
            return true;

        if (value.length() == 0)
            return true;

        return value.trim().length() == 0;
    }   

    /** @deprecated */
    public static boolean isCachingDTDGrammars()
    {
        // short circuit for testing outside of an
        // Eclipse runtime

        if (getDefault() == null)
            return true;

        return getDefault().getPreferenceStore().getBoolean(CACHE_GRAMMAR_PREFERENCE);
    }

    public static void throwErrorException(String message) throws TapestryException
    {
        throw createErrorException(message);
    }

    public static TapestryException createErrorException(String message)
    {
        SpindleStatus status = new SpindleStatus();
        status.setError(message);
        TapestryException exception = new TapestryException(status);
        return exception;
    }

    public int getBuildMissPriority()
    {
        String pref = getPreferenceStore().getString(BUILDER_MARKER_MISSES);
        return convertCoreStatusToPriority(pref);
    }

    public int getHandleAssetProblemPriority()
    {
        String pref = getPreferenceStore().getString(BUILDER_HANDLE_ASSETS);
        return convertCoreStatusToPriority(pref);
    }

    private int convertCoreStatusToPriority(String pref)
    {
        if (pref.equals(CORE_STATUS_IGNORE))
            return -1;

        for (int i = 0; i < CORE_STATUS_ARRAY.length; i++)
        {
            if (pref.equals(CORE_STATUS_ARRAY[i]))
                return i;
        }
        return 0;
    }

   

    public ProjectExternalMetadataLocator getExternalMetadataLocator()
    {
        if (externalMetadataLocator == null)
            externalMetadataLocator = new ProjectExternalMetadataLocator();

        return externalMetadataLocator;
    }

}