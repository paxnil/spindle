package com.iw.plugins.spindle.parser.filters;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.eclipse.pde.internal.ui.editor.XMLConfiguration;

/**
 * To make it easy to build filters.
 * 
 * This class implements a filter that simply passes document
 * events to the next handler.
 */
public class BaseDocumentFilter implements XMLDocumentFilter {

  protected XMLDocumentSource xmlSource;

  protected XMLDocumentHandler xmlHandler;
  
  protected XMLConfiguration configuration;

  public XMLDocumentHandler getDocumentHandler() {
    return xmlHandler;
  }

  public XMLDocumentSource getDocumentSource() {
    return xmlSource;
  }

  public void setDocumentHandler(XMLDocumentHandler handler) {
  	xmlHandler = handler;
  }

  public void setDocumentSource(XMLDocumentSource xmlSource) {
    this.xmlSource = xmlSource;
  }
  
  public XMLConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(XMLConfiguration configuration) {
    this.configuration = configuration;
  }


  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(XMLLocator, String, Augmentations)
   */
  public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.startDocument(locator, encoding, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#xmlDecl(String, String, String, Augmentations)
   */
  public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.xmlDecl(version, encoding, standalone, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(String, String, String, Augmentations)
   */
  public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.doctypeDecl(rootElement, publicId, systemId, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#comment(XMLString, Augmentations)
   */
  public void comment(XMLString text, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.comment(text, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#processingInstruction(String, XMLString, Augmentations)
   */
  public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.processingInstruction(target, data, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startPrefixMapping(String, String, Augmentations)
   */
  public void startPrefixMapping(String prefix, String uri, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.startPrefixMapping(prefix, uri, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(QName, XMLAttributes, Augmentations)
   */
  public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.startElement(element, attributes, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(QName, XMLAttributes, Augmentations)
   */
  public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.emptyElement(element, attributes, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startGeneralEntity(String, XMLResourceIdentifier, String, Augmentations)
   */
  public void startGeneralEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs)
    throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.startGeneralEntity(name, identifier, encoding, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#textDecl(String, String, Augmentations)
   */
  public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.textDecl(version, encoding, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endGeneralEntity(String, Augmentations)
   */
  public void endGeneralEntity(String name, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.endGeneralEntity(name, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#characters(XMLString, Augmentations)
   */
  public void characters(XMLString text, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.characters(text, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#ignorableWhitespace(XMLString, Augmentations)
   */
  public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.ignorableWhitespace(text, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(QName, Augmentations)
   */
  public void endElement(QName element, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.endElement(element, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endPrefixMapping(String, Augmentations)
   */
  public void endPrefixMapping(String prefix, Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.endPrefixMapping(prefix, augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startCDATA(Augmentations)
   */
  public void startCDATA(Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.startCDATA(augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endCDATA(Augmentations)
   */
  public void endCDATA(Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.endCDATA(augs);
  	}
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endDocument(Augmentations)
   */
  public void endDocument(Augmentations augs) throws XNIException {
  	if (xmlHandler != null) {
  	 xmlHandler.endDocument(augs);
  	}
  }



 
}
