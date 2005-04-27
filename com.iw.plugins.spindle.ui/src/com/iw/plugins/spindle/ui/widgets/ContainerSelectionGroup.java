/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.iw.plugins.spindle.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownComposite;

import com.iw.plugins.spindle.UIPlugin;

/**
 * Workbench-level composite for choosing a container.
 */
public class ContainerSelectionGroup extends Composite
{
  // The listener to notify of events
  private Listener listener;

  // Enable user to type in new container name
  private boolean allowNewContainerName = true;

  // show all projects by default
  private boolean showClosedProjects = true;

  // Last selection made by user
  private IContainer selectedContainer;

  // handle on parts
  private Text containerNameField;
  TreeViewer treeViewer;

  private IContainer root;

  private int heightHint;
  private int widhtHint;

  // the message to display at the top of this dialog
  private static final String DEFAULT_MSG_NEW_ALLOWED = UIPlugin
      .getString("ContainerGroup.message"); //$NON-NLS-1$
  private static final String DEFAULT_MSG_SELECT_ONLY = UIPlugin
      .getString("ContainerGroup.selectFolder"); //$NON-NLS-1$

  // sizing constants
  private static final int SIZING_SELECTION_PANE_WIDTH = 320;
  private static final int SIZING_SELECTION_PANE_HEIGHT = 300;
  /**
   * Creates a new instance of the widget.
   * 
   * @param parent The parent widget of the group.
   * @param listener A listener to forward events to. Can be null if no listener
   *                     is required.
   * @param allowNewContainerName Enable the user to type in a new container
   *                     name instead of just selecting from the existing ones.
   */
  public ContainerSelectionGroup(Composite parent, Listener listener,
      boolean allowNewContainerName, IContainer root)
  {
    this(parent, listener, allowNewContainerName, null, root);
  }
  /**
   * Creates a new instance of the widget.
   * 
   * @param parent The parent widget of the group.
   * @param listener A listener to forward events to. Can be null if no listener
   *                     is required.
   * @param allowNewContainerName Enable the user to type in a new container
   *                     name instead of just selecting from the existing ones.
   * @param message The text to present to the user.
   */
  public ContainerSelectionGroup(Composite parent, Listener listener,
      boolean allowNewContainerName, String message, IContainer root)
  {
    this(parent, listener, allowNewContainerName, message, true, root);
  }
  /**
   * Creates a new instance of the widget.
   * 
   * @param parent The parent widget of the group.
   * @param listener A listener to forward events to. Can be null if no listener
   *                     is required.
   * @param allowNewContainerName Enable the user to type in a new container
   *                     name instead of just selecting from the existing ones.
   * @param message The text to present to the user.
   * @param showClosedProjects Whether or not to show closed projects.
   */
  public ContainerSelectionGroup(Composite parent, Listener listener,
      boolean allowNewContainerName, String message, boolean showClosedProjects,
      IContainer root)
  {
    this(
        parent,
        listener,
        allowNewContainerName,
        message,
        showClosedProjects,
        SIZING_SELECTION_PANE_HEIGHT,
        root);
  }
  /**
   * Creates a new instance of the widget.
   * 
   * @param parent The parent widget of the group.
   * @param listener A listener to forward events to. Can be null if no listener
   *                     is required.
   * @param allowNewContainerName Enable the user to type in a new container
   *                     name instead of just selecting from the existing ones.
   * @param message The text to present to the user.
   * @param showClosedProjects Whether or not to show closed projects.
   * @param heightHint height hint for the drill down composite
   */
  public ContainerSelectionGroup(Composite parent, Listener listener,
      boolean allowNewContainerName, String message, boolean showClosedProjects,
      int heightHint, IContainer root)
  {
    super(parent, SWT.NONE);
    this.listener = listener;
    this.allowNewContainerName = allowNewContainerName;
    this.showClosedProjects = showClosedProjects;
    this.root = root;
    this.heightHint = heightHint;
    if (message != null)
      createContents(message, heightHint);
    else if (allowNewContainerName)
      createContents(DEFAULT_MSG_NEW_ALLOWED, heightHint);
    else
      createContents(DEFAULT_MSG_SELECT_ONLY, heightHint);
  }
  /**
   * The container selection has changed in the tree view. Update the container
   * name field value and notify all listeners.
   */
  public void containerSelectionChanged(IContainer container)
  {
    selectedContainer = container;

    if (allowNewContainerName)
    {
      if (container == null)
        containerNameField.setText("");//$NON-NLS-1$
      else
        containerNameField.setText(container.getFullPath().makeRelative().toString());
    }

    // fire an event so the parent can update its controls
    if (listener != null)
    {
      Event changeEvent = new Event();
      changeEvent.type = SWT.Selection;
      changeEvent.widget = this;
      listener.handleEvent(changeEvent);
    }
  }

  public void setRootContainer(IContainer container)
  {
    root = container;
  }

  public TreeViewer getTreeViewer()
  {
    return treeViewer;
  }
  /**
   * Creates the contents of the composite.
   */
  public void createContents(String message)
  {
    createContents(message, SIZING_SELECTION_PANE_HEIGHT);
  }
  /**
   * Creates the contents of the composite.
   * 
   * @param heightHint height hint for the drill down composite
   */
  public void createContents(String message, int heightHint)
  {
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));

    Label label = new Label(this, SWT.WRAP);
    label.setText(message);
    label.setFont(this.getFont());

    if (allowNewContainerName)
    {
      containerNameField = new Text(this, SWT.SINGLE | SWT.BORDER);
      containerNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      containerNameField.addListener(SWT.Modify, listener);
      containerNameField.setFont(this.getFont());
    } else
    {
      // filler...
      new Label(this, SWT.NONE);
    }

    createTreeViewer(heightHint);
    Dialog.applyDialogFont(this);
  }
  /**
   * Returns a new drill down viewer for this dialog.
   * 
   * @param heightHint height hint for the drill down composite
   * @return a new drill down viewer
   */
  protected void createTreeViewer(int heightHint)
  {
    // Create drill down.
    DrillDownComposite drillDown = new DrillDownComposite(this, SWT.BORDER);
    GridData spec = new GridData(GridData.VERTICAL_ALIGN_FILL
        | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
        | GridData.GRAB_VERTICAL);
    spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
    spec.heightHint = heightHint;
    drillDown.setLayoutData(spec);

    // Create tree viewer inside drill down.
    treeViewer = new TreeViewer(drillDown, SWT.NONE);
    drillDown.setChildTree(treeViewer);
    ContainerContentProvider cp = new ContainerContentProvider();
    cp.showClosedProjects(showClosedProjects);
    treeViewer.setContentProvider(cp);
    treeViewer.setLabelProvider(WorkbenchLabelProvider
        .getDecoratingWorkbenchLabelProvider());
    treeViewer.setSorter(new ViewerSorter());
    treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
    {
      public void selectionChanged(SelectionChangedEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        containerSelectionChanged((IContainer) selection.getFirstElement()); // allow
        // null
      }
    });
    treeViewer.addDoubleClickListener(new IDoubleClickListener()
    {
      public void doubleClick(DoubleClickEvent event)
      {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection)
        {
          Object item = ((IStructuredSelection) selection).getFirstElement();
          if (treeViewer.getExpandedState(item))
            treeViewer.collapseToLevel(item, 1);
          else
            treeViewer.expandToLevel(item, 1);
        }
      }
    });

    // This has to be done after the viewer has been laid out
    if (root == null || !root.exists())
    {
      treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
    } else
    {
      treeViewer.setInput(new Object());
    }
  }
  /**
   * Returns the currently entered container name. Null if the field is empty.
   * Note that the container may not exist yet if the user entered a new
   * container name in the field.
   */
  public IPath getContainerFullPath()
  {
    if (allowNewContainerName)
    {
      String pathName = containerNameField.getText();
      if (pathName == null || pathName.length() < 1)
        return null;
      else
        //The user may not have made this absolute so do it for them
        return (new Path(pathName)).makeAbsolute();
    } else
    {
      if (selectedContainer == null)
        return null;
      else
        return selectedContainer.getFullPath();
    }
  }
  /**
   * Gives focus to one of the widgets in the group, as determined by the group.
   */
  public void setInitialFocus()
  {
    if (allowNewContainerName)
      containerNameField.setFocus();
    else
      treeViewer.getTree().setFocus();
  }
  /**
   * Sets the selected existing container.
   */
  public void setSelectedContainer(IContainer container)
  {
    selectedContainer = container;

    //expand to and select the specified container
    List itemsToExpand = new ArrayList();
    IContainer parent = container.getParent();
    while (parent != null)
    {
      itemsToExpand.add(0, parent);
      parent = parent.getParent();
    }
    treeViewer.setExpandedElements(itemsToExpand.toArray());
    treeViewer.setSelection(new StructuredSelection(container), true);
  }

  class ContainerContentProvider implements ITreeContentProvider
  {
    private boolean showClosedProjects = true;
    /**
     * Creates a new ResourceContentProvider.
     */
    public ContainerContentProvider()
    {
    }
    /**
     * The visual part that is using this content provider is about to be
     * disposed. Deallocate all allocated SWT resources.
     */
    public void dispose()
    {
    }
    /**
     * @see ITreeContentProvider#getChildren
     */
    public Object[] getChildren(Object element)
    {

      if (element instanceof IWorkspace)
      {
        // check if closed projects should be shown
        IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
        if (showClosedProjects)
          return allProjects;

        ArrayList accessibleProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++)
        {
          if (allProjects[i].isOpen())
          {
            accessibleProjects.add(allProjects[i]);
          }
        }
        return accessibleProjects.toArray();
      } else if (element instanceof IContainer)
      {
        IContainer container = (IContainer) element;
        if (container.isAccessible())
        {
          try
          {
            List children = new ArrayList();
            IResource[] members = container.members();
            for (int i = 0; i < members.length; i++)
            {
              if (members[i].getType() != IResource.FILE)
              {
                children.add(members[i]);
              }
            }
            return children.toArray();
          } catch (CoreException e)
          {
            // this should never happen because we call #isAccessible before
            // invoking #members
          }
        }
      }
      return new Object[0];
    }
    /**
     * @see ITreeContentProvider#getElements
     */
    public Object[] getElements(Object element)
    {
      if (element.getClass() == Object.class)
        return new Object[]{root};
      return getChildren(element);
    }
    /**
     * @see ITreeContentProvider#getParent
     */
    public Object getParent(Object element)
    {
      if (element instanceof IResource)
        return ((IResource) element).getParent();
      return null;
    }
    /**
     * @see ITreeContentProvider#hasChildren
     */
    public boolean hasChildren(Object element)
    {
      return getChildren(element).length > 0;
    }
    /**
     * @see IContentProvider#inputChanged
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }
    /**
     * Specify whether or not to show closed projects in the tree viewer.
     * Default is to show closed projects.
     * 
     * @param show boolean if false, do not show closed projects in the tree
     */
    public void showClosedProjects(boolean show)
    {
      showClosedProjects = show;
    }
  }
}