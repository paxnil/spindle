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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.decorators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 *  Base class for label decorators
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class AbstractDecorator implements ILightweightLabelDecorator, TapestryCore.ICoreListener
{
    public AbstractDecorator()
    {}

    protected List fLabelProviderListeners;

    public void addListener(ILabelProviderListener listener)
    {
        if (fLabelProviderListeners == null)
        {
            fLabelProviderListeners = new ArrayList();
            TapestryCore.addCoreListener(this);
        }

        if (!fLabelProviderListeners.contains(listener))
            fLabelProviderListeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener)
    {
        if (fLabelProviderListeners != null)
        {

            fLabelProviderListeners.remove(listener);
            if (fLabelProviderListeners.isEmpty())
            {
                TapestryCore.removeCoreListener(this);
                fLabelProviderListeners = null;
            }
        }
    }

    public void coreChanged()
    {
        if (fLabelProviderListeners != null)
            for (Iterator iter = fLabelProviderListeners.iterator(); iter.hasNext();)
            {
                ILabelProviderListener listener = (ILabelProviderListener) iter.next();
                listener.labelProviderChanged(new LabelProviderChangedEvent(this));
            }
    }

}
