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
package com.iw.plugins.spindle.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class JarEditorInputWrapper implements IStorageEditorInput, IAdaptable {

  JarEntryEditorInput wrapped;

  public JarEditorInputWrapper(JarEntryEditorInput wrapped) {
    this.wrapped = wrapped;
  }

  /**
  * @see IAdaptable#getAdapter(Class)
  */
  public Object getAdapter(Class clazz) {
    if (clazz == IFile.class) {
      return new JarEntryFileFaker(wrapped.getStorage());
    }
    return null;
  }

  /**
   * @see IEditorInput#exists()
   */
  public boolean exists() {
    return wrapped.exists();
  }

  /**
   * @see IEditorInput#getImageDescriptor()
   */
  public ImageDescriptor getImageDescriptor() {
    return wrapped.getImageDescriptor();
  }

  /**
   * @see IEditorInput#getName()
   */
  public String getName() {
    return wrapped.getName();
  }

  /**
   * @see IEditorInput#getPersistable()
   */
  public IPersistableElement getPersistable() {
    return wrapped.getPersistable();
  }

  /**
   * @see IEditorInput#getToolTipText()
   */
  public String getToolTipText() {
    return wrapped.getToolTipText();
  }

  public IStorage getStorage() {
    return wrapped.getStorage();
  }

}