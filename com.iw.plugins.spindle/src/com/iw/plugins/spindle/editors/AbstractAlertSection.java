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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;
import org.eclipse.ui.views.tasklist.TaskList;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.HyperlinkAdapter;
import org.eclipse.update.ui.forms.internal.HyperlinkHandler;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;

public abstract class AbstractAlertSection extends SpindleFormSection implements IResourceChangeListener {

  private String name;
  private HyperlinkHandler handler;
  private Composite container;
  protected IMarker[] foundProblems;
  protected IMarker[] foundTasks;
  protected String markerMessage;

  /**
   * Constructor for AlertSection
   */
  public AbstractAlertSection(SpindleFormPage page) {
    this("AlertSection", page);
  }

  /**
   * Constructor for AlertSection
   */
  public AbstractAlertSection(String name, SpindleFormPage page) {
    super(page);
    setHeaderText(MessageUtil.getString(name + ".title"));

    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(this);
    this.name = name;
    handler = new HyperlinkHandler();

  }

  public void dispose() {
    handler.dispose();
    container.dispose();
    super.dispose();
  }

  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */
  public Composite createClientContainer(Composite parent, FormWidgetFactory factory) {
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    handler.setBackground(factory.getBackgroundColor());
    handler.setForeground(factory.getForegroundColor());
    handler.setActiveForeground(factory.getHyperlinkColor());
    handler.setHyperlinkUnderlineMode(HyperlinkHandler.UNDERLINE_ROLLOVER);

    container = factory.createComposite(parent);
    container.setLayout(layout);
    createAlerts(container, factory);
    return container;
  }

  /**
   * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
   */
  public void resourceChanged(IResourceChangeEvent arg0) {
    /* TBD when more a strategy for handling markers is devised
        if (eventType.getType() == event.POST_CHANGE) {
          try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
              // IResourceDeltaVisitor
              public boolean visit(IResourceDelta delta) {
                if (delta != null && delta.getKind() == IResourceDelta.CHANGED) {
                	switch (delta.getFlags()) {
                		case IResourceDelta.MARKERS :
                		
                }
                return true;
              }
            });
          } catch (CoreException ex) { // put this for now
            TapestryPlugin.getDefault().logException(ex);
          }
        }
    */
  }

  /**
   *  Create the Alert's message line
   *  A message line widget will be created as a result
   */
  public void createAlerts(Composite container, FormWidgetFactory factory) {
    boolean hasAlerts = false;
    hasAlerts = checkMarkers(container, factory);
    if (checkReferences(container, factory)) {
      hasAlerts = true;
    }
    if (hasAlerts == false) {
      Label label = factory.createLabel(container, "No Alerts");
      GridData gd = new GridData();
      gd.horizontalSpan = 2;
      label.setLayoutData(gd);
    }
  }

  /**
   * populate the foundMarkers, and foundTasks from the Project, create an Alert Message Line
   * if they are not empty.
   */
  private boolean checkMarkers(Composite parent, FormWidgetFactory factory) {
    IEditorInput input = getFormPage().getEditor().getEditorInput();
    if (!(input instanceof IFileEditorInput))
      return false;
    IFile file = ((IFileEditorInput) input).getFile();
    IProject project = file.getProject();
    try {
      final IMarker[] problems = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      final IMarker[] tasks = project.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE);
      if (problems.length == 0 && tasks.length == 0) {
        return false;
      }
      final IMarker[] markers = mergeMarkers(problems, tasks);
      foundProblems = problems;
      foundTasks = tasks;
      createAlertMessageLineWidget(parent, factory);
    } catch (CoreException e) {
      TapestryPlugin.getDefault().logException(e);
      return false;
    }
    return true;
  }

  /**
   * Override to provide reference checking in your model - i.e. resolve all component references
   * in an application. 
   * <p>If your implementation find problems to report, create a Label widget here and return
   * true.
   * 
   * @return true if a message line was created (reference problems found)
   */
  protected abstract boolean checkReferences(Composite parent, FormWidgetFactory factory);

  /**
   * Override to produce your own Alert Message!
   */
  protected void createAlertMessageLineWidget(Composite parent, FormWidgetFactory factory) {
    String[] args = { "" + foundProblems.length, "" + foundTasks.length };
    final IMarker[] markers = mergeMarkers(foundProblems, foundTasks);
    String message;
    message = MessageUtil.getFormattedString(name + ".alert.markers", args);
    Label imageLabel = factory.createLabel(parent, null);
    imageLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJS_ERROR_TSK));
    Label label = factory.createLabel(parent, message);
    label.setToolTipText(MessageUtil.getString(name + ".alert.markers.tooltip"));
    handler.registerHyperlink(label, new HyperlinkAdapter() {
      public void linkActivated(Control link) {
        try {
          IViewPart view = TapestryPlugin.getDefault().getActivePage().showView(IPageLayout.ID_TASK_LIST);
          final TaskList tasklist = (TaskList) view;
          Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
              tasklist.setSelection(new StructuredSelection(markers), true);
            }
          });
        } catch (PartInitException e) {
          TapestryPlugin.getDefault().logException(e);
        }
      }
    });
  }

  private IMarker[] mergeMarkers(IMarker[] problems, IMarker[] tasks) {
    IMarker[] result = new IMarker[problems.length + tasks.length];
    int i = 0;
    for (; i < problems.length; i++) {
      result[i] = problems[i];
    }
    for (int j = 0; j < tasks.length; j++) {
      result[i++] = tasks[j];
    }
    return result;
  }

}