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
 * The Original Code is XMLContentFormatter
 *
 * The Initial Developer of the Original Code is
 * Christian Sell <christian.sell@netcologne.de>.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *  christian.sell@netcologne.de
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.DefaultPartitioner;

import com.iw.plugins.spindle.core.util.Assert;

/**
 * a formatter for XML content.
 * 
 * <em>This code is partially modelled after Eclipses ContentFormatter in non-partition-aware 
 * mode. Position management is basically copied from the original, formatting is delgeated to a
 * strategy object (not the org.eclipse.jface.text.formatter.IFormattingStrategy interface).</em>
 * 
 * Modified again by GWL to work in the new Master/Slave formatting setup in
 * Eclipse 3.0
 * 
 * @author cse
 * @version $Id: XMLContentFormatter.java,v 1.1.4.1 2004/06/10 16:48:19 glongman
 *                     Exp $
 */
public class XMLContentFormatter
{
//  public static interface FormatWorker
//  {
//    /**
//     * Do the actual formatting. The document should not be modified by this
//     * method. Instead, the formatted string must be returned and will be used
//     * by the caller to replace the document text, after synchronizing
//     * positionings.
//     * 
//     * @param prefs the formatter preferences
//     * @param document the document containing the region to be formatted.
//     * @param offset the offset into the document
//     * @param length length of the region to format
//     * @param positions positions that must be maintained by the formatter
//     * @return the formatted string to be inserted in place of the selected
//     *                 region
//     */
//    String format(
//        FormattingPreferences prefs,
//        IDocument document,
//       TypedPosition position,
//        int[] positions);
//
//  }

  /**
   * Defines a reference to either the offset or the end offset of a particular
   * position.
   */
  static class PositionReference implements Comparable
  {

    /** The referenced position */
    protected Position fPosition;
    /** The reference to either the offset or the end offset */
    protected boolean fRefersToOffset;
    /** The original category of the referenced position */
    protected String fCategory;
    /** true if this is an end offset duplicate for an existing offset reference */
    protected boolean fDuplicate;

    /**
     * @param position the position to be referenced
     * @param refersToOffset <code>true</code> if position offset should be
     *                     referenced
     * @param category the categpry the given position belongs to
     * @param isDuplicate whether this is a duplicate reference, if both offset
     *                     and length overlap
     */
    protected PositionReference(Position position, boolean refersToOffset,
        String category, boolean isDuplicate)
    {
      fPosition = position;
      fRefersToOffset = refersToOffset;
      fCategory = category;
      fDuplicate = isDuplicate;
    }

    protected int getOffset()
    {
      return fPosition.getOffset();
    }

    protected void setOffset(int offset)
    {
      fPosition.setOffset(offset);
    }

    protected int getLength()
    {
      return fPosition.getLength();
    }

    protected void setLength(int length)
    {
      fPosition.setLength(length);
    }

    protected boolean isDuplicate()
    {
      return fDuplicate;
    }

    /**
     * @return <code>true</code> if the offset of the position is referenced,
     *                 <code>false</code> otherwise
     */
    protected boolean refersToOffset()
    {
      return fRefersToOffset;
    }

    /**
     * @return the category of the referenced position
     */
    protected String getCategory()
    {
      return fCategory;
    }

    /**
     * Returns the referenced position.
     * 
     * @return the referenced position
     */
    protected Position getPosition()
    {
      return fPosition;
    }

    /**
     * Returns the referenced character position
     * 
     * a check is done to ensure that a zero length position returns its offset
     * as the character position for its length.
     * 
     * @return the referenced character position
     */
    protected int getCharacterPosition()
    {
      if (fRefersToOffset)
      {
        return getOffset();
      } else
      {
        return getLength() == 0 ? getOffset() : getOffset() + getLength() - 1;
      }

    }

    /**
     * set the referenced character position
     * 
     * a check is done to ensure that a zero length postion remains so.
     * 
     * @param the updated position
     */
    public void setCharacterPosition(int position)
    {
      if (refersToOffset())
      {
        setOffset(position);
      } else
      {
        setLength(getLength() == 0 ? 0 : position - getOffset() + 1);
      }
    }

    /*
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object obj)
    {

      if (obj instanceof PositionReference)
      {
        PositionReference r = (PositionReference) obj;
        return getCharacterPosition() - r.getCharacterPosition();
      }

      throw new ClassCastException();
    }
  }

  /**
   * The position updater which runs as first updater on the document's
   * positions. Used to remove all affected positions from their categories to
   * avoid them from being regularily updated.
   * 
   * @see IPositionUpdater
   */
  class RemoveAffectedPositions implements IPositionUpdater
  {
    /**
     * @see IPositionUpdater#update(DocumentEvent)
     */
    public void update(DocumentEvent event)
    {
      removeAffectedPositions(event.getDocument());
    }
  };

  /**
   * The position updater which runs as last updater on the document's
   * positions. Used to update all affected positions and adding them back to
   * their original categories.
   * 
   * @see IPositionUpdater
   */
  class UpdateAffectedPositions implements IPositionUpdater
  {

    /** The affected positions */
    private int[] fPositions;
    /** The offset */
    private int fOffset;

    /**
     * Creates a new updater.
     * 
     * @param positions the affected positions
     * @param offset the offset
     */
    public UpdateAffectedPositions(int[] positions, int offset)
    {
      fPositions = positions;
      fOffset = offset;
    }

    /*
     * @see IPositionUpdater#update(DocumentEvent)
     */
    public void update(DocumentEvent event)
    {
      updateAffectedPositions(event.getDocument(), fPositions, fOffset);
    }
  };

  /** The partition information managing document position categories */
  private String[] fPartitionManagingCategories;
  /**
   * The list of references to offset and end offset of all overlapping
   * positions
   */
  private List fOverlappingPositionReferences;
  /** The strategy used to do the actual formatting */
  private FormatWorker fFormatWorker; 
  /** The store to pull formatting preferences from */
  private FormattingPreferences fFormattingPreferences;
  /** Display tab width - set on construction only */

  /**
   * @param partitioningCategories the position categories which are used to
   *                     manage the document's partitioning information and thus should be
   *                     ignored when this formatter updates positions
   */
  public XMLContentFormatter(FormatWorker formatWorker,
      String[] partitioningCategories, FormattingPreferences formattingPreferences)
  {
    Assert.isLegal(!formatWorker.usesEdits());
    fPartitionManagingCategories = partitioningCategories;
    fFormatWorker = formatWorker;
    fFormattingPreferences = formattingPreferences;
  }

  public void format(IDocument document, TypedPosition partition)
  {

    try
    {

      final int[] positions = getAffectedPositions(
          document,
          partition.offset,
          partition.length);

      String formatted = (String) fFormatWorker.format(
          fFormattingPreferences,
          document,
          partition,
          positions);

      if (formatted != null)
      {
        IPositionUpdater first = new RemoveAffectedPositions();
        document.insertPositionUpdater(first, 0);
        IPositionUpdater last = new UpdateAffectedPositions(positions, partition.offset);
        document.addPositionUpdater(last);

        document.replace(partition.offset, partition.length, formatted);

        document.removePositionUpdater(first);
        document.removePositionUpdater(last);
      }
    } catch (BadLocationException x)
    {
      // should not happen
    }
  }

  /**
   * Returns all offset and the end offset of all positions overlapping with the
   * specified document range.
   * 
   * @param document the document to be formatted
   * @param offset the offset of the document region to be formatted
   * @param length the length of the document to be formatted
   * @return all character positions of the interleaving positions
   */
  private int[] getAffectedPositions(IDocument document, int offset, int length)
  {

    fOverlappingPositionReferences = new ArrayList();

    determinePositionsToUpdate(document, offset, length);

    Collections.sort(fOverlappingPositionReferences);

    int[] positions = new int[fOverlappingPositionReferences.size()];
    for (int i = 0; i < positions.length; i++)
    {
      PositionReference r = (PositionReference) fOverlappingPositionReferences.get(i);
      positions[i] = r.getCharacterPosition();
    }

    return positions;
  }

  /**
   * Determines all embracing, overlapping, and follow up positions for the
   * given region of the document.
   * 
   * @param document the document to be formatted
   * @param offset the offset of the document region to be formatted
   * @param length the length of the document to be formatted
   */
  private void determinePositionsToUpdate(IDocument document, int offset, int length)
  {

    String[] categories = document.getPositionCategories();
    if (categories != null)
    {
      for (int i = 0; i < categories.length; i++)
      {

        if (ignoreCategory(categories[i]))
          continue;

        try
        {

          Position[] positions = document.getPositions(categories[i]);

          for (int j = 0; j < positions.length; j++)
          {

            Position p = (Position) positions[j];
            if (p.overlapsWith(offset, length))
            {

              boolean duplicate = false;

              if (offset < p.getOffset())
              {
                duplicate = true;
                fOverlappingPositionReferences.add(new PositionReference(
                    p,
                    true,
                    categories[i],
                    false));
              }
              if (p.getOffset() + p.getLength() < offset + length)
                fOverlappingPositionReferences.add(new PositionReference(
                    p,
                    false,
                    categories[i],
                    duplicate));
            }
          }

        } catch (BadPositionCategoryException x)
        {
          // can not happen
        }
      }
    }
  }

  /**
   * Removes the affected positions from their categories to avoid that they are
   * invalidly updated.
   * 
   * @param document the document
   */
  private void removeAffectedPositions(IDocument document)
  {
    int size = fOverlappingPositionReferences.size();
    for (int i = 0; i < size; i++)
    {
      PositionReference r = (PositionReference) fOverlappingPositionReferences.get(i);
      try
      {
        document.removePosition(r.getCategory(), r.getPosition());
      } catch (BadPositionCategoryException x)
      {
        // can not happen
      }
    }
  }

  /**
   * Updates all the overlapping positions. Note, all other positions are
   * automatically updated by their document position updaters.
   * 
   * @param document the document to has been formatted
   * @param positions the adapted character positions to be used to update the
   *                     document positions
   * @param offset the offset of the document region that has been formatted
   */
  private void updateAffectedPositions(IDocument document, int[] positions, int offset)
  {

    if (positions.length == 0)
      return;
    int count = 0;
    // things get wonky because its (the old way) not guranteed that the offset
    // of positions is
    // updated first. If the offset is not updated first, then the length *is*.
    // And the length will always be wrong if the offset moved and has not been
    // updated yet.

    //to fix: for now.
    // must iterate over the posistions 3 times
    //
    // first: update all the offsets
    // second: update all the lengths and add the updated positions
    //

    //update the position offsets
    for (int i = 0; i < positions.length; i++)
    {
      PositionReference r = (PositionReference) fOverlappingPositionReferences.get(i);
      if (r.refersToOffset())
        r.setCharacterPosition(positions[i]);
    }

    //update the position lengths and add the postions back into the document
    for (int i = 0; i < positions.length; i++)
    {
      PositionReference r = (PositionReference) fOverlappingPositionReferences.get(i);
      if (!r.refersToOffset())
        r.setCharacterPosition(positions[i]);
      Position p = r.getPosition();
      String category = r.getCategory();
      if (!r.isDuplicate())
      { //document.containsPosition(category, p.offset, p.length)) { WHOA!!
        try
        {
          if (positionAboutToBeAdded(document, category, p))
          {
            count++;
            document.addPosition(r.getCategory(), p);
          }
        } catch (BadPositionCategoryException x)
        {
          // can not happen
        } catch (BadLocationException x)
        {
          // should not happen
        }
      }
    }
    // old code
    //        for (int i = 0; i < positions.length; i++)
    //        {
    //
    //            PositionReference r = (PositionReference)
    // fOverlappingPositionReferences.get(i);
    //            r.setCharacterPosition(positions[i]);
    //
    //            Position p = r.getPosition();
    //            String category = r.getCategory();
    //            if (!r.isDuplicate())
    //            { //document.containsPosition(category, p.offset, p.length)) { WHOA!!
    //                try
    //                {
    //                    if (positionAboutToBeAdded(document, category, p))
    //                    {
    //                        count++;
    //                        document.addPosition(r.getCategory(), p);
    //                    }
    //                } catch (BadPositionCategoryException x)
    //                {
    //                    // can not happen
    //                } catch (BadLocationException x)
    //                {
    //                    // should not happen
    //                }
    //            }
    //        }
    fOverlappingPositionReferences = null;
  }

  /**
   * The given position is about to be added to the given position category of
   * the given document.
   * <p>
   * This default implementation enacts the same rule as the TextViewer, i.e. if
   * the position is used for managing slave documents it is ensured that the
   * slave document starts at a line offset.
   * 
   * @param document the document
   * @param category the position categroy
   * @param position the position that will be added
   * @return <code>true</code> if the position can be added,
   *                 <code>false</code> if it should be ignored
   */
  private boolean positionAboutToBeAdded(
      IDocument document,
      String category,
      Position position)
  {
    //TODO revisit postionAboutToBeAdded in TextViewer
    //        if (ProjectionDocument..equals(category))
    //        {
    //            /*
    //             * We assume child document offsets to be at the beginning
    //             * of a line. Because the formatter might have moved the
    //             * position to be somewhere in the middle of a line we patch it here.
    //             */
    //            try
    //            {
    //                int lineOffset =
    // document.getLineInformationOfOffset(position.offset).getOffset();
    //                position.setLength(position.length + position.offset - lineOffset);
    //                position.setOffset(lineOffset);
    //            } catch (BadLocationException x)
    //            {
    //                return false;
    //            }
    //        }
    return true;
  }

  private boolean ignoreCategory(String category)
  {

    if (fPartitionManagingCategories != null)
    {
      for (int i = 0; i < fPartitionManagingCategories.length; i++)
      {
        String ignore = fPartitionManagingCategories[i];
        if (ignore == DefaultPartitioner.CONTENT_TYPES_CATEGORY
            && category.startsWith(ignore))
        {
          return true;
        } else if (ignore.equals(category))
        {
          return true;
        }
      }
    }
    return false;
  }
}