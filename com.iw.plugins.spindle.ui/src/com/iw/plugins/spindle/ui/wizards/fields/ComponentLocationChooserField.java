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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryProject;
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

  private IContainer fResultLocation;
  private String fName;

  private AbstractNameField fNameField;
  private TapestryProjectDialogField fTapestryProjectField;

  public ComponentLocationChooserField(String name)
  {
    super(UIPlugin.getString(name + ".label"));
    fName = name;
  }

  public void init(
      AbstractNameField nameField,
      TapestryProjectDialogField projectField,
      IRunnableContext context)
  {
    fNameField = nameField;
    nameField.addListener(this);
    this.init(projectField, context);
  }

  public void init(TapestryProjectDialogField projectField, IRunnableContext context)
  {
    super.init(context);
    fTapestryProjectField = projectField;
    projectField.addListener(this);
    setButtonLabel(UIPlugin.getString(fName + ".button"));
    TapestryProject tproject = fTapestryProjectField.getTapestryProject();
    if (tproject == null)
    {
      setTextValue("");
    } else
    {
      setTextValue("WEB-INF/");
    }
  }

  public void dialogFieldChanged(DialogField field)
  {

    if (field == fTapestryProjectField)
      projectChanged();

    if (field == this)
      setStatus(locationChanged());

    if (fNameField != null && field == fNameField)
      setStatus(locationChanged());
  }

  public void setLocation(IContainer container)
  {
    throw new Error("not implemented yet");
  }

  public IContainer getLocation()
  {
    return fResultLocation;
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

    ContainerSelectionDialog dialog = new ContainerSelectionDialog(
        getShell(),
        webinf,
        true,
        null);
    dialog.setFilter(new OutputDirFilter());
    if (dialog.open() == dialog.OK)
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

      TapestryProject tproject = fTapestryProjectField.getTapestryProject();
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

      boolean isComponent = fNameField.getKind() == fNameField.COMPONENT_NAME;
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
    setTextValue("");
  }
}