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
public interface IXMLSyntaxConstants {
	/** 
	 * Note: This constant is for internal use only. Clients should not use
	 * this constant. The prefix all color constants start with.
	 */
	String PREFIX = "xml_"; //$NON-NLS-1$

	/** The style key for XML text. */
	String XML_DEFAULT = PREFIX + "text"; //$NON-NLS-1$

	/** The style key for XML tag names. */
	String XML_TAG = PREFIX + "tag"; //$NON-NLS-1$

	/** The style key for XML attribute names. */
	String XML_ATT_NAME = PREFIX + "attribute"; //$NON-NLS-1$

	/** The style key for XML attribute values. */
	String XML_ATT_VALUE = PREFIX + "string"; //$NON-NLS-1$

	/** The style key for XML entities. */
	String XML_ENTITY = PREFIX + "entity"; //$NON-NLS-1$

	/** The style key for XML processing instructions. */
	String XML_PI = PREFIX + "processing_instruction"; //$NON-NLS-1$

	/** The style key for XML CDATA sections. */
	String XML_CDATA = PREFIX + "cdata"; //$NON-NLS-1$

	/** The style key for XML comments. */
	String XML_COMMENT = PREFIX + "comment"; //$NON-NLS-1$

	/** The style key for XML declaration. */
	String XML_DECL = PREFIX + "declaration"; //$NON-NLS-1$

	/** The style key for external DTD conditional sections. */
	String DTD_CONDITIONAL = PREFIX + "conditional"; //$NON-NLS-1$
}
