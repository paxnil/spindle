package com.iw.plugins.spindle.core.parser.xml;
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

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocationAwareDocument extends DocumentImpl {

  /**
   * Constructor for TestDocument.
   */
  public LocationAwareDocument() {
    super();
  }

  /**
   * Constructor for TestDocument.
   * @param grammarAccess
   */
  public LocationAwareDocument(boolean grammarAccess) {
    super(grammarAccess);
  }

  /**
   * Constructor for TestDocument.
   * @param doctype
   */
  public LocationAwareDocument(DocumentType doctype) {
    super(doctype);
  }

  /**
   * Constructor for TestDocument.
   * @param doctype
   * @param grammarAccess
   */
  public LocationAwareDocument(DocumentType doctype, boolean grammarAccess) {
    super(doctype, grammarAccess);
  }

  /**
   * @see org.w3c.dom.Document#createAttribute(String)
   */
  public Attr createAttribute(String name) throws DOMException {
    if (errorChecking && !isXMLName(name)) {
      throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "DOM002 Illegal character");
    }
    return new Attribute(this, name);
  }

  /**
   * @see org.apache.xerces.dom.CoreDocumentImpl#createAttributeNS(String, String, String)
   */
  public Attr createAttributeNS(String namespaceURI, String qualifiedName, String arg2)
    throws DOMException {
    if (errorChecking && !isXMLName(qualifiedName)) {
      throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "DOM002 Illegal character");
    }
    return new AttributeNS(this, namespaceURI, qualifiedName, arg2);
  }

  /**
   * @see org.w3c.dom.Document#createAttributeNS(String, String)
   */
  public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
    if (errorChecking && !isXMLName(qualifiedName)) {
      throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "DOM002 Illegal character");
    }
    return new AttributeNS(this, namespaceURI, qualifiedName);
  }

  /**
   * @see org.w3c.dom.Document#createElement(String)
   */
  public Element createElement(String tagName) throws DOMException {
    if (errorChecking && !isXMLName(tagName)) {
      throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "DOM002 Illegal character");
    }
    return new LElement(this, tagName);
  }

  /**
   * @see org.apache.xerces.dom.CoreDocumentImpl#createElementNS(String, String, String)
   */
  public Element createElementNS(String namespaceURI, String qualifiedName, String arg2)
    throws DOMException {
    if (errorChecking && !isXMLName(qualifiedName)) {
      throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "DOM002 Illegal character");
    }
    return new LElementNS(this, namespaceURI, qualifiedName, arg2);
  }

  /**
   * @see org.w3c.dom.Document#createElementNS(String, String)
   */
  public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
    if (errorChecking && !isXMLName(qualifiedName)) {
      throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "DOM002 Illegal character");
    }
    return new LElementNS(this, namespaceURI, qualifiedName);
  }

}
