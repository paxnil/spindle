package com.iw.plugins.spindle.core.parser;
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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.iw.plugins.spindle.core.Files;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.xml.TapestryDOMParser;
import com.iw.plugins.spindle.core.parser.xml.TapestryEntityResolver;
import com.iw.plugins.spindle.core.parser.xml.TapestryParserConfiguration;

/**
 * The xml parser used in the builds.
 *
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class Parser implements ISourceLocationResolver, XMLErrorHandler {

  private Document eclipseDocument;
  private DocumentImpl xmlDocument;

  private TapestryDOMParser parser = null;
  private ArrayList collectedExceptions;

  public Parser() {
    TapestryEntityResolver.register(
      SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID,
      "Tapestry_1_3.dtd");
    TapestryEntityResolver.register(
      SpecificationParser.TAPESTRY_DTD_1_4_PUBLIC_ID,
      "Tapestry_1_4.dtd");
  }

  private void checkParser() {
    if (parser == null) {
      TapestryParserConfiguration parserConfig = new TapestryParserConfiguration(this);
      parser = new TapestryDOMParser(parserConfig);
      parser.setSourceResolver(this);
      try {
        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        parser.setFeature(
          "http://apache.org/xml/features/dom/include-ignorable-whitespace",
          false);
        parser.setFeature("http://xml.org/sax/features/validation", true);
      } catch (SAXException e) {
        TapestryCore.log(e);
      }
    }
  }

  public int getLineOffset(int parserReportedLineNumber) {
    try {
      if (eclipseDocument != null) {
        return eclipseDocument.getLineOffset(parserReportedLineNumber - 1);
      }
    } catch (BadLocationException e) {
      TapestryCore.log(e);
    }
    return 0;
  }

  public int getColumnOffset(int parserReportedLineNumber, int parserReportedColumn) {
    int result = getLineOffset(parserReportedLineNumber);
    int lineCount = eclipseDocument.getNumberOfLines();
    int totalLength = eclipseDocument.getLength();
    if (parserReportedColumn > 0) {
      if (parserReportedLineNumber > lineCount) {
        result = Math.min(totalLength - 2, result + parserReportedColumn - 1);
      } else {
        try {
          int lastCharOnLine =
            result + eclipseDocument.getLineLength(parserReportedLineNumber - 1) - 1;
          result = Math.min(lastCharOnLine, result + parserReportedColumn - 1);
        } catch (BadLocationException e) {
        	TapestryCore.log(e);
        }
      }

    }
    return result;
  }

  public Element parse(IFile file) throws IOException, DocumentParseException, CoreException {
    return parse(file.getContents(true));
  }

  public Element parse(InputStream input) throws IOException, DocumentParseException {
    String content = Files.readFileToString(input, null);
    return parse(content);
  }

  public Element parse(String content) throws IOException, DocumentParseException {

    collectedExceptions = new ArrayList();
    eclipseDocument = new Document();
    eclipseDocument.set(content);
    StringReader reader = new StringReader(content);
    xmlDocument = null;
    Element result = null;

    try {
      //      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      //      factory.setValidating(true);
      //      factory.setIgnoringElementContentWhitespace(true);
      //      factory.setIgnoringComments(true);
      //      factory.setCoalescing(true);
      //      DocumentBuilder parser = factory.newDocumentBuilder();
      //      parser.setErrorHandler(this);
      checkParser();
      parser.parse(new InputSource(reader));
      xmlDocument = (DocumentImpl) parser.getDocument();
      result = xmlDocument.getDocumentElement();
    } catch (SAXException e) {

      if (e instanceof SAXParseException) {

        throw createDPEx((SAXParseException) e, IMarker.SEVERITY_ERROR);

      }

      throw new DocumentParseException(e.getMessage(), IMarker.SEVERITY_ERROR, 0, 0, 0, e);

    } finally {
      reader.close();
    }

    return result;

  }

  public DocumentImpl getParsedDocument() {
    return xmlDocument;
  }

  public ElementSourceLocationInfo getSourceLocationInfo(Node node) {
    if (xmlDocument != null) {
      return (ElementSourceLocationInfo) xmlDocument.getUserData(node, TapestryCore.PLUGIN_ID);
    }
    return null;
  }

  private void collect(DocumentParseException ex) {
    if (!collectedExceptions.contains(ex)) {
      collectedExceptions.add(ex);
    }
  }

  public DocumentParseException[] getCollectedExceptions() {
    if (collectedExceptions != null && !collectedExceptions.isEmpty()) {
      return (DocumentParseException[]) collectedExceptions.toArray(
        new DocumentParseException[collectedExceptions.size()]);
    }
    return new DocumentParseException[0];
  }

  private DocumentParseException createDPEx(SAXParseException parseException, int severity) {
    return createDPEx(
      (Throwable) parseException,
      parseException.getLineNumber(),
      parseException.getColumnNumber(),
      severity);
  }

  private DocumentParseException createDPEx(XMLParseException parseException, int severity) {
    return createDPEx(
      (Throwable) parseException,
      parseException.getLineNumber(),
      parseException.getColumnNumber(),
      severity);
  }

  private DocumentParseException createDPEx(
    Throwable ex,
    int lineNumber,
    int columnNumber,
    int severity) {

    int charStart = 0;
    int charEnd = 0;

    charStart = getColumnOffset(lineNumber, columnNumber);
    charEnd = charStart + 1;

    return new DocumentParseException(
      ex.getMessage(),
      severity,
      lineNumber,
      charStart,
      charEnd,
      ex);
  }

  public boolean isElement(Node node, String elementName) {
    if (node.getNodeType() != Node.ELEMENT_NODE) {
      return false;
    }
    Element element = (Element) node;
    return element.getTagName().equals(elementName);
  }

  public String getValue(Node node) {
    StringBuffer buffer = new StringBuffer();
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      Text text = (Text) child;
      buffer.append(text.getData());
    }

    String result = buffer.toString().trim();
    if (result == null || "".equals(result)) {
      return null;
    }

    return result;
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(String, String, XMLParseException)
   */
  public void error(String domain, String key, XMLParseException exception) throws XNIException {
    collect(createDPEx(exception, IMarker.SEVERITY_ERROR));
    TapestryCore.log(exception);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(String, String, XMLParseException)
   */
  public void fatalError(String domain, String key, XMLParseException exception)
    throws XNIException {
    collect(createDPEx(exception, IMarker.SEVERITY_ERROR));
    TapestryCore.log(exception);
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(String, String, XMLParseException)
   */
  public void warning(String domain, String key, XMLParseException exception)
    throws XNIException {
    collect(createDPEx(exception, IMarker.SEVERITY_WARNING));
    TapestryCore.log(exception);
  }

}
