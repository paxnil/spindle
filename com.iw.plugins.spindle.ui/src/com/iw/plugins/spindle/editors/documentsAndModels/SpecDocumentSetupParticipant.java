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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.documentsAndModels;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.spec.SpecAnnotationModel;

/**
 * SpecDocumentSetupParticipant adds setup for syntax coloring and annotations
 * 
 * @author glongman@gmail.com
 * 
 */
public class SpecDocumentSetupParticipant extends BaseDocumentSetupParticipant
{

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.BaseDocumentSetupParticipant#getSyntaxPartitioner()
   */
  protected IDocumentPartitioner getSyntaxPartitioner()
  {
    return UIPlugin.getDefault().getXMLTextTools().createXMLPartitioner();
  }

  /* (non-Javadoc)
   * @see com.iw.plugins.spindle.editors.documentsAndModels.BaseDocumentSetupParticipant#createAnnotationModel(java.lang.Object)
   */
  protected IAnnotationModel createAnnotationModel(Object element) throws CoreException
  {

    if (element instanceof IFileEditorInput)
    {
      return new SpecAnnotationModel((IFileEditorInput) element);
    }

    return null;
  }

}