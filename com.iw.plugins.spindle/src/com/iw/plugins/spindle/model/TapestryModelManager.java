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
package com.iw.plugins.spindle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.core.IModelProvider;
import org.eclipse.pde.internal.core.IModelProviderListener;
import org.eclipse.pde.internal.core.ModelProviderEvent;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.TapestryLookup;
import com.primix.tapestry.util.xml.AbstractDocumentParser;

public class TapestryModelManager implements IModelProvider, IModelChangedListener {

	ArrayList providerListeners;
	HashMap allModels = new HashMap();
	ArrayList applicationModels;
	ArrayList componentModels;
	HashMap jarProjectsMap;

	/**
	 * Constructor for TapestryModelManager
	 */
	public TapestryModelManager() {
		super();
		providerListeners = new ArrayList(50);
		//allModels = new HashMap();
		Category CAT = Category.getInstance(AbstractDocumentParser.class);
		CAT.setPriority(Priority.DEBUG);
		CAT.addAppender(new ConsoleAppender(new PatternLayout("%c{1} [%p] %m%n"), "System.out"));

	}

	public List getAllComponents(Object element) {

		if (allModels == null) {
			if (element == null) {
				return Collections.EMPTY_LIST;
			} else {
				populateAllModels(element);
			}
		}
		return Collections.unmodifiableList(componentModels);
	}

	public List getAllApplications(Object element) {
		if (allModels == null) {
			if (element == null) {
				return Collections.EMPTY_LIST;
			} else {
				populateAllModels(element);
			}
		}
		return Collections.unmodifiableList(applicationModels);
	}

	public Iterator componentsIterator() {
		return (Collections.unmodifiableList(componentModels)).iterator();
	}

	public Iterator applicationsIterator() {
		return (Collections.unmodifiableList(applicationModels)).iterator();
	}

	/**
	 * @see IModelProvider#connect(Object, Object)
	 */
	public void connect(Object element, Object consumer) {
		connect(element, consumer, true);
	}

	public void addModel(ITapestryModel model) {
		if (applicationModels == null) {
			populateAllModels(model);
		}
		if (model instanceof TapestryApplicationModel && !applicationModels.contains(model)) {
			applicationModels.add(model);
		} else if (model instanceof TapestryComponentModel && !componentModels.contains(model)) {
			componentModels.add(model);
		}
		allModels.put(model.getUnderlyingStorage(), model);
		fireModelProviderEvent(ModelProviderEvent.MODELS_ADDED, (IModel) model);
	}

	public ITapestryModel getModel(Object element) {
		if (applicationModels == null) {
			populateAllModels(element);
		}
		ITapestryModel model = (ITapestryModel) allModels.get(element);
		if (model == null) {
			model = createModel(element, true);
			if (model != null) {
				addModel(model);
			}
		}

		return model;
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
		Object element = model.getUnderlyingStorage();
		IModel nuked = (IModel) allModels.get(element);
		if (nuked == null) {
			return;
		}
		if (nuked instanceof TapestryApplicationModel && applicationModels.contains(model)) {
			applicationModels.remove(nuked);
		} else if (model instanceof TapestryApplicationModel && componentModels.contains(model)) {
			componentModels.remove(nuked);
		}
		allModels.remove(element);
		((BaseTapestryModel) model).removeModelChangedListener(this);
		fireModelProviderEvent(ModelProviderEvent.MODELS_REMOVED, nuked);
	}

	public void removeComponentModel(TapestryComponentModel model) {
		componentModels.remove(model);
		fireModelProviderEvent(ModelProviderEvent.MODELS_REMOVED, (IModel) model);
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
		applicationModels = new ArrayList();
		componentModels = new ArrayList();

		Iterator foundElements = lookupCollector.getResults().iterator();
		while (foundElements.hasNext()) {
			ITapestryModel model = createModel(foundElements.next());
			if (model != null) {
				try {
					((BaseTapestryModel) model).load();
				} catch (CoreException e) {
				}
				addModel(model);
			}
		}
		TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(new ResourceChangedAdapter());
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
			String name = storage.getName();
			if (name.endsWith(".application")) {
				TapestryApplicationModel model = new TapestryApplicationModel(storage);
				//model.setEditable(editable);
				result = model;
			} else if (name.endsWith(".jwc")) {
				TapestryComponentModel model = new TapestryComponentModel(storage);
				//model.setEditable(editable);
				result = model;
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
				event = new ModelProviderEvent(this, type, new IModel[] { model }, new IModel[0], new IModel[0]);
				break;
			case ModelProviderEvent.MODELS_REMOVED :
				event = new ModelProviderEvent(this, type, new IModel[0], new IModel[] { model }, new IModel[0]);
				break;
			case ModelProviderEvent.MODELS_CHANGED :
				event = new ModelProviderEvent(this, type, new IModel[0], new IModel[0], new IModel[] { model });
				break;
		}
		Iterator i = providerListeners.iterator();
		while (i.hasNext()) {
			IModelProviderListener listener = (IModelProviderListener) i.next();
			listener.modelsChanged(event);
		}
	}

	public TapestryApplicationModel getFirstApplicationModel() {
		TapestryApplicationModel result = null;
		Iterator iter = applicationModels.iterator();
		while (iter.hasNext()) {
			result = (TapestryApplicationModel) iter.next();
			if (!result.isLoaded()) {
				try {
					result.load();
				} catch (Exception e) {
					continue;
				}
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * the model called root is used only to figure out which project to search
	 */
	public TapestryComponentModel findComponent(String specificationPath, ITapestryModel root) {
		Assert.isNotNull(specificationPath);
		Assert.isNotNull(root);
		if (!specificationPath.endsWith(".jwc")) {
			return null;
		}
		TapestryLookup lookup = new TapestryLookup();
		try {
			IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(root.getUnderlyingStorage());
			lookup.configure(jproject);
			IStorage[] results = lookup.findComponent(specificationPath);
			if (results.length == 0) {
				return null;
			}
			return (TapestryComponentModel) getModel(results[0]);
		} catch (JavaModelException jmex) {
			return null;
		}
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
		TapestryPlugin.getDefault().getWorkspace().addResourceChangeListener(new ResourceChangedAdapter());
	}

	public TapestryComponentModel findComponentWithHTML(IStorage storage) {
		if (applicationModels == null) {
			populateAllModels(storage);
		}
		if (componentModels != null && !componentModels.isEmpty()) {
			IPath htmlPath = storage.getFullPath().removeFileExtension();
			IPath jwcPath = new Path(htmlPath.toString() + ".jwc");
			Iterator iter = componentModels.iterator();
			while (iter.hasNext()) {
				TapestryComponentModel model = (TapestryComponentModel) iter.next();
				IStorage underlier = model.getUnderlyingStorage();
				IPath underlierPath = underlier.getFullPath();
				if (underlierPath.equals(jwcPath)) {
					if (storage instanceof IResource && underlier instanceof IResource) {
						return model;
					} else if (storage instanceof IStorage && underlier instanceof IStorage) {
						return model;
					}
				}
			}
		}
		return null;
	}

	public List findComponentsUsingAlias(String alias) {
		ArrayList result = new ArrayList();
		Iterator iter = componentModels.iterator();
		while (iter.hasNext()) {
			TapestryComponentModel model = (TapestryComponentModel) iter.next();
			PluginComponentSpecification componentSpec = model.getComponentSpecification();
			if (componentSpec == null) {
				continue;
			}
			if (componentSpec.usesAlias(alias)) {
				result.add(model);
			}
		}
		return result;
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
			IJavaProject jproject =JavaCore.create(project);
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
			return result;
		}
	}

}