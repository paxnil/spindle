package com.iw.plugins.spindle.ui.text;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CommentScanner extends RuleBasedScanner {

	/**
	 * Constructor for CommentScanner.
	 * @param manager
	 */
	public CommentScanner(ISpindleColorManager manager) {
		IToken comment =
			new Token(manager.getColor(IColorConstants.P_XML_COMMENT));

		setRules(
		  new IRule [] { new MultiLineRule("<!--", "-->", comment) });
		
	}

  /**
   * @see ITokenScanner#nextToken()
   */
  public IToken nextToken() {
    return super.nextToken();
  }

}