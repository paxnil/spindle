package com.iw.plugins.spindle.ui.text;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;



/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TagAttributeScanner extends RuleBasedScanner {

	/**
	 * Constructor for TagScanner.
	 * @param manager
	 */
	public TagAttributeScanner(ISpindleColorManager manager) {
		IToken string =
			new Token(new TextAttribute(manager.getColor(IColorConstants.STRING)));
		
		setRules(
			new IRule[] {
				new SingleLineRule("\"", "\"", string, '\\'),
				new SingleLineRule("'", "'", string, '\\'),
				new WhitespaceRule(new WhitespaceDetector())
			}
		);
	}

}