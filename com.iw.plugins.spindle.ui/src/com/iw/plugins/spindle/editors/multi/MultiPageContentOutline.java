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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MultiPageContentOutline implements IContentOutlinePage, ISelectionChangedListener
{
    private PageBook fPagebook;
    private ISelectionProvider fSelectionProvider;
    private MultiPageSpecEditor fEditor;
    private IContentOutlinePage fCurrentPage;
    private boolean fDisposed;

    public MultiPageContentOutline(MultiPageSpecEditor editor)
    {
        this.fEditor = editor;
         fSelectionProvider = editor.getSite().getSelectionProvider();
    }
    
    public void addFocusListener(org.eclipse.swt.events.FocusListener listener)
    {}
    
    public void addSelectionChangedListener(ISelectionChangedListener listener)
    {
        fSelectionProvider.addSelectionChangedListener(listener);
    }
    public void createControl(Composite parent)
    {
        fPagebook = new PageBook(parent, SWT.NONE);
        if (fCurrentPage != null)
            setPageActive(fCurrentPage);
    }
    public void dispose()
    {
        if (fPagebook != null && !fPagebook.isDisposed())
            fPagebook.dispose();
        fPagebook = null;
        fDisposed = true;
    }

    public boolean isDisposed()
    {
        return fDisposed;
    }

    public Control getControl()
    {
        return fPagebook;
    }
    public PageBook getPagebook()
    {
        return fPagebook;
    }
    public ISelection getSelection()
    {
        return fSelectionProvider.getSelection();
    }
    public void makeContributions(
        IMenuManager menuManager,
        IToolBarManager toolBarManager,
        IStatusLineManager statusLineManager)
    {}
    public void removeFocusListener(FocusListener listener)
    {}
    public void removeSelectionChangedListener(ISelectionChangedListener listener)
    {
        fSelectionProvider.removeSelectionChangedListener(listener);
    }
    public void selectionChanged(SelectionChangedEvent event)
    {
        fSelectionProvider.setSelection(event.getSelection());
    }
    public void setActionBars(org.eclipse.ui.IActionBars actionBars)
    {}
    public void setFocus()
    {
        if (fCurrentPage != null)
            fCurrentPage.setFocus();
    }
    public void setPageActive(IContentOutlinePage page)
    {
        if (fCurrentPage != null)
        {
            fCurrentPage.removeSelectionChangedListener(this);
        }
        page.addSelectionChangedListener(this);
        this.fCurrentPage = page;
        if (fPagebook == null)
        {
            // still not being made
            return;
        }
        Control control = page.getControl();
        if (control == null || control.isDisposed())
        {
            // first time
            page.createControl(fPagebook);
            control = page.getControl();

        }
        fPagebook.showPage(control);
        this.fCurrentPage = page;
    }
    /**
     * Set the selection.
     */
    public void setSelection(ISelection selection)
    {
        fSelectionProvider.setSelection(selection);
    }
}
