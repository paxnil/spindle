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

package com.iw.plugins.spindle.editors.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IStorageEditorInput;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JumpToTemplateAction extends BaseJumpAction
{
    /**
     * 
     */
    public JumpToTemplateAction()
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
     */
    protected void doRun()
    {
        PluginComponentSpecification component = getEditorSpecification();
        if (component == null && fEditor.getEditorInput() instanceof IStorageEditorInput)
        {
            MessageDialog.openInformation(
                fEditor.getEditorSite().getShell(),
                "Operation Aborted",
                "Unable to Jump to Templates from  a jar based Specification");
            return;
        }

        List possibles = getTemplateLocations(component);
        if (possibles == null || possibles.isEmpty())
            return;
        if (possibles.size() > 1)
        {
            new ChooseTemplateLocationPopup(possibles, true).run();
        } else
        {
            reveal((IResourceWorkspaceLocation) possibles.get(0));
        }

    }

    /**
     * @return
     */
    private PluginComponentSpecification getEditorSpecification()
    {
        Object spec = fEditor.getSpecification();

        if (spec instanceof ILibrarySpecification)
            return null;

        return (PluginComponentSpecification) spec;
    }

    private List getTemplateLocations(PluginComponentSpecification spec)
    {
        List locations = spec.getTemplateLocations();

        if (locations.isEmpty() || !(fEditor instanceof TemplateEditor))
            return locations;

        IStorage currentStorage = fEditor.getStorage();
        List finalResult = new ArrayList();
        for (Iterator iter = locations.iterator(); iter.hasNext();)
        {
            IResourceWorkspaceLocation element = (IResourceWorkspaceLocation) iter.next();
            IStorage storage = element.getStorage();
            if (storage == null || storage.equals(currentStorage))
                continue;
            finalResult.add(element);
        }
        return finalResult;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.actions.BaseEditorAction#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void editorContextMenuAboutToShow(IMenuManager menu)
    {
        PluginComponentSpecification component = getEditorSpecification();
        if (component != null)
        {
            List possibles = getTemplateLocations(component);
            for (Iterator iter = possibles.iterator(); iter.hasNext();)
            {
                IResourceWorkspaceLocation element = (IResourceWorkspaceLocation) iter.next();
                Action openAction = new MenuOpenTemplateAction(element);
                openAction.setEnabled(element.getStorage() != null);
                menu.add(openAction);
            }
        }

    }

    class MenuOpenTemplateAction extends Action
    {
        IResourceWorkspaceLocation location;

        public MenuOpenTemplateAction(IResourceWorkspaceLocation location)
        {
            Assert.isNotNull(location);
            this.location = location;
            setText(location.getName());
        }

        public void run()
        {
            reveal(location);
        }
    }

    class ChooseTemplateLocationPopup extends ChooseLocationPopup
    {
        /**
         * @param templateLocations
         * @param forward
         */
        public ChooseTemplateLocationPopup(List templateLocations, boolean forward)
        {
            super(templateLocations, forward);
        }

        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.editors.actions.JumpToTemplateAction.ChooseLocationPopup#getImage(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
        */
        protected Image getImage(IResourceWorkspaceLocation location)
        {
            return Images.getSharedImage("html16.gif");
        }

    }

}
