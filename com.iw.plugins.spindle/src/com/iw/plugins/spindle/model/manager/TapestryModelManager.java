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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.tapestry.util.xml.AbstractDocumentParser;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.core.IModelProvider;
import org.eclipse.pde.internal.core.IModelProviderListener;
import org.eclipse.pde.internal.core.ModelProviderEvent;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

public class TapestryModelManager implements IModelProvider, IModelChangedListener {

  static private final String EXTENSION_ID = "modelManagers";

  ArrayList providerListeners;
  HashMap allModels;
  HashMap modelDelegates;
  HashMap parsers = new HashMap();

  /**
   * Constructor for TapestryModelManager
   */
  public TapestryModelManager() {
    super();
    providerListeners = new ArrayList(11);
    Category CAT = Category.getInstance(AbstractDocumentParser.class);
    CAT.setPriority(Priority.DEBUG);
    CAT.addAppender(new ConsoleAppender(new PatternLayout("%c{1} [%p] %m%n"), "System.out"));

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
            }
          } catch (CoreException e) {
          }

        }
      }
    }

  }

  public AbstractDocumentParser getParserFor(String extension) {
    return (AbstractDocumentParser) parsers.get(extension);
  }

  public void registerParser(String extension, AbstractDocumentParser parser) {
    parsers.put(extension, parser);
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
   * @see IModelProvider#connect(Object, Object)
   */
  public void connect(Object element, Object consumer) {
    connect(element, consumer, true);
  }

  public void addModel(ITapestryModel model) {
    checkPopulated(model);
    if (!allModels.containsKey(model.getUnderlyingStorage())) {
      allModels.put(model.getUnderlyingStorage(), model);
      ITapestryModelManagerDelegate delegate = getDelegate(model);
      if (delegate != null) {
        delegate.addModel(model);
      }
      fireModelProviderEvent(ModelProviderEvent.MODELS_ADDED, (IModel) model);
    }
  }

  public ITapestryModel getModel(Object element) {
    checkPopulated(element);
    ITapestryModel model = (ITapestryModel) allModels.get(element);
    if (model == null) {
      model = createModel(element, true);
      if (model != null) {
        addModel(model);
      }
    }

    return model;
  }

  private void checkPopulated(Object element) {
    if (allModels == null) {
      populateAllModels(element);
    }
  }

  public void modelChanged(IModelChangedEvent event) {
    if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
      Object[] changed = event.getChangedObjects();
      if (changed.length > 0 && changed[0] instanceof BaseTapestryModel) {
        ((BaseTapestryModel) changed[0]).setDirty(true);
      }
    }
  }

  public void removeModel(ITapestryModel model) {
    checkPopulated(model);
    Object element = model.getUnderlyingStorage();
    IModel nuked = (IModel) allModels.get(element);
    if (nuked == null) {
      return;
    }
    allModels.remove(element);
    ((BaseTapestryModel) model).removeModelChangedListener(this);
    ITapestryModelManagerDelegate delegate = getDelegate(model);
    if (delegate != null) {
      delegate.removeModel(nuked);
    }
    fireModelProviderEvent(ModelProviderEvent.MODELS_REMOVED, nuked);
  }

  // hmm, might not need this!
  protected void populateAllModels(Object element) {
    IStorage storage = null;
    if (element instanceof ITapestryModel) {
      storage = ((ITapestryModel) element).getUnderlyingStorage();
    } else {
      storage = (IStorage) element;
    }

    TapestryPlugin.getDefault().getWorkspace().getRoot().getProjects();
    Collector lookupCollector = null;

    try {
      lookupCollector = new Collector();
      IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(storage);
      TapestryLookup lookup = new TapestryLookup();
      lookup.configure(jproject);

      lookup.findAllManaged("*", true, lookupCollector);
    } catch (JavaModelException jmex) {
      jmex.printStackTrace();
    }

    if (lookupCollector == null) {
      return;
    }
    allModels = new HashMap();

    Iterator foundElements = lookupCollector.getResults().iterator();
    while (foundElements.hasNext()) {
      ITapestryModel model = createModel(foundElements.next());
      if (model != null) {
        addModel(model);
      }
    }
    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(
      new ResourceChangedAdapter());
    //    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(
    //      new MyResourceChangeReporter());
  }

  // this method is 'modeled' (ha ha) after the connect() method in org.eclipse.pde.internal.WorkspaceModelManager
  // I have not decided yet if I like this. If I decide I don't, I will replace it.
  /**
   * @see IModelProvider#connect(Object, Object, boolean)
   */
  public void connect(Object element, Object consumer, boolean editableCopy) {
    /*
    ModelHolder holder = (ModelHolder) allModels.get(element);
    if (holder == null) {
      holder = new ModelHolder();
      holder.consumerCount = 0;
      allModels.put(element, holder);
    }
    holder.consumerCount++;
    if (holder.model != null && holder.editableConsumer != null) {
      if (holder.editableConsumer != consumer)
        verifyConsumer(holder);
    }
    if (holder.model == null && editableCopy) {
      // create editable copy and register the exclusive owner
      holder.model = createModel(element);
      holder.editableConsumer = consumer;
    } else {
      // editable model already created or not editable - use read only
      holder.readOnlyModel = createModel(element, false);
    }
    return;
    */
  }

  private void verifyConsumer(ModelHolder info) {
  }

  // this method is 'modeled' (ha ha) after the disconnect() method in org.eclipse.pde.internal.WorkspaceModelManager
  // I have not decided yet if I like this. If I decide I don't, I will replace it.
  /**
   * @see IModelProvider#disconnect(Object, Object)
   */
  public void disconnect(Object element, Object consumer) {
    /*
    ModelHolder holder = (ModelHolder) allModels.get(element);
    if (holder == null) {
      return;
    }
    if (holder.model != null) {
    	holder.consumerCount--;
      if (consumer == holder.editableConsumer) {
        holder.model.dispose();
        holder.model = null;
        holder.editableConsumer = null;
      } 
      if (holder.consumerCount == 0) {
      	if (holder.model != null) {
      		holder.model.dispose();
      	} 
      	if (holder.readOnlyModel != null) {
      		holder.readOnlyModel.dispose();
      	}
      	allModels.remove(holder);
      }      	
    }
    */

  }

  /**
   * @see IModelProvider#getModel(Object, Object)
   */
  public IModel getModel(Object element, Object consumer) {
    return (IModel) getModel(element);
  }

  protected ITapestryModel createModel(Object element) {
    return createModel(element, true);
  }

  protected ITapestryModel createModel(Object element, boolean editable) {
    BaseTapestryModel result = null;

    if (element instanceof IStorage) {

      IStorage storage = (IStorage) element;

      String extension = extension(storage);

      ITapestryModelManagerDelegate delegate = getDelegate(storage);

      if (delegate != null) {
        result = delegate.createModel(storage);
      }

      if (result != null) {

        result.addModelChangedListener(this);
      }

    }
    return result;
  }

  private void fireModelProviderEvent(int type, IModel model) {
    ModelProviderEvent event = null;

    switch (type) {
      case ModelProviderEvent.MODELS_ADDED :
        event =
          new ModelProviderEvent(this, type, new IModel[] { model }, new IModel[0], new IModel[0]);
        break;
      case ModelProviderEvent.MODELS_REMOVED :
        event =
          new ModelProviderEvent(this, type, new IModel[0], new IModel[] { model }, new IModel[0]);
        break;
      case ModelProviderEvent.MODELS_CHANGED :
        event =
          new ModelProviderEvent(this, type, new IModel[0], new IModel[0], new IModel[] { model });
        break;
    }
    Iterator i = providerListeners.iterator();
    while (i.hasNext()) {
      IModelProviderListener listener = (IModelProviderListener) i.next();
      listener.modelsChanged(event);
    }
  }

  //  public TapestryApplicationModel getFirstApplicationModel() {
  //    TapestryApplicationModel result = null;
  //    Iterator iter = applicationModels.iterator();
  //    while (iter.hasNext()) {
  //      result = (TapestryApplicationModel) iter.next();
  //      if (!result.isLoaded()) {
  //        try {
  //          result.load();
  //        } catch (Exception e) {
  //          continue;
  //        }
  //      } else {
  //        break;
  //      }
  //    }
  //    return result;
  //  }

  public ITapestryModel getFirstLoadedModel(String extension) {
    ITapestryModelManagerDelegate delegate = getDelegate(extension);
    if (delegate != null) {
      return delegate.getFirstLoadedModel();
    }
    return null;
  }

  public Set getAllModels(Object element) {
    checkPopulated(element);
    return Collections.unmodifiableSet(allModels.entrySet());
  }

  public List getAllModels(Object element, String extension) {
    checkPopulated(element);
    ITapestryModelManagerDelegate delegate = getDelegate(extension);
    if (delegate != null) {
      return delegate.getAllModels();
    }
    return null;
  }

  /**
   * @see IModelProvider#addModelProviderListener(IModelProviderListener)
   */
  public void addModelProviderListener(IModelProviderListener listener) {
    if (!providerListeners.contains(listener)) {
      providerListeners.add(listener);
    }
  }

  /**
  * @see IModelProvider#removeModelProviderListener(IModelProviderListener)
  */
  public void removeModelProviderListener(IModelProviderListener listener) {
    providerListeners.remove(listener);
  }

  private void init() {
    TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(
      new ResourceChangedAdapter());
  }

  // Adapter added as a listener to the workspace.
  class ResourceChangedAdapter implements IResourceChangeListener, IResourceDeltaVisitor {
    // IResourceChangeListener
    public void resourceChanged(IResourceChangeEvent event) {
      int eventType = event.getType();
      if (eventType == event.PRE_CLOSE) {
        handleProjectIsClosing(event);
      } else if (eventType == event.PRE_DELETE) {
        handleProjectIsBeingDeleted(event);
      } else if (eventType == event.POST_CHANGE) {
        try {
          event.getDelta().accept(this);
        } catch (CoreException ex) { // put this for now
          TapestryPlugin.getDefault().logException(ex);
        }
      }
    }

    private void handleProjectIsClosing(IResourceChangeEvent event) {
    }

    private void handleProjectIsBeingDeleted(IResourceChangeEvent event) {
      IProject project = (IProject) event.getResource();
      IJavaProject jproject = JavaCore.create(project);
      Collector lookupCollector = null;
      try {
        lookupCollector = new Collector();
        TapestryLookup lookup = new TapestryLookup();
        lookup.configure(jproject);

        lookup.findAll(
          "*",
          true,
          TapestryLookup.ACCEPT_APPLICATIONS | TapestryLookup.ACCEPT_COMPONENTS,
          lookupCollector);
      } catch (JavaModelException jmex) {
        jmex.printStackTrace();
      }

      if (lookupCollector == null) {
        return;
      }
      Iterator foundlings = lookupCollector.getResults().iterator();
      while (foundlings.hasNext()) {
        ITapestryModel model = getModel((IStorage) foundlings.next());
        if (model == null) {
          continue;
        }
        removeModel(model);
      }
    }

    // IResourceDeltaVisitor
    public boolean visit(IResourceDelta delta) {
      if (delta != null) {
        handleFileDelta(delta);
      }
      return true;
    }

    private void handleFileDelta(IResourceDelta delta) {
      IResource res = delta.getResource();
      ITapestryModel imodel = getModel(res);
      if (imodel == null) {
        return;
      }
      BaseTapestryModel bmodel = null;
      switch (delta.getKind()) {
        case IResourceDelta.ADDED :
          //System.out.print("Resource ");
          //System.out.print(res.getFullPath());
          //System.out.println(" was added.");
          bmodel = (BaseTapestryModel) imodel;
          if (bmodel != null) {
            if (!bmodel.isLoaded() && bmodel.findProblemMarkers().length == 0) {
              try {
                bmodel.load();
              } catch (CoreException e) {
                //do nothing
              }
            }
          }
          break;
        case IResourceDelta.REMOVED :
          //System.out.print(" Resource ");
          //System.out.print(res.getFullPath());
          //System.out.println(" was removed.");
          if (imodel != null) {
            removeModel(imodel);
          }
          break;
        case IResourceDelta.CHANGED :
          //System.out.print("Resource ");
          //System.out.print(delta.getFullPath());
          //System.out.println(" has changed.");
          switch (delta.getFlags()) {
            case IResourceDelta.CONTENT :
              //System.out.println("--> Content Change");
              break;
            case IResourceDelta.REPLACED :
              //System.out.println("--> Content Replaced");
              bmodel = (BaseTapestryModel) imodel;
              try {
                bmodel.reload();
              } catch (CoreException e) {
                TapestryPlugin.getDefault().logException(e);
              }
              break;
            case IResourceDelta.REMOVED :
              //System.out.println("--> Removed");
              break;
            case IResourceDelta.MARKERS :
              //System.out.println("--> Marker Change");
              IMarkerDelta[] markers = delta.getMarkerDeltas();
              // if interested in markers, check these deltas
              break;
          }
      }
    }

  }

  // this class is 'modeled' (ha ha) after the ModelInfo inner class in org.eclipse.pde.internal.WorkspaceModelManager
  // I have not decided yet if I like this. If I decide I don't, I will replace it.
  class ModelHolder {
    int consumerCount;
    IModel model;
    IModel readOnlyModel;
    Object editableConsumer;
  }

  class Collector implements ITapestryLookupRequestor {
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

  /**
   * Method getModelsFor
   * @param appResources
   * @return an array of models found for the parameter IStorages
   */
  public ArrayList getModelListFor(List storages) {
    ArrayList result = new ArrayList();
    for (int i = 0; i < storages.size(); i++) {
      ITapestryModel foundModel = getModel(storages.get(i));
      if (foundModel != null) {
        result.add(foundModel);
      }
    }
    return result;
  }

  /**
   * Method getModelListFor.
   * @param storages
   * @return ArrayList
   */
  public ITapestryModel[] getModelsFor(IStorage[] storages) {
    List models = getModelListFor(Arrays.asList(storages));
    return (ITapestryModel[]) models.toArray(new ITapestryModel[models.size()]);
  }

  /**
   * Method getModelListFor.
   * @param storages
   * @return ArrayList
   */
  public ITapestryModel[] getModelsFor(List storages) {
    List models = getModelListFor(storages);
    return (ITapestryModel[]) models.toArray(new ITapestryModel[models.size()]);
  }

  /**
   * Method getManagedExtensions.
   * @return Set
   */
  public Set getManagedExtensions() {
    return modelDelegates.keySet();
  }

}