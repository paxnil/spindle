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

package com.iw.plugins.spindle.editors.template.actions;

import org.apache.tapestry.INamespace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.editors.actions.BaseEditorAction;

/**
 * Base class for spec actions that need the xml partitioning (templates).
 * 
 * @author glongman@gmail.com
 */
public abstract class BaseTemplateAction extends BaseEditorAction
{

    protected INamespace fNamespace;

    protected INamespace fFrameworkNamespace;

    protected IDocument fDocument;

    public BaseTemplateAction()
    {
        super();
    }

    public BaseTemplateAction(String text)
    {
        super(text);
    }

    public BaseTemplateAction(String text, ImageDescriptor image)
    {
        super(text, image);
    }

    public BaseTemplateAction(String text, int style)
    {
        super(text, style);
    }

    protected IStatus getStatus()
    {                
        if (getDocumentOffset() < 0)
            return null;

        try
        {
            ITextEditor editor = getTextEditor();
            fDocument = editor.getDocumentProvider().getDocument(editor.getEditorInput());
            if (fDocument.getLength() == 0 || fDocument.get().trim().length() == 0)
                return null;            

        }
        catch (RuntimeException e)
        {
            UIPlugin.log(e);
            throw e;
        }
        
        return new SpindleStatus();
    }
}