package core;

import core.util.Assert;

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

/**
 * TODO this has de-evolved into a util class. if and when we go with Hivemind, this stuff can all be
 * pushed into services. ie. a Logger service to replace the log calls, a CoreEvent service to
 * replace CoreListeners, a PreferenceService to obtain preference values
 * 
 * @author glongman@gmail.com
 */
public class TapestryCore implements IPreferenceConstants
{
    public static final String IDENTIFIER = "com.iw.plugins.spindle.core";

    ILogger logger;

    ICoreListeners coreListeners;

    IPreferenceSource preferenceSource;

    // The shared instance.
    private static TapestryCore instance;

    /**
     * The constructor.
     */
    public TapestryCore(ILogger logger, ICoreListeners coreListeners, IPreferenceSource source)
    {
        Assert.isTrue(instance == null, "Can't init TapestryCore more than once!");
        instance = this;
        this.logger = logger;
        this.coreListeners = coreListeners;
        this.preferenceSource = source;
    }
    
    void doDestroy() {
        this.logger = null;
        this.coreListeners = null;
        this.preferenceSource = null;
    }

    /** for testing only */
    public void setLogger(ILogger logger)
    {
        this.logger = logger;
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
    
    /**
     * only for use by unit tests.
     */
    public static void destroy() {
        if (instance != null) {
            instance.doDestroy();
            instance = null;
        }
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
        String pref = preferenceSource.getString(BUILDER_MARKER_MISSES);
        return convertCoreStatusToPriority(pref);
    }
    
    public int getNamespaceClashPriority() {
        String pref = preferenceSource.getString(NAMESPACE_CLASH_SEVERITY);
        return convertCoreStatusToPriority(pref);
    }
    
    public boolean isMissPriorityIgnore() {
        return getBuildMissPriority() == convertCoreStatusToPriority(CORE_STATUS_IGNORE);
    }

    public int getHandleAssetProblemPriority()
    {
        String pref = preferenceSource.getString(BUILDER_HANDLE_ASSETS);
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