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

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.core.ModelChangedEvent;

import com.iw.plugins.spindle.MessageUtil;

public abstract class AbstractModel implements ITapestryModel, IModel, IModelChangeProvider {
	private long timeStamp;
	private ArrayList changeListeners;
	protected boolean disposed;
	protected boolean loaded;
	/**
	 * Constructor for AbstractModel
	 */
	public AbstractModel() {
		super();
		changeListeners = new ArrayList(5);
		disposed = false;
		loaded = false;
	}

	/**
	 * @see IModel#dispose()
	 */
	public void dispose() {
		disposed = true;
	}

	/**
	 * @see IModel#getResourceString(String)
	 */
	public String getResourceString(String key) {
		return MessageUtil.getString(key);
	}

	/**
	 * @see IModel#getUnderlyingResource()
	 */
	public IResource getUnderlyingResource() {
		throw new Error();
	}

	public IStorage getUnderlyingStorage() {
		return null;
	}

	/**
	 * @see IModel#isDisposed()
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * @see IModel#isEditable()
	 */
	abstract public boolean isEditable();

	/**
	 * @see IModel#isLoaded()
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @see IModel#load()
	 */
	public abstract void load() throws CoreException;

	/**
	 * @see IModel#load(InputStream)
	 */
	public void load(InputStream source) throws CoreException {
		load(source, true);
	}

	/**
	 * @see IModel#load(InputStream, boolean)
	 */
	public void load(InputStream source, boolean outOfSync) throws CoreException {
		loaded = true;
	}		

	/**
	 * @see IModel#reload(InputStream)
	 */
	public void reload(InputStream source) throws CoreException {
		reload(source, true);
	}
	
	/**
	 * @see IModel#reload(InputStream, boolean)
	 */
	public void reload(InputStream source, boolean outOfSync) throws CoreException {
	}

	/**
	 * @see IModelChangeProvider#addModelChangedListener(IModelChangedListener)
	 */
	public void addModelChangedListener(IModelChangedListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * @see IModelChangeProvider#fireModelChanged(IModelChangedEvent)
	 */
	public void fireModelChanged(IModelChangedEvent event) {
		Iterator i = changeListeners.iterator();
		while (i.hasNext()) {
			IModelChangedListener listener = (IModelChangedListener) i.next();
			listener.modelChanged(event);
		}
	}

	/**
	 * @see IModelChangeProvider#fireModelObjectChanged(Object, String)
	 */
	public void fireModelObjectChanged(Object obj, String property) {
		Object[] objects = new Object[] { obj };
		fireModelChanged(new ModelChangedEvent(IModelChangedEvent.WORLD_CHANGED, objects, property));
	}

	/**
	* @see IModelChangeProvider#fireModelObjectChanged(Object, String, Object, Object)
	*/
	public void fireModelObjectChanged(
		Object object,
		String property,
		Object oldValue,
		Object newValue) {
		Object[] objects = new Object[] { oldValue, newValue };
		fireModelChanged(new ModelChangedEvent(IModelChangedEvent.WORLD_CHANGED, objects, property));		
	}

	/**
	 * @see IModelChangeProvider#removeModelChangedListener(IModelChangedListener)
	 */
	public void removeModelChangedListener(IModelChangedListener listener) {
		changeListeners.remove(listener);
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ITapestryModel other = (ITapestryModel) obj;
		return getUnderlyingStorage().equals(other.getUnderlyingStorage());
	}

	/**
	 * Not used (yet)
	 * @see IModel#getTimeStamp()
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @see IModel#isInSync()
	 */
	public boolean isInSync() {
		return !outOfSynch;
	}

	/**
	 * @see IEditable#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/**
	 * @see IEditable#save(PrintWriter)
	 */
	public void save(PrintWriter arg0) {
	}

	/**
	 * @see IEditable#setDirty(boolean)
	 */
	public void setDirty(boolean arg0) {
	}



	protected boolean outOfSynch = false;



  /**
   * Sets the timeStamp.
   * @param timeStamp The timeStamp to set
   */
  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

}