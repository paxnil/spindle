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
package net.sf.solareclipse.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class EditorMessages {
	private static ResourceBundle bundle = ResourceBundle
		.getBundle("net.sf.solareclipse.editor.EditorMessages"); //$NON-NLS-1$

	private EditorMessages() {}

	public static String getString( String key ) {
		try {
			return bundle.getString( key );
		} catch ( MissingResourceException e ) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
