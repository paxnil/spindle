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
import java.util.List;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.util.xml.DocumentParseException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.util.SourceWriter;

public class TapestryApplicationModel
  extends TapestryLibraryModel
  implements PropertyChangeListener {

  /**
   * Constructor for TapestryApplicationModel
   */
  public TapestryApplicationModel(IStorage storage) {
    super(storage);
  }

  public void load(final InputStream source) throws CoreException {
    final TapestryApplicationModel thisModel = this;
    TapestryPlugin.getDefault().getWorkspace().run(new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) {

        PluginApplicationSpecification pluginSpec =
          (PluginApplicationSpecification) librarySpecification;

        removeAllProblemMarkers();
        if (librarySpecification != null) {

          pluginSpec.removePropertyChangeListener(TapestryApplicationModel.this);
          pluginSpec.setParent(null);
        }
        try {

          SpecificationParser parser =
            (SpecificationParser) TapestryPlugin.getParserFor("application");
          librarySpecification =
            (PluginApplicationSpecification) parser.parseApplicationSpecification(
              source,
              getUnderlyingStorage().getName(),
              null);

          pluginSpec = (PluginApplicationSpecification) librarySpecification;
          pluginSpec.addPropertyChangeListener(TapestryApplicationModel.this);
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
    PluginApplicationSpecification spec = (PluginApplicationSpecification) getSpecification();
    StringWriter stringwriter = new StringWriter();
    spec.write(new SourceWriter(stringwriter));
    writer.print(stringwriter.toString());
  }

  public ReferenceInfo resolveReferences(boolean reverse) {
    return null;
  }

  //  public ReferenceInfo resolveReferences(boolean reverse) {
  //    try {
  //      if (!loaded) {
  //        load();
  //      }
  //    } catch (CoreException e) {
  //      return new ReferenceInfo(new ArrayList(), new ArrayList(), reverse);
  //    }
  //    if (!reverse) {
  //
  //      // find all components that are pages or that use my aliases!
  //
  //      ArrayList pagesAndComponents = new ArrayList();
  //      PluginApplicationSpecification spec = getApplicationSpec();
  //      Iterator iter = spec.getPageNames().iterator();
  //      while (iter.hasNext()) {
  //        String name = (String) iter.next();
  //        ReferenceInfo.ReferenceHolder holder = new ReferenceInfo.ReferenceHolder();
  //        PageSpecification pageSpec = spec.getPageSpecification(name);
  //        holder.name = pageSpec.getSpecificationPath();
  //        holder.description = "[page name =" + name + "] " + holder.name;
  //        pagesAndComponents.add(holder);
  //      }
  //      iter = spec.getComponentMapAliases().iterator();
  //      while (iter.hasNext()) {
  //        String alias = (String) iter.next();
  //        ReferenceInfo.ReferenceHolder holder = new ReferenceInfo.ReferenceHolder();
  //        holder.name = spec.getComponentAlias(alias);
  //        holder.description = "[component providing alias=" + alias + "] " + holder.name;
  //        pagesAndComponents.add(holder);
  //      }
  //      return resolveForwardReferences(pagesAndComponents);
  //    } else {
  //
  //      // find all components that provide my aliases!
  //      return resolveReverseReferences();
  //    }
  //  }

  //  private ReferenceInfo resolveForwardReferences(List pagesAndComponents) {
  //    ArrayList resolved = new ArrayList();
  //    ArrayList unresolved = new ArrayList();
  //    Iterator iter = pagesAndComponents.iterator();
  //    while (iter.hasNext()) {
  //      ReferenceInfo.ReferenceHolder holder = (ReferenceInfo.ReferenceHolder) iter.next();
  //      IStorage[] resolvedArray =
  //        TapestryPlugin.getDefault().resolveTapestryComponent(getUnderlyingStorage(), holder.name);
  //      if (resolvedArray.length > 0) {
  //        holder.model = (BaseTapestryModel) TapestryPlugin.getTapestryModelManager().getModel(resolvedArray[0]);
  //        resolved.add(holder);
  //      } else {
  //        unresolved.add(holder);
  //      }
  //    }
  //    return new ReferenceInfo(resolved, unresolved, false);
  //  }

  //  private ReferenceInfo resolveReverseReferences() {
  //    ArrayList resolved = new ArrayList();
  //    Set aliases = getApplicationSpec().getComponentMapAliases();
  //    Iterator allComponents = ModelUtils.getComponentModels();
  //    if (!allComponents.hasNext() || aliases.isEmpty()) {
  //      return new ReferenceInfo(resolved, new ArrayList(), true);
  //    }
  //    while (allComponents.hasNext()) {
  //      TapestryComponentModel model = (TapestryComponentModel) allComponents.next();
  //      PluginComponentSpecification componentSpec = model.getComponentSpecification();
  //      ArrayList found = new ArrayList();
  //      Iterator toCheck = aliases.iterator();
  //      while (toCheck.hasNext()) {
  //        String alias = (String) toCheck.next();
  //        if (componentSpec != null && componentSpec.usesAlias(alias)) {
  //          found.add(alias);
  //        }
  //      }
  //      if (found.isEmpty()) {
  //        continue;
  //      }
  //      ReferenceInfo.ReferenceHolder holder = new ReferenceInfo.ReferenceHolder();
  //      holder.description = " [uses alias(es)=";
  //      Iterator foundAliases = found.iterator();
  //      while (foundAliases.hasNext()) {
  //        holder.description += (String) foundAliases.next();
  //        if (foundAliases.hasNext()) {
  //          holder.description += ", ";
  //        }
  //      }
  //      holder.description += "]";
  //      IStorage storage = model.getUnderlyingStorage();
  //      if (storage instanceof JarEntryFile) {
  //        holder.name = ((JarEntryFile) storage).toString();
  //      } else {
  //        holder.name = model.getSpecificationLocation();
  //      }
  //      holder.description = holder.name + holder.description;
  //      holder.model = model;
  //      resolved.add(holder);
  //    }
  /*
  Iterator aliases = getApplicationSpec().getComponentMapAliases().iterator();
  List allComponents = TapestryPlugin.getTapestryModelManager().getAllComponents(getUnderlyingStorage());
  if (allComponents.isEmpty()) {
    return new ReferenceInfo(unresolvedAliases, resolvedAliases, true);
  }
  while (aliases.hasNext()) {
    String alias = (String) aliases.next();
    String desc = "[uses alias=" + alias + "] ";
    ArrayList found = new ArrayList();
    Iterator foundComponents = allComponents.iterator();
    while (foundComponents.hasNext()) {
      TapestryComponentModel component = (TapestryComponentModel) foundComponents.next();
      if (!component.getComponentSpecification().usesAlias(alias)) {
        continue;
      }
      ReferenceInfo.ReferenceHolder holder = new ReferenceInfo.ReferenceHolder();
      IStorage storage = component.getUnderlyingStorage();
      if (storage instanceof JarEntryFile) {
        holder.name = ((JarEntryFile) storage).toString();
      } else {
        holder.name = component.getSpecificationLocation();
      }
      holder.description = desc + holder.name;
      holder.model = component;
      found.add(holder);
  
    }
    if (found.isEmpty()) {
      ReferenceInfo.ReferenceHolder holder = new ReferenceInfo.ReferenceHolder();
      holder.description = "[no components found using alias=" + alias + "]";
      unresolvedAliases.add(holder);       
    } else {
    	resolvedAliases.addAll(found);
    }
  }*/
  //    return new ReferenceInfo(resolved, new ArrayList(), true);
  //  }

  //  public boolean containsReference(String name) {
  //    PluginApplicationSpecification spec = (PluginApplicationSpecification)getSpecification();
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

}
