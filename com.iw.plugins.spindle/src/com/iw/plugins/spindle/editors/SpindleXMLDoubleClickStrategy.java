package com.iw.plugins.spindle.editors;

import java.text.BreakIterator;
import java.text.CharacterIterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.pde.internal.ui.editor.text.PDEPartitionScanner;
import sun.security.krb5.internal.i;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class SpindleXMLDoubleClickStrategy implements ITextDoubleClickStrategy {

  private DocumentCharacterIterator documentIterator = new DocumentCharacterIterator();

  /**
   * Constructor for SpindleDoubleClickStrategy.
   */
  public SpindleXMLDoubleClickStrategy() {
    super();
  }

  /**
   * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(ITextViewer)
   */
  public void doubleClicked(ITextViewer text) {
    int position = text.getSelectedRange().x;

    if (position < 0)
      return;

    try {

      final IDocument document = text.getDocument();

      IRegion line = document.getLineInformationOfOffset(position);

      if (position == line.getOffset() + line.getLength()) {
        return;
      }

      if (!false) { //!selectComment(document, position, text)) {

        if (!selectTag(document, position, text)) {

          documentIterator.setDocument(document, line);
          documentIterator.setIndex(position);
          BreakIterator breakIter = BreakIterator.getWordInstance();
          breakIter.setText(documentIterator);

          int start = breakIter.preceding(position);
          if (start == BreakIterator.DONE)
            start = line.getOffset();

          int end = breakIter.following(position);
          if (end == BreakIterator.DONE)
            end = line.getOffset() + line.getLength();

          if (start != end)
            text.setSelectedRange(start, end - start);
        }
      }

    } catch (BadLocationException x) {
    }
  }

  /**
   * Method findComment.
   * @param documentIterator
   * @return boolean
   */
  private boolean selectComment(IDocument document, int position, ITextViewer text)
    throws BadLocationException {

    ITypedRegion region = document.getPartition(position);
    if (region.getType().equals(PDEPartitionScanner.XML_COMMENT)) {

      if (position - region.getOffset() < 4) {

        text.setSelectedRange(region.getOffset(), region.getLength());

        return true;
      }
    }

    return false;
  }

  /**
   * Method selectComment.
   * @param documentIterator
   */
  private boolean selectTag(IDocument document, int position, ITextViewer text)
    throws BadLocationException {

    ITypedRegion region = document.getPartition(position);


    if (region.getType().equals(PDEPartitionScanner.XML_TAG)) {

      final int regionOffset = region.getOffset();
      final int documentLength = document.getLength();

      IRegion regionToEnd = new IRegion() {

        public int getLength() {
          return documentLength - regionOffset;
        }

        public int getOffset() {
          return regionOffset;
        }

      };

      documentIterator.setDocument(document, regionToEnd);

      documentIterator.setIndex(regionOffset);

      char c = documentIterator.next(true);

      // we must eliminate the case of double clicking in: </end>

      if (c == documentIterator.DONE || c == '/') {

        return false;

      }

      int count = 1;

      // now we count the length of the tag element name

      while (c != documentIterator.DONE) {

        if (Character.isWhitespace(c) || c == '/' || c == '>') {

          break;
        }

        count++;
        c = documentIterator.next();
      }

      // if our double click location <= length of bracket+element name we proceed

      if (c != documentIterator.DONE && (position - region.getOffset() < count)) {

        documentIterator.setIndex(region.getOffset() + region.getLength() - 1);

        c = documentIterator.previous(true);

        // short circuit this if the tag we've double clicked in is like <a/>
        if (c == '/') {

          text.setSelectedRange(region.getOffset(), region.getLength());

          return true;
        }

        documentIterator.setIndex(regionOffset);

        int openBrackets = 1;

        c = documentIterator.next();

        // we must find the location of the "/>" that closes this tag - it will
        // include any nested tags

        while (c != documentIterator.DONE) {

          if (c == '<') {

            ITypedRegion subRegion = document.getPartition(documentIterator.getIndex());
            if (subRegion.getType().equals(PDEPartitionScanner.XML_TAG)) {

              c = documentIterator.next(true);

              if (c != '/') {

                openBrackets++;

              } else {

                openBrackets--;

              }

            }

          } else if (c == '/' && documentIterator.next(true) == '>') {

            openBrackets--;

          }

          if (openBrackets == 0) {

            if (c == '/') {

              while (c != documentIterator.DONE && documentIterator.next() != '>');

            }
            break;
          }

          c = documentIterator.next(true);
        }

        if (c != documentIterator.DONE) {

          text.setSelectedRange(regionOffset, documentIterator.getIndex() - regionOffset + 1);

          return true;
        }
      }
    }

    return false;
  }

  static class DocumentCharacterIterator implements CharacterIterator {

    /** Document to iterate over. */
    private IDocument document;
    /** Start offset of iteration. */
    private int offset = -1;
    /** Endoffset of iteration. */
    private int endOffset = -1;
    /** Current offset of iteration. */
    private int index = -1;

    /** Creates a new document iterator. */
    public DocumentCharacterIterator() {
    }

    /**
     * Configures this document iterator with the document section to be iteratored. 
     *
     * @param document the document to be iterated
     * @param iteratorRange the range in the document to be iterated
     */
    public void setDocument(IDocument document, IRegion iteratorRange) {
      this.document = document;
      offset = iteratorRange.getOffset();
      endOffset = offset + iteratorRange.getLength();
    }

    public IDocument getDocument() {
      return document;
    }

    /*
     * @see CharacterIterator#first()
     */
    public char first() {
      index = offset;
      return current();
    }

    /*
     * @see CharacterIterator#last()
     */
    public char last() {
      index = offset < endOffset ? endOffset - 1 : endOffset;
      return current();
    }

    /*
     * @see CharacterIterator#current()
     */
    public char current() {
      if (offset <= index && index < endOffset) {
        try {
          return document.getChar(index);
        } catch (BadLocationException x) {
        }
      }
      return DONE;
    }

    /*
     * @see CharacterIterator#next()
     */
    private char doNext() {
      if (index == endOffset - 1) {
        return DONE;
      }

      if (index < endOffset) {
        ++index;
      }


      return current();
    }

    public char next() {
      return next(false);
    }

    public char next(boolean ignoreWhitespace) {
      char c = doNext();

      if (c == DONE) {
        return c;
      }
      if (ignoreWhitespace) {
        while (c != DONE && Character.isWhitespace(c)) {
          c = doNext();
        }
      }
      return current();
    }

    /*
     * @see CharacterIterator#previous()
     */
    public char doPrevious() {
      if (index == offset) {
        return DONE;
      }

      if (index > offset) {
        --index;
      }


      return current();
    }

    public char previous() {
      return previous(false);
    }

    public char previous(boolean ignoreWhitespace) {
      char c = doPrevious();

      if (c == DONE) {
        return c;
      }
      if (ignoreWhitespace) {
        while (c != DONE && Character.isWhitespace(c)) {
          c = doPrevious();
        }
      }
      return current();
    }

    /*
     * @see CharacterIterator#setIndex(int)
     */
    public char setIndex(int index) {
      this.index = index;
      return current();
    }

    /*
     * @see CharacterIterator#getBeginIndex()
     */
    public int getBeginIndex() {
      return offset;
    }

    /*
     * @see CharacterIterator#getEndIndex()
     */
    public int getEndIndex() {
      return endOffset;
    }

    /*
     * @see CharacterIterator#getIndex()
     */
    public int getIndex() {
      return index;
    }

    /*
     * @see CharacterIterator#clone()
     */
    public Object clone() {
      DocumentCharacterIterator i = new DocumentCharacterIterator();
      i.document = document;
      i.index = index;
      i.offset = offset;
      i.endOffset = endOffset;
      return i;
    }

  };

}
