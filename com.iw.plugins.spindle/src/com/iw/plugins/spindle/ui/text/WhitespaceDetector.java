package com.iw.plugins.spindle.ui.text;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class WhitespaceDetector implements IWhitespaceDetector {

	/**
	 * @see IWhitespaceDetector#isWhitespace(char)
	 */
	public boolean isWhitespace(char c) {
		return Character.isWhitespace(c);
	}

}
