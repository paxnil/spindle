package com.iw.plugins.spindle.core.builder;
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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Element;

import com.iw.plugins.spindle.core.Markers;
import com.iw.plugins.spindle.core.TapestryCore;

/**
 * Builds a Tapestry project from scratch.
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
/*package*/
class FullBuild extends Build {

  protected IType tapestryServletType;

  private LocationQueue applicationQueue;
  private LocationQueue libraryQueue;
  private LocationQueue pageQueue;
  private LocationQueue componentQueue;
  private LocationQueue htmlQueue;
  private LocationQueue scriptQueue;

  /**
   * Constructor for FullBuilder.
   */
  public FullBuild(TapestryBuilder builder) {
    super(builder);
    this.tapestryServletType = getType("org.apache.tapestry.ApplicationServlet");

  }

  public void build() {
    if (TapestryBuilder.DEBUG)
      System.out.println("FULL Tapestry build");

    try {
      notifier.subTask("Tapestry builder starting");
      Markers.removeProblemsFor(tapestryBuilder.currentProject);
      if (tapestryServletType == null) {
        Markers.addProblemMarkerToResource(
          tapestryBuilder.currentProject,
          "ignoring applications in project because '"
            + tapestryBuilder.currentProject.getName()
            + "', type 'org.apache.tapestry.ApplicationServlet' not found in project build path!",
          IMarker.SEVERITY_WARNING,
          0,
          0,
          0);
      } else {
        findDeclaredApplications();
      }
      notifier.updateProgressDelta(0.1f);

      //      notifier.subTask(Util.bind("build.analyzingSources"));
      //      ArrayList locations = new ArrayList(33);
      //      ArrayList typeNames = new ArrayList(33);
      //      addAllSourceFiles(locations, typeNames);
      //      notifier.updateProgressDelta(0.15f);
      //
      //      if (locations.size() > 0) {
      //        String[] allSourceFiles = new String[locations.size()];
      //        locations.toArray(allSourceFiles);
      //        String[] initialTypeNames = new String[typeNames.size()];
      //        typeNames.toArray(initialTypeNames);
      //
      //        notifier.setProgressPerCompilationUnit(0.75f / allSourceFiles.length);
      //        workQueue.addAll(allSourceFiles);
      //        compile(allSourceFiles, initialTypeNames);
      //      }
    } catch (CoreException e) {
      TapestryCore.log(e);
    } finally {
      cleanUp();
    }
  }

  public void cleanUp() {
  }

  protected void findDeclaredApplications() throws CoreException {
    if (tapestryBuilder.webXML != null) {
      Element wxmlElement = parseToElement(tapestryBuilder.webXML);
      if (wxmlElement == null) {
        return;
      }
      List servletInfos = new WebXMLProcessor(this).getServletInformation(wxmlElement);
    }
  }

  public class ServletInfo {
    String name;
    String classname;
    Map parameters = new HashMap();
    boolean isServletSubclass;
    public String toString() {
      StringBuffer buffer = new StringBuffer("ServletInfo(");
      buffer.append(name);
      buffer.append(")::");
      buffer.append("classname = ");
      buffer.append(classname);
      buffer.append(", params = ");
      buffer.append(parameters);
      return buffer.toString();
    }
  }

}
