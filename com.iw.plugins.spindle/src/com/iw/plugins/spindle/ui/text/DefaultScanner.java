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
public class DefaultScanner extends RuleBasedScanner {

	/**
	 * Constructor for DefaultScanner.
	 * @param manager
	 */
	public DefaultScanner(ISpindleColorManager manager) {
		
		IToken procInstr =
			new Token(new TextAttribute(manager.getColor(IColorConstants.PROC_INSTR)));

		
		setRules(
			new IRule[] {
				new SingleLineRule("<?", "?>", procInstr),
				new WhitespaceRule(new WhitespaceDetector())
			}
		);
	}

}