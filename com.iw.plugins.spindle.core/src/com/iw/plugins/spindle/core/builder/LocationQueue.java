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

import com.iw.plugins.spindle.core.resources.IResourceDescriptor;
/**
 * Helper class used by the Full Build
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class LocationQueue {

  ArrayList toBeProcessed;
  ArrayList haveBeenProcessed;

  public LocationQueue() {
    this.toBeProcessed = new ArrayList(11);
    this.haveBeenProcessed = new ArrayList(11);
  }

  public void add(IResourceDescriptor element) {
    toBeProcessed.add(element);
  }

  public void addAll(IResourceDescriptor[] elements) {
    for (int i = 0, length = elements.length; i < length; i++) {
      add(elements[i]);
    }
  }

  public void clear() {
    this.toBeProcessed.clear();
    this.haveBeenProcessed.clear();
  }

  public void finished(IResourceDescriptor element) {
    toBeProcessed.remove(element);
    haveBeenProcessed.add(element);
  }

  public boolean isProcessed(IResourceDescriptor element) {
    return haveBeenProcessed.contains(element);
  }

  public boolean isWaiting(IResourceDescriptor element) {
    return toBeProcessed.contains(element);
  }

  public String toString() {
    return "LocationQueue: " + toBeProcessed;
  }
}
