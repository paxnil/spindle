/**********************************************************************
Copyright (c) 2002  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:
	Igor Malinin - initial contribution

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.ui.text;


/**
 * 
 * 
 * @author Igor Malinin
 */
public interface ICSSSyntaxConstants {
	/** 
	 * Note: This constant is for internal use only. Clients should not use
	 * this constant. The prefix all color constants start with.
	 */
	String PREFIX = "css_"; //$NON-NLS-1$

	/** The style key for CSS text. */
	String CSS_DEFAULT = PREFIX + "text"; //$NON-NLS-1$

	/** The style key for CSS comments. */
	String CSS_COMMENT = PREFIX + "comment"; //$NON-NLS-1$

	/** The style key for CSS id-rules. */
	String CSS_ID_RULE = PREFIX + "id-rule"; //$NON-NLS-1$

	/** The style key for CSS at-rules. */
	String CSS_AT_RULE = PREFIX + "at-rule"; //$NON-NLS-1$

	/** The style key for CSS properties. */
	String CSS_PROPERTY = PREFIX + "property"; //$NON-NLS-1$

	/** The style key for CSS values. */
	String CSS_VALUE = PREFIX + "value"; //$NON-NLS-1$

// TODO: colors
//	/** The style key for CSS ... */
//	String CSS_CLASS = PREFIX + "class"; //$NON-NLS-1$

	/** The style key for CSS numbers. */
	String CSS_NUMBER = PREFIX + "number"; //$NON-NLS-1$

	/** The style key for CSS strings. */
	String CSS_STRING = PREFIX + "string"; //$NON-NLS-1$
}
