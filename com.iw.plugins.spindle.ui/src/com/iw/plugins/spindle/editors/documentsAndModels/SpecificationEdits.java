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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.xmen.internal.ui.text.XMLReconciler;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryException;
import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.assist.DTDAccess;
import com.iw.plugins.spindle.ui.util.UIUtils;
import com.wutka.dtd.DTD;

/**
 * SpecificationEdits base class for creating an Edit to change a specification
 * file.
 * 
 * Instances are good for one insert only!
 * 
 * @author glongman@gmail.com
 *  
 */
public abstract class SpecificationEdits
{

  protected class EditFactory
  {
    // the offset for the created edit
    int offset = 0;
    // this is set if a replace is required (empty tag only!)
    XMLNode node;
    TextEdit getTextEdit(String content)
    {

      if (node != null)
      {
        // its an empty node that must be converted to a non empty one.
        MultiTextEdit edit = new MultiTextEdit(node.offset, node.length);
        int lastOffset = node.offset + node.length - 1;
        //change to non empty tag
        edit.addChild(new ReplaceEdit(lastOffset - 1, 2, ">"));
        edit.addChild(new InsertEdit(lastOffset + 1, fLineDelimiter + content
            + fLineDelimiter + "</" + node.getName() + ">"));

        return edit;

      } else
      {
        return new InsertEdit(offset, content);
      }
    }
  }

  protected IDocument fDocument;
  private IDocument fOriginalDocument;
  protected String fLineDelimiter;
  private TextEdit fEdit;
  private boolean alreadyShot = false;
  private XMLReconciler model;

  /**
   *  
   */
  public SpecificationEdits(IDocument document)
  {
    fDocument = document;
    fOriginalDocument = document;
    fEdit = null;
    fLineDelimiter = UIUtils.getLineDelimiter(document);
    IXMLModelProvider modelProvider = UIPlugin.getDefault().getXMLModelProvider();
    model = modelProvider.getModel(fDocument);

    if (model == null)
    {
      fDocument = new Document();
      fDocument.set(fOriginalDocument.get());
      model = new SpecDocumentSetupParticipant().setup(fDocument);
    }
  }

  protected void setEdit(TextEdit edit)
  {
    Assert.isLegal(fEdit == null, "good for one shot only!");
    fEdit = edit;
  }

  public void apply() throws MalformedTreeException, BadLocationException
  {
    try
    {
      Assert.isLegal(!alreadyShot, "good for one shot only!");
      fEdit.apply(fDocument);
    } finally
    {
      alreadyShot = true;
      if (fDocument != fOriginalDocument)
      {
        fOriginalDocument.set(fDocument.get());
        new SpecDocumentSetupParticipant().removeModel(fDocument);
        fDocument = fOriginalDocument;
      }
    }
  }

  protected EditFactory getFactoryFromDocumentPartitioning(String desiredTag) throws TapestryException
  {

    EditFactory factory = new EditFactory();

    XMLNode rootNode = model.getRoot();

    XMLNode documentRoot = null;
    for (Iterator iter = rootNode.getChildren().iterator(); iter.hasNext();)
    {
      XMLNode element = (XMLNode) iter.next();
      if (element.isTagOrEmptyTag())
      {
        documentRoot = element;
        break;
      }
    }

    if (documentRoot == null)
    {
      factory.offset = getDocumentInsertBeforeOffset(fDocument.getLength() - 1);
      return factory;
    }

    if (documentRoot.isEmptyTag())
    {
      factory.node = documentRoot;
    } else if (documentRoot.isTag())
    {
      //normal handling here.
      List children = documentRoot.getChildren();

      //only interested in tag or empty tag!
      for (Iterator iter = children.iterator(); iter.hasNext();)
      {
        XMLNode element = (XMLNode) iter.next();
        if (!element.isTagOrEmptyTag())
          iter.remove();
      }

      Collections.sort(children, XMLNode.COMPARATOR);

      if (children.isEmpty())
      {
        factory.offset = getDocumentInsertAfterOffset(documentRoot.offset + documentRoot.length);
      } else if (desiredTag.equals(children.get(0)))
      {

        XMLNode element = (XMLNode) children.get(0);

        factory.offset = getDocumentInsertBeforeOffset(element.offset);

      } else
      {

        String publicId = rootNode.publicId;
        DTD dtd = null;
        if (publicId == null)
        {
          dtd = getDefaultDTD();
        } else
        {
          dtd = DOMValidator.getDTD(publicId);
          if (dtd == null)
            dtd = getDefaultDTD();
        }

        String rootNodeName = rootNode.rootNodeId == null
            ? getExpectedRootNodeName() : rootNode.rootNodeId;

        //must do this as the result of getAllowedChildren is unmodifiable
        List elements = new ArrayList();
        elements.addAll(DTDAccess.getAllowedChildren(dtd, rootNodeName, null, false));

        //rip through and see if we found an exact match!
        XMLNode found = null;

        for (Iterator iter = children.iterator(); iter.hasNext();)
        {
          XMLNode element = (XMLNode) iter.next();
          if (element.isTagOrEmptyTag() && desiredTag.equals(element.getName()))
          {
            found = element;
            break;
          }
        }

        if (found != null)
        {
          factory.offset = getDocumentInsertBeforeOffset(found.getOffset());
        } else
        {
          //go until we find the first allowed tag *after* the desired one.
          boolean hitDesired = false;

          for (Iterator iter = children.iterator(); iter.hasNext();)
          {
            if (elements.isEmpty())
              break;

            XMLNode element = (XMLNode) iter.next();
            String name = element.getName();

            if (element.isTagOrEmptyTag() && name != null)
            {
              found = element;
              if (elements.get(0).equals(desiredTag))
              {
                hitDesired = true;
                while (iter.hasNext())
                {
                  XMLNode n = (XMLNode) iter.next();
                  if (n.isTagOrEmptyTag())
                    found = n;
                  break;
                }
                break;
              } else
              {
                elements.remove(name.toLowerCase());
              }
            }
          }

          if (found == null)
          {
            factory.offset = getDocumentInsertAfterOffset(documentRoot.offset
                + documentRoot.length);
          } else if (hitDesired)
          {
            factory.offset = getDocumentInsertBeforeOffset(found.offset);
          } else
          {
            factory.offset = getDocumentInsertAfterOffset(found);
          }
        }
      }
    }
    return factory;
  }
  protected final int getDocumentInsertBeforeOffset(int desiredOffset) throws TapestryException
  {
    try
    {
      return fDocument.getLineOffset(fDocument.getLineOfOffset(desiredOffset));
    } catch (BadLocationException e)
    {
      TapestryCorePlugin.throwErrorException(e.getMessage());
    }
    return -1;
  }

  protected final int getDocumentInsertAfterOffset(int desiredOffset) throws TapestryException
  {
    try
    {
      int line = fDocument.getLineOfOffset(desiredOffset);
      line = line == fDocument.getNumberOfLines() ? fDocument.getLength() - 1 : line + 1;
      return fDocument.getLineOffset(line);
    } catch (BadLocationException e)
    {
        TapestryCorePlugin.throwErrorException(e.getMessage());
    }
    return -1;
  }

  private final int getDocumentInsertAfterOffset(XMLNode node) throws TapestryException
  {
    XMLNode correspondingNode = node.getCorrespondingNode();
    if (node.isEmptyTag() || correspondingNode == null)
      return getDocumentInsertAfterOffset(node.offset + node.length);

    return getDocumentInsertAfterOffset(correspondingNode.offset
        + correspondingNode.length);
  }

  protected abstract DTD getDefaultDTD();
  protected abstract String getExpectedRootNodeName();
}