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

package com.iw.plugins.spindle.editors.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * Base class for editor actions
 * 
 * @author glongman@gmail.com
 */
public abstract class BaseEditorAction extends BaseAction
{

    protected Object[] fInterestingObjects;

    protected IStatus fStatus;

    private int fOffsetOverride = -1;

    /**
     * 
     */
    public BaseEditorAction()
    {
        super();
    }

    /**
     * @param text
     */
    public BaseEditorAction(String text)
    {
        super(text);
    }

    /**
     * @param text
     * @param image
     */
    public BaseEditorAction(String text, ImageDescriptor image)
    {
        super(text, image);
    }

    /**
     * @param text
     * @param style
     */
    public BaseEditorAction(String text, int style)
    {
        super(text, style);
    }

    public final void run()
    {
        if (canProceed())
            reveal(fInterestingObjects);
        else if (fStatus != null && fStatus.getSeverity() == IStatus.ERROR)
        {
            ErrorDialog.openError(
                    UIPlugin.getDefault().getActiveWorkbenchShell(),
                    "Spindle Error",
                    "Unable to complete the request",
                    fStatus);
        }
    }

    public final void run(int offset)
    {
        try {
            fOffsetOverride = offset;
            run();
        } finally {
            fOffsetOverride = -1;
        }
    }

    protected int getDocumentOffset()
    {
        if (fOffsetOverride >= 0)
            return fOffsetOverride;
        return super.getDocumentOffset();
    }
    
    public boolean canProceed(int offset) {
        try {
            fOffsetOverride = offset;
            return canProceed();
        } finally {
            fOffsetOverride = -1;
        }
    }

    public boolean canProceed()
    {
        fInterestingObjects = null;
        fStatus = null;

        fStatus = doGetStatus(new SpindleStatus());

        return fStatus != null && fStatus.isOK() && fInterestingObjects != null
                && fInterestingObjects.length != 0;
    }

    protected IStatus doGetStatus(SpindleStatus status)
    {
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        run();
    }

    /**
     * contribute to a menu. Override in subclasses.
     * 
     * @param menu
     */
    public void editorContextMenuAboutToShow(IMenuManager menu)
    {
    }
}