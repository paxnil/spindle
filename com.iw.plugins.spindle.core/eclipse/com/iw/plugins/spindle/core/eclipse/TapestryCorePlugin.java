package com.iw.plugins.spindle.core.eclipse;

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

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.iw.plugins.spindle.core.ICoreListeners;
import com.iw.plugins.spindle.core.ILogger;
import com.iw.plugins.spindle.core.IPreferenceConstants;
import com.iw.plugins.spindle.core.IPreferenceSource;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.metadata.ProjectExternalMetadataLocator;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author glongman@gmail.com
 */
public class TapestryCorePlugin extends AbstractUIPlugin implements IPreferenceConstants

{
    public static final String PLUGIN_ID = TapestryCore.IDENTIFIER;

    public static final String NATURE_ID = PLUGIN_ID + ".tapestrynature";

    public static final String BUILDER_ID = PLUGIN_ID + ".tapestrybuilder";

    public static final String CORE_CONTAINER = PLUGIN_ID + ".TAPESTRY_FRAMEWORK";

    // The shared instance.
    private static TapestryCorePlugin plugin;

    private ProjectExternalMetadataLocator externalMetadataLocator;

    class PropertySource implements IPreferenceSource
    {
        public boolean getBoolean(String name)
        {
            return getPreferenceStore().getBoolean(name);
        }

        public double getDouble(String name)
        {
            return getPreferenceStore().getDouble(name);
        }

        public float getFloat(String name)
        {
            // TODO Auto-generated method stub
            return getPreferenceStore().getFloat(name);
        }

        public int getInt(String name)
        {

            return getPreferenceStore().getInt(name);
        }

        public long getLong(String name)
        {
            return getPreferenceStore().getLong(name);
        }

        public String getString(String name)
        {
            return getPreferenceStore().getString(name);
        }
    }

    /**
     * The constructor.
     */
    public TapestryCorePlugin()
    {
        super();
        plugin = this;
        ILogger logger = new EclipsePluginLogger(Platform.getLog((Bundle) this), PLUGIN_ID);
        ICoreListeners coreListeners = new EclipseCoreListeners();
        new TapestryCore(logger, coreListeners, new PropertySource());

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
    public static TapestryCorePlugin getDefault()
    {
        return plugin;
    }

    public ProjectExternalMetadataLocator getExternalMetadataLocator()
    {
        if (externalMetadataLocator == null)
            externalMetadataLocator = new ProjectExternalMetadataLocator();

        return externalMetadataLocator;
    }

    public String getPropertyValue(String key)
    {
        return getPreferenceStore().getString(key);
    }
}