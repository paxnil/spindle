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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ReferenceInfo {

  boolean reverse;
  List resolved;
  List unresolved;
  /**
   * Constructor for ReferenceInfo
   */
  public ReferenceInfo(List resolved, List unresolved, boolean reverse) {
    this.resolved = resolved;
    this.unresolved = unresolved;
    this.reverse = reverse;
  }

  public Iterator resolvedRefs() {
    return resolved.iterator();
  }

  public Iterator unresolvedRefs() {
    return unresolved.iterator();
  }

  public List getResolved() {
    return Collections.unmodifiableList(resolved);
  }

  public List getUnresolved() {
    return Collections.unmodifiableList(unresolved);
  }

  public static class ReferenceHolder {
    public String description;
    public BaseTapestryModel model;
    public String name;
    public boolean isAlias = false;
  }

}