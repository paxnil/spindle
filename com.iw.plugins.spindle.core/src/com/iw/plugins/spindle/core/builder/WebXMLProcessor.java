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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.FullBuild.ServletInfo;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.parser.xml.ILocatable;
import com.iw.plugins.spindle.core.util.Utils;

/**
 * A helper class used by FullBuild that extracts Tapestry information from the file web.xml
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class WebXMLProcessor {

  protected FullBuild builder;
  protected Parser parser;
  protected IJavaProject javaProject;
  protected ArrayList servletNames;

  /**
   * Constructor for WebXMLProcessor.
   */
  public WebXMLProcessor(FullBuild fullBuilder) {
    this.builder = fullBuilder;
    this.parser = builder.parser;
    this.javaProject = builder.javaProject;
  }

  public List getServletInformation(Element webxml) {
    ArrayList infos = new ArrayList(11);
    for (Node node = webxml.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (parser.isElement(node, "servlet")) {
        infos.add(getServletInfo(node));
      }
    }
    return infos;
  }

  protected FullBuild.ServletInfo getServletInfo(Node servletNode) {
    FullBuild.ServletInfo newInfo = builder.new ServletInfo();
    for (Node node = servletNode.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (parser.isElement(node, "servlet-name")) {
        if (!processServletName(node, newInfo)) {
          return null;
        }
      }
      if (parser.isElement(node, "servlet-class")) {
        if (!processServletClass(node, newInfo)) {
          return null;
        }
      }
      if (parser.isElement(node, "init-param")) {
        try {
          getInitParam(node, newInfo.parameters);
        } catch (BuilderException e) {
          TapestryCore.log(e.getMessage());
          return null;
        }
      }
    }
    if (TapestryBuilder.DEBUG) {
      System.out.println("parsing web.xml found servlet:");
      System.out.println(newInfo.toString());
    }
    return newInfo;
  }

  protected boolean processServletName(Node node, FullBuild.ServletInfo newInfo) {
    newInfo.name = parser.getValue(node);
    if (servletNames == null) {
      servletNames = new ArrayList(11);
    }
    if (servletNames.contains(newInfo.name)) {
      String message = " spindle ignoring duplicate servlet name: " + newInfo.name;
      TapestryCore.log(message);
      if (TapestryBuilder.DEBUG) {
        System.out.println(message);
      }
      return false;
    }
    return true;
  }

  protected boolean processServletClass(Node node, FullBuild.ServletInfo newInfo) {
    newInfo.classname = parser.getValue(node);
    if (newInfo.classname == null) {
      String message = "ignoring servlet: " + newInfo.name + ". found null for classname.";
      TapestryCore.log(message);
      if (TapestryBuilder.DEBUG) {
        System.out.println(message);
        return false;
      }
    }
    IType servletType = builder.getType(newInfo.classname);
    if (servletType == null) {
      builder.markError(
        IMarker.SEVERITY_ERROR,
        (ILocatable) node,
        "servlet class not found: " + newInfo.classname);
      return false;
    }
    if (!isTapestryServlet(servletType)) {
      String message =
        "ignoring servlet: " + newInfo.name + ". servlet class is not a Tapestry servlet";
      TapestryCore.log(message);
      if (TapestryBuilder.DEBUG) {
        System.out.println(message);
        return false;
      }
    } else if (!builder.tapestryServletType.equals(servletType)) {
      newInfo.isServletSubclass = true; // its a subclass
      IPath path = getApplicationPathFromServlet(servletType);
      if (path != null) {
        try {
          checkApplicationLocation(path);
        } catch (BuilderException e) {
          builder.markError(
            IMarker.SEVERITY_ERROR,
            (ILocatable) node,
            "Ignoring servlet: "
              + newInfo.name
              + "'s getApplicationSpecificationPath() method returns invalid: "
              + e.getMessage());
          return false;
        }
        newInfo.parameters.put(TapestryBuilder.APP_SPEC_PATH_PARAM, path.toString());
      }
    }
    return true;
  }

  protected void getInitParam(Node initParamNode, Map map) throws BuilderException {
    String key = null;
    String value = null;
    boolean appSpecLocationSpecified = false;
    for (Node node = initParamNode.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (parser.isElement(node, "param-name")) {
        key = parser.getValue(node);
        appSpecLocationSpecified = TapestryBuilder.APP_SPEC_PATH_PARAM.equals(key);
        if (appSpecLocationSpecified && map.containsKey(TapestryBuilder.APP_SPEC_PATH_PARAM)) {
          builder.markError(
            IMarker.SEVERITY_WARNING,
            (ILocatable) node,
            "Duplicate application location found. Using first one = "
              + map.get(TapestryBuilder.APP_SPEC_PATH_PARAM));
          return;
        }
      }
      if (parser.isElement(node, "param-value")) {
        value = parser.getValue(node);
        if (appSpecLocationSpecified) {
          if (value == null) {
            BuilderException ex =
              new BuilderException("null value found for property 'org.apache.tapestry.application-specification'");
            builder.markError(IMarker.SEVERITY_ERROR, (ILocatable) node, ex.getMessage());
            throw ex;
          } else {
            IPath path = new Path(value);
            try {

              checkApplicationLocation(path);
            } catch (BuilderException e) {
              builder.markError(
                IMarker.SEVERITY_ERROR,
                (ILocatable) node,
                "invalid value for 'org.apache.tapestry.application-specification': "
                  + e.getMessage());
              throw e;
            }

          }
        }
      }
      if (key != null && value != null) {
        map.put(key, value);
      }
    }
  }

  protected IStorage checkApplicationLocation(IPath path) throws BuilderException {
    String extension = path.getFileExtension();
    if (extension == null || !extension.equals(TapestryBuilder.APPLICATION_EXTENSION)) {
      throw new BuilderException("wrong file ext: " + path.toString());
    }

    IStorage found = null;
    try {
      IJavaElement element = javaProject.findElement(path.makeRelative().removeLastSegments(1));
      if (element != null) {
        IPackageFragment frag = (IPackageFragment) element;
        found = builder.findInPackage(frag, path.lastSegment());
      }
    } catch (JavaModelException e) {
      TapestryCore.log(e);
    }
    if (found == null) {
      throw new BuilderException("not found on classpath: " + path.toString());
    }
    return found;
  }

  private boolean isTapestryServlet(IType candidate) {
    try {
      if (candidate != null) {
        if (!candidate.equals(builder.tapestryServletType)) {
          return Utils.extendsType(candidate, builder.tapestryServletType);
        }
        return true;
      }
    } catch (JavaModelException e) {
    }
    return false;
  }

  private IPath getApplicationPathFromServlet(IType servletType) {
    IPath result = null;
    try {
      IMethod pathMethod = servletType.getMethod("getApplicationSpecificationPath", new String[0]);
      if (pathMethod != null) {
        String methodSource = pathMethod.getSource();
        if (methodSource != null && !"".equals(methodSource.trim())) {
          int start = methodSource.indexOf("return");
          methodSource = methodSource.substring(start);
          int first = methodSource.indexOf("\"");
          int last = methodSource.lastIndexOf("\"");
          if (first >= 0 && last > first) {
            result = new Path(methodSource.substring(first + 1, last));
          }
        }
      }
    } catch (JavaModelException e) {
      TapestryCore.log(e);
    }
    return result;
  }

}
