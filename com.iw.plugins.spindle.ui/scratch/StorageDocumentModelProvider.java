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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.internal.ui.text.XMLReconciler;

import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * StorageDocumentModelProvider documents and models for files in jars
 * 
 * @author glongman@gmail.com
 * @version $Id: StorageDocumentModelProvider.java,v 1.1.2.1 2004/06/22 12:13:52
 *          glongman Exp $
 */
public abstract class StorageDocumentModelProvider extends StorageDocumentProvider
    implements
      IXMLModelProvider
{
  private Map fModelMap = new HashMap();

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider#getModel(org.eclipse.ui.IEditorInput)
   */
  public XMLReconciler getModel(IEditorInput input)
  {
    IDocument document = getDocument(input);
    if (document == null)
      return null;

    return (XMLReconciler) fModelMap.get(document);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider#resetModel(org.eclipse.ui.IEditorInput)
   */
  public void resetModel(IEditorInput input)
  {
    // do nothing, these are read only files!
  }

  protected IDocument createDocument(Object element) throws CoreException
  {
    IDocument document = super.createDocument(element);

    if (document != null)
    {
      XMLReconciler model = new XMLReconciler();
      model.setDocument(document);
      document.addDocumentListener(model);
      fModelMap.put(document, model);
      TextUtilities.addDocumentPartitioners(document, createParitionerMap());
    }
    return document;
  }

  private Map createParitionerMap()
  {
    Map result = new HashMap();

    IDocumentPartitioner syntaxPartitioner = getSyntaxPartitioner();
    if (syntaxPartitioner instanceof IDocumentPartitionerExtension2)
    {
      result.put(((IDocumentPartitionerExtension2) syntaxPartitioner)
          .getManagingPositionCategories()[0], syntaxPartitioner);
    } else
    {
      result.put(IDocumentExtension3.DEFAULT_PARTITIONING, syntaxPartitioner);
    }

    XMLDocumentPartitioner structureParitioner = UIUtils.createXMLStructurePartitioner();
    result.put(
        structureParitioner.getManagingPositionCategories()[0],
        structureParitioner);

    return result;
  }

  protected abstract IDocumentPartitioner getSyntaxPartitioner();
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#disposeElementInfo(java.lang.Object,
   *      org.eclipse.ui.texteditor.AbstractDocumentProvider.ElementInfo)
   */
  protected void disposeElementInfo(Object element, ElementInfo info)
  {
    XMLReconciler model = (XMLReconciler) fModelMap.remove(info.fDocument);
    if (model != null)
      model.dispose();
    super.disposeElementInfo(element, info);
  }
}