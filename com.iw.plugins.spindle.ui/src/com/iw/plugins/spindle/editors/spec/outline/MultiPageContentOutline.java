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

package com.iw.plugins.spindle.editors.spec.outline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.IReconcileListener;
import com.iw.plugins.spindle.editors.spec.SpecEditor;
import com.iw.plugins.spindle.editors.util.DoubleClickSelection;

/**
 * Mulitpage content outline. Users can toggle between two
 * 
 * @author glongman@gmail.com
 */
public class MultiPageContentOutline
    implements
      IContentOutlinePage,
      IPageBookViewPage,
      ISelectionChangedListener,
      IReconcileListener
{

  private static final String SHOW_TAPESTRY_OUTLINE = UIPlugin.PLUGIN_ID
      + ".mpoutline.showTapestryOutline";

  public static final void initializeDefaultPreferences(IPreferenceStore store)
  {
    store.setDefault(SHOW_TAPESTRY_OUTLINE, false);
  }

  private PageBook fPageBook;
  private ISelectionProvider fSelectionProvider;
  private SpecEditor fEditor;
  private IContentOutlinePage fCurrentPage;
  private MessagePage fMessagePage;
  private XMLOutlinePage fXMLOutlinePage;
  private TapestryOutlinePage fTapestryOutlinePage;
  private List fPages = new ArrayList();
  private Object fReconciledObject;
  private boolean fDisposed;
  private MessagePoster fMessagePoster = new MessagePoster();
  private PageTurnPoster fPageTurner = new PageTurnPoster();
  private List fSelectionListeners = new ArrayList();
  private IPageSite fSite;
  private MultiPageContentOutline.ToggleAction fToggleAction;

  public MultiPageContentOutline(SpecEditor editor, IEditorInput input)
  {
    this.fEditor = editor;
    fToggleAction = new MultiPageContentOutline.ToggleAction(this);
    fSelectionProvider = editor.getSelectionProvider();
    fMessagePage = new MessagePage();
    fXMLOutlinePage = new XMLOutlinePage(editor, input);
    fTapestryOutlinePage = new TapestryOutlinePage(editor);
    fCurrentPage = getInitialPage();
    fEditor.addReconcileListener(this);
  }

  private IContentOutlinePage getInitialPage()
  {
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    boolean flag = store.getBoolean(SHOW_TAPESTRY_OUTLINE);
    if (flag)
      return fTapestryOutlinePage;

    return fXMLOutlinePage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.part.IPageBookViewPage#getSite()
   */
  public IPageSite getSite()
  {
    return fSite;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
   */
  public void init(IPageSite site) throws PartInitException
  {
    fSite = site;
    fXMLOutlinePage.init(site);
    fTapestryOutlinePage.init(site);
  }

  
//  public void setInput(Object obj)
//  {
//    if (obj instanceof XMLNode)
//    {
//      fXMLOutlinePage.setInput(obj);
//    } else
//    {
//      fTapestryOutlinePage.setInput(obj);
//    }
//
//  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.IReconcileListener#reconciled(java.lang.Object)
   */
  public void reconciled(Object reconcileResults)
  {
    try
    {
      fReconciledObject = reconcileResults;
      fTapestryOutlinePage.setInput(fReconciledObject);
      if (fCurrentPage == fTapestryOutlinePage || fCurrentPage == fMessagePage)
      {
        if (fReconciledObject == null)
        {
          fMessagePoster
              .postMessage("Unable to resolve this outline. \n\nThere may be malformed XML, or the file can not be seen by Tapestry.");
          fPageTurner.post(fMessagePage);
        } else
        {
          fPageTurner.post(fTapestryOutlinePage);
        }
      }
    } catch (IllegalArgumentException e)
    {
      fMessagePoster.postMessage("internalError");
      fPageTurner.post(fMessagePage);
    } catch (RuntimeException e)
    {
      UIPlugin.log(e);
      throw e;
    } finally
    {
      fToggleAction.setEnabled(true);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.IReconcileListener#reconcileStarted()
   */
  public void reconcileStarted()
  {
    fReconciledObject = null;
    fToggleAction.setEnabled(false);
  }

  
//  public void addFocusListener(org.eclipse.swt.events.FocusListener listener)
//  {
//  }
  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
    if (!fSelectionListeners.contains(listener))
      fSelectionListeners.add(listener);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    fPageBook = new PageBook(parent, SWT.NONE);
    if (fCurrentPage != null)
      setPageActive(fCurrentPage);
  }
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  public void dispose()
  {
    if (fPageBook != null && !fPageBook.isDisposed())
      fPageBook.dispose();
    fPageBook = null;
    fDisposed = true;
  }

//  public boolean isDisposed()
//  {
//    return fDisposed;
//  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.IPage#getControl()
   */
  public Control getControl()
  {
    return fPageBook;
  }
//  public PageBook getPagebook()
//  {
//    return fPageBook;
//  }
  public ISelection getSelection()
  {
    return fSelectionProvider.getSelection();
  }
 
//  public void makeContributions(
//      IMenuManager menuManager,
//      IToolBarManager toolBarManager,
//      IStatusLineManager statusLineManager)
//  {
//  }
//  public void removeFocusListener(FocusListener listener)
//  {
//  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
    fSelectionListeners.remove(listener);
  }

  
  private void fireSelectionChange(ISelectionProvider provider, ISelection selection)
  {
    SelectionChangedEvent evt = new SelectionChangedEvent(provider, selection);
    for (Iterator iter = fSelectionListeners.iterator(); iter.hasNext();)
    {
      ISelectionChangedListener listener = (ISelectionChangedListener) iter.next();
      listener.selectionChanged(evt);
    }
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event)
  {
    ISelection selection = event.getSelection();
    if (selection instanceof DoubleClickSelection)
      fEditor.openTo(((DoubleClickSelection) selection).getFirstElement());
    fireSelectionChange(event.getSelectionProvider(), selection);
  }
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
   */
  public void setActionBars(org.eclipse.ui.IActionBars actionBars)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.IPage#setFocus()
   */
  public void setFocus()
  {
    if (fCurrentPage != null)
      fCurrentPage.setFocus();
  }
  /**
   * @param page
   */
  public void setPageActive(IContentOutlinePage page)
  {

    synchronized (fEditor) // don't want to call this while a reconciling action
                           // is occuring!
    {
      Control control = fCurrentPage.getControl();
      if ((control != null && !control.isDisposed()) && page == fCurrentPage)
        return;

      if (page == fMessagePage && fCurrentPage != fTapestryOutlinePage)
        page = fCurrentPage;

      if (page == fTapestryOutlinePage && fReconciledObject == null)
        page = fMessagePage;

      if (fCurrentPage != null)
      {
        fCurrentPage.removeSelectionChangedListener(this);
      }
      page.addSelectionChangedListener(this);
      this.fCurrentPage = page;
      if (fPageBook == null)
      {
        // still not being made
        return;
      }
      control = page.getControl();
      if (control == null || control.isDisposed())
      {
        // first time
        page.createControl(fPageBook);
        control = page.getControl();

      }
      fPageBook.showPage(control);
      this.fCurrentPage = page;
    }
    updateToolbar();
  }

  private void updateToolbar()
  {
    IActionBars bars = getSite().getActionBars();
    IToolBarManager manager = bars.getToolBarManager();
    manager.removeAll();
    if (fCurrentPage == fTapestryOutlinePage)
      fTapestryOutlinePage.makeContributions(null, manager, null);
    manager.add(fToggleAction);
    bars.updateActionBars();
  }

  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
    fXMLOutlinePage.setSelection(selection);
  }

 
  public void switchPages(boolean showTapestry)
  {
    if (showTapestry)
      setPageActive(fTapestryOutlinePage);
    else
      setPageActive(fXMLOutlinePage);
  }

  private static class ToggleAction extends Action
  {
    private boolean fInitiatedByMe = false;
    private MultiPageContentOutline fOutline;
    public ToggleAction(MultiPageContentOutline outline)
    {
      super();
      fOutline = outline;
      setText("Toggle between outline views");
      setToolTipText("Toggle between outline views");
      setImageDescriptor(Images.getImageDescriptor("application16.gif"));

      IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
      store.addPropertyChangeListener(new IPropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent event)
        {
          if (event.getProperty().equals(SHOW_TAPESTRY_OUTLINE))
          {
            if (fInitiatedByMe == false)
            {
              boolean showTapestry = ((Boolean) event.getNewValue()).booleanValue();
              valueChanged(showTapestry, false);
              fOutline.switchPages(showTapestry);
            }
          }
        }
      });
      boolean checked = store.getBoolean(SHOW_TAPESTRY_OUTLINE);
      valueChanged(checked, false);
    }

    public void run()
    {

      boolean checked = isChecked();
      fOutline.switchPages(checked);
      valueChanged(checked, true);
    }

    public void valueChanged(boolean on, boolean store)
    {
      setChecked(on);
      if (store)
      {
        fInitiatedByMe = true;
        UIPlugin.getDefault().getPreferenceStore().setValue(SHOW_TAPESTRY_OUTLINE, on);
        fInitiatedByMe = false;

      }
    }
  }

  private class MessagePage implements IContentOutlinePage
  {

    Control fControl;
    Label fMessageLabel;
    String fSavedMessage; //a message might have been posted before the
                          // creatControl called;s

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
      Font font = parent.getFont();
      Composite top = new Composite(parent, SWT.CENTER);
      top.setLayout(new GridLayout());
      fControl = top;
      top.setFont(font);

      // Sets the layout data for the top composite's
      // place in its parent's layout.
      top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      createVerticalSpacer(top, 1);

      fMessageLabel = new Label(top, SWT.WRAP);
      fMessageLabel.setForeground(top.getDisplay().getSystemColor(SWT.COLOR_RED));
      fMessageLabel.setLayoutData(new GridData(GridData.FILL_BOTH
          | GridData.GRAB_VERTICAL));
      fMessageLabel.setText(fSavedMessage == null ? "working..." : fSavedMessage);
    }

    /**
     * Create some empty space.
     */
    protected void createVerticalSpacer(Composite comp, int colSpan)
    {
      Label label = new Label(comp, SWT.NONE);
      GridData gd = new GridData();
      gd.horizontalSpan = colSpan;
      label.setLayoutData(gd);
    }

    public void setMessage(String message)
    {
      if (fMessageLabel == null)
        fSavedMessage = message;
      else
        fMessageLabel.setText(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose()
    {
      if (!fMessageLabel.isDisposed())
        fMessageLabel.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#getControl()
     */
    public Control getControl()
    {
      return fControl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
     */
    public void setActionBars(IActionBars actionBars)
    {
      MultiPageContentOutline.this.setActionBars(actionBars);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.IPage#setFocus()
     */
    public void setFocus()
    {
      fMessageLabel.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    public ISelection getSelection()
    {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection)
    {
    }

  }

  class MessagePoster implements Runnable
  {

    /** Has the runnable already been posted? */
    private boolean posted = false;
    private String message = "take a look at this error!";

    /*
     * @see Runnable#run()
     */
    public void run()
    {

      fMessagePage.setMessage(message);
      posted = true;
    }

    /**
     * Posts this runnable into the event queue.
     */
    public void postMessage(String message)
    {
      this.message = message;
      if (posted)
        return;

      Display d = Display.getDefault();
      if (d != null && !d.isDisposed())
      {
        posted = false;
        d.asyncExec(this);
      }
    }
  }

  class PageTurnPoster implements Runnable
  {

    /** Has the runnable already been posted? */
    private boolean posted = false;
    private IContentOutlinePage page;

    /*
     * @see Runnable#run()
     */
    public void run()
    {
      setPageActive(page);
      posted = false;
    }

    /**
     * Posts this runnable into the event queue.
     */
    public void post(IContentOutlinePage page)
    {
      this.page = page;
      if (posted)
        return;

      Display d = Display.getDefault();
      if (d != null && !d.isDisposed())
      {
        posted = true;
        d.asyncExec(this);
      }
    }
  };
}