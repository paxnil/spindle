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
package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;
import com.iw.plugins.spindle.ui.widgets.ContainerSelectionDialog;

/**
 * ComponentLocationChooserField TODO add something here
 * 
 * @author glongman@gmail.com
 *  
 */
public class ComponentLocationChooserField extends StringButtonField
{
  private IFolder fResultLocation;
  private String fName;
  private boolean fForPage;
  private Button fTemplateLocationButton;
  private Button fGenTemplate;

  private AbstractNameField fNameField;
  private TapestryProjectDialogField fTapestryProjectField;

  public ComponentLocationChooserField(String name)
  {
    this(name, false);
  }

  public ComponentLocationChooserField(String name, boolean forPage)
  {
    super(UIPlugin.getString(name + ".label"));
    fName = name;
    fForPage = forPage;
  }

  public void fillIntoGrid(Composite parent, int numcols)
  {
    super.fillIntoGrid(parent, numcols);
    if (fForPage)
    {
      Font font = parent.getFont();
      fTemplateLocationButton = new Button(parent, SWT.CHECK);
      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
      data.horizontalSpan = numcols;
      fTemplateLocationButton.setLayoutData(data);
    }
  }
  public void init(
      AbstractNameField nameField,
      TapestryProjectDialogField projectField,
      Button genTemplate,
      IResource initResource,
      IRunnableContext context)
  {
    fNameField = nameField;
    nameField.addListener(this);
    fGenTemplate = genTemplate;
    fGenTemplate.addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e)
      {
        if (fTemplateLocationButton != null && !fTemplateLocationButton.isDisposed())
          fTemplateLocationButton.setEnabled(fGenTemplate.getSelection() && isEnabled());
      }
      public void widgetDefaultSelected(SelectionEvent e)
      {
        //eat it
      }
    });
    this.init(projectField, initResource, context);
  }

  public void setEnabled(boolean flag)
  {
    super.setEnabled(flag);
    if (fTemplateLocationButton != null && !fTemplateLocationButton.isDisposed() && fGenTemplate != null )
      fTemplateLocationButton.setEnabled(fGenTemplate.getSelection() && flag);
  } 
  public void init(
      TapestryProjectDialogField projectField,
      IResource initResource,
      IRunnableContext context)
  {
    super.init(context);
    fTapestryProjectField = projectField;
    projectField.addListener(this);
    setButtonLabel(UIPlugin.getString(fName + ".button"));
    ITapestryProject tproject = fTapestryProjectField.getTapestryProject();
    if (tproject == null)
    {
      setTextValue("");
    } else
    {
      IFolder webinf = tproject.getWebContextFolder().getFolder("WEB-INF");

      if (fForPage)
      {
        if (tproject != null)
          fTemplateLocationButton.setText(UIPlugin.getString(fName
              + ".templateInContextQuestion", tproject
              .getWebContextFolder()
              .getFullPath()
              .toString()));
      }

      if (initResource != null && webinf.exists())
      {
        IContainer container = (initResource instanceof IContainer)
            ? (IContainer) initResource : initResource.getParent();
        IPath webinfPath = webinf.getFullPath();
        IPath containerPath = container.getFullPath();
        if (webinfPath.isPrefixOf(containerPath))
        {
          setTextValue(containerPath
              .removeFirstSegments(webinfPath.segmentCount() - 1)
              .toString());
        } else
        {
          setTextValue("WEB-INF/");
        }
      } else
      {
        setTextValue("WEB-INF/");
      }
    }
  }

  public void dialogFieldChanged(DialogField field)
  {

    if (field == fTapestryProjectField)
      projectChanged();

    if (field == this)
      refreshStatus();

    if (fNameField != null && field == fNameField)
      refreshStatus();
  }

  public void refreshStatus()
  {
    setStatus(locationChanged());
  }

  public IFolder getSpecLocation()
  {
    return fResultLocation;
  }

  public IFolder getTemplateLocation()
  {
    if (!fForPage || fTemplateLocationButton == null
        || !fTemplateLocationButton.isEnabled()
        || !fTemplateLocationButton.getSelection() || !fGenTemplate.getSelection())
      return fResultLocation;

    ITapestryProject tproject = fTapestryProjectField.getTapestryProject();
    return tproject.getWebContextFolder();
  }

  public void dialogFieldButtonPressed(DialogField field)
  {
    if (field == this)
      chooseLocation();
  }

  /**
   *  
   */
  private void chooseLocation()
  {
    IStatus status = fTapestryProjectField.getStatus();
    if (!status.isOK())
    {
      MessageDialog
          .openError(getShell(), "Project Field has errors", status.getMessage());
      return;
    }

    IFolder webinf = fTapestryProjectField
        .getTapestryProject()
        .getWebContextFolder()
        .getFolder("WEB-INF");
    IPath webinfPath = webinf.getFullPath();

    IFolder selection;

    if (fResultLocation != null && fResultLocation.exists()
        && webinfPath.isPrefixOf(fResultLocation.getFullPath()))
    {
      selection = fResultLocation;
    } else
    {
      selection = webinf;
    }

    ContainerSelectionDialog dialog = new ContainerSelectionDialog(
        getShell(),
        webinf,
        true,
        null);
    dialog.setInitialSelections(new Object[]{selection});
    dialog.setFilter(new OutputDirFilter());
    if (dialog.open() == Window.OK)
    {
      Object[] results = dialog.getResult();
      if (results != null && results.length > 0)
      {
        IPath resultPath = (IPath) results[0];

        setTextValue(resultPath
            .removeFirstSegments(webinfPath.segmentCount() - 1)
            .toString());

        locationChanged();
      }
    }
  }

  class OutputDirFilter extends ViewerFilter
  {

    IPath outputLocation;

    public OutputDirFilter()
    {
      super();
      try
      {
        IJavaProject jproject = fTapestryProjectField
            .getTapestryProject()
            .getJavaProject();
        outputLocation = jproject.getOutputLocation();
      } catch (CoreException e)
      {
        UIPlugin.log(e);
      }
    }
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
      if (outputLocation == null)
        return true;

      return !outputLocation.isPrefixOf(((IContainer) element).getFullPath());
    }
  }

  /**
   * @return
   */
  private IStatus locationChanged()
  {
    fResultLocation = null;
    SpindleStatus result = new SpindleStatus();

    if (fTapestryProjectField == null)
      return result;

    String value = getTextValue().trim();

    if (!"WEB-INF".equals(value) && !value.startsWith("WEB-INF/"))
    {
      result.setError("must be a path prefixed with WEB-INF");
      return result;
    }

    if (fTapestryProjectField.getStatus().isOK())
    {

      ITapestryProject tproject = fTapestryProjectField.getTapestryProject();
      IProject project = tproject.getProject();

      IFolder webContext = tproject.getWebContextFolder().getFolder("WEB-INF");

      IPath path = webContext.getFullPath();

      if (!path.isValidPath(value))
      {
        result.setError("Not a valid path: " + value);
        return result;
      }

      IPath enteredPath = new Path(value).removeTrailingSeparator();

      if (enteredPath.segmentCount() == 1 && "WEB-INF".equals(enteredPath.segment(0)))
      {
        fResultLocation = webContext;
      } else
      {

        if (!webContext.exists())
        {
          result.setError("'" + path.toString() + "'  does not exist.");
          return result;
        }

        if (!path.isValidPath(value))
        {
          result.setError("Not a valid path: " + value);
          return result;
        }

        fResultLocation = webContext.getFolder(enteredPath.removeFirstSegments(1));
      }
    }

    if (fResultLocation != null && fNameField != null)
    {

      boolean isComponent = fNameField.getKind() == AbstractNameField.COMPONENT_NAME;
      IFile file = fResultLocation.getFile(new Path(fNameField.getTextValue()
          + (isComponent ? ".jwc" : ".page")));
      if (file.exists())
      {
        result.setError(UIPlugin.getString(fName + ".error.ComponentAlreadyExists", file
            .getFullPath()
            .toString()));
        return result;
      }
    }

    return result;
  }
  /**
   * @return
   */
  private void projectChanged()
  {
    if (!fTapestryProjectField.getStatus().isOK())
    {
      setStatus(new SpindleStatus());
    }

    if (fForPage)
    {
      ITapestryProject tproject = fTapestryProjectField.getTapestryProject();
      if (tproject != null)
        fTemplateLocationButton.setText(UIPlugin.getString(fName
            + ".templateInContextQuestion", tproject
            .getWebContextFolder()
            .getFullPath()
            .toString()));
    }

    setTextValue("");
  }
}