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

import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.util.xml.DocumentParseException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.util.SourceWriter;
import com.iw.plugins.spindle.util.SpindleStatus;

public class TapestryLibraryModel extends BaseTapestryModel implements PropertyChangeListener {

  protected IPluginLibrarySpecification librarySpecification;

  /**
   * Constructor for TapestryApplicationModel
   */
  public TapestryLibraryModel(IStorage storage) {
    super(storage);
  }

  public IPluginLibrarySpecification getSpecification() {
    if (librarySpecification == null || !loaded) {
      return null;
    }
    return librarySpecification;
  }

  public String findComponentAlias(String path) {

    IPluginLibrarySpecification spec = getSpecification();

    for (Iterator iter = spec.getComponentAliases().iterator(); iter.hasNext();) {
      String possibleAlias = (String) iter.next();

      String existingPath = spec.getComponentSpecificationPath(possibleAlias);

      if (existingPath.equals(path)) {

        return possibleAlias;
      }
    }

    return null;

  }

  public String findPageName(String path) {

    IPluginLibrarySpecification spec = getSpecification();

    for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();) {
      String possibleName = (String) iter.next();

      String existingPath = spec.getPageSpecificationPath(possibleName);

      if (existingPath.equals(path)) {

        return possibleName;
      }
    }

    return null;

  }

  public String findComponentPath(String type) throws CoreException {

    SpindleStatus status = new SpindleStatus();
    String result = null;
    ITapestryProject project = getProject();

    Assert.isNotNull(type);

    int index = type.indexOf(":");

    if (index == 0) {

      status.setError("invalid type: " + type);
      throw new CoreException(status);

    }

    String namespacePart = null;
    String componentAlias = type;

    if (index >= 0) {

      namespacePart = type.substring(0, index);
      componentAlias = type.substring(index + 1);
    }

    if (namespacePart == null) {

      result = librarySpecification.getComponentSpecificationPath(componentAlias);

      if (result == null) {

        result = project.findFrameworkComponentPath(componentAlias);

      }

    } else {

      String subLibrarySpecPath = librarySpecification.getLibrarySpecificationPath(namespacePart);

      if (subLibrarySpecPath == null) {

        status.setError("unable to resolve namespace: " + namespacePart);

        throw new CoreException(status);

      }

      TapestryLibraryModel subLib =
        (TapestryLibraryModel) project.findModelByPath(subLibrarySpecPath);

      result = subLib.getSpecification().getComponentSpecificationPath(componentAlias);

    }

    return result;

  }

  public void load(final InputStream source) throws CoreException {
    final TapestryLibraryModel thisModel = this;
    TapestryPlugin.getDefault().getWorkspace().run(new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) {

        removeAllProblemMarkers();

        PluginLibrarySpecification pluginSpec = (PluginLibrarySpecification) librarySpecification;
        if (pluginSpec != null) {

          pluginSpec.removePropertyChangeListener(TapestryLibraryModel.this);
          pluginSpec.setParent(null);
        }
        try {

          SpecificationParser parser =
            (SpecificationParser) TapestryPlugin.getParserFor("application");
          librarySpecification =
            (PluginLibrarySpecification) parser.parseLibrarySpecification(
              source,
              getUnderlyingStorage().getName(),
              null);

          pluginSpec = (PluginLibrarySpecification) librarySpecification;
          pluginSpec.addPropertyChangeListener(TapestryLibraryModel.this);
          loaded = true;
          editable = !(getUnderlyingStorage().isReadOnly());
          dirty = false;

          pluginSpec.setIdentifier(getUnderlyingStorage().getName());
          pluginSpec.setParent(thisModel);
          fireModelObjectChanged(librarySpecification, "applicationSpec");

        } catch (DocumentParseException dpex) {

          addProblemMarker(dpex);
          loaded = false;

        }
      }
    }, null);

  }

  /**
   * @see IEditable#save()
   */
  public void save(PrintWriter writer) {
    PluginLibrarySpecification spec = (PluginLibrarySpecification) getSpecification();
    StringWriter stringwriter = new StringWriter();
    spec.write(new SourceWriter(stringwriter));
    writer.print(stringwriter.toString());
  }

  public void setDescription(String description) {
    ILibrarySpecification spec = getSpecification();

    if (spec != null) {
      spec.setDescription(description);
      fireModelObjectChanged(this, "description");
    }
  }

  public String getDescription() {
    ILibrarySpecification spec = getSpecification();
    if (spec != null) {
      return spec.getDescription();
    }
    return "";
  }

  //  public boolean containsReference(String name) {
  //    PluginLibrarySpecification spec = (PluginLibrarySpecification)getSpecification();
  //    Iterator aliases = spec.getComponentMapAliases().iterator();
  //    while (aliases.hasNext()) {
  //      String alias = (String) aliases.next();
  //      if (spec.getComponentSpecificationPath(alias).equals(name)) {
  //        return true;
  //      }
  //    }
  //    Iterator pages = spec.getPageNames().iterator();
  //    while (pages.hasNext()) {
  //      String pageName = (String) pages.next();
  //
  //      String path =  spec.getPageSpecificationPath(pageName);
  //      if (path.equals(name)) {
  //        return true;
  //      }
  //    }
  //    return false;
  //  }

  public List getPropertyNames() {
    return getSpecification().getPropertyNames();
  }

  public String getProperty(String name) {
    return getSpecification().getProperty(name);
  }

  public void setProperty(String name, String value) {
    if (isEditable()) {
      getSpecification().setProperty(name, value);
    }
  }

  public void removeProperty(String name) {

    if (isEditable()) {

      getSpecification().removeProperty(name);
    }

  }

  /**
   * @see com.iw.plugins.spindle.model.BaseTapestryModel#resolveReferences(boolean)
   */
  public ReferenceInfo resolveReferences(boolean reverse) {
    return null;
  }

  /**
  * @see com.iw.plugins.spindle.model.ITapestryModel#getPublicId()
  */
  public String getPublicId() {
    if (librarySpecification != null) {
      return librarySpecification.getPublicId();
    }
    return "";
  }

  /**
  * @see com.iw.plugins.spindle.model.ITapestryModel#setPublicId(String)
  */
  public void setPublicId(String value) {

    if (librarySpecification != null) {

      librarySpecification.setPublicId(value);
    }
  }

}
