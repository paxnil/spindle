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

import net.sf.tapestry.util.xml.DocumentParseException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.parser.SpecificationParser;
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
            (PluginApplicationSpecification) parser.parseApplicationSpecification(source);

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

}
