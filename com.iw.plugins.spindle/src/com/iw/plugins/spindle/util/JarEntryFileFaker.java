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

import java.io.InputStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.internal.core.JarEntryFile;

import com.iw.plugins.spindle.TapestryPlugin;

public class JarEntryFileFaker implements IFile {

  JarEntryFile faked;

  public JarEntryFileFaker(IStorage storage) {
    if (!(storage instanceof JarEntryFile)) {
      throw new IllegalArgumentException("not JarEntryFile");
    }
    faked = (JarEntryFile) storage;
  }
  
  public JarEntryFile getJarEntryFile() {
  	return faked;
  }

  public boolean equals(Object object) {
  	
  	if (this == object) {
  		return true;
  	} else if (object == null || getClass() != object.getClass()) {
  		return false;
  	}
  	
  	JarEntryFileFaker other = (JarEntryFileFaker) object;
  	
    return faked.equals(other.getJarEntryFile());
  }

  /**
   * @see IFile#appendContents(InputStream, boolean, boolean, IProgressMonitor)
   */
  public void appendContents(InputStream arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)
    throws CoreException {
  }

  /**
   * @see IFile#create(InputStream, boolean, IProgressMonitor)
   */
  public void create(InputStream arg0, boolean arg1, IProgressMonitor arg2) throws CoreException {
  }

  /**
   * @see IFile#delete(boolean, boolean, IProgressMonitor)
   */
  public void delete(boolean arg0, boolean arg1, IProgressMonitor arg2) throws CoreException {
  }

  /**
   * @see IFile#getContents()
   */
  public InputStream getContents() throws CoreException {
    return faked.getContents();
  }

  /**
   * @see IFile#getContents(boolean)
   */
  public InputStream getContents(boolean arg0) throws CoreException {
    return getContents();
  }

  /**
   * @see IFile#getFullPath()
   */
  public IPath getFullPath() {
    return faked.getFullPath();
  }

  /**
   * @see IFile#getHistory(IProgressMonitor)
   */
  public IFileState[] getHistory(IProgressMonitor arg0) throws CoreException {
    return new IFileState[0];
  }

  /**
   * @see IFile#getName()
   */
  public String getName() {
    return faked.getName();
  }

  /**
   * @see IFile#isReadOnly()
   */
  public boolean isReadOnly() {
    return faked.isReadOnly();
  }

  /**
   * @see IFile#move(IPath, boolean, boolean, IProgressMonitor)
   */
  public void move(IPath arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)
    throws CoreException {
  }

  /**
   * @see IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)
   */
  public void setContents(InputStream arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)
    throws CoreException {
  }

  /**
   * @see IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)
   */
  public void setContents(IFileState arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)
    throws CoreException {
  }

  /**
   * @see IResource#accept(IResourceVisitor)
   */
  public void accept(IResourceVisitor arg0) throws CoreException {
  }

  /**
   * @see IResource#accept(IResourceVisitor, int, boolean)
   */
  public void accept(IResourceVisitor arg0, int arg1, boolean arg2) throws CoreException {
  }

  /**
   * @see IResource#clearHistory(IProgressMonitor)
   */
  public void clearHistory(IProgressMonitor arg0) throws CoreException {
  }

  /**
   * @see IResource#copy(IProjectDescription, boolean, IProgressMonitor)
   */
  public void copy(IProjectDescription arg0, boolean arg1, IProgressMonitor arg2)
    throws CoreException {
  }

  /**
   * @see IResource#copy(IPath, boolean, IProgressMonitor)
   */
  public void copy(IPath arg0, boolean arg1, IProgressMonitor arg2) throws CoreException {
  }

  /**
   * @see IResource#createMarker(String)
   */
  public IMarker createMarker(String arg0) throws CoreException {
    return null;
  }

  /**
   * @see IResource#delete(boolean, IProgressMonitor)
   */
  public void delete(boolean arg0, IProgressMonitor arg1) throws CoreException {
  }

  /**
   * @see IResource#deleteMarkers(String, boolean, int)
   */
  public void deleteMarkers(String arg0, boolean arg1, int arg2) throws CoreException {
  }

  /**
   * @see IResource#exists()
   */
  public boolean exists() {
    return true;
  }

  /**
   * @see IResource#findMarker(long)
   */
  public IMarker findMarker(long arg0) throws CoreException {
    return null;
  }

  /**
   * @see IResource#findMarkers(String, boolean, int)
   */
  public IMarker[] findMarkers(String arg0, boolean arg1, int arg2) throws CoreException {
    return new IMarker[0];
  }

  /**
   * @see IResource#getFileExtension()
   */
  public String getFileExtension() {
    return faked.getFullPath().getFileExtension();
  }

  /**
   * @see IResource#getLocation()
   */
  public IPath getLocation() {
    return faked.getFullPath();
  }

  /**
   * @see IResource#getMarker(long)
   */
  public IMarker getMarker(long arg0) {
    return null;
  }

  /**
   * @see IResource#getModificationStamp()
   */
  public long getModificationStamp() {
    return 0;
  }

  /**
   * @see IResource#getParent()
   */
  public IContainer getParent() {
    return null;
  }

  /**
   * @see IResource#getPersistentProperty(QualifiedName)
   */
  public String getPersistentProperty(QualifiedName arg0) throws CoreException {
    return null;
  }

  /**
   * @see IResource#getProject()
   */
  public IProject getProject() {
    return TapestryPlugin.getDefault().getProjectFor(faked);
  }

  /**
   * @see IResource#getProjectRelativePath()
   */
  public IPath getProjectRelativePath() {
    return null;
  }

  /**
   * @see IResource#getSessionProperty(QualifiedName)
   */
  public Object getSessionProperty(QualifiedName arg0) throws CoreException {
    return null;
  }

  /**
   * @see IResource#getType()
   */
  public int getType() {
    return IFile.FILE;
  }

  /**
   * @see IResource#getWorkspace()
   */
  public IWorkspace getWorkspace() {
    return null;
  }

  /**
   * @see IResource#isAccessible()
   */
  public boolean isAccessible() {
    return false;
  }

  /**
   * @see IResource#isLocal(int)
   */
  public boolean isLocal(int arg0) {
    return false;
  }

  /**
   * @see IResource#isPhantom()
   */
  public boolean isPhantom() {
    return false;
  }

  /**
   * @see IResource#move(IProjectDescription, boolean, boolean, IProgressMonitor)
   */
  public void move(IProjectDescription arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)
    throws CoreException {
  }

  /**
   * @see IResource#move(IPath, boolean, IProgressMonitor)
   */
  public void move(IPath arg0, boolean arg1, IProgressMonitor arg2) throws CoreException {
  }

  /**
   * @see IResource#refreshLocal(int, IProgressMonitor)
   */
  public void refreshLocal(int arg0, IProgressMonitor arg1) throws CoreException {
  }

  /**
   * @see IResource#setLocal(boolean, int, IProgressMonitor)
   */
  public void setLocal(boolean arg0, int arg1, IProgressMonitor arg2) throws CoreException {
  }

  /**
   * @see IResource#setPersistentProperty(QualifiedName, String)
   */
  public void setPersistentProperty(QualifiedName arg0, String arg1) throws CoreException {
  }

  /**
   * @see IResource#setReadOnly(boolean)
   */
  public void setReadOnly(boolean arg0) {
  }

  /**
   * @see IResource#setSessionProperty(QualifiedName, Object)
   */
  public void setSessionProperty(QualifiedName arg0, Object arg1) throws CoreException {
  }

  /**
   * @see IResource#touch(IProgressMonitor)
   */
  public void touch(IProgressMonitor arg0) throws CoreException {
  }

  /**
   * @see IAdaptable#getAdapter(Class)
   */
  public Object getAdapter(Class clazz) {
    if (IStorage.class == clazz) {
      return faked;
    }
    return null;
  }

	/**
	 * @see IFile#appendContents(InputStream, int, IProgressMonitor)
	 */
	public void appendContents(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IFile#create(InputStream, int, IProgressMonitor)
	 */
	public void create(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IFile#setContents(IFileState, int, IProgressMonitor)
	 */
	public void setContents(
		IFileState source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IFile#setContents(InputStream, int, IProgressMonitor)
	 */
	public void setContents(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IResource#accept(IResourceVisitor, int, int)
	 */
	public void accept(IResourceVisitor visitor, int depth, int memberFlags)
		throws CoreException {
	}

	/**
	 * @see IResource#copy(IPath, int, IProgressMonitor)
	 */
	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IResource#copy(IProjectDescription, int, IProgressMonitor)
	 */
	public void copy(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IResource#delete(int, IProgressMonitor)
	 */
	public void delete(int updateFlags, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IResource#isDerived()
	 */
	public boolean isDerived() {
		return false;
	}

	/**
	 * @see IResource#isTeamPrivateMember()
	 */
	public boolean isTeamPrivateMember() {
		return false;
	}

	/**
	 * @see IResource#move(IPath, int, IProgressMonitor)
	 */
	public void move(IPath destination, int updateFlags, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IResource#move(IProjectDescription, int, IProgressMonitor)
	 */
	public void move(
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see IResource#setDerived(boolean)
	 */
	public void setDerived(boolean isDerived) throws CoreException {
	}

	/**
	 * @see IResource#setTeamPrivateMember(boolean)
	 */
	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
	}

	/**
	 * @see IResource#isSynchronized(int)
	 */
	public boolean isSynchronized(int arg0) {
		return true;
	}

	/**
	 * @see org.eclipse.core.resources.IFile#getEncoding()
	 */
	public int getEncoding() throws CoreException {
		return 0;
	}

}