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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.spec.assist.usertemplates;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;


public class UserTemplateContext extends DocumentTemplateContext
{

  /**
   * @param type
   * @param document
   * @param completionOffset
   * @param completionLength
   */
  public UserTemplateContext(TemplateContextType type, IDocument document,
      int completionOffset, int completionLength)
  {
    super(type, document, completionOffset, completionLength);
    // TODO Auto-generated constructor stub
  }
  
  /* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {

		TemplateBuffer templateBuffer= super.evaluate(template);
		
		if (templateBuffer == null) {
			return null;
		}
		//TODO Not enabled see bug 55356
//		if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
//			FormattingPreferences prefs = new FormattingPreferences();
//			XmlFormatter.format(templateBuffer, this, prefs);
//		}
		return templateBuffer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.DocumentTemplateContext#getEnd()
	 */
	public int getEnd() {
		int replacementOffset = getCompletionOffset();
		int replacementLength = getCompletionLength();
		if (replacementOffset > 0 && getDocument().get().charAt(replacementOffset - 1) == '<') {
			replacementLength++;
		}
		return replacementLength;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.DocumentTemplateContext#getStart()
	 */
	public int getStart() {
		int replacementOffset= getCompletionOffset();
		if (replacementOffset > 0 && getDocument().get().charAt(replacementOffset - 1) == '<') {
			replacementOffset--;
		}
		return replacementOffset;
	}

}
