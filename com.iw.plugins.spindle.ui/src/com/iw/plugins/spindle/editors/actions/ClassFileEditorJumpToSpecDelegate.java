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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.actions;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.ui.IEditorInput;

/**
 * Action to Jump from a class file editor to a related tapestry spec (if one exists)
 * 
 * @author glongman@gmail.com
 * @version $Id: ClassFileEditorJumpToSpecDelegate.java,v 1.1 2004/05/06
 *          17:21:16 glongman Exp $
 */
public class ClassFileEditorJumpToSpecDelegate extends CUEditorJumpToSpecDelegate
{

  protected Object getInputObject(IEditorInput input)
  {
    return input.getAdapter(IClassFile.class);
  }

}