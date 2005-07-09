/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.iw.plugins.spindle.UIPlugin;

public class RequiredSaveEditorAction
{

  IEditorPart[] unsavedEditors;
  public RequiredSaveEditorAction(IEditorPart editor)
  {
    this(new IEditorPart[]{editor});
  }

  public RequiredSaveEditorAction(Collection editorParts)
  {
    this((IEditorPart[]) editorParts.toArray(new IEditorPart[editorParts.size()]));
  }

  public RequiredSaveEditorAction(IEditorPart[] editors)
  {
    unsavedEditors = editors;
  }

  public boolean save()
  {
    return save(UIPlugin.getString("requiredSaveTitle"), UIPlugin
        .getString("requiredSaveMessage"));
  }

  public boolean save(String title, String message)
  {
    Shell parent = getShell();
    ListSelectionDialog dialog = new ListSelectionDialog(
        parent,
        unsavedEditors,
        new ListContentProvider(),
        new SaveLabelProvider(),
        message);
    dialog.setTitle(title);
    dialog.setBlockOnOpen(true);
    dialog.setInitialSelections(unsavedEditors);
    if (dialog.open() == ListSelectionDialog.CANCEL
        || dialog.getResult().length != unsavedEditors.length)
    {
      return false;
    }

    try
    {
      PlatformUI.getWorkbench().getProgressService().run(
          false,
          false,
          createRunnable(Arrays.asList(unsavedEditors)));
      //            new ProgressMonitorDialog(getShell()).run(false, false, );
    } catch (InvocationTargetException e)
    {
      UIPlugin.log(e);
      return false;
    } catch (InterruptedException e)
    {
      // Can't happen. Operation isn't cancelable.
    }
    return true;
  }

  private Shell getShell()
  {
    return UIPlugin.getDefault().getActiveWorkbenchShell();
  }

  private static IRunnableWithProgress createRunnable(final List editorsToSave)
  {
    return new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor)
      {
        Iterator iter = editorsToSave.iterator();
        while (iter.hasNext())
          ((IEditorPart) iter.next()).doSave(monitor);
      }
    };
  }

  class SaveLabelProvider extends LabelProvider
  {
    public Image getImage(Object element)
    {
      return ((IEditorPart) element).getTitleImage();
    }
    public String getText(Object element)
    {
      return ((IEditorPart) element).getTitle();
    }
  }

  class ListContentProvider implements IStructuredContentProvider
  {
    Object[] contents;

    public ListContentProvider()
    {
    }
    public void dispose()
    {
    }
    public Object[] getElements(Object input)
    {
      if (contents != null && contents == input)
        return contents;
      return new Object[0];
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      if (newInput instanceof Object[])
        contents = (Object[]) newInput;
      else
        contents = null;
      // we use a fixed set.
    }

  }

}