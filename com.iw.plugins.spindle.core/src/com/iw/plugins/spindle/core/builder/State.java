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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An object intended to store the state of the build between builds.
 * Normally, a builder's output is the result of compiling source files, and the State
 * is merely there to make things like incremental builds possible.
 * 
 * This is true for Tapestry but different in that the builds state is *the* result
 * of the build!
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class State {

  /**
   * Constructor for State.
   */
  public State() {
    super();
  }

  /**
   * Constructor State.
   * @param builder
   */
  public State(TapestryBuilder builder) {
  }

  
  void write(DataOutputStream out) throws IOException {
  }
  
  static State read(DataInputStream in) throws IOException {
  	return null;
  }

}
