package net.sf.spindle.core;

/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import net.sf.spindle.core.util.Assert;

/**
 * TODO this has de-evolved into a util class. if and when we go with Hivemind, this stuff can all
 * be pushed into services. ie. a Logger service to replace the log calls, a CoreEvent service to
 * replace CoreListeners, a PreferenceService to obtain preference values
 * 
 * @author glongman@gmail.com
 */
public class TapestryCore implements IPreferenceConstants
{

    /**
     * Prefix for all string identifiers used in the core. for example useage see (@link
     * net.sf.spindle.core.source.IProblem}
     */
    public static final String IDENTIFIER = "net.sf.spindle.core";

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
        Assert.isNotNull(logger, "logger must not be null");
        Assert.isNotNull(coreListeners, "coreListeners must not be null");
        Assert.isNotNull(preferenceSource, "preferenceSource must not be null");
        this.logger = logger;
        this.coreListeners = coreListeners;
        this.preferenceSource = source;
    }

    void doDestroy()
    {
        this.logger = null;
        this.coreListeners = null;
        this.preferenceSource = null;
        instance = null;
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
     * Cleanup. Usually only needed on shutdown of the IDE.
     */
    public static void destroy()
    {
        if (instance != null)
        {
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

    /**
     * Register an {@link ICoreListener} to recieve build events.
     * 
     * @param listener
     */
    public static void addCoreListener(ICoreListener listener)
    {
        getDefault().getCoreListeners().removeCoreListener(listener);
    }

    /**
     * Unregister an {@link ICoreListener}. Said listener will no longer recieve build events.
     * 
     * @param listener
     */
    public static void removeCoreListener(ICoreListener listener)
    {
        getDefault().getCoreListeners().removeCoreListener(listener);
    }

    /**
     * Notify all registered {@link ICoreListener}'s that a build occured.
     */
    public static void buildOccurred()
    {
        getDefault().getCoreListeners().buildOccurred();
    }

    /**
     * Utility Method
     * 
     * @param value
     * @return true iff the parameter is not null or an empty string (trimmed or not).
     */
    public static boolean isNull(String value)
    {
        if (value == null)
            return true;

        if (value.length() == 0)
            return true;

        return value.trim().length() == 0;
    }

    /**
     * Retrieve the priority the core should lend to build 'misses'.
     * <p>
     * If the preference store does not return a valid result, use
     * {@link IPreferenceConstants#CORE_STATUS_ERROR}
     * 
     * @see IPreferenceConstants
     * @return priority integer
     */
    public int getBuildMissPriority()
    {
        String pref = preferenceSource.getString(BUILDER_MARKER_MISSES);
        if (pref == null)
            pref = CORE_STATUS_ERROR;
        return convertCoreStatusToPriority(pref);
    }

    /**
     * Retrieve the priority the core should lend to clashing problems. TODO decide if clash
     * detection is in or out.
     * <p>
     * If the preference store does not return a valid result, use
     * {@link IPreferenceConstants#CORE_STATUS_ERROR}
     * 
     * @see IPreferenceConstants
     * @return priority integer
     */
    public int getNamespaceClashPriority()
    {
        String pref = preferenceSource.getString(NAMESPACE_CLASH_SEVERITY);
        if (pref == null)
            pref = CORE_STATUS_ERROR;
        return convertCoreStatusToPriority(pref);
    }

    public boolean isMissPriorityIgnore()
    {
        return getBuildMissPriority() == convertCoreStatusToPriority(CORE_STATUS_IGNORE);
    }

    /**
     * Retrieve the priority the core should lend to asset problems.
     * <p>
     * If the preference store does not return a valid result, use
     * {@link IPreferenceConstants#CORE_STATUS_ERROR}
     * 
     * @see IPreferenceConstants
     * @return priority integer
     */
    public int getHandleAssetProblemPriority()
    {
        String pref = preferenceSource.getString(BUILDER_HANDLE_ASSETS);
        if (pref == null)
            pref = CORE_STATUS_ERROR;
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