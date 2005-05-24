package com.iw.plugins.spindle.core.eclipse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import com.iw.plugins.spindle.core.ICoreListener;
import com.iw.plugins.spindle.core.ICoreListeners;

/**
 * @author gwl
 */
public class EclipseCoreListeners implements ICoreListeners
{
    private List fListeners;

    public void addCoreListener(ICoreListener listener)
    {
        if (fListeners == null)
            fListeners = new ArrayList();

        if (!fListeners.contains(listener))
            fListeners.add(listener);
    }

    public void removeCoreListener(ICoreListener listener)
    {
        if (fListeners != null)
            fListeners.remove(listener);

    }

    public void buildOccurred()
    {
        Display d = Display.getDefault();
        if (d == null)
            return;

        d.asyncExec(new Runnable()
        {
            public void run()
            {
                fireCoreListenerEvent();
            }
        });

    }

    public void fireCoreListenerEvent()
    {
        if (fListeners == null)
            return;

        for (Iterator iter = fListeners.iterator(); iter.hasNext();)
        {
            ICoreListener listener = (ICoreListener) iter.next();
            // simple for now - may create an event type later
            listener.coreChanged();
        }

    }

}
