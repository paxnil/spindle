package com.iw.plugins.spindle.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.iw.plugins.spindle.util.InvalidStringConverter;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class BadStringMarkerUpdater implements IMarkerUpdater {

  private final static String[] ATTRIBUTES =
    { IMarker.CHAR_START, IMarker.CHAR_END, IMarker.LINE_NUMBER };

  /**
   * Constructor for BadStringMarkerUpdater.
   */
  public BadStringMarkerUpdater() {
    super();
  }

  /**
   * @see org.eclipse.ui.texteditor.IMarkerUpdater#getAttribute()
   */
  public String[] getAttribute() {
    return ATTRIBUTES;
  }

  /**
   * @see org.eclipse.ui.texteditor.IMarkerUpdater#getMarkerType()
   */
  public String getMarkerType() {
    return "com.iw.plugins.spindle.badwordproblem";
  }

  /**
   * @see org.eclipse.ui.texteditor.IMarkerUpdater#updateMarker(IMarker, IDocument, Position)
   */
  public boolean updateMarker(IMarker marker, IDocument document, Position position) {

    if (position.isDeleted()) {

      return false;

    }

    int lineNumber = 1;

    String invalidString;
    String pattern;
    try {

      invalidString = (String) marker.getAttribute("invalidString");

      pattern = (String) marker.getAttribute("pattern");

      lineNumber =
        InvalidStringConverter.getLineNumberForInvalidString(invalidString, pattern, document);

    } catch (Exception e) {
    	
    	e.printStackTrace();

    }

    try {
      
      MarkerUtilities.setLineNumber(marker, document.getLineOfOffset(lineNumber));

    } catch (BadLocationException x) {
    }

    return true;

  }

}
