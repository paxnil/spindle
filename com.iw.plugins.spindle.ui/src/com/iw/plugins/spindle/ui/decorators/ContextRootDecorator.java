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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;

/**
 *  Decorator that indicates Tapestry projects
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ContextRootDecorator
    extends AbstractDecorator
    implements ILightweightLabelDecorator, TapestryCore.ICoreListener
{

    public ContextRootDecorator()
    {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    public void decorate(Object element, IDecoration decoration)
    {
        if (isContextRoot(element))
        {
            decoration.addOverlay(Images.getImageDescriptor("project_ovr.gif"));
        }

    }

    private boolean isContextRoot(Object element)
    {
        try
        {
            IContainer container = (IContainer) ((IAdaptable) element).getAdapter(IContainer.class);
            if (container != null)
            {
                IProject project = container.getProject();
                TapestryProject tproject = (TapestryProject) project.getNature(TapestryCore.NATURE_ID);
                if (tproject != null && tproject.getProjectType() == tproject.APPLICATION_PROJECT_TYPE)
                {
                    return container.equals(tproject.getWebContextFolder());
                }
            }
        } catch (CoreException e)
        {} catch (ClassCastException e)
        {}
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose()
    {
        // no need, the image is managed by the plugin!
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property)
    {
        return true;
    }

}
