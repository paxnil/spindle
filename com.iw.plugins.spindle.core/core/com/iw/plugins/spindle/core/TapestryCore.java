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

import com.iw.plugins.spindle.core.util.Assert;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author glongman@gmail.com
 */
public class TapestryCore implements IPreferenceConstants
{
    public static final String IDENTIFIER = "com.iw.plugins.spindle.core";

    public static final String SERVLET_2_2_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";

    public static final String SERVLET_2_3_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";

    public static final String SERVLET_2_4_SCHEMA = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";

    ILogger logger;

    ICoreListeners coreListeners;

    IPreferenceSource source;

    //The shared instance.
    private static TapestryCore instance;

    /**
     * The constructor.
     */
    public TapestryCore(ILogger logger, ICoreListeners coreListeners, IPreferenceSource source)
    {
        Assert.isTrue(instance == null, "Only one instance of TapestryCore is allowed");
        instance = this;
        this.logger = logger;
        this.coreListeners = coreListeners;
        this.source = source;
    }

    ILogger getLogger()
    {
        return logger;
    }

    public ICoreListeners getCoreListeners()
    {
        return coreListeners;
    }

    /**
     * Returns the shared instance.
     */
    public static TapestryCore getDefault()
    {
        return instance;
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

    public int getBuildMissPriority()
    {
        String pref = source.getString(BUILDER_MARKER_MISSES);
        return convertCoreStatusToPriority(pref);
    }

    public int getHandleAssetProblemPriority()
    {
        String pref = source.getString(BUILDER_HANDLE_ASSETS);
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

}