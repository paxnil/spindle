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
 *  phraktle@imapmail.org
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

import com.iw.plugins.spindle.UIPlugin;

/**
 * HTML file document provider.
 * 
 * @author Igor Malinin
 */
public class TemplateStorageDocumentProvider extends StorageDocumentProvider
{
  /*
   * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
   */
  protected IDocument createDocument(Object element) throws CoreException
  {
    IDocument document = super.createDocument(element);
    if (document != null)
    {
      IDocumentPartitioner partitioner = UIPlugin
          .getDefault()
          .getTemplateTextTools()
          .createXMLPartitioner();

      if (partitioner != null)
      {
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
      }
    }

    return document;
  }
}