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
package com.iw.plugins.spindle.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

public class TagRule extends MultiLineRule {

  public TagRule(IToken token) {
    super("<", ">", token);
  } 
  
  protected boolean sequenceDetected(
    ICharacterScanner scanner,
    char[] sequence,
    boolean eofAllowed) {
    int c = scanner.read();
    if (sequence[0] == '<') {
      if (c == '?') {
        // processing instruction - abort
        scanner.unread();
        return false;
      }
      if (c == '!') {
        scanner.unread();
        // comment - abort
        return false;
      }
    }
    /* the following was ommited in the PDE class of the
     * same name. Without the call to unread(),
     * the scanner is left with one characted too many having been
     * read. this screws up Rules evaluated on the same line
     */
    scanner.unread();
    return super.sequenceDetected(scanner, sequence, eofAllowed);
  }
}