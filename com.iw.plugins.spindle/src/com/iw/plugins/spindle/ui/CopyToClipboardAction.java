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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;


import com.iw.plugins.spindle.util.StringReplacer;

public class CopyToClipboardAction extends Action {
	
  private Clipboard clipboard;
  
  public CopyToClipboardAction(Display display, String valueToCopy) {
  	setText(valueToCopy);
    clipboard = new Clipboard(display);
  }
  
  /**
    * Constructor for CopyToClipboardAction
    */
  public CopyToClipboardAction(Display display, String template, String key, String value) {
    super();
    StringReplacer replacer = new StringReplacer(template);
    replacer.replace(key, value);
    setText(replacer.toString());
    clipboard = new Clipboard(display);    
  }

  /**
  * @see Action#run()
  */
  public void run() {
    if (!"".equals(getText().trim())) {
      TextTransfer transfer = TextTransfer.getInstance();
      try {
        clipboard.setContents(new String[] { getText()}, new Transfer[] { transfer });
      } catch (SWTError e) {
        //ignore - some other app was accessing the
        //clipboard
      }
    }
  }
}