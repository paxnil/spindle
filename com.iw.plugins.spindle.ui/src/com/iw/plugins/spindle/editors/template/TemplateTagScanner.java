/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@intelligentworks.com phraktle@imapmail.org
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.editors.template;

import java.util.Map;

import net.sf.solareclipse.xml.internal.ui.text.NameDetector;
import net.sf.solareclipse.xml.internal.ui.text.XMLTagRule;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class TemplateTagScanner extends BufferedRuleBasedScanner
{

  public TemplateTagScanner(Map tokens)
  {
    setDefaultReturnToken((Token) tokens.get(ITemplateSyntaxConstants.XML_DEFAULT));

    IToken tag = (Token) tokens.get(ITemplateSyntaxConstants.XML_TAG);
    IToken jwcidAttribute = (Token) tokens
        .get(ITemplateSyntaxConstants.TAPESTRY_ATT_NAME);
    IToken attribute = (Token) tokens.get(ITemplateSyntaxConstants.XML_ATT_NAME);

    IRule[] rules = {new XMLTagRule(tag), new JWCIDRule(jwcidAttribute),
        new WordRule(new NameDetector(), attribute)};

    setRules(rules);
  }

  private static class JWCIDRule implements IRule
  {

    private IWordDetector fDetector = new JWCIDDetector();
    private IToken fSuccessToken;
    private StringBuffer fBuffer = new StringBuffer();

    public JWCIDRule(IToken successToken)
    {
      fSuccessToken = successToken;
    }

    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
      int c = scanner.read();
      if (fDetector.isWordStart((char) c))
      {

        fBuffer.setLength(0);
        do
        {
          fBuffer.append((char) c);
          c = scanner.read();
        } while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
        scanner.unread();

        String test = fBuffer.toString().toLowerCase();

        if (test.equals("jwcid"))
          return fSuccessToken;

        unreadBuffer(scanner);

        return Token.UNDEFINED;
      }

      scanner.unread();
      return Token.UNDEFINED;
    }

    protected void unreadBuffer(ICharacterScanner scanner)
    {
      for (int i = fBuffer.length() - 1; i >= 0; i--)
        scanner.unread();
    }
  }

  private static class JWCIDDetector implements IWordDetector
  {

    public boolean isWordStart(char c)
    {
      return Character.isLetter(c);
    }

    public boolean isWordPart(char c)
    {
      return Character.isLetter(c);
    }
  };

}