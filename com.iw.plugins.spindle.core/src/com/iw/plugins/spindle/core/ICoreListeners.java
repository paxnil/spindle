package com.iw.plugins.spindle.core;


/**
 * @author gwl
 *
 */
public interface ICoreListeners
{
    void addCoreListener(ICoreListener listener);
    void removeCoreListener(ICoreListener listener);
    void buildOccurred();
    void fireCoreListenerEvent();
}

