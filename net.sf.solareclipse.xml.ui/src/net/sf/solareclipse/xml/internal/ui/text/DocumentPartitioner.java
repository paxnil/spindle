/**********************************************************************
Copyright (c) 2002  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:
	Igor Malinin - initial contribution

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class DocumentPartitioner extends DefaultPartitioner {
	/**
	 * Creates a new partitioner that uses the given scanner and may return 
	 * partitions of the given legal content types.
	 * 
	 * @param scanner  the scanner this partitioner is supposed to use
	 * @param legalContentTypes  the legal content types of this partitioner
	 */
	public DocumentPartitioner(
		IPartitionTokenScanner scanner, String[] legalContentTypes
	) {
		super( scanner, legalContentTypes );
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension#documentChanged2(DocumentEvent)
	 */
	public IRegion documentChanged2( DocumentEvent event ) {
        IDocument d = event.getDocument();
       String categoryLookup = CONTENT_TYPES_CATEGORY;
        Position [] category = null;
      try
        {
              category = d. getPositions( categoryLookup );
        } catch (BadPositionCategoryException e)
        {
            // Eclipse 3.0?
            categoryLookup += hashCode();
         try
            {
                   category = d. getPositions( categoryLookup );
            } catch (BadPositionCategoryException e1)
            {
              e1.printStackTrace();
            }
        }
        if (category != null) {
           
		try {			

			String contentType = IDocument.DEFAULT_CONTENT_TYPE;

			int offset = event.getOffset();

			int first = d.computeIndexInCategory( categoryLookup, offset );
			if ( first > 0 ) {
				TypedPosition partition = (TypedPosition) category[ first-1 ];
				if ( partition.includes(offset) ) {
					offset = partition.getOffset();
					contentType = partition.getType();
					--first;
				} else if ( offset == partition.getOffset() + partition.getLength() ) {
					offset = partition.getOffset();
					contentType = partition.getType();
					--first;
				} else {
					offset = partition.getOffset() + partition.getLength();
				}
			}

			fPositionUpdater.update( event );
			for ( int i = first; i < category.length; i++ ) {
				Position p = category[ i ];
				if ( p.isDeleted ) {
					fDeleteOffset= event.getOffset();
					break;
				}
			}

			category = d.getPositions( categoryLookup );

			fScanner.setPartialRange( d, offset, d.getLength(), contentType, offset );

			int lastScannedPosition = offset;
			IToken token = fScanner.nextToken();
			while ( !token.isEOF() ) {
				contentType = getTokenContentType( token );

				if ( !isSupportedContentType(contentType) ) {
					token = fScanner.nextToken();
					continue;
				}

				int start = fScanner.getTokenOffset();
				int length = fScanner.getTokenLength();

				lastScannedPosition = start + length; 

				// remove all affected positions
				while ( first < category.length ) {
					TypedPosition p = (TypedPosition) category[ first ];
					if ( p.offset + p.length < lastScannedPosition || 
							(p.overlapsWith(start, length) && 
							 	(!d.containsPosition(categoryLookup, start, length) || 
							 	 !contentType.equals(p.getType()))))
					{
						rememberRegion( p.offset, p.length );
						d.removePosition( categoryLookup, p );
						++first;
					} else {
						break;
					}
				}

				// if position already exists we are done
				if ( d.containsPosition(categoryLookup, start, length) ) {
					if ( lastScannedPosition > event.getOffset() ) {
						return createRegion();
					}

					++first;
				} else {
					// insert the new type position
					try {
						d.addPosition( categoryLookup,
							new TypedPosition(start, length, contentType) );
						rememberRegion( start, length );
					} catch ( BadPositionCategoryException x ) {
					} catch ( BadLocationException x ) {}
				}

				token = fScanner.nextToken();
			}

			// remove all positions behind lastScannedPosition since there aren't any further types
			TypedPosition p;

			first = d.computeIndexInCategory( categoryLookup, lastScannedPosition );
			while ( first < category.length ) {
				p = (TypedPosition) category[ first++ ];
				d.removePosition( categoryLookup, p );
				rememberRegion( p.offset, p.length );
			}
		} catch ( BadPositionCategoryException x ) {
			// should never happen on connected documents
		} catch ( BadLocationException x ) {}
        }

		return createRegion();
	}

	/**
	 * Helper method for tracking the minimal region containg all partition
	 * changes. If <code>offset</code> is smaller than the remembered
	 * offset, <code>offset</code> will from now on be remembered. If
	 * <code>offset  + length</code> is greater than the remembered end
	 * offset, it will be remembered from now on.
	 * 
	 * @param offset  the offset
	 * @param length  the length
	 */
	private void rememberRegion( int offset, int length ) {
		// remember start offset
		if ( fStartOffset == -1 ) {
			fStartOffset = offset;
		} else if (offset < fStartOffset) {
			fStartOffset = offset;
		}

		// remember end offset
		int endOffset= offset + length;

		if ( fEndOffset == -1 ) {
			fEndOffset = endOffset;
		} else if (endOffset > fEndOffset) {
			fEndOffset = endOffset;
		}
	}

	/**
	 * Remembers the given offset as the deletion offset.
	 * 
	 * @param offset  the offset
	 */
	private void rememberDeletedOffset( int offset ) {
		fDeleteOffset= offset;
	}

	/**
	 * Creates the minimal region containing all partition changes using
	 * the remembered offset, end offset, and deletion offset.
	 * 
	 * @return  the minimal region containing all the partition changes
	 */
	private IRegion createRegion() {
		if ( fDeleteOffset == -1 ) {
			if ( fStartOffset == -1 || fEndOffset == -1 ) {
				return null;
			}

			return new Region( fStartOffset, fEndOffset - fStartOffset );
		} else if ( fStartOffset == -1 || fEndOffset == -1 ) {
			return new Region( fDeleteOffset, 0 );
		} else {
			int start = Math.min( fDeleteOffset, fStartOffset );
			int end = Math.max( fDeleteOffset, fEndOffset );

			return new Region( start, end - start );
		}
	}
}
