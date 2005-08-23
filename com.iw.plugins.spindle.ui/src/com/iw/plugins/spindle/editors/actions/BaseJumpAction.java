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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.ui.util.WrappedImageDescriptor;

/**
 * Base class for actions that cause a jump from one editor to another
 * 
 * @author glongman@gmail.com
 * 
 */
public abstract class BaseJumpAction extends BaseEditorAction
{

  static protected ILabelProvider LABEL_PROVIDER = new LabelProvider();

  static private class LabelProvider implements ILabelProvider
  {
    ILabelProvider javaElementProvider;
    public LabelProvider()
    {
      super();
      javaElementProvider = new JavaElementLabelProvider(
          JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_QUALIFIED
              | JavaElementLabelProvider.SHOW_ROOT);
    }

    public Image getImage(Object element)
    {
      if (element instanceof IJavaElement)
      {
        return javaElementProvider.getImage(element);
      } else
      {
        return null;
      }
    }

    public String getText(Object element)
    {
      if (element instanceof IJavaElement)
      {
        return javaElementProvider.getText(element);
      } else
      {
        return null;
      }
    }

    public void addListener(ILabelProviderListener listener)
    {
    }

    public void dispose()
    {
      javaElementProvider.dispose();
    }

    public boolean isLabelProperty(Object element, String property)
    {
      return false;
    }

    public void removeListener(ILabelProviderListener listener)
    {
    }
  }

  protected IDocument fDocument = null;
  /**
   *  
   */
  public BaseJumpAction()
  {
    super();
  }

  /**
   * @param text
   */
  public BaseJumpAction(String text)
  {
    super(text);
  }

  /**
   * @param text
   * @param image
   */
  public BaseJumpAction(String text, ImageDescriptor image)
  {
    super(text, image);
  }

  /**
   * @param text
   * @param style
   */
  public BaseJumpAction(String text, int style)
  {
    super(text, style);
  }

  public void run()
  {
    super.run();
    try
    {
      doRun();
    } catch (RuntimeException e)
    {
      UIPlugin.log(e);
    } 
  }
  
  protected void showMessage(String title, String message) {
      MessageDialog.openInformation(UIPlugin.getDefault().getActiveWorkbenchShell(), title, message);
  }


  protected abstract void doRun();

  protected IDocument getDocument()
  {
    Assert.isTrue(fDocument != null);
    return fDocument;
  }

  protected ImageDescriptor getImageDescriptorFor(Image image)
  {
    if (image == null)
      return null;
    return new WrappedImageDescriptor(image);
  }

}