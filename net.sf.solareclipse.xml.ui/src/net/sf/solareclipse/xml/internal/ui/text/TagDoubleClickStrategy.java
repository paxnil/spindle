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

import net.sf.solareclipse.text.TextDoubleClickStrategy;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class TagDoubleClickStrategy extends TextDoubleClickStrategy {
	/*
	 * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(ITextViewer)
	 */
	public void doubleClicked( ITextViewer viewer ) {
		int offset = viewer.getSelectedRange().x;
		if ( offset < 0 ) {
			return;
		}

		try {
			IDocument document = viewer.getDocument();

			ITypedRegion region = document.getPartition( offset );

			int start = region.getOffset();

			if ( offset == start && document.getChar(offset) == '<' ) {
				region = document.getPartition( offset );
				offset = region.getOffset() + region.getLength();

				if ( document.getChar(offset - 1) != '>' ) {
					while ( true ) {
						if ( offset >= document.getLength() ) {
							break;
						}

						region = document.getPartition( offset );
						offset = region.getOffset() + region.getLength();

						if ( XMLPartitionScanner.XML_ATTRIBUTE
								.equals(region.getType()) ) {
							continue;
						}

						if ( XMLPartitionScanner.XML_TAG
								.equals(region.getType()) ) {
							if ( document.getChar(region.getOffset()) == '<' ) {
								break;
							}

							if ( document.getChar(offset - 1) == '>' ) {
								break;
							}

							continue;
						}

						offset = region.getOffset();
						break;
					}
				}

				viewer.setSelectedRange( start, offset - start );
				return;
			}

			int end = start + region.getLength();

			if ( offset == end - 1 && document.getChar(offset) == '>' ) {
				region = document.getPartition( offset );
				offset = region.getOffset();

				if ( document.getChar(offset) != '<' ) {
					while ( true ) {
						if ( offset <= 0 ) {
							break;
						}

						region = document.getPartition( offset - 1 );
						offset = region.getOffset();

						if ( XMLPartitionScanner.XML_ATTRIBUTE
								.equals(region.getType()) ) {
							continue;
						}

						if ( XMLPartitionScanner.XML_TAG
								.equals(region.getType()) ) {
							if ( document.getChar(offset) == '<' ) {
								break;
							}

							continue;
						}

						offset += region.getLength();
						break;
					}
				}

				viewer.setSelectedRange( offset, end - offset );
				return;
			}

			super.doubleClicked( viewer );
		} catch ( BadLocationException e ) {}
	}
}
