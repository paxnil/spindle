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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.internal.ui.text.XMLReconciler;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.template.TemplateTextTools;

/**
 * Document provider for xml files that have a model
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: FileDocumentModelProvider.java,v 1.1.2.1 2004/06/22 12:13:52
 *          glongman Exp $
 */
public class FileDocumentModelProvider extends FileDocumentProvider
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
    XMLReconciler model = getModel(input);
    if (model != null)
      model.reset();

  }

  protected IDocument createDocument(Object element) throws CoreException
  {
    IDocument document = super.createDocument(element);

    if (document != null)
    {
      TextUtilities.addDocumentPartitioners(document, createParitionerMap());
      XMLReconciler model = new XMLReconciler();
      model.createTree(document);
      document.addDocumentListener(model);
      fModelMap.put(document, model);
    }
    return document;
  }

  private Map createParitionerMap()
  {
    Map result = new HashMap();
    TemplateTextTools tools = UIPlugin.getDefault().getTemplateTextTools();

    DefaultPartitioner syntaxPartitioner = tools.createXMLPartitioner();
    result.put(syntaxPartitioner.getManagingPositionCategories()[0], syntaxPartitioner);

    XMLDocumentPartitioner structureParitioner = tools.createXMLStructurePartitioner();
    result.put(
        structureParitioner.getManagingPositionCategories()[0],
        structureParitioner);

    return result;
  }

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