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
package com.iw.plugins.spindle.editors.template;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

import com.iw.plugins.spindle.ui.text.JWCIDTagRule;
import com.iw.plugins.spindle.ui.text.TagRule;


public class TemplatePartitionScanner extends RuleBasedScanner {

  public final static String HTML_COMMENT = "__html_comment";
  public final static String JWCID_TAG = "__jwcid_tag";
  public final static String JWC_TAG = "__jwc_tag";
  public final static String HTML_TAG = "__html_tag";

  public TemplatePartitionScanner() {

	ArrayList rules = new ArrayList();

    IToken htmlComment = new Token(HTML_COMMENT);
    IToken jwcidTag = new Token(JWCID_TAG);
    IToken jwcTag = new Token(JWC_TAG);
    IToken tag = new Token(HTML_TAG);

    rules.add(new MultiLineRule("<!--", "-->", htmlComment));    
    rules.add(new JWCIDTagRule(jwcidTag));
    rules.add(new MultiLineRule("<jwc", ">", jwcTag));
    rules.add(new TagRule(tag));

    setRules((IRule [])rules.toArray(new IRule[rules.size()]));
  }
}