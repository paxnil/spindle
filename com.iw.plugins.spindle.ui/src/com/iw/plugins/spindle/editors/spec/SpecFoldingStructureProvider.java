/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.iw.plugins.spindle.editors.spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.internal.ui.text.XMLModelListener;
import org.xmen.internal.ui.text.XMLReconciler;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.documentsAndModels.IXMLModelProvider;

public class SpecFoldingStructureProvider implements IProjectionListener
{

  private SpecEditor fEditor;
  private IDocument fDocument;
  private ProjectionViewer fViewer;

  private XMLModelListener fXMLDocumentListener = new XMLModelListener()
  {

    public void modelChanged(XMLReconciler reconciler)
    {
      updateFoldingRegions(reconciler);
    }
  };
  private XMLReconciler fXMLModel;

  public SpecFoldingStructureProvider()
  {
  }

  public void install(ITextEditor editor, ProjectionViewer viewer)
  {
    if (editor instanceof SpecEditor)
    {
      fEditor = (SpecEditor) editor;
      fViewer = viewer;
      fViewer.addProjectionListener(this);
    }
  }

  public void uninstall()
  {
    if (isInstalled())
    {
      projectionDisabled();
      fViewer.removeProjectionListener(this);
      fViewer = null;
      fEditor = null;
    }
  }

  protected boolean isInstalled()
  {
    return fEditor != null;
  }

  public void initialize()
  {

    if (!isInstalled())
      return;

    initializePreferences();

    IDocumentProvider provider = fEditor.getDocumentProvider();
    fDocument = provider.getDocument(fEditor.getEditorInput());

    IXMLModelProvider xmlprovider = UIPlugin.getDefault().getXMLModelProvider();
    fXMLModel = xmlprovider.getModel(fDocument);
    fXMLModel.addListener(fXMLDocumentListener);
    updateFoldingRegions(fXMLModel);
  }

  private void initializePreferences()
  {
    // TODO For Later

  }

  public void projectionDisabled()
  {
    fDocument = null;
    if (fXMLModel != null)
      fXMLModel.removeListener(fXMLDocumentListener);

  }
  public void projectionEnabled()
  {
    projectionDisabled();
    
    initialize();

  }
  private void updateFoldingRegions(ProjectionAnnotationModel model, Set currentRegions)
  {
    Annotation[] deletions = computeDifferences(model, currentRegions);

    Map additionsMap = new HashMap();
    for (Iterator iter = currentRegions.iterator(); iter.hasNext();)
    {
      additionsMap.put(new ProjectionAnnotation(), iter.next());
    }

    if ((deletions.length != 0 || additionsMap.size() != 0))
    {
      model.modifyAnnotations(deletions, additionsMap, new Annotation[]{});
    }
  }

  private Annotation[] computeDifferences(ProjectionAnnotationModel model, Set additions)
  {
    List deletions = new ArrayList();
    for (Iterator iter = model.getAnnotationIterator(); iter.hasNext();)
    {
      Object annotation = iter.next();
      if (annotation instanceof ProjectionAnnotation)
      {
        Position position = model.getPosition((Annotation) annotation);
        if (additions.contains(position))
        {
          additions.remove(position);
        } else
        {
          deletions.add(annotation);
        }
      }
    }
    return (Annotation[]) deletions.toArray(new Annotation[deletions.size()]);
  }

  public void updateFoldingRegions(XMLReconciler reconciler)
  {
    if (reconciler.getDocument() != fDocument)
      return;
    try
    {
      ProjectionAnnotationModel model = (ProjectionAnnotationModel) fEditor
          .getAdapter(ProjectionAnnotationModel.class);
      if (model == null || reconciler == null)
        return;

      Set regions = new HashSet();
      XMLNode root = reconciler.getRoot();
      if (root == null)
        return;

      List children = new ArrayList(1);
      children.add(root);
      addFoldingRegions(regions, children, new ArrayList());
      updateFoldingRegions(model, regions);
    } catch (BadLocationException be)
    {
      UIPlugin.log(be);
    }
  }

  private void addFoldingRegions(Set regions, List children, List ignore) throws BadLocationException
  {
    // add a Position to 'regions' for each foldable region
    Iterator iter = children.iterator();
    XMLNode element = null;
    while (iter.hasNext())
    {
      element = (XMLNode) iter.next();
      String type = element.getType();
      if (ignore.contains(element) || ITypeConstants.TEXT.equals(type))
        continue;

      int startLine = -1;
      int endLine = -1;
      if (!"/".equals(type))
      {

        if (ITypeConstants.DECL.equals(type) || ITypeConstants.COMMENT.equals(type)
            || ITypeConstants.EMPTYTAG.equals(type))
        {
          startLine = fDocument.getLineOfOffset(element.getOffset());
          endLine = fDocument.getLineOfOffset(element.getOffset() + element.getLength());

        } else
        {
          startLine = fDocument.getLineOfOffset(element.getOffset());
          XMLNode corresponder = element.getCorrespondingNode();
          if (corresponder == null)
            corresponder = element;
          endLine = fDocument.getLineOfOffset(corresponder.getOffset()
              + corresponder.getLength());
          ignore.add(corresponder);
        }
        createFoldingRegion(regions, startLine, endLine);
      }
      children = element == null ? null : element.getChildren();
      if (children != null)
      {
        addFoldingRegions(regions, children, ignore);
      }
    }
  }

  private void createFoldingRegion(Set regions, int startLine, int endLine) throws BadLocationException
  {
    if (startLine < endLine)
    {
      int start = fDocument.getLineOffset(startLine);
      int end = fDocument.getLineOffset(endLine) + fDocument.getLineLength(endLine);
      Position position = new Position(start, end - start);
      regions.add(position);
    }
  }

}