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
package com.iw.plugins.spindle.factories;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class MethodAdapter implements IMethodEvaluator {

  static private String[] empty = new String[0];
  /**
   * Constructor for MethodAdapter
   */
  public MethodAdapter() {
    super();
  }

  /**
   * @see IMethodEvaluator#methodsToCreate()
   */
  public String[] methodsToCreate() {
    return empty;
  }

  /**
   * @see IMethodEvaluator#createdMethods(IType, IMethod[])
   */
  public void createdMethods(IType type, IMethod[] newMethods) {
    // do nothing
  }

  /**
   * @see IMethodEvaluator#newInheritedMethods(IType, IMethod[])
   */
  public void newInheritedMethods(IType type, IMethod[] newMethods) {
    //do nothing
  }

}