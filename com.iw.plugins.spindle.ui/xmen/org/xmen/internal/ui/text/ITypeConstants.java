/*******************************************************************************
 * Copyright (c) 2000, 2003 Jens Lukowski 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *    Jens Lukowski - initial API and implementation
  *******************************************************************************/
package org.xmen.internal.ui.text;

import org.eclipse.jface.text.IDocument;

/**
 * @author jll
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ITypeConstants
{

  public final String TAG = "TAG";
  public final String TEXT = "TEXT";
  public final String PI = "PI";
  public final String DECL = "DECL";
  public final String START_DECL = "STARTDECL";
  public final String END_DECL = "ENDDECL";
  public final String COMMENT = "COMMENT";
  public final String ENDTAG = "ENDTAG";
  public final String ATTR = "ATTR";
  public final String EMPTYTAG = "EMPTYTAG";

  public final String[] TYPES = {IDocument.DEFAULT_CONTENT_TYPE, TAG, TEXT, PI,
      START_DECL, END_DECL, DECL, COMMENT, ENDTAG, EMPTYTAG};

}