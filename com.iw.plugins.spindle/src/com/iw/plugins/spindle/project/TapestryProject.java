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

package com.iw.plugins.spindle.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.refactor.DeletedComponentOrPageRefactor;
import com.iw.plugins.spindle.refactor.MovedComponentOrPageRefactor;
import com.iw.plugins.spindle.refactor.RenamedComponentOrPageRefactor;
import com.iw.plugins.spindle.util.SpindleStatus;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author GWL
 * @version $Id$
 *
 * 
 * Notes:
 * 
 *  this project needs to read/write an xml file defining its application
 */
public class TapestryProject implements IProjectNature, ITapestryProject {
	
  static public boolean migrating = false;

  private IPath projectModelPath;
  private String projectName;
  private IProject project;
  private IStorage projectResource;

  private TapestryProjectModelManager modelManager;

  private ArrayList listeners = null;

  private boolean dirty; // has the application changed?

  /**
   * Constructor for TapestryProject.
   */
  public TapestryProject() {
    super();
    try {
      configure();
    } catch (CoreException e) {
    }

  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  public void configure() throws CoreException {

    addResourceListeners();
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  public void deconfigure() throws CoreException {

    removeResourceListeners();

    removeProperties();
  }

  private void addResourceListeners() {

    if (listeners == null) {

      listeners = new ArrayList();

      listeners.add(new ProjectResourceChangeListener());

      listeners.add(new DeletedComponentOrPageRefactor(this));
      
      listeners.add(new RenamedComponentOrPageRefactor(this));
      
      listeners.add(new MovedComponentOrPageRefactor(this));

      for (Iterator iter = listeners.iterator(); iter.hasNext();) {

        ResourcesPlugin.getWorkspace().addResourceChangeListener(
          (IResourceChangeListener) iter.next(),
          IResourceChangeEvent.PRE_AUTO_BUILD | IResourceChangeEvent.PRE_CLOSE);

      }

    }
  }

  private void removeResourceListeners() {

    if (listeners != null) {
    	
      for (Iterator iter = listeners.iterator(); iter.hasNext();) {
      	
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(
          (IResourceChangeListener) iter.next());

      }
      
      listeners = null;
    }
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  public IProject getProject() {
    return project;
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#setProject(IProject)
   */
  public void setProject(IProject project) {

    this.project = project;
    this.projectName = project.getName();
  }

  /**
   * Returns the dirty.
   * @return boolean
   */
  public boolean isDirty() {
    return dirty;
  }

  public TapestryLookup getLookup() throws CoreException {

    TapestryLookup lookup = new TapestryLookup();
    lookup.configure(TapestryPlugin.getDefault().getJavaProjectFor(project));

    return lookup;
  }

  private void reset() {

    if (modelManager != null) {
      modelManager.shutdown();
    }
    modelManager = null;
    projectResource = null;

  }

  private void handleProjectStorageRemoved() {

    MessageDialog.openInformation(
      TapestryPlugin.getDefault().getActiveWorkbenchShell(),
      "Spindle problem",
      "you have removed or improperly renamed the Application or Library associated with project: "
        + getProject().getName()
        + ".\nSpindle behaviour from this point on is undefined.");

    TapestryPlugin.getDefault().removeTapestryProjectNature(this, new NullProgressMonitor());

  }

  /**
   * Sets the dirty.
   * @param dirty The dirty to set
   */
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  /**
   * @see com.iw.plugins.spindle.project.ITapestryProject#getModelManager()
   */
  public synchronized TapestryProjectModelManager getModelManager() throws CoreException {

    if (modelManager == null) {

      createModelManager();

    }

    return modelManager;

  }

  private synchronized void createModelManager() throws CoreException {

    modelManager = new TapestryProjectModelManager(getProject());

    modelManager.buildModelDelegates();

    modelManager.getReadOnlyModel(getProjectStorage());

  }

  /**
   * @see com.iw.plugins.spindle.project.ITapestryProject#setProjectStorage(IStorage)
   */
  public void setProjectStorage(IStorage file) throws CoreException {

    projectResource = file;

    TapestryLookup lookup = getLookup();
    IPackageFragment fragment = null;
    try {
      fragment = lookup.findPackageFragment(file);
    } catch (JavaModelException e) {
    }

    if (fragment == null) {

      MessageDialog.openWarning(
        TapestryPlugin.getDefault().getActiveWorkbenchShell(),
        "Spindle Warning",
        "The project Application/Library '"
          + projectResource.getFullPath().toString()
          + "'\nis located outside of the project classpath.\n You must move it back or Spindle will break.");
    }

    try {

      saveProperties(saveProjectResourcePathAsXML(projectResource));

    } catch (IOException e) {

      SpindleStatus status = new SpindleStatus(e);
      status.setError("Could not save .spindle file");

      throw new CoreException(status);
    }

  }

  public ITapestryModel getProjectModel() throws CoreException {

    return getModelManager().getReadOnlyModel(getProjectStorage());

  }

  public String findFrameworkComponentPath(String alias) throws CoreException {

    return getDefaultLibraryModel().getSpecification().getComponentSpecificationPath(alias);

  }

  public String findFrameworkPagePath(String alias) throws CoreException {

    return getDefaultLibraryModel().getSpecification().getPageSpecificationPath(alias);

  }

  public TapestryLibraryModel getDefaultLibraryModel() throws CoreException {

    return getModelManager().getDefaultLibrary();

  }

  public ITapestryModel findModelByPath(String specificationPath) throws CoreException {

    return findModelByPath(specificationPath, TapestryLookup.ACCEPT_ANY);
  }

  public ITapestryModel findModelByPath(String specificationPath, int acceptFlags)
    throws CoreException {

    TapestryLookup.StorageOnlyRequest request = new TapestryLookup.StorageOnlyRequest();

    getLookup().findAll(
      specificationPath,
      false,
      acceptFlags | TapestryLookup.FULL_TAPESTRY_PATH,
      request);

    IStorage[] results = request.getResults();

    if (results.length > 0) {

      return getModelManager().getReadOnlyModel(results[0]);

    }

    return null;

  }

  public synchronized IStorage getProjectStorage() throws CoreException {

    if (projectResource == null) {

      IProject project = getProject();

      String projectPath = project.getFullPath().toString();

      try {

        String properties = readProperties();
        if (properties == null) {

          SpindleStatus status = new SpindleStatus();
          status.setError(".spindle file does not exist");

          throw new CoreException(status);

        }

        Path path = new Path(properties);

        //        String xmlStuff = readProperties();
        //
        //        Path path = new Path(readApplicationPath(xmlStuff));

        IFile file = TapestryPlugin.getDefault().getWorkspace().getRoot().getFile(path);

        if (!file.exists() || !file.getProject().equals(project)) {

          SpindleStatus status =
            new SpindleStatus(
              SpindleStatus.ERROR,
              path.toString() + "does not exist in " + projectPath);

          throw new CoreException(status);
        }

        projectResource = (IStorage) file;

      } catch (IOException e) {

        SpindleStatus status = new SpindleStatus(e);
        status.setError("Could not read " + projectPath + "/.spindle file");

        throw new CoreException(status);

      }
    }

    return projectResource;

  }

  // not called because of the conflicting org.wc3.dom stuff
  //  protected String readApplicationPath(String xmlSpec) throws IOException {
  //
  //    IPath projectPath = getProject().getFullPath();
  //    StringReader reader = new StringReader(xmlSpec);
  //    Element configElement;
  //
  //    String path = null;
  //
  //    try {
  //
  //      DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
  //      configElement = parser.parse(new InputSource(reader)).getDocumentElement();
  //
  //    } catch (SAXException e) {
  //
  //      throw new IOException("bad file format");
  //    } catch (ParserConfigurationException e) {
  //
  //      reader.close();
  //      throw new IOException("bad file format");
  //    } finally {
  //
  //      reader.close();
  //    }
  //
  //    if (!configElement.getNodeName().equalsIgnoreCase("spindle")) {
  //      throw new IOException("bad file format");
  //    }
  //
  //    NodeList list = configElement.getChildNodes();
  //    int length = list.getLength();
  //
  //    for (int i = 0; i < length; ++i) {
  //      Node node = list.item(i);
  //      short type = node.getNodeType();
  //      if (type == Node.ELEMENT_NODE) {
  //        Element element = (Element) node;
  //
  //        if (element.getNodeName().equalsIgnoreCase("application")) {
  //
  //          path = element.getAttribute("path"); //$NON-NLS-1$
  //
  //        }
  //      }
  //    }
  //    return path;
  //  }

  protected String saveProjectResourcePathAsXML(IStorage storage) throws IOException {

    StringBuffer buffer = new StringBuffer();
    buffer.append("# Spindle project kludge");
    buffer.append("\n#\n");
    buffer.append("# To get around problem of having multiple versions");
    buffer.append("\n");
    buffer.append("# of org.wc3.dom in the same plugin");
    buffer.append("\n");
    buffer.append("\n");
    buffer.append(TapestryPlugin.NATURE_ID);
    buffer.append("=");
    buffer.append(storage.getFullPath().toString());

    return buffer.toString();

  }

  //  protected String saveApplicationPathAsXML(IStorage storage) throws IOException {
  //
  //    IPath path = storage.getFullPath();
  //
  //    String projectResourcePath = path.toString();
  //
  //    Document doc = new DocumentImpl();
  //    Element configElement = doc.createElement("spindle");
  //    doc.appendChild(configElement);
  //
  //    Element appPathElement = doc.createElement("application");
  //    appPathElement.setAttribute("path", projectResourcePath);
  //    configElement.appendChild(appPathElement);
  //
  //    ByteArrayOutputStream s = new ByteArrayOutputStream();
  //    OutputFormat format = new OutputFormat();
  //    format.setIndenting(true);
  //    format.setLineSeparator(System.getProperty("line.separator"));
  //
  //    Serializer serializer =
  //      SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
  //        new OutputStreamWriter(s, "UTF8"),
  //        format);
  //    serializer.asDOMSerializer().serialize(doc);
  //    return s.toString("UTF8");
  //  }

  public void saveProperties(String xmlValue) throws CoreException {

    String filename = ".spindle";
    IFile rscFile = getProject().getFile(filename);
    InputStream inputStream = new ByteArrayInputStream(xmlValue.getBytes());
    // update the resource content
    if (rscFile.exists()) {
      rscFile.setContents(inputStream, IResource.FORCE, null);
    } else {
      rscFile.create(inputStream, IResource.FORCE, null);
    }
  }

  private void removeProperties() {

    String filename = ".spindle";
    IFile rscFile = getProject().getFile(filename);
    // update the resource content
    if (rscFile.exists()) {
      try {

        rscFile.delete(true, new NullProgressMonitor());

      } catch (CoreException e) {
      }
    }
  }

  public String readProperties() throws CoreException, IOException {

    String property = null;
    String propertyFileName = ".spindle";
    IFile rscFile = getProject().getFile(propertyFileName);
    if (rscFile.exists()) {
      property = new String(Utils.getResourceContentsAsByteArray(rscFile));

      // Ugly hack follows

      Properties props = new Properties();
      props.load(rscFile.getContents());
      property = props.getProperty(TapestryPlugin.NATURE_ID);
    }

    return property;
  }

  class ProjectResourceChangeListener implements IResourceChangeListener {

    /**
    * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
    */
    public void resourceChanged(IResourceChangeEvent event) {
    	
      if (migrating) {
      	
      	return;
      	
      }

      if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {

        IProject closing = (IProject) event.getResource();
        if (closing.equals(getProject())) {

          reset();

        }

      } else if (event.getType() == IResourceChangeEvent.PRE_AUTO_BUILD) {

        IProject project = getProject();
        IResourceDelta topLevelDelta = event.getDelta();
        IResourceDelta projectDelta = topLevelDelta.findMember(project.getFullPath());

        if (project.isOpen() && projectDelta != null) {

          IResource projectResource = null;
          try {

            projectResource = (IResource) getProjectStorage();
          } catch (CoreException e) {
          }

          if (projectResource != null) {

            IResourceDelta projectResourceDelta =
              topLevelDelta.findMember(projectResource.getFullPath());
            if (projectResourceDelta != null) {

              handleProjectResourcePresentCase(projectResource, projectResourceDelta);
            }

          }
        }

      }
    }

    /**
    * Method handleProjectResourcePresentCase.
    * @param delta
    * @return boolean
    */
    private void handleProjectResourcePresentCase(
      IResource projectResource,
      IResourceDelta delta) {

      IResource resource = delta.getResource();
      if (projectResource.equals(resource)) {

        int flags = delta.getFlags();
        if (delta.getKind() == IResourceDelta.REMOVED && (flags & ResourceDelta.MOVED_TO) == 0) {

          handleProjectStorageRemoved();

        } else if ((flags & IResourceDelta.MOVED_TO) != 0) {

          IPath movedFrom = delta.getMovedFromPath();
          IPath movedTo = delta.getMovedToPath();

          IResource newStorage = ResourcesPlugin.getWorkspace().getRoot().findMember(movedTo);

          if (!newStorage.getProject().equals(getProject())) {

            handleProjectStorageRemoved();

          } else {

            if (!checkNameStillValid(movedTo)) {

              handleProjectStorageRemoved();
            } else {

              try {

                setProjectStorage((IStorage) newStorage);
              } catch (CoreException e) {
              }
            }
          }

        }
      }
    }

    private boolean checkNameStillValid(IPath newPath) {

      String extension = newPath.getFileExtension();

      return extension != null && ("application".equals(extension) || "library".equals(extension));

    }
  }
}
