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
package com.iw.plugins.spindle.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;

public class JWCIDTagRule extends MultiLineRule {

  protected char[] jwcStart;
  protected char[] jwcEnd;

  public JWCIDTagRule(IToken token) {
    this(token, (char) 0);
  }

  public JWCIDTagRule(IToken token, char escapeChar) {
    super("<", ">", token, escapeChar);
    jwcStart = "jwcid=\"".toCharArray();
    jwcEnd = "\"".toCharArray();
  }

  protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) {
    if (resume) {
      if (endSequenceDetected(scanner))
        return fToken;

    } else {
      int c = scanner.read();
      char poo = (char) c;
      if (c == fStartSequence[0]) {
        if (sequenceDetected(scanner, fStartSequence, false)) {
          if (sequenceDetected(scanner, jwcStart, fEndSequence)) {
            if (sequenceDetected(scanner, jwcEnd, fEndSequence)) {
              if (endSequenceDetected(scanner)) {
                return fToken;
              }
            }
          }
        }
      }
    }

    scanner.unread();
    return Token.UNDEFINED;
  }

  protected boolean sequenceDetected(
    ICharacterScanner scanner,
    char[] desired,
    char[] partyPooper) {
    int unwind = 0;
    int c;
    boolean success = false;
    while ((c = scanner.read()) != ICharacterScanner.EOF) {
      unwind++;
      if (c == fEscapeCharacter) {
        scanner.read();
        unwind++;
      } else if (
        c == partyPooper[0] && sequenceDetected(scanner, partyPooper, true)) {
        break;
      } else if (c == desired[0] && sequenceDetected(scanner, desired, true)) {
        success = true;
        break;
      }
    }
    if (!success) {
      for (int i = 0; i < unwind; i++) {
        scanner.unread();
      }
    }
    return success;
  }
}