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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.IReconcileListener;
import com.iw.plugins.spindle.editors.spec.SpecEditor;

/**
 *  base class for MultiPage versions of the Spec Editor
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class MultiPageSpecEditor extends MultiPageEditorPart implements IReconcileListener
{
    static public final String PAGE_ORDER_PREFERENCE_KEY = UIPlugin.PLUGIN_ID + ".mulipageOrderSourceLast";
    static protected final int FIRST_PAGE = 0;
    static protected final int SECOND_PAGE = 1;

    public static void initializeDefaults(IPreferenceStore store)
    {
        store.setDefault(PAGE_ORDER_PREFERENCE_KEY, true);
    }

    private int fOverviewIndex;
    private int fSourceIndex;
    private IMultiPage fOverviewPage;
    private IMultiPage fSourcePage;
    private IMultiPage fErrorPage;
    private Object fModel;

    protected boolean fCreated = false;
    protected boolean fInError = false;
    private Object fEditorReconcileResult = null;
    private MultiPageContentOutline fContentOutline;
    private MultiPagePropertySheet fPropertySheet;
    private IMultiPage fActiveMultiPage;

    public MultiPageSpecEditor()
    {
        super();
    }

    public SpecEditor createSourcePage()
    {
        return new SpecEditor();
    }

    public abstract IMultiPage createOverview();

    protected Control errorPage;

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    protected void createPages()
    {
        IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
        boolean showSourcePageLast = store.getBoolean(PAGE_ORDER_PREFERENCE_KEY);
        try
        {
            if (showSourcePageLast)
            {
                fOverviewIndex = FIRST_PAGE;
                fSourceIndex = SECOND_PAGE;
                fOverviewPage = createOverview();
                addPage(fOverviewPage.getControl());
                fSourcePage = createSourcePage();
                addPage((IEditorPart) fSourcePage, getEditorInput());
            } else
            {
                fSourceIndex = FIRST_PAGE;
                fOverviewIndex = SECOND_PAGE;
                fSourcePage = createSourcePage();
                addPage((IEditorPart) fSourcePage, getEditorInput());
                fOverviewPage = createOverview();
                addPage(fOverviewPage.getControl());
            }
            setPageText(fOverviewIndex, fOverviewPage.getLabel());
            setPageText(fSourceIndex, fSourcePage.getLabel());
        } catch (PartInitException e)
        {
            removeAllPages();
            UIPlugin.log(e);
            fErrorPage = createErrorPage();
            setModel(e);
            addPage(fErrorPage.getControl());
            setPageText(0, fErrorPage.getLabel());
            fInError = true;
        }
    }

    private void removeAllPages()
    {
        for (int i = getPageCount(); i > 0; i--)
        {
            removePage(i);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#removePage(int)
     */
    public void removePage(int pageIndex)
    {
        if (pageIndex == fSourceIndex)
             ((SpecEditor) fSourcePage).removeReconcileListener(this);
        super.removePage(pageIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
     */
    protected void pageChange(int newPageIndex)
    {
        if (newPageIndex == fOverviewIndex && fEditorReconcileResult == null)
        {
            warnErrorsInSource();
            setActivePage(fSourceIndex);
            return;
        } else
        {
            super.pageChange(newPageIndex);
            if (fInError)
            {
                fActiveMultiPage = fErrorPage;
            } else
            {
                fActiveMultiPage = newPageIndex == fSourceIndex ? fSourcePage : fOverviewPage;
            }

            updateSynchronizedViews(fActiveMultiPage);

        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.IReconcileListener#reconciled(java.lang.Object)
     */
    public void reconciled(Object reconcileResults)
    {
        if (getActivePage() == fOverviewIndex)
        {
            if (reconcileResults == null)
            {
                warnErrorsInSource();
                setActivePage(fSourceIndex);
            } else
            {
                //TODO update the overview page
            }
        }
    }

    public MultiPageContentOutline getContentOutline()
    {
        if (fContentOutline == null || fContentOutline.isDisposed())
        {
            fContentOutline = new MultiPageContentOutline(this);
            updateContentOutline(getCurrentPage());
        }
        return fContentOutline;
    }

    /**
     * @return
     */
    private IMultiPage getCurrentPage()
    {
        int index = getActivePage();

        if (index == fOverviewIndex)
            return fOverviewPage;

        if (index == fSourceIndex)
            return fSourcePage;

        return null;
    }

    void updateSynchronizedViews(IMultiPage page)
    {
        updateContentOutline(page);
        updatePropertySheet(page);
    }

    void updateContentOutline(IMultiPage page)
    {
        IContentOutlinePage outlinePage = page.getContentOutlinePage();
        if (outlinePage != null)
        {
            fContentOutline.setPageActive(outlinePage);
        }
    }

    void updatePropertySheet(IMultiPage page)
    {
        IPropertySheetPage propertySheetPage = page.getPropertySheetPage();
        if (propertySheetPage != null)
        {
            fPropertySheet.setPageActive(propertySheetPage);
        } else
        {
            fPropertySheet.setDefaultPageActive();
        }
    }

    protected void warnErrorsInSource()
    {
        Display.getCurrent().beep();
        MessageDialog.openError(
            UIPlugin.getDefault().getActiveWorkbenchShell(),
            UIPlugin.getString("multieditor-error-source-title"),
            UIPlugin.getString("multieditor-error-source-title"));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor)
    {
        ((SpecEditor) fSourcePage).doSave(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs()
    {
        ((SpecEditor) fSourcePage).doSaveAs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#gotoMarker(org.eclipse.core.resources.IMarker)
     */
    public void gotoMarker(IMarker marker)
    {
        if (getActivePage() != fSourceIndex)
            setActivePage(fSourceIndex);
        ((SpecEditor) fSourcePage).gotoMarker(marker);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed()
    {
        return ((SpecEditor) fSourcePage).isSaveAsAllowed();
    }

    public void openTo(Object obj)
    {
        ((SpecEditor) fSourcePage).openTo(obj);
    }

    protected IMultiPage createErrorPage()
    {
        Composite container = getContainer();
        return null;
    }

    public IMultiPage getCurrentMultiPage()
    {
        return fActiveMultiPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
        //TODO check the validity of the input and show the error page if there are problems!
        super.init(site, input);
    }

    /**
     * @return
     */
    public Object getModel()
    {
        return fModel;
    }

    /**
     * @param object
     */
    public void setModel(Object object)
    {
        fModel = object;
    }

    public ISelection getSelection()
    {
        return getSite().getSelectionProvider().getSelection();
    }

    public void setSelection(ISelection selection)
    {
        getSite().getSelectionProvider().setSelection(selection);
    }

    /**
     * @return
     */
    public boolean isReadOnly()
    {
        return getUnderlyingStorage().isReadOnly();
    }

    /**
     * 
     */
    public IStorage getUnderlyingStorage()
    {
        IEditorInput input = getEditorInput();
        IStorage result;
        if (input instanceof JarEntryEditorInput)
        {
            return ((JarEntryEditorInput) input).getStorage();
        } else
        {
            return ((IStorage) input.getAdapter(IStorage.class));
        }
    }

    public abstract Image getDefaultHeadingImage();

    //    public IAction getAction(String id)
    //    {
    //        return getContributor().getGlobalAction(id);
    //    }
    //
    //    public MultiPageEditorActionBarContributor getContributor()
    //    {
    //        return (MultiPageEditorActionBarContributor) getEditorSite().getActionBarContributor();
    //    }

}
