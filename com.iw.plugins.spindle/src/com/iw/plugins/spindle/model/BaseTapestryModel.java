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

import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.ModelChangedEvent;
import org.eclipse.ui.texteditor.MarkerUtilities;

public abstract class BaseTapestryModel extends AbstractModel implements IEditable {

  protected boolean editable = true;
  protected boolean dirty = false;

  private IStorage storageResource;

  /**
   * Constructor for BaseTapestryModel
   */
  public BaseTapestryModel(IStorage storage) {
    super();
    this.storageResource = storage;
  }

  /**
   * @see IEditable#setDirty()
   */
  public void setDirty(boolean flag) {
    dirty = flag;
  }

  /**
   * @see IEditable#isDirty()
   */
  public boolean isDirty() {
    return dirty;
  }

  public void setEditable(boolean flag) {
    editable = flag;
  }

  public abstract void setDescription(String description);

  public abstract String getDescription();

  /**
   * @see AbstractModel#isEditable()
   */
  public boolean isEditable() {
    if (editable) {
      return !storageResource.isReadOnly();
    }
    return editable;
  }

  public IStorage getUnderlyingStorage() {
    return storageResource;
  }

  public void setOutOfSynch(boolean flag) {
    outOfSynch = flag;
  }

  protected void removeAllProblemMarkers() {
    IStorage storage = getUnderlyingStorage();
    if (storage instanceof IResource) {
      IMarker[] found = findProblemMarkers();
      for (int i = 0; i < found.length; i++) {
        try {
          found[i].delete();
        } catch (CoreException corex) {
        }
      }
    }

  }

  public IMarker[] findProblemMarkers() {
    IStorage storage = getUnderlyingStorage();
    if (storage instanceof IResource) {
      String type = "com.iw.plugins.spindle.tapestryproblem";
      try {
        return ((IResource) storage).findMarkers(type, true, 0);
      } catch (CoreException corex) {
      }
    }
    return new IMarker[0];
  }

  protected void addProblemMarker(String message, int line, int column, int severity) {
    IStorage storage = getUnderlyingStorage();
    if (storage instanceof IResource) {
      try {
        IMarker marker = ((IResource) storage).createMarker("com.iw.plugins.spindle.tapestryproblem");
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.LINE_NUMBER, new Integer(line));
        marker.setAttribute(IMarker.CHAR_START, column);
        marker.setAttribute(IMarker.CHAR_END, column + 1);
     } catch (CoreException corex) {
      }
    }

  }

  /**
   * @see AbstractModel#load()
   */
  public void load() throws CoreException {
    if (!isLoaded()) {
      InputStream contents;
      if (storageResource instanceof IFile) {
        contents = ((IFile) storageResource).getContents(true);
        load(contents);
        setTimeStamp(((IFile) storageResource).getModificationStamp());
      } else {
        contents = storageResource.getContents();
        load(contents);
        setTimeStamp(Long.MAX_VALUE);
      }
    }
  }

  public void reload() throws CoreException {
    reload(storageResource.getContents());
  }

  public void reload(InputStream source) throws CoreException {
    load(source);
    fireModelChanged(new ModelChangedEvent(ModelChangedEvent.WORLD_CHANGED, new Object[0], "All"));
  }

  public abstract ReferenceInfo resolveReferences(boolean reverse);

  public abstract Set getPropertyNames();

  public abstract String getProperty(String name);

  public abstract void setProperty(String name, String value);

  /**
  	* @see IAdaptable#getAdapter(Class)
  	*/
  public Object getAdapter(Class adapter) {
    IStorage underlier = getUnderlyingStorage();
    if (IStorage.class == adapter) {
      return underlier;
    }
    if (IFile.class == adapter && underlier instanceof IFile) {
      return (IFile) underlier;
    }
    if (IResource.class == adapter && underlier instanceof IResource) {
      return (IResource) underlier;
    }
    return null;
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event) {
    dirty = true;
    fireModelObjectChanged(this, event.getPropertyName());
  }

  /**
   * @see com.iw.plugins.spindle.model.ITapestryModel#toXML()
   */
  public String toXML() {
    StringWriter swriter = new StringWriter();
    PrintWriter writer = new PrintWriter(swriter);
    save(writer);
    writer.flush();
    return swriter.toString();
  }

}