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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;

import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;

/**
 * Text Hover for Editor annotations
 * 
 * @author glongman@intelligentworks.com
 * 
 */
public class DefaultTextHover implements ITextHover
{

  Editor fEditor;

  public DefaultTextHover(Editor editor)
  {
    fEditor = editor;
  }

  /*
   * Formats a message as HTML text.
   */
  private String formatMessage(String message)
  {
    return message;
  }

  /*
   * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
   */
  public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
  {
    if (fEditor == null)
      return null;

    try
    {
      ITypedRegion typedRegion = textViewer.getDocument().getPartition(
          hoverRegion.getOffset());

      ContentAssistant assistant = fEditor.getContentAssistant();
      if (assistant == null)
        return null;

      synchronized (fEditor)
      {
        ContentAssistProcessor assister = (ContentAssistProcessor) assistant
            .getContentAssistProcessor(typedRegion.getType());
        if (assister != null)
        {
          IContextInformation[] infos = assister.computeInformation(
              textViewer,
              hoverRegion.getOffset());
          if (infos != null && infos.length > 0)
            return infos[0].getInformationDisplayString();
        }
      }

    } catch (BadLocationException e)
    {      
      e.printStackTrace();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer,
   *      int)
   */
  public IRegion getHoverRegion(ITextViewer textViewer, int offset)
  {
    return new Region(offset, 0);
  }

}