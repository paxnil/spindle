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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PartInitException;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.editors.Editor;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class BaseEditorAction extends Action
{

    protected Editor fEditor;
    protected int fDocumentOffset;

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

    public void setActiveEditor(Editor editor)
    {
        fEditor = editor;
    }

    public void run()
    {
        fDocumentOffset = fEditor.getCaretOffset();
    }

    protected IType resolveType(String typeName)
    {
        IJavaProject jproject = TapestryCore.getDefault().getJavaProjectFor(fEditor.getStorage());
        if (jproject == null)
            return null;
    
        try
        {
            return jproject.findType(typeName);
        } catch (JavaModelException e)
        {
            //do nothing
        }
        return null;
    }

    /**
         * @param resolvedType
         */
    protected void reveal(IType resolvedType)
    {
        try
        {
            JavaUI.openInEditor(resolvedType);
        } catch (PartInitException e)
        {
            UIPlugin.log(e);
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }
    }

}
