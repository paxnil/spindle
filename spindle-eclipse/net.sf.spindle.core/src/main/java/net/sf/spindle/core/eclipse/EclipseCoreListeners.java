package net.sf.spindle.core.eclipse;

import java.util.ArrayList;
import java.util.List;

import net.sf.spindle.core.ICoreListener;
import net.sf.spindle.core.ICoreListeners;

import org.eclipse.swt.widgets.Display;

/**
 * @author gwl
 */
public class EclipseCoreListeners implements ICoreListeners
{
    private List<ICoreListener> fListeners;

    public void addCoreListener(ICoreListener listener)
    {
        if (fListeners == null)
            fListeners = new ArrayList<ICoreListener>();

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

        for (ICoreListener listener : fListeners)
        {
            listener.coreChanged();
        }
    }

}
