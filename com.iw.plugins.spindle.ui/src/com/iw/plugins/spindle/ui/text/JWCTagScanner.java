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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

public class JWCTagScanner extends RuleBasedScanner {

  public JWCTagScanner(IColorManager manager) {
    IToken jwcid = new Token(new TextAttribute(manager.getColor(IColorConstants.P_JWCID)));
    IToken string = new Token(new TextAttribute(manager.getColor(IColorConstants.P_STRING)));
    setRules(
		new IRule [] {
			new SingleLineRule("id=\"", "\"", jwcid, '\\'),
			new SingleLineRule("\"", "\"", string, '\\'),
			new SingleLineRule("'", "'", string, '\\'),
			new WhitespaceRule(new WhitespaceDetector())
		}
	);
  }

  public IToken nextToken() {
    return super.nextToken();
  }
}