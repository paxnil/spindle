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
package com.iw.plugins.spindle.editors.documentsAndModels;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.xmen.internal.ui.text.XMLReconciler;

/**
 * Document provider for xml files that have a model
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: FileDocumentModelProvider.java,v 1.1.2.1 2004/06/22 12:13:52
 *                     glongman Exp $
 */
public  class SpindleStorageDocumentProvider extends StorageDocumentProvider
{

  private BaseDocumentSetupParticipant fSetupParticipant;

  public SpindleStorageDocumentProvider(BaseDocumentSetupParticipant setupParticipant)
  {
    super();
    fSetupParticipant = setupParticipant;
  }

  protected IDocument createDocument(Object element) throws CoreException
  {
    IDocument document = super.createDocument(element);

    if (document != null)
    {
      XMLReconciler model = fSetupParticipant.setup(document);
      fSetupParticipant.addModel(document, model);
    }
    return document;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#disposeElementInfo(java.lang.Object,
   *              org.eclipse.ui.texteditor.AbstractDocumentProvider.ElementInfo)
   */
  protected void disposeElementInfo(Object element, ElementInfo info)
  {
    XMLReconciler model = fSetupParticipant.removeModel(info.fDocument);

    if (model != null)
      info.fDocument.removeDocumentListener(model);

    super.disposeElementInfo(element, info);
  }
}