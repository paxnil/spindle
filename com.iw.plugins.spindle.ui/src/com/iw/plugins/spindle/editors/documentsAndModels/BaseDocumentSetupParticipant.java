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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.internal.ui.text.XMLReconciler;

import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * BaseDocumentSetupParticipant TODO add something here
 * 
 * @author glongman@gmail.com
 * 
 */
public abstract class BaseDocumentSetupParticipant implements IXMLModelProvider
{

  private static Map MODEL_MAP = Collections.synchronizedMap(new HashMap());

  static void addModel(IDocument document, XMLReconciler model)
  {
    Assert.isTrue(!MODEL_MAP.containsKey(document));
    MODEL_MAP.put(document, model);
  }

  public static XMLReconciler removeModel(IDocument document) 
  {
    XMLReconciler model = (XMLReconciler) MODEL_MAP.remove(document);
    if (model != null)
    {
      model.dispose();     
    }
    return model;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider#getModel(org.eclipse.jface.text.IDocument)
   */
  public XMLReconciler getModel(IDocument document)
  {
    return (XMLReconciler) MODEL_MAP.get(document);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider#resetModel(org.eclipse.jface.text.IDocument)
   */
  public void resetModel(IDocument document)
  {
    XMLReconciler model = getModel(document);
    if (model != null)
      model.reset();
  }

  /**
   * Setup the document with syntax coloring and XML partitioning. A
   * XMLReconciler model for the document is created and returned. But not
   * stored anywhere.
   * 
   * @param document the document to setup
   * @return the model created.
   */
  public XMLReconciler setup(IDocument document)
  {
    XMLReconciler model = new XMLReconciler();
    model.setDocument(document);
    document.addDocumentListener(model);
    TextUtilities.addDocumentPartitioners(document, createParitionerMap());
    model.setDocument(document);
    return model;
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
  
  protected abstract IAnnotationModel createAnnotationModel(Object element) throws CoreException;

}