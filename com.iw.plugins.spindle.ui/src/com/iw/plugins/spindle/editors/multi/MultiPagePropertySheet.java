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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MultiPagePropertySheet implements IPropertySheetPage
{
    private PageBook fPagebook;
    private Map fRecMap = new HashMap();
    private PropertySheetPage fDefaultPage;
    private IActionBars fActionBars;
    private IPropertySheetPage fCurrentPage;
    private boolean fDisposed = false;

    public MultiPagePropertySheet()
    {
        fDefaultPage = new PropertySheetPage();
    }

    private void activateBars(PageRec rec, boolean activate)
    {
        rec.setBarsActive(activate);
    }

    public void createControl(Composite parent)
    {
        fPagebook = new PageBook(parent, SWT.NULL);
        fDefaultPage.createControl(fPagebook);
        if (fCurrentPage != null)
            setPageActive(fCurrentPage);
    }

    private PageRec createPageRec(IPropertySheetPage page)
    {
        if (fActionBars == null)
            return null;
        PageRec rec = new PageRec();
        rec.page = page;

        rec.bars = new SubActionBars(fActionBars);
        getPageControl(page);

        page.setActionBars(rec.bars);
        fRecMap.put(page, rec);
        return rec;
    }

    public void dispose()
    {
        updateActionBars();

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

    public void closeActiveEditor()
    {
        if (fCurrentPage == null)
            return;
    }

    private Control getPageControl(IPropertySheetPage page)
    {
        Control control = page.getControl();
        if (control == null || control.isDisposed())
        {
            // first time
            page.createControl(fPagebook);
            control = page.getControl();
        }
        return control;
    }
    public void selectionChanged(IWorkbenchPart part, ISelection sel)
    {
        if (fCurrentPage != null)
            fCurrentPage.selectionChanged(part, sel);
    }
    public void setActionBars(IActionBars bars)
    {
        this.fActionBars = bars;

        createPageRec(fDefaultPage);

        if (fCurrentPage != null)
        {
            PageRec rec = createPageRec(fCurrentPage);
            setPageActive(rec);
            updateActionBars();
        }
    }
    public void setDefaultPageActive()
    {
        setPageActive(fDefaultPage);
    }
    public void setFocus()
    {
        if (fCurrentPage != null)
            fCurrentPage.setFocus();
    }
    private void setPageActive(PageRec pageRec)
    {
        IPropertySheetPage page = pageRec.page;
        Control control = getPageControl(page);
        fPagebook.showPage(control);
        pageRec.setBarsActive(true);
    }
    public void setPageActive(IPropertySheetPage page)
    {
        IPropertySheetPage oldPage = fCurrentPage;
        this.fCurrentPage = page;
        if (fPagebook == null)
        {
            // still not being made
            return;
        }
        if (oldPage != null)
        {
            PageRec oldRec = (PageRec) fRecMap.get(oldPage);
            if (oldRec != null)
            {
                oldRec.setBarsActive(false);
            }
        }
        PageRec rec = (PageRec) fRecMap.get(page);
        if (rec == null)
        {
            rec = createPageRec(page);
        }
        if (rec != null)
        {
            setPageActive(rec);
            updateActionBars();
        }
    }
    private void updateActionBars()
    {
        refreshGlobalActionHandlers();
        fActionBars.updateActionBars();
    }
    private void refreshGlobalActionHandlers()
    {
        // Clear old actions.
        fActionBars.clearGlobalActionHandlers();

        // Set new actions.
        PageRec activeRec = (PageRec) fRecMap.get(fCurrentPage);
        Map newActionHandlers = activeRec.bars.getGlobalActionHandlers();
        if (newActionHandlers != null)
        {
            Set keys = newActionHandlers.entrySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                fActionBars.setGlobalActionHandler((String) entry.getKey(), (IAction) entry.getValue());
            }
        }
    }

    class PageRec
    {
        IPropertySheetPage page;
        SubActionBars bars;
        void setBarsActive(boolean active)
        {
            if (active)
                bars.activate();
            else
                bars.deactivate();
        }
    }

}
