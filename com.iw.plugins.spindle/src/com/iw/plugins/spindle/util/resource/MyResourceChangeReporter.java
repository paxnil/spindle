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
package com.iw.plugins.spindle.util.resource;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class MyResourceChangeReporter implements IResourceChangeListener {
  public void resourceChanged(IResourceChangeEvent event) {
    IResource res = event.getResource();
    try {
      switch (event.getType()) {
        case IResourceChangeEvent.PRE_CLOSE :
          System.out.print(" Reporter: Project ");
          System.out.print(res.getFullPath());
          System.out.println(" is about to close.");
          break;
        case IResourceChangeEvent.PRE_DELETE :
          System.out.print(" Reporter: Project ");
          System.out.print(res.getFullPath());
          System.out.println(" is about to be deleted.");
          break;
        case IResourceChangeEvent.POST_CHANGE :
          System.out.println(" Reporter: Resources have changed.");
          event.getDelta().accept(new DeltaPrinter());
          break;
        case IResourceChangeEvent.PRE_AUTO_BUILD :
          System.out.println(" Reporter: Auto build about to run.");
          event.getDelta().accept(new DeltaPrinter());
          break;
        case IResourceChangeEvent.POST_AUTO_BUILD :
          System.out.println(" Reporter: Auto build complete.");
          event.getDelta().accept(new DeltaPrinter());
          break;
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  class DeltaPrinter implements IResourceDeltaVisitor {
    public boolean visit(IResourceDelta delta) {
      IResource res = delta.getResource();
      switch (delta.getKind()) {
        case IResourceDelta.ADDED :
          System.out.print(" Reporter: Resource ");
          System.out.print(res.getFullPath());
          System.out.println(" was added.");
          break;
        case IResourceDelta.REMOVED :
          System.out.print(" Reporter: Resource ");
          System.out.print(res.getFullPath());
          System.out.println(" was removed.");
          break;
        case IResourceDelta.CHANGED :
          System.out.print(" Reporter: Resource ");
          System.out.print(delta.getFullPath());
          System.out.println(" has changed.");
          switch (delta.getFlags()) {
            case IResourceDelta.CONTENT :
              System.out.println("--> Content Change");
              break;
            case IResourceDelta.REPLACED :
              System.out.println("--> Content Replaced");
              break;
            case IResourceDelta.REMOVED :
              System.out.println("--> Removed");
              break;
            case IResourceDelta.MARKERS :
              System.out.println("--> Marker Change");
              IMarkerDelta[] markers = delta.getMarkerDeltas();
              // if interested in markers, check these deltas
              break;
          }
      }
      return true; // visit the children
    }
  }

}