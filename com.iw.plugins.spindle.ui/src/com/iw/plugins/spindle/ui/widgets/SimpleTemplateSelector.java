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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite; 
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.ui.wizards.factories.TemplateFactory;

/**
 * TemplateSelector a class that creates a combo that is populated with
 * templates from a particular TemplateContextId.
 * 
 * @author glongman@gmail.com
 *  
 */
public class SimpleTemplateSelector implements ISelectionProvider
{

  public static final String WORKSPACE = "(workspace default)";
  public static final String PROJECT = "(project default)";

  static public class TemplateComparator implements Comparator
  {
    // TODO this must change when workbench and project defaults are
    // implemented!
    String string1;
    String string2;
    int z1;
    int z2;
    public int compare(Object o1, Object o2)
    {
      string1 = getString(o1);
      string2 = getString(o2);

      z1 = getOrder(o1);
      z2 = getOrder(o2);

      return (z1 == z2) ? string1.compareTo(string2) : (z1 < z2 ? -1 : 1);
    }

    private String getString(Object obj)
    {
      if (obj instanceof Template)
        return ((Template) obj).getName();

      return obj.toString();
    }

    private int getOrder(Object obj)
    {
      if (obj instanceof Template)
        return ((Template) obj).getName().equals("default") ? 0 : 1;

      String check = obj.toString();
      if (check.indexOf(WORKSPACE) > 0)
        return -1;
      if (check.indexOf(PROJECT) > 0)
        return -2;

      return 99;
    }

  }

  public static final Comparator COMPARATOR = new TemplateComparator();

  String fTemplateContextId;
  Combo fCombo;
  Label fLabel;
  Label fIconLabel;
  List fListeners = new ArrayList();

  public SimpleTemplateSelector(String templateContextId)
  {
    fTemplateContextId = templateContextId;
  }

  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
    if (!fListeners.contains(listener))
      fListeners.add(listener);
  }

  public ISelection getSelection()
  {
    return new StructuredSelection(getSelectedTemplate());
  }

  public void setSelection(ISelection selection)
  {
    //do nothing
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
    fListeners.remove(listener);
  }

  protected void fireSelectionChanged()
  {
    Template selected = getSelectedTemplate();
    ISelection selection = selected == null
        ? StructuredSelection.EMPTY : new StructuredSelection(selected);

    SelectionChangedEvent event = new SelectionChangedEvent(this, selection);

    for (Iterator iter = fListeners.iterator(); iter.hasNext();)
    {
      ISelectionChangedListener listener = (ISelectionChangedListener) iter.next();
      listener.selectionChanged(event);
    }
  }

  public Template getSelectedTemplate()
  {
    if (fCombo.getItemCount() == 0)
      return null;

    return (Template) fCombo.getData(fCombo.getItem(fCombo.getSelectionIndex()));
  }

  public Control createControl(Composite parent, int columnCount)
  {
    return createControl(parent, UIPlugin.getString(fTemplateContextId), columnCount);
  }

//  public Control createFormControl(Composite parent, int columnCount)
//  {
//   Font font = parent.getFont();
//   Composite container = new Composite(parent, SWT.NULL);
//
//    fIconLabel = new Label(parent, SWT.NULL);
//    fLabel = new Label(parent, SWT.NULL);
//    fCombo = new Combo(parent, SWT.READ_ONLY);
//
//    Font font = parent.getFont();
//
//    Composite container = new Composite(parent, SWT.NULL);
//    FormLayout layout = new FormLayout();
//    container.setLayout(layout);
//
//    FormData formData = new FormData();
//    formData.width = 7;
//    formData.top = new FormAttachment(0, 5);
//    formData.left = new FormAttachment(0, 0);
//    fIconLabel.setLayoutData(formData);
//
//    fLabel.setText(UIPlugin.getString(fTemplateContextId));
//    formData = new FormData();
//    formData.top = new FormAttachment(0, 3);
//    formData.left = new FormAttachment(fIconLabel, 4);
//    formData.width = 75;
//    formData.right = new FormAttachment(fCombo, -4);
//    fLabel.setLayoutData(formData);
//
//    formData = new FormData();
//    formData.height = 25;
//    formData.left = new FormAttachment(fLabel,14);
//    formData.right = new FormAttachment(100, 0);
//    fCombo.setLayoutData(formData);
//
//    return container;
//
//  }

  /** @deprecated */
  public Control createControl(Composite parent, String label, int columnCount)
  {
    Font font = parent.getFont();
    Assert.isLegal(columnCount >= 3);

    GridData data;

    fIconLabel = new Label(parent, SWT.NULL);
    data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 1;
    data.widthHint = 7;
    fIconLabel.setLayoutData(data);

    fLabel = new Label(parent, SWT.NULL);
    fLabel.setText(label);
    data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = 1;
    fLabel.setLayoutData(data);

    fCombo = new Combo(parent, SWT.READ_ONLY);
    fCombo.setFont(font);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = columnCount - 2;
    fCombo.setLayoutData(data);

    populate();

    fCombo.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e)
      {
        fireSelectionChanged();
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
    });

    return fCombo;
  }

  public void dispose()
  {
    if (fCombo != null && !fCombo.isDisposed())
      fCombo.dispose();

    fListeners.clear();
  }

  public final IStatus validate()
  {
    IStatus status = doValidate();
    processValidationResult(status);
    return status;
  }

  /* subclasses should override with thier own behaviour! */
  protected IStatus doValidate()
  {
    return new SpindleStatus();
  }

  protected void processValidationResult(IStatus status)
  {
    setImage(!status.isOK());
  }

  public void setEnabled(boolean flag)
  {
    if (fCombo != null)
    {
      fLabel.setEnabled(flag);
      fCombo.setEnabled(flag);
      setImage(fIconLabel.getImage() != null);
    }
  }

  void setImage(boolean flag)
  {
    if (fIconLabel != null)
    {
      if (flag)
      {
        fIconLabel.setImage(fCombo.isEnabled()
            ? Images.getSharedImage("error_co.gif") : Images
                .getSharedImage("error_co_dis.gif"));
      } else
      {
        fIconLabel.setImage(null);
      }
    }
  }

  protected void populate()
  {
    if (fCombo != null)
    {
      fCombo.removeAll();
      List templates = TemplateFactory.getAllTemplates(fTemplateContextId);
      Collections.sort(templates, COMPARATOR);
      String description;
      String label;
      for (Iterator iter = templates.iterator(); iter.hasNext();)
      {
        Template template = (Template) iter.next();

        description = template.getDescription();
        if (description != null)
          description = description.trim();
        else
          description = "";

        label = template.getName()
            + (description.length() > 0 ? " - " + description : "");

        fCombo.add(label);
        fCombo.setData(label, template);
      }
      if (fCombo.getItemCount() >= 0)
        fCombo.select(0);
    }
  }

}