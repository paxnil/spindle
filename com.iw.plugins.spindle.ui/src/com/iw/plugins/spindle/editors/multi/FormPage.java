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

package com.iw.plugins.spindle.editors.multi;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;
import org.eclipse.update.ui.forms.internal.IFormPage;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class FormPage extends EditorPart implements IMultiPage
{
    private AbstractSectionForm form;
    private Control control;
    private MultiPageSpecEditor editor;
    private IContentOutlinePage contentOutlinePage;
    private IPropertySheetPage propertySheetPage;
    private org.eclipse.jface.viewers.ISelection selection;

    public FormPage(MultiPageSpecEditor editor, String title)
    {
        this(editor, title, null);
    }

    public FormPage(MultiPageSpecEditor editor, String title, AbstractSectionForm form)
    {
        this.editor = editor;
        if (form == null)
            form = createForm();
        this.form = form;
        if (isWhiteBackground())
            form.setHeadingImage(getHeadingImage());
        setTitle(title);
    }

    protected Image getHeadingImage()
    {
        return editor.getDefaultHeadingImage();
    }

    private boolean isWhiteBackground()
    {
        Color bg = form.getFactory().getBackgroundColor();
        return (bg.getRed() == 255 && bg.getGreen() == 255 && bg.getBlue() == 255);
    }
    public boolean becomesInvisible(IFormPage newPage)
    {
        ////        if (getModel() instanceof IModel && ((IModel) getModel()).isEditable())
        ////            form.commitChanges(false);
        //        getEditor().setSelection(new StructuredSelection());
        //        if (newPage instanceof PDESourcePage)
        //        {
        //            getEditor().updateDocument();
        //        }
        return true;
    }
    public void becomesVisible(IFormPage oldPage)
    {
        update();
        setFocus();
    }
    public boolean contextMenuAboutToShow(IMenuManager manager)
    {
        return true;
    }
    public abstract IContentOutlinePage createContentOutlinePage();
    public void createControl(Composite parent)
    {
        createPartControl(parent);
    }
    protected abstract AbstractSectionForm createForm();

    public void createPartControl(Composite parent)
    {
        control = form.createControl(parent);
        //TODO menu stuff
        //        control.setMenu(editor.getContextMenu());
        form.initialize(getModel());
    }
    public IPropertySheetPage createPropertySheetPage()
    {
        return null;
    }
    public void dispose()
    {
        form.dispose();
        if (contentOutlinePage != null)
            contentOutlinePage.dispose();
        if (propertySheetPage != null)
            propertySheetPage.dispose();
    }
    public void doSave(IProgressMonitor monitor)
    {}
    public void doSaveAs()
    {}

    public IAction getAction(String id)
    {
        return null;
        // TODO            return editor.getAction(id);
    }
    public IContentOutlinePage getContentOutlinePage()
    {
        if (contentOutlinePage == null
            || (contentOutlinePage.getControl() != null && contentOutlinePage.getControl().isDisposed()))
        {
            contentOutlinePage = createContentOutlinePage();
        }
        return contentOutlinePage;
    }
    public Control getControl()
    {
        return control;
    }
    public MultiPageSpecEditor getEditor()
    {
        return editor;
    }
    public AbstractSectionForm getForm()
    {
        return form;
    }
    public String getLabel()
    {
        return getTitle();
    }
    public Object getModel()
    {
        return getEditor().getModel();
    }
    public IPropertySheetPage getPropertySheetPage()
    {
        if (propertySheetPage == null
            || (propertySheetPage.getControl() != null && propertySheetPage.getControl().isDisposed()))
        {
            propertySheetPage = createPropertySheetPage();
        }
        return propertySheetPage;
    }
    public org.eclipse.jface.viewers.ISelection getSelection()
    {
        return selection;
    }
    public String getStatusText()
    {
        IEditorInput input = getEditor().getEditorInput();
        String status = "";

        if (input instanceof IFileEditorInput)
        {
            IFile file = ((IFileEditorInput) input).getFile();
            status = file.getFullPath().toString() + IPath.SEPARATOR;
        }
        status += getTitle();

        return status;
    }
    public void gotoMarker(IMarker marker)
    {}
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {}
    public boolean isDirty()
    {
        return false;
    }
    public boolean isSaveAsAllowed()
    {
        return false;
    }
    public boolean isSource()
    {
        return false;
    }
    public boolean isVisible()
    {
        return getEditor().getCurrentMultiPage() == this;
    }
    public void openTo(Object object)
    {
        getForm().expandTo(object);
    }
    public boolean performGlobalAction(String id)
    {
        return getForm().doGlobalAction(id);
    }
    public void setFocus()
    {
        getForm().setFocus();
    }
    public void setSelection(ISelection newSelection)
    {
        selection = newSelection;
        getEditor().setSelection(selection);
    }
    public String toString()
    {
        return getTitle();
    }
    public void update()
    {
        form.update();
    }

    public boolean canPaste(Clipboard clipboard)
    {
        return form.canPaste(clipboard);
    }

    public boolean isEditorReadOnly()
    {
        return getEditor().isReadOnly();
    }
}