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

package com.iw.plugins.spindle.model.manager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.IModelProviderEvent;
import org.eclipse.pde.internal.core.IModelProviderListener;
import org.eclipse.pde.internal.core.ModelProviderEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.util.lookup.DefaultLibraryNamespaceFragment;
import com.iw.plugins.spindle.util.lookup.ILookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class TapestryProjectModelManager
  implements IResourceChangeListener, IResourceDeltaVisitor {

  static private final String EXTENSION_ID = "modelManagers";

  //  static public boolean updateMarkers = true;  

  protected HashMap models = null;
  protected List listeners = new ArrayList();
  protected List allModels = null;
  protected HashMap modelDelegates;

  private List modelChanges = null;
  private boolean startup = true;

  protected TapestryLibraryModel defaultLibrary = null;

  protected IProject project;

  class ModelChange {
    ITapestryModel model;
    boolean added;
    public ModelChange(ITapestryModel model, boolean added) {
      this.model = model;
      this.added = added;
    }
  }

  class ModelInfo {
    int count;
    ITapestryModel model;
    ITapestryModel readOnlyModel;
    Object consumer;
    public boolean isExclusiveAccess() {
      return true;
    }
  }
  protected boolean initialized;

  private String message = "Scanning for Tapestry files...";

  public TapestryProjectModelManager(IProject project) {
    super();
    this.project = project;

  }

  public TapestryProjectModelManager(IProject project, String message) {
    super();
    this.project = project;
    this.message = message;

  }

  public List checkForUnloadableModels() {

    ArrayList result = new ArrayList();

    List allModelClone = (List) ((ArrayList) allModels).clone();

    for (Iterator iter = allModelClone.iterator(); iter.hasNext();) {
      ITapestryModel element = (ITapestryModel) iter.next();

      if (!element.isLoaded()) {

        try {
          element.load();
        } catch (CoreException e) {

        }

        if (!element.isLoaded()) {
          result.add(element.getUnderlyingStorage());
        }

      }
    }

    return result;

  }

  public void addModelProviderListener(IModelProviderListener listener) {
    listeners.add(listener);
  }

  /**
   * Method buildModelDelegates.
   * @param modelDelegateExtensions
   */
  public void buildModelDelegates() {

    modelDelegates = new HashMap();

    IPluginRegistry registry = Platform.getPluginRegistry();
    IExtensionPoint point = registry.getExtensionPoint(TapestryPlugin.ID_PLUGIN, EXTENSION_ID);
    if (point != null) {

      IExtension[] extensions = point.getExtensions();
      System.out.println("Found " + extensions.length + " extensions");

      for (int i = 0; i < extensions.length; i++) {

        IConfigurationElement[] elements = extensions[i].getConfigurationElements();
        for (int j = 0; j < elements.length; j++) {

          try {
            if ("manager".equals(elements[j].getName())) {
              String extension = elements[j].getAttribute("file-extension");
              ITapestryModelManagerDelegate delegate =
                (ITapestryModelManagerDelegate) elements[j].createExecutableExtension("class");

              if (modelDelegates.containsKey(extension)) {
                throw new IllegalArgumentException(
                  extension + " is already registered with a Spindle Model Delegate");
              }

              modelDelegates.put(extension, delegate);
              delegate.registerParserFor(extension);
              TapestryPlugin.registerManagedExtension(extension);
            }
          } catch (CoreException e) {
            e.printStackTrace();
          }

        }
      }
      initializeProjectTapestryModels();

    }

  }

  private ITapestryModelManagerDelegate getDelegate(IStorage storage) {
    return getDelegate(extension(storage));
  }

  private ITapestryModelManagerDelegate getDelegate(ITapestryModel model) {
    return getDelegate(model.getUnderlyingStorage());
  }

  private ITapestryModelManagerDelegate getDelegate(String extension) {
    ITapestryModelManagerDelegate result = null;
    if (modelDelegates != null) {
      result = (ITapestryModelManagerDelegate) modelDelegates.get(extension);
    }

    return result;
  }

  private String extension(IStorage storage) {
    return storage.getFullPath().getFileExtension();
  }

  /**
   * Method getManagedExtensions.
   * @return Set
   */
  public List getManagedExtensions() {
    ArrayList list = new ArrayList();
    list.addAll(modelDelegates.keySet());
    return list;
  }

  protected void addModel(ITapestryModel model) {

    if (allModels == null) {
      initializeProjectTapestryModels();
    }

    if (!allModels.contains(model)) {

      allModels.add(model);

      ITapestryModelManagerDelegate delegate = getDelegate(model);
      if (delegate != null) {
        delegate.addModel(model);
      }

      ModelInfo info = new ModelInfo();
      info.count = 0;
      models.put(model.getUnderlyingStorage(), info);

      if (modelChanges != null)
        modelChanges.add(new ModelChange(model, true));

    }

  }

  public void connect(Object element, Object consumer) {
    this.connect(element, consumer, true);
  }

  public void connect(Object element, Object consumer, boolean editable) {
    initializeProjectTapestryModels();

    ModelInfo info = (ModelInfo) models.get(element);

    if (info == null) {
      info = new ModelInfo();
      info.count = 0;
      models.put(element, info);
    }

    info.count++;

    if (info.model != null && info.consumer != null && info.consumer != consumer) {

      verifyConsumer(info);

    }
    if (info.model == null && editable) {
      // create editable copy and register the exclusive owner
      info.model = createModel(element);
      info.consumer = consumer;

    } else {
      // editable model already created or not editable - use read only
      info.readOnlyModel = createModel(element, false);
    }
  }

  protected ITapestryModel createModel(Object element) {
    return createModel(element, true);
  }

  protected ITapestryModel createModel(Object element, boolean editable) {
    ITapestryModel result = null;

    if (element instanceof IStorage) {

      IStorage storage = (IStorage) element;

      String extension = extension(storage);

      ITapestryModelManagerDelegate delegate = getDelegate(storage);

      if (delegate != null) {
        result = delegate.createModel(storage);
      }

      if (result != null) {

        result.setEditable(editable && !storage.isReadOnly());

      }

    }
    return result;
  }

  public void disconnect(Object element, Object consumer) {
    ModelInfo info = (ModelInfo) models.get(element);

    if (info != null) {
      info.count--;

      if (info.consumer != null && info.consumer.equals(consumer)) {
        // editable copy can go
        info.model.dispose();
        info.model = null;

      }
      if (info.count == 0) {

        if (info.model != null) {
          info.model.dispose();
        }
        info.readOnlyModel = null;
      }
    }
  }

  private void fireModelProviderEvent(ModelProviderEvent event) {
    for (Iterator iter = listeners.iterator(); iter.hasNext();) {
      ((IModelProviderListener) iter.next()).modelsChanged(event);
    }
  }

  public void fireModelsChanged(ITapestryModel[] models) {
    ModelProviderEvent event =
      new ModelProviderEvent(this, IModelProviderEvent.MODELS_CHANGED, null, null, models);
    fireModelProviderEvent(event);
  }

  public boolean getAllEditableModelsUnused(Class modelClass) {

    for (Iterator iter = models.keySet().iterator(); iter.hasNext();) {

      ModelInfo info = (ModelInfo) models.get(iter.next());

      if (info.model != null && info.model.getClass().isInstance(modelClass)) {
        return false;
      }

    }
    return true;
  }

  public ITapestryModel getReadOnlyModel(Object element) {

    return getReadOnlyModel(element, false);

  }

  /**
   * will never return an editable model
   */
  public ITapestryModel getReadOnlyModel(Object element, boolean force) {

    initializeProjectTapestryModels();

    ModelInfo info = (ModelInfo) models.get(element);
    ITapestryModel result = null;

    if (info == null && force) {

      ITapestryModel possible = createModel(element);
      if (possible != null) {

        addModel(possible);
        info = (ModelInfo) models.get(element);

      }

    }

    if (info != null) {

      if (info.readOnlyModel == null) {
        info.readOnlyModel = createModel(element, false);
      }
      result = info.readOnlyModel;

    }

    if (result != null) {
      refreshModel(result, false);
    }

    return result;

  }

  /**
   * May return an editable model, if no consumer is already registered
   * If a consumer is registered, return a readonly copy
   */
  public ITapestryModel getEditableModel(Object element, Object consumer) {

    ModelInfo info = (ModelInfo) models.get(element);

    ITapestryModel result = null;
    boolean editable = true;

    if (info != null) {
      if (info.consumer != null && info.consumer.equals(consumer)) {

        if (info.model == null) {
          info.model = createModel(element, false);
        }
        result = info.model;

        if (result != null) {
          refreshModel(result, true);
        }

      } else {

        result = getReadOnlyModel(element);

      }
    }

    return result;

  }

  private void refreshModel(ITapestryModel model, boolean editable) {
    if (!project.isOpen()) {

      return;

    }
    IStorage storage = model.getUnderlyingStorage();
    if (storage instanceof IFile) {
      IFile file = (IFile) storage;
      if (!file.exists()) {
        return;
      }
      InputStream stream = null;
      boolean outOfSync = false;
      try {
        stream = file.getContents(false);
      } catch (CoreException e) {
        outOfSync = true;
      }
      if (outOfSync) {
        try {
          stream = file.getContents(true);
        } catch (CoreException e) {
          // cannot get file contents - something is 
          // seriously wrong

          return;
        }
      }
      if (outOfSync) {
        reloadModel(model);
        model.setEditable(editable && !file.isReadOnly());
      }
    }
  }

  private void handleFileDelta(IResourceDelta delta) {
    IFile file = (IFile) delta.getResource();
    if (isSupportedElement(file) == false)
      return;

    if (delta.getKind() == IResourceDelta.ADDED) {
      // manifest added - add the model
      ITapestryModel model = getWorkspaceModel(file);
      if (model == null) {
        model = createModel(file);
        addModel(model);
      } else {
        try {
          model.reload();
        } catch (CoreException e) {
        }
      }
    } else {

      ITapestryModel model = getWorkspaceModel(file);
      if (model != null) {
        if (delta.getKind() == IResourceDelta.REMOVED) {
          // manifest has been removed - ditch the model
          removeModel(model);

        } else if (delta.getKind() == IResourceDelta.CHANGED) {
          if ((IResourceDelta.CONTENT & delta.getFlags()) != 0) {
            reloadModel(model);

          }
        }
      }
    }
  }

  private void handleProjectClosing(IProject project) {
    if (!project.equals(this.project)) {

      return;

    }
    handleProjectToBeDeleted(project);
  }

  private void handleProjectDelta(IResourceDelta delta) {
    IProject project = (IProject) delta.getResource();

    if (!project.equals(this.project)) {

      return;

    }
    int kind = delta.getKind();

    if (project.isOpen() == false)
      return;

    if (kind == IResourceDelta.CHANGED && (delta.getFlags() | IResourceDelta.DESCRIPTION) != 0) {
      // Project description changed. Test if this
      // is now a Tapestry project and act
      try {
        if (!project.hasNature(TapestryPlugin.NATURE_ID)) {

          reset();
        }
      } catch (CoreException e) {
      }
    }
  }

  //  private void validateBinaryStatus(IProject project) {
  //    boolean shared = RepositoryProvider.getProvider(project) != null;
  //    if (shared) {
  //      try {
  //        String binary = project.getPersistentProperty(PDECore.EXTERNAL_PROJECT_PROPERTY);
  //        if (binary != null) {
  //          // The project contents has been replaced by
  //          // core - this is not a binary project any more
  //          project.setPersistentProperty(PDECore.EXTERNAL_PROJECT_PROPERTY, null);
  //        }
  //      } catch (CoreException e) {
  //        TapestryPlugin.getDefault().logException(e);
  //      }
  //    }
  //  }

  //  private void handleProjectToBeDeleted(IProject project) {
  //    if (!isPluginProject(project) && !isFeatureProject(project)) {
  //      return;
  //    }
  //    ITapestryModel model = getWorkspaceModel(project);
  //    if (model != null) {
  //      removeWorkspaceModel(model);
  //    }
  //  }
  private void handleResourceDelta(IResourceDelta delta) {
    try {
      delta.accept(this);
    } catch (CoreException e) {
      TapestryPlugin.getDefault().logException(e);
    }
  }
  protected void initializeProjectTapestryModels() {
    if (initialized) {
      return;
    }
    allModels = new ArrayList();
    models = new HashMap();
    if (project.isOpen()) {

      Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();

      if (shell != null && shell.getVisible()) {

        IWorkbenchWindow window = TapestryPlugin.getDefault().getActiveWorkbenchWindow();

        try {

          window.run(false, false, new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor)
              throws InvocationTargetException, InterruptedException {

              populateAllModels(project, TapestryLookup.ACCEPT_ANY, monitor);
            }

          });

        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }

      } else {

        populateAllModels(project, TapestryLookup.ACCEPT_ANY, new NullProgressMonitor());
      }

    }
    IWorkspace workspace = TapestryPlugin.getDefault().getWorkspace();

    workspace.addResourceChangeListener(
      this,
      IResourceChangeEvent.PRE_CLOSE
        | IResourceChangeEvent.PRE_DELETE
        | IResourceChangeEvent.PRE_AUTO_BUILD);
    initialized = true;
  }

  protected void populateAllModels(IProject project, int acceptFlags, IProgressMonitor monitor) {

    Collector lookupCollector = null;

    try {
      lookupCollector = new Collector();
      IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(project);
      TapestryLookup lookup = new TapestryLookup();
      lookup.configure(jproject);

      lookup.findAllManaged("*", true, lookupCollector, acceptFlags);
    } catch (CoreException jmex) {
      jmex.printStackTrace();
    }

    if (lookupCollector == null) {
      return;
    }

    List foundList = lookupCollector.getResults();

    monitor.beginTask(message, foundList.size());

    Iterator foundElements = foundList.iterator();
    while (foundElements.hasNext()) {

      monitor.worked(1);
      IStorage storage = (IStorage) foundElements.next();
      if (models.containsKey(storage)) {
      	
      	continue;
      }
      ITapestryModel model = createModel(storage);
      if (model != null ) {

        addModel(model);

        if (storage.getName().equals("Framework.library")) {

          defaultLibrary = (TapestryLibraryModel) model;
          if (DefaultLibraryNamespaceFragment.getInstance() == null) {

            try {
              DefaultLibraryNamespaceFragment.getInstance(defaultLibrary.getSpecification());
            } catch (CoreException e) {

              e.printStackTrace();
            }

          }
        }
      }

    }
    monitor.done();
  }

  public class Collector implements ILookupRequestor {
    ArrayList result;
    public boolean isCancelled() {
      return false;
    }

    public boolean accept(IStorage storage, IPackageFragment pkg) {
      if (result == null) {
        result = new ArrayList();
      }
      result.add(storage);
      return true;
    }

    public List getResults() {
      return result == null ? new ArrayList() : result;
    }
  }

  public static boolean isJavaPluginProject(IProject project) {
    try {
      if (!project.hasNature(JavaCore.NATURE_ID))
        return false;
    } catch (CoreException e) {
      TapestryPlugin.getDefault().logException(e);
      return false;
    }
    return true;
  }

  private void ensureModelExists(IStorage element) {
    if (!initialized)
      return;
    ITapestryModel model = getWorkspaceModel(element);
    if (model == null) {
      model = createModel(element);
      if (model != null) {
        addModel(model);
      }
    }
  }

  private boolean isSupportedElement(IStorage storage) {

    IJavaProject jproject = null;

    try {

      jproject = TapestryPlugin.getDefault().getJavaProjectFor(storage);

    } catch (CoreException e) {
    }

    if (jproject == null) {

      return false;
    }

    if (!jproject.getProject().equals(project)) {

      return false;

    }

    String extension = extension(storage);
    return modelDelegates.containsKey(extension);
  }

  private void loadModel(ITapestryModel model) {
    IStorage storage = model.getUnderlyingStorage();
    InputStream stream = null;
    try {
      if (storage instanceof IFile) {
        IFile file = (IFile) storage;

        boolean outOfSync = false;
        try {
          stream = file.getContents(false);
        } catch (CoreException e) {
          outOfSync = true;
        }
        if (outOfSync) {
          try {
            stream = file.getContents(true);
          } catch (CoreException e) {
            // cannot get file contents - something is 
            // seriously wrong

            TapestryPlugin.getDefault().logException(e);

            return;
          }
        }
      } else {
        stream = storage.getContents();
      }

      BaseTapestryModel bmodel = (BaseTapestryModel) model;
      bmodel.load(stream);
      stream.close();
    } catch (CoreException e) {
      // errors in loading, but we will still
      // initialize.
    } catch (IOException e) {
      TapestryPlugin.getDefault().logException(e);
    }
  }

  private void reloadModel(ITapestryModel model) {
    loadModel(model);
    fireModelsChanged(new ITapestryModel[] { model });
  }

  public void removeModelProviderListener(IModelProviderListener listener) {
    listeners.remove(listener);
  }

  private void removeModel(ITapestryModel nuked) {

    if (nuked == null) {
      return;
    }
    if (allModels != null) {
      allModels.remove(nuked);
    }
    ITapestryModelManagerDelegate delegate = getDelegate(nuked);
    if (delegate != null) {
      delegate.removeModel(nuked);
    }
    if (modelChanges != null)
      modelChanges.add(new ModelChange(nuked, false));
    // disconnect
    Object element = nuked.getUnderlyingStorage();
    models.remove(element);
    nuked.dispose();
  }
  public void reset() {
    initialized = false;
    initializeProjectTapestryModels();
  }

  public void resourceChanged(IResourceChangeEvent event) {
    // No need to do anything if nobody has the models
    if (allModels == null) {
      return;
    }
    
    IPath thisProjectPath = getProject().getFullPath();
    IResourceDelta topLevelDelta = event.getDelta();
    IResourceDelta thisProjectDelta = topLevelDelta.findMember(thisProjectPath);
    if (thisProjectDelta == null) {
    	
    	// the event does not pertain to this project!
    	return;
    	
    }
    

    switch (event.getType()) {
      case IResourceChangeEvent.PRE_AUTO_BUILD :
      
 		IPath classpath = thisProjectPath.append(".classpath");
 		IResourceDelta classpathDelta = topLevelDelta.findMember(classpath);
 		
 		if (classpathDelta != null && classpathChanged(classpathDelta)) {
 			
 			reset();
 			return;
 			
 		}
 		
        if (modelChanges == null)
          modelChanges = new ArrayList();
        handleResourceDelta(event.getDelta());
        processModelChanges();
        break;
      case IResourceChangeEvent.PRE_CLOSE :
        if (modelChanges == null)
          modelChanges = new ArrayList();
        // project about to close
        handleProjectClosing((IProject) event.getResource());
        processModelChanges();
        break;
      case IResourceChangeEvent.PRE_DELETE :
        // project about to be deleted
        if (modelChanges == null)
          modelChanges = new ArrayList();
        handleProjectToBeDeleted((IProject) event.getResource());
        processModelChanges();
        break;
    }
  }

  /**
   * Method classpathChanged.
   * @param classpathDelta
   * @return boolean
   */
  private boolean classpathChanged(IResourceDelta classpathDelta) {
  	int kind = classpathDelta.getKind();
  	int flags = classpathDelta.getFlags();
    return kind == IResourceDelta.CHANGED && (flags & IResourceDelta.CONTENT) != 0;
  }


  private void processModelChanges() {
    if (modelChanges.size() == 0) {
      modelChanges = null;
      return;
    }

    ArrayList added = new ArrayList();
    ArrayList removed = new ArrayList();
    for (int i = 0; i < modelChanges.size(); i++) {
      ModelChange change = (ModelChange) modelChanges.get(i);
      if (change.added)
        added.add(change.model);
      else
        removed.add(change.model);
    }
    ITapestryModel[] addedArray =
      added.size() > 0
        ? (ITapestryModel[]) added.toArray(new ITapestryModel[added.size()])
        : (ITapestryModel[]) null;
    ITapestryModel[] removedArray =
      removed.size() > 0
        ? (ITapestryModel[]) removed.toArray(new ITapestryModel[removed.size()])
        : (ITapestryModel[]) null;
    int type = 0;
    if (addedArray != null)
      type |= IModelProviderEvent.MODELS_ADDED;
    if (removedArray != null)
      type |= IModelProviderEvent.MODELS_REMOVED;
    modelChanges = null;
    if (type != 0) {
      final ModelProviderEvent event =
        new ModelProviderEvent(this, type, addedArray, removedArray, null);
      fireModelProviderEvent(event);
    }
  }

  public void shutdown() {
    if (!initialized)
      return;
    IWorkspace workspace = TapestryPlugin.getDefault().getWorkspace();
    workspace.removeResourceChangeListener(this);
    for (Iterator iter = models.values().iterator(); iter.hasNext();) {
      ModelInfo info = (ModelInfo) iter.next();
      if (info.model != null)
        info.model.dispose();
      if (info.readOnlyModel != null)
        info.readOnlyModel.dispose();
      info = null;
    }
    models.clear();
    allModels = null;
    initialized = false;
  }
  private void verifyConsumer(ModelInfo info) {
    //		Object consumer = info.consumer;
    //		if (consumer instanceof PDEMultiPageEditor) {
    //			PDEMultiPageEditor editor = (PDEMultiPageEditor) consumer;
    //			if (isEditorOpened(editor) == false) { // stale reference
    //				info.consumer = null;
    //				info.model.dispose();
    //				info.model = null;
    //			}
    //		}
  }
  public boolean visit(IResourceDelta delta) throws CoreException {
    if (delta != null) {
      IResource resource = delta.getResource();
      if (resource instanceof IProject) {
        handleProjectDelta(delta);
        IProject project = (IProject) resource;
        return (isJavaPluginProject(project));
      } else if (resource instanceof IFile) {
        handleFileDelta(delta);
      }
    }
    return true;
  }

  private void validate() {
    // let's be paranoid - see if the underlying resources
    // are still valid
    if (allModels != null) {
      validate(allModels);
    }

    for (Iterator iter = modelDelegates.keySet().iterator(); iter.hasNext();) {
      ITapestryModelManagerDelegate mgr =
        (ITapestryModelManagerDelegate) getDelegate((String) iter.next());
      validate(mgr.getAllModels());
    }

  }
  private void validate(List models) {
    Object[] entries = models.toArray();
    for (int i = 0; i < entries.length; i++) {
      ITapestryModel model = (ITapestryModel) entries[i];
      if (!isValid(model)) {
        // drop it
        models.remove(model);
        allModels.remove(model);
      }
    }
  }
  private boolean isValid(ITapestryModel model) {
    IStorage storage = model.getUnderlyingStorage();
    // Must have one
    if (storage == null)
      return false;
    if (storage instanceof IResource) {
      IResource resource = (IResource) storage;
      // Must have a resource handle that exists
      if (resource.exists() == false)
        return false;
      // The project must not be closed
      IProject project = resource.getProject();
      if (project == null)
        return false;
      if (project.isOpen() == false)
        return false;

      //      if (!project.equals(this.project))
      //        return false;
      return hasRootObject(model);
    }
    return true;
  }
  private boolean hasRootObject(ITapestryModel model) {
    //    if (model instanceof IPluginModelBase)
    //      return hasRootObject((IPluginModelBase) model);
    //    if (model instanceof IFeatureModel)
    //      return hasRootObject((IFeatureModel) model);
    //    return false;
    return true;
  }

  private void handleProjectToBeDeleted(IProject project) {
    if (!project.equals(this.project)) {
      return;
    }
    reset();
  }

  private ITapestryModel getWorkspaceModel(IStorage storage) {
    String extension = extension(storage);
    List models = null;
    validate();

    ITapestryModelManagerDelegate delegate = getDelegate(storage);
    if (delegate == null) {
      return null;
    }
    models = delegate.getAllModels();
    return getWorkspaceModel(storage, models);
  }

  private ITapestryModel getWorkspaceModel(IStorage storage, List models) {
    if (models == null) {
      return null;
    }

    for (int i = 0; i < models.size(); i++) {
      ITapestryModel model = (ITapestryModel) models.get(i);
      IStorage foundStorage = model.getUnderlyingStorage();
      if (foundStorage == storage) {
        return model;
      }
    }
    return null;
  }

  /**
   * @return a clone of the models list
   */
  public List getAllModels(Object element) {
    initializeProjectTapestryModels();
    return (List) ((ArrayList) allModels).clone();
  }

  public List getAllModels(Object element, String extension) {
    initializeProjectTapestryModels();
    ITapestryModelManagerDelegate delegate = getDelegate(extension);
    if (delegate != null) {
      return delegate.getAllModels();
    }
    return null;
  }

  /**
   * Method getModelsFor
   * @param appResources
   * @return an array of models found for the parameter IStorages
   */
  public ArrayList getModelListFor(List storages) {
    initializeProjectTapestryModels();
    ArrayList result = new ArrayList();
    for (int i = 0; i < storages.size(); i++) {
      Object element = storages.get(i);
      ModelInfo info = (ModelInfo) models.get(element);

      if (info == null) {
        info = new ModelInfo();
        info.count = 0;
        models.put(element, info);
      }
      if (info.readOnlyModel == null) {
        info.readOnlyModel = createModel(element, false);
      }

      result.add(info.readOnlyModel);
    }
    return result;
  }

  /**
   * Method getModelListFor.
   * @param storages
   * @return ArrayList
   */
  public ITapestryModel[] getModelsFor(IStorage[] storages) {
    initializeProjectTapestryModels();
    List models = getModelListFor(Arrays.asList(storages));
    return (ITapestryModel[]) models.toArray(new ITapestryModel[models.size()]);
  }

  /**
   * Method getModelListFor.
   * @param storages
   * @return ArrayList
   */
  public ITapestryModel[] getModelsFor(List storages) {
    initializeProjectTapestryModels();
    List models = getModelListFor(storages);
    return (ITapestryModel[]) models.toArray(new ITapestryModel[models.size()]);
  }

  public TapestryLibraryModel getDefaultLibrary() {

    return defaultLibrary;

  }

  public void setDefaultLibrary(TapestryLibraryModel model) {

    defaultLibrary = model;

  }

  /**
   * @see org.eclipse.pde.core.IModelChangedListener#modelChanged(IModelChangedEvent)
   */
  public void modelChanged(IModelChangedEvent event) {
  }

  public List getAllPageModelsDefinedIn(TapestryLibraryModel model, TapestryLookup lookup) {

    ArrayList result = new ArrayList();

    IPluginLibrarySpecification libSpec = model.getSpecification();

    for (Iterator iter = libSpec.getPageNames().iterator(); iter.hasNext();) {
      String alias = (String) iter.next();

      String path = libSpec.getPageSpecificationPath(alias);

      IStorage[] found = lookup.findComponent(path);

      if (found == null || found.length == 0) {

        found = lookup.findPage(path);

      }

      if (found != null && found.length > 0) {

        ITapestryModel foundModel = getReadOnlyModel(found[0]);
        if (foundModel != null) {

          result.add(foundModel);
        }
      }

    }

    return result;
  }

  public List getAllComponentModelsDefinedIn(TapestryLibraryModel model, TapestryLookup lookup) {

    ArrayList result = new ArrayList();
    IPluginLibrarySpecification libSpec = model.getSpecification();
    for (Iterator iter = libSpec.getComponentAliases().iterator(); iter.hasNext();) {
      String alias = (String) iter.next();
      IStorage[] found = lookup.findComponent(libSpec.getComponentSpecificationPath(alias));
      if (found != null && found.length > 0) {

        ITapestryModel foundModel = getReadOnlyModel(found[0]);
        if (foundModel != null && !result.contains(foundModel)) {

          result.add(foundModel);
        }
      }
    }

    return result;
  }

  /**
   * Returns the project.
   * @return IProject
   */
  public IProject getProject() {
    return project;
  }

}
