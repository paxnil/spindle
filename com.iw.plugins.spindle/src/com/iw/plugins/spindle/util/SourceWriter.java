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
package com.iw.plugins.spindle.util;

import java.io.*;
import java.security.AccessController;
/** 
 * a subclass of PrintWriter that overrides the <code>println()</code>
 * method so I can control the line delimiter
 */

public class SourceWriter extends PrintWriter {

  public SourceWriter(Writer out) {
    this(out, false);
  }
  
  public SourceWriter(Writer out, boolean autoFlush) {
    super(out, autoFlush);
    
  }
 
  public SourceWriter(OutputStream out) {
    super(out, false);
  }
  
  public SourceWriter(OutputStream out, boolean autoFlush) {
    super(out, autoFlush);
  }
  
  public void println() {
  	print("\n");
  }
}