/**********************************************************************
 Copyright (c) 2003  Widespace, OU  and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://solareclipse.sourceforge.net/legal/cpl-v10.html

 Contributors:
 Igor Malinin - initial contribution

 $Id$
 **********************************************************************/
package net.sf.solareclipse.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Base class for internationalized text editors.
 * 
 * @author Igor Malinin
 */
public class I18NTextEditor extends TextEditor
{
  /**
   * The <code>I18NTextEditor</code> implementation of this
   * <code>IEditorPart</code> method calls <code>performSaveAs</code>.
   * 
   * Subclasses may reimplement.
   */
  public void doSaveAs()
  {
    performSaveAs(getProgressMonitor());
  }

  /**
   * The <code>I18NTextEditor</code> implementation of this
   * <code>IEditorPart</code> method may be extended by subclasses.
   * 
   * @param progressMonitor
   *          the progress monitor for communicating result state or
   *          <code>null</code>
   */
  public void doSave(IProgressMonitor progressMonitor)
  {
    IDocumentProvider p = getDocumentProvider();
    if (p == null)
    {
      return;
    }

    if (p.isDeleted(getEditorInput()))
    {
      if (isSaveAsAllowed())
      {
        performSaveAs(progressMonitor);
      } else
      {
        Shell shell = getSite().getShell();

        String title = EditorMessages.getString("I18NTextEditor.error.save.deleted.title"); //$NON-NLS-1$
        String msg = EditorMessages.getString("I18NTextEditor.error.save.deleted.message"); //$NON-NLS-1$

        MessageDialog.openError(shell, title, msg);
      }
    } else
    {
      performSave(false, progressMonitor);

    }
  }
}