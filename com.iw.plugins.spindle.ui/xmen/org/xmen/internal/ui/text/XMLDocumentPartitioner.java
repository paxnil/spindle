/*******************************************************************************
 * Copyright (c) 2000, 2003 Jens Lukowski and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *    Jens Lukowski - initial API and implementation
 *    Geoff Longman - heavily modified for Spindle
 *******************************************************************************/
package org.xmen.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;

/**
 * Complimentary paritioner. Pust postitions in a custom category.
 * 
 * @author Jens Lukowski (dark_angel@users.sourceforge.net )
 * @version $Id: XMLDocumentPartitioner.java,v 1.2 2004/04/23 03:36:51 glongman
 *          Exp $
 */
public class XMLDocumentPartitioner
    implements
      IDocumentPartitioner,
      IDocumentPartitionerExtension
{
  public static final String TAG = "TAG";
  public static final String TEXT = "TEXT";
  public static final String PI = "PI";
  public static final String DECL = "DECL";
  public static final String COMMENT = "COMMENT";
  public static final String ENDTAG = "ENDTAG";
  public static final String ATTR = "ATTR";
  public static final String EMPTYTAG = "EMPTYTAG";

  public static final String[] TYPES = {IDocument.DEFAULT_CONTENT_TYPE, TAG, TEXT, PI,
      DECL, COMMENT, ENDTAG, EMPTYTAG};

  public static RuleBasedPartitionScanner SCANNER;

  static
  {
    XMLDocumentPartitioner.SCANNER = new RuleBasedPartitionScanner();
    XMLDocumentPartitioner.SCANNER
        .setPredicateRules(new IPredicateRule[]{new XMLTagsRule()});
  }

  public static final String CONTENT_TYPES_CATEGORY = "__artifacts_category";
  protected IPartitionTokenScanner fScanner;
  protected String fLegalContentTypes[];
  protected IDocument fDocument;
  protected int fPreviousDocumentLength;
  protected DefaultPositionUpdater fPositionUpdater;
  protected int fStartOffset;
  protected int fEndOffset;
  protected int fDeleteOffset;
  protected String fCategory;

  public XMLDocumentPartitioner(IPartitionTokenScanner scanner,
      String legalContentTypes[])
  {
    this(CONTENT_TYPES_CATEGORY, scanner, legalContentTypes);
  }

  public XMLDocumentPartitioner(String useCategory, IPartitionTokenScanner scanner,
      String legalContentTypes[])
  {
    fCategory = useCategory;
    fPositionUpdater = new DefaultPositionUpdater(fCategory);
    fScanner = scanner;
    fLegalContentTypes = legalContentTypes;
  }

  public String getPositionCategory()
  {
    return fCategory;
  }

  public void connect(IDocument document)
  {
    Assert.isNotNull(document);
    Assert.isTrue(!document.containsPositionCategory(fCategory));
    fDocument = document;
    fDocument.addPositionCategory(fCategory);
    initialize();
  }

  protected void initialize()
  {
    fScanner.setRange(fDocument, 0, fDocument.getLength());
    try
    {
      for (IToken token = fScanner.nextToken(); !token.isEOF(); token = fScanner
          .nextToken())
      {
        String contentType = getTokenContentType(token);
        if (isSupportedContentType(contentType))
        {
          TypedPosition p = new XMLNode(fScanner.getTokenOffset(), fScanner
              .getTokenLength(), contentType, fDocument);
          fDocument.addPosition(fCategory, p);
        }
      }

    } catch (BadLocationException e)
    {} catch (BadPositionCategoryException e)
    {}
  }

  public void disconnect()
  {
    try
    {
      fDocument.removePositionCategory(fCategory);
    } catch (BadPositionCategoryException e)
    {}
  }

  public void documentAboutToBeChanged(DocumentEvent e)
  {
    Assert.isTrue(e.getDocument() == fDocument);
    fPreviousDocumentLength = e.getDocument().getLength();
    fStartOffset = -1;
    fEndOffset = -1;
    fDeleteOffset = -1;
  }

  public boolean documentChanged(DocumentEvent e)
  {
    return documentChanged2(e) != null;
  }

  private void rememberRegion(int offset, int length)
  {
    if (fStartOffset == -1)
    {
      fStartOffset = offset;
    } else if (offset < fStartOffset)
    {
      fStartOffset = offset;
    }
    int endOffset = offset + length;
    if (fEndOffset == -1)
    {
      fEndOffset = endOffset;
    } else if (endOffset > fEndOffset)
    {
      fEndOffset = endOffset;
    }
  }

  private void rememberDeletedOffset(int offset)
  {
    fDeleteOffset = offset;
  }

  private IRegion createRegion()
  {
    if (fDeleteOffset == -1)
      if (fStartOffset == -1 || fEndOffset == -1)
        return null;
      else
        return new Region(fStartOffset, fEndOffset - fStartOffset);
    if (fStartOffset == -1 || fEndOffset == -1)
    {
      return new Region(fDeleteOffset, 0);
    } else
    {
      int offset = Math.min(fDeleteOffset, fStartOffset);
      int endOffset = Math.max(fDeleteOffset, fEndOffset);
      return new Region(offset, endOffset - offset);
    }
  }

  public IRegion documentChanged2(DocumentEvent e)
  {
    try
    {
      IDocument d = e.getDocument();
      Position category[] = d.getPositions(fCategory);
      IRegion line = d.getLineInformationOfOffset(e.getOffset());
      int reparseStart = line.getOffset();
      int partitionStart = -1;
      String contentType = null;
      int first = d.computeIndexInCategory(fCategory, reparseStart);
      if (first > 0)
      {
        TypedPosition partition = (TypedPosition) category[first - 1];
        if (partition.includes(reparseStart))
        {
          partitionStart = partition.getOffset();
          contentType = partition.getType();
          if (e.getOffset() == partition.getOffset() + partition.getLength())
            reparseStart = partitionStart;
          first--;
        } else if (reparseStart == e.getOffset()
            && reparseStart == partition.getOffset() + partition.getLength())
        {
          partitionStart = partition.getOffset();
          contentType = partition.getType();
          reparseStart = partitionStart;
          first--;
        } else
        {
          partitionStart = partition.getOffset() + partition.getLength();
          contentType = "__dftl_partition_content_type";
        }
      }
      fPositionUpdater.update(e);
      for (int i = first; i < category.length; i++)
      {
        Position p = category[i];
        if (!p.isDeleted)
          continue;
        rememberDeletedOffset(e.getOffset());
        d.removePosition(fCategory, p);
      }

      category = d.getPositions(fCategory);
      fScanner.setPartialRange(
          d,
          reparseStart,
          d.getLength() - reparseStart,
          contentType,
          partitionStart);
      int lastScannedPosition = reparseStart;
      for (IToken token = fScanner.nextToken(); !token.isEOF();)
      {
        contentType = getTokenContentType(token);
        if (!isSupportedContentType(contentType))
        {
          token = fScanner.nextToken();
        } else
        {
          int start = fScanner.getTokenOffset();
          int length = fScanner.getTokenLength();
          lastScannedPosition = (start + length) - 1;
          for (; first < category.length; first++)
          {
            TypedPosition p = (TypedPosition) category[first];
            if (lastScannedPosition < ((Position) (p)).offset + ((Position) (p)).length
                && (!p.overlapsWith(start, length) || d.containsPosition(
                    fCategory,
                    start,
                    length)
                    && contentType.equals(p.getType())))
              break;
            rememberRegion(((Position) (p)).offset, ((Position) (p)).length);
            p.delete();
            d.removePosition(fCategory, p);
          }

          if (d.containsPosition(fCategory, start, length))
          {
            if (lastScannedPosition > e.getOffset())
              return createRegion();
            first++;
          } else
          {

            try
            {
              d
                  .addPosition(fCategory, new XMLNode(
                      start,
                      length,
                      contentType,
                      fDocument));
              rememberRegion(start, length);
            } catch (BadLocationException e1)
            {
              //do nothing
            }

          }
          token = fScanner.nextToken();
        }
      }

      if (lastScannedPosition != reparseStart)
        lastScannedPosition++;
      for (first = d.computeIndexInCategory(fCategory, lastScannedPosition); first < category.length;)
      {
        TypedPosition p = (TypedPosition) category[first++];
        p.delete();
        d.removePosition(fCategory, p);
        rememberRegion(((Position) (p)).offset, ((Position) (p)).length);
      }
    } catch (BadPositionCategoryException e1)
    {
      //do nothing
    } catch (BadLocationException e1)
    {
      //do nothing
    }
    return createRegion();
  }

  protected TypedPosition findClosestPosition(int offset)
  {
    try
    {
      int index = fDocument.computeIndexInCategory(fCategory, offset);
      Position category[] = fDocument.getPositions(fCategory);
      if (category.length == 0)
        return null;
      if (index < category.length && offset == category[index].offset)
        return (TypedPosition) category[index];
      if (index > 0)
        index--;
      return (TypedPosition) category[index];
    } catch (BadPositionCategoryException e)
    {
      // do nothing
    } catch (BadLocationException e)
    {
      // do nothing
    }
    return null;
  }

  public String getContentType(int offset)
  {
    TypedPosition p = findClosestPosition(offset);
    if (p != null && p.includes(offset))
    {
      return p.getType();
    } else
    {
      return "__dftl_partition_content_type";
    }
  }

  public ITypedRegion getPartition(int offset)
  {
    try
    {
      Position category[] = fDocument.getPositions(fCategory);
      if (category == null || category.length == 0)
        return new TypedRegion(0, fDocument.getLength(), "__dftl_partition_content_type");
      int index = fDocument.computeIndexInCategory(fCategory, offset);
      if (index < category.length)
      {
        TypedPosition next = (TypedPosition) category[index];
        if (offset == ((Position) (next)).offset)
          return new TypedRegion(next.getOffset(), next.getLength(), next.getType());
        if (index == 0)
          return new TypedRegion(
              0,
              ((Position) (next)).offset,
              "__dftl_partition_content_type");
        TypedPosition previous = (TypedPosition) category[index - 1];
        if (previous.includes(offset))
        {
          return new TypedRegion(previous.getOffset(), previous.getLength(), previous
              .getType());
        } else
        {
          int endOffset = previous.getOffset() + previous.getLength();
          return new TypedRegion(
              endOffset,
              next.getOffset() - endOffset,
              "__dftl_partition_content_type");
        }
      }
      TypedPosition previous = (TypedPosition) category[category.length - 1];
      if (previous.includes(offset))
      {
        return new TypedRegion(previous.getOffset(), previous.getLength(), previous
            .getType());
      } else
      {
        int endOffset = previous.getOffset() + previous.getLength();
        return new TypedRegion(
            endOffset,
            fDocument.getLength() - endOffset,
            "__dftl_partition_content_type");
      }
    } catch (BadPositionCategoryException e)
    {
      // do nothing
    } catch (BadLocationException e)
    {
      // do nothing
    }
    return new TypedRegion(0, fDocument.getLength(), "__dftl_partition_content_type");
  }

  public ITypedRegion[] computePartitioning(int offset, int length)
  {
    List list = new ArrayList();
    try
    {
      int endOffset = offset + length;
      Position category[] = fDocument.getPositions(fCategory);
      TypedPosition previous = null;
      TypedPosition current = null;
      Position gap = null;
      for (int i = 0; i < category.length; i++)
      {
        current = (TypedPosition) category[i];
        if (current.isDeleted())
        {
          Exception e = new Exception("w" + current);
          UIPlugin.log(e.fillInStackTrace());
        }
        int gapOffset = previous == null ? 0 : previous.getOffset()
            + previous.getLength();
        gap = new Position(gapOffset, current.getOffset() - gapOffset);
        if (gap.getLength() > 0 && gap.overlapsWith(offset, length))
        {
          int start = Math.max(offset, gapOffset);
          int end = Math.min(endOffset, gap.getOffset() + gap.getLength());
          list.add(new TypedRegion(start, end - start, "__dftl_partition_content_type"));
        }
        if (current.overlapsWith(offset, length))
        {
          int start = Math.max(offset, current.getOffset());
          int end = Math.min(endOffset, current.getOffset() + current.getLength());
          list.add(new TypedRegion(start, end - start, current.getType()));
        }
        previous = current;
      }

      if (previous != null)
      {
        int gapOffset = previous.getOffset() + previous.getLength();
        gap = new Position(gapOffset, fDocument.getLength() - gapOffset);
        if (gap.getLength() > 0 && gap.overlapsWith(offset, length))
        {
          int start = Math.max(offset, gapOffset);
          int end = Math.min(endOffset, fDocument.getLength());
          list.add(new TypedRegion(start, end - start, "__dftl_partition_content_type"));
        }
      }
      if (list.isEmpty())
        list.add(new TypedRegion(offset, length, "__dftl_partition_content_type"));
    } catch (BadPositionCategoryException e)
    {
      // do nothing
    }
    TypedRegion result[] = new TypedRegion[list.size()];
    list.toArray(result);
    return result;
  }

  public String[] getLegalContentTypes()
  {
    return fLegalContentTypes;
  }

  protected boolean isSupportedContentType(String contentType)
  {
    if (contentType != null)
    {
      for (int i = 0; i < fLegalContentTypes.length; i++)
      {
        if (fLegalContentTypes[i].equals(contentType))
          return true;
      }
    }
    return false;
  }

  protected String getTokenContentType(IToken token)
  {
    Object data = token.getData();
    if (data instanceof String)
    {
      return (String) data;
    } else
    {
      return null;
    }
  }

}