package com.iw.plugins.spindle.editorlib;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeListener;

import net.sf.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.viewsupport.StorageLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.spec.PluginContainedComponent;
import com.iw.plugins.spindle.ui.AbstractDialog;
import com.iw.plugins.spindle.ui.PixelConverter;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class RenamedLibraryAction extends Action {

  TapestryLibraryModel model;
  ILibrarySpecification libSpecification;
  String newName;
  String oldName;
  TapestryProjectModelManager mgr;
  TapestryLookup lookup;

  /**
   * Constructor for RenamedLibraryAction.
   */
  public RenamedLibraryAction(
    TapestryLibraryModel model,
    String newLibraryName,
    String oldLibraryName)
    throws CoreException {
    super();
    this.model = model;
    this.newName = newLibraryName;
    this.oldName = oldLibraryName;

    configure();
  }

  private void configure() throws CoreException {

    ITapestryProject project = TapestryPlugin.getDefault().getTapestryProjectFor(model);
    mgr = project.getModelManager();
    lookup = project.getLookup();
    libSpecification = model.getSpecification();

  }

  /**
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    if (lookup == null || mgr == null) {
      return;
    }
    Map changedData = collectChangedModels();

    if (!changedData.isEmpty()) {

      ChoosePartsToUpdateDialog dialog =
        new ChoosePartsToUpdateDialog(
          TapestryPlugin.getDefault().getActiveWorkbenchWindow().getShell(),
          changedData);

      if (dialog.open() == dialog.OK) {

        performChanges(dialog.getResult(), changedData);

      }
    }
  }

  private void performChanges(Object[] toChange, Map changeData) {
    if (toChange.length > 0) {

      ProgressMonitorDialog dialog =
        new ProgressMonitorDialog(
          TapestryPlugin.getDefault().getActiveWorkbenchWindow().getShell());

      try {

        dialog.run(false, true, getRunnable(toChange, changeData, oldName, newName));

      } catch (InvocationTargetException e) {

      } catch (InterruptedException e) {

      }

    }
  }

  /**
   * Method getRunnable.
   * @param toChange
   * @return IRunnableWithProgress
   */
  private IRunnableWithProgress getRunnable(
    Object[] toChange,
    Map changeData,
    String oldLibraryName,
    String newLibraryName) {

    final Object[] useDirty = toChange;
    final Map useMap = changeData;
    final TapestryProjectModelManager useManager = mgr;
    final String useOld = oldLibraryName;
    final String useNew = newLibraryName;

    return new IRunnableWithProgress() {
      /**
       * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
       */
      public void run(IProgressMonitor monitor)
        throws InvocationTargetException, InterruptedException {
        for (int i = 0; i < useDirty.length; i++) {

          IStorage storage = (IStorage) useDirty[i];

          IEditorPart editor = Utils.getEditorFor(storage);

          if (editor != null) {

            if (editor.isDirty()) {

              editor.doSave(monitor);

            }

            SpindleMultipageEditor spindleEditor = (SpindleMultipageEditor) editor;

            TapestryComponentModel editorModel = (TapestryComponentModel) spindleEditor.getModel();

            try {

              performChangeInModel(editorModel, useMap, useOld, useNew, monitor);

            } catch (RuntimeException e) {

              e.printStackTrace();
            }

            editorModel.setOutOfSynch(true);

            //            spindleEditor.showPage(spindleEditor.SOURCE_PAGE);

            spindleEditor.fireSaveNeeded();

          } else {

            final TapestryComponentModel workspaceModel =
              (TapestryComponentModel) mgr.getEditableModel(storage, this);

            mgr.connect(storage, this);
            performChangeInModel(workspaceModel, useMap, useOld, useNew, monitor);

            try {

              (new WorkspaceModifyOperation() {
                public void execute(final IProgressMonitor monitor) throws CoreException {

                  Utils.saveModel(workspaceModel, monitor);

                }
              }).execute(monitor);

            } catch (CoreException e) {
            }

            mgr.disconnect(workspaceModel, this);

          }

        }

      }
    };

  }

  private void performChangeInModel(
    TapestryComponentModel model,
    Map changeData,
    String oldLibraryName,
    String newLibraryName,
    IProgressMonitor monitor) {

    monitor.beginTask("updating " + model.getUnderlyingStorage().getFullPath().toString(), 1);

    List changes = (List) changeData.get(model.getUnderlyingStorage());

    for (Iterator iter = changes.iterator(); iter.hasNext();) {

      String id = (String) iter.next();

      PluginComponentSpecification modelSpec = model.getComponentSpecification();

      PluginContainedComponent newElement = (PluginContainedComponent) modelSpec.getComponent(id);

      String typeToChange = newElement.getType();

      int nsIndex = typeToChange.indexOf(":");

      if (nsIndex > 0) {

        typeToChange = newLibraryName + typeToChange.substring(nsIndex);

        newElement.setType(typeToChange);

        modelSpec.setComponent(id, newElement);

        model.fireModelObjectChanged(model, "components");

      }

    }

    monitor.worked(1);

  }

  private Map collectChangedModels() {

    HashMap result = new HashMap();

    for (Iterator iter = libSpecification.getComponentAliases().iterator(); iter.hasNext();) {

      String alias = (String) iter.next();
      findComponentChanges(result, libSpecification.getComponentSpecificationPath(alias));

    }

    for (Iterator iter = libSpecification.getPageNames().iterator(); iter.hasNext();) {
      String pageName = (String) iter.next();
      findComponentChanges(result, libSpecification.getPageSpecificationPath(pageName));

    }

    return result;

  }

  private void findComponentChanges(HashMap result, String path) {

    TapestryComponentModel component = resolveComponent(path);

    if (component != null && !component.getUnderlyingStorage().isReadOnly()) {

      IEditorPart editor = Utils.getEditorFor(component.getUnderlyingStorage());

      if (editor != null && !(editor instanceof SpindleMultipageEditor)) {

        return;
      }

      List componentPartsToUpdate = findComponentPartsToUpdate(component);
      if (!componentPartsToUpdate.isEmpty()) {
        result.put(component.getUnderlyingStorage(), componentPartsToUpdate);
      }
    }
  }

  /**
   * Method findComponentPartsToUpdate.
   * @param component
   * @return List
   */
  private List findComponentPartsToUpdate(TapestryComponentModel component) {

    ArrayList result = new ArrayList();

    PluginComponentSpecification componentSpec = component.getComponentSpecification();

    for (Iterator iter = componentSpec.getComponentIds().iterator(); iter.hasNext();) {
      String id = (String) iter.next();

      PluginContainedComponent contained =
        (PluginContainedComponent) componentSpec.getComponent(id);

      if (contained.getType().startsWith(oldName + ":")) {

        result.add(id);

      }

    }
    return result;
  }

  /**
   * Method resolveComponent.
   * @param path
   * @return TapestryComponentModel
   */
  private TapestryComponentModel resolveComponent(String path) {

    TapestryComponentModel result = null;

    IStorage[] storages = null;

    if (path.endsWith(".jwc")) {

      storages = lookup.findComponent(path);

    } else if (path.endsWith(".page")) {

      storages = lookup.findPage(path);
    }

    if (storages != null && storages.length == 1) {

      result = (TapestryComponentModel) mgr.getReadOnlyModel(storages[0]);

    }

    return result;

  }

  class ChoosePartsToUpdateDialog extends ListSelectionDialog {

    public ChoosePartsToUpdateDialog(Shell parent, Map data) {
      super(
        parent,
        data,
        new ContentProvider(),
        new ChangedLabelProvider(),
        "The following depend on the library you have changed.\n\nChoose those you wish to update with the new name\n\ndirty editors will be saved.");

      setTitle("Library Name Changed");
      setChecked(true);
      setInitialSelections(data.keySet().toArray(new Object[data.size()]));
    }

  }

  public class ContentProvider implements IStructuredContentProvider {

    Map changeData;
    /**
     * Constructor for ContentProvider.
     */
    public ContentProvider() {
      super();
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
     */
    public Object[] getElements(Object inputElement) {

      Set keys = changeData.keySet();
      return (Object[]) keys.toArray(new Object[keys.size()]);

    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

      changeData = (Map) newInput;
    }

  }

  public class ChangedLabelProvider extends StorageLabelProvider {

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
     */
    public String getText(Object element) {

      return ((IStorage) element).getFullPath().toString();
    }

  }
}
