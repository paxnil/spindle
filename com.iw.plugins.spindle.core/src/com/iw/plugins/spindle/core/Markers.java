package com.iw.plugins.spindle.core;
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.builder.ITapestryMarker;
import com.iw.plugins.spindle.core.parser.DocumentParseException;
import com.iw.plugins.spindle.core.parser.IOffsetResolver;
import com.iw.plugins.spindle.core.parser.xml.ILocatable;

/**
 * Marker utililties
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class Markers {

  public static final String TAPESTRY_MARKER_TAG = ITapestryMarker.TAPESTRY_PROBLEM_MARKER;

  public static void addProblemMarkerToResource(
    IResource resource, String message, int severity, ILocatable source, IOffsetResolver resolver) {

    addProblemMarkerToResource(
      resource,
      message,
      severity,
      source.getStartLine(),
      source.getCharStart(resolver),
      source.getCharEnd(resolver));

  }

  public static void addProblemMarkerToResource(
    IResource resource,
    DocumentParseException exception) {

    addProblemMarkerToResource(
      resource,
      exception.getMessage(),
      exception.getSeverity(),
      exception.getLineNumber(),
      exception.getCharStart(),
      exception.getCharEnd());

  }

  public static void addProblemMarkerToResource(
    IResource resource,
    String message,
    int severity,
    int lineNumber,
    int charStart,
    int charEnd) {

    addProblemMarkerToResource(
      resource,
      message,
      new Integer(severity),
      new Integer(lineNumber),
      new Integer(charStart),
      new Integer(charEnd));
  }

  public static void addProblemMarkerToResource(
    IResource resource,
    String message,
    Integer severity,
    Integer lineNumber,
    Integer charStart,
    Integer charEnd) {
    try {
      IMarker marker = resource.createMarker(TAPESTRY_MARKER_TAG);

      marker.setAttributes(
        new String[] {
          IMarker.MESSAGE,
          IMarker.SEVERITY,
          IMarker.LINE_NUMBER,
          IMarker.CHAR_START,
          IMarker.CHAR_END },
        new Object[] { message, severity, lineNumber, charStart, charEnd });
    } catch (CoreException e) {
      TapestryCore.log(e);
    }

  }

  public static IMarker[] getProblemsFor(IResource resource) {
    try {
      if (resource != null && resource.exists())
        return resource.findMarkers(TAPESTRY_MARKER_TAG, false, IResource.DEPTH_INFINITE);
    } catch (CoreException e) {
    } // assume there are no problems
    return new IMarker[0];
  }

  public static void removeProblemsFor(IResource resource) {
    try {
      if (resource != null && resource.exists())
        resource.deleteMarkers(Markers.TAPESTRY_MARKER_TAG, false, IResource.DEPTH_INFINITE);
    } catch (CoreException e) {
    } // assume there were no problems
  }

}
