package com.iw.plugins.spindle.util;

import java.text.MessageFormat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.util.xml.DocumentParseException;
import net.sf.tapestry.util.xml.InvalidStringException;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class InvalidStringConverter {

  static private final String badAsset = "asset name=\"{0}\"";
  static private final String badBean = "bean name=\"{0}\"";
  static private final String badComponentAlias = "component-alias type=\"{0}\"";
  static private final String badComponentId = "component id=\"{0}\"";
  static private final String badComponentType = "type==\"{0}\"";
  static private final String badPropertyName = "property name=\"{0}\"";
  static private final String badExtensionName = "extension name=\"{0}\"";
  static private final String badLibraryId = "library id=\"{0}\"";
  static private final String badParameterName = "parameter name=\"{0}\"";
  static private final String badPageName = "page name=\"{0}\"";
  static private final String badServiceName = "page name=\"{0}\"";


  public static int getLineNumberForInvalidString(InvalidStringException e, IDocument document) {
  	
  	return getLineNumberForInvalidString(e.getInvalidString(), e.getPattern(), document);
  	
  }

  public static int getLineNumberForInvalidString(String invalid, String pattern, IDocument document) {

    String[] error = new String[] {invalid};  
    String formatString = null;

    if (SpecificationParser.ASSET_NAME_PATTERN.equals(pattern)) {

      formatString = badAsset;

    } else if (SpecificationParser.BEAN_NAME_PATTERN.equals(pattern)) {

      formatString = badBean;

    } else if (SpecificationParser.COMPONENT_ALIAS_PATTERN.equals(pattern)) {

      formatString = badComponentAlias;

    } else if (SpecificationParser.COMPONENT_ID_PATTERN.equals(pattern)) {

      formatString = badComponentId;

    } else if (SpecificationParser.COMPONENT_TYPE_PATTERN.equals(pattern)) {

      formatString = badComponentType;

    } else if (SpecificationParser.EXTENDED_PROPERTY_NAME_PATTERN.equals(pattern)) {

      formatString = badPropertyName;

    } else if (SpecificationParser.EXTENSION_NAME_PATTERN.equals(pattern)) {

      formatString = badExtensionName;

    } else if (SpecificationParser.LIBRARY_ID_PATTERN.equals(pattern)) {

      formatString = badLibraryId;

    } else if (SpecificationParser.PAGE_NAME_PATTERN.equals(pattern)) {

      formatString = badPageName;

    } else if (SpecificationParser.PARAMETER_NAME_PATTERN.equals(pattern)) {

      formatString = badParameterName;

    } else if (SpecificationParser.PROPERTY_NAME_PATTERN.equals(pattern)) {

      formatString = badPropertyName;

    } else if (SpecificationParser.SERVICE_NAME_PATTERN.equals(pattern)) {

      formatString = badServiceName;

    }

    try {
      int resultOffset = -1;

      if (formatString != null) {

        MessageFormat format = new MessageFormat(formatString);
        String searchString = format.format(error);

        resultOffset = document.search(0, searchString, true, true, false);

      }

      if (resultOffset == -1) {

        resultOffset = 0;

      }

      return document.getLineOfOffset(resultOffset + 1);

    } catch (BadLocationException ex) {
    }

    return 1;

  }
}
