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

import org.apache.xerces.dom.DeferredDocumentImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class TapestryDOMParser extends DOMParser {

  /**
   * Constructor for MyDOMParser.
   */
  public TapestryDOMParser() {
    super();
  }

  /**
   * Constructor for MyDOMParser.
   * @param config
   */
  public TapestryDOMParser(XMLParserConfiguration config) {
    super(config);
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(XMLLocator, String, Augmentations)
   */
  public void startDocument(XMLLocator locator, String encoding, Augmentations augs)
    throws XNIException {
    fInDocument = true;
    if (!fDeferNodeExpansion) {
      if (fDocumentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
        fDocument = new LocationAwareDocument();
        fDocumentImpl = (DocumentImpl) fDocument;
        // REVISIT: when DOM Level 3 is REC rely on Document.support
        //          instead of specific class
        // set DOM error checking off
        fDocumentImpl.setStrictErrorChecking(false);
        // set actual encoding
        fDocumentImpl.setActualEncoding(encoding);
      } else {
        // use specified document class
        try {
          Class documentClass = Class.forName(fDocumentClassName);
          fDocument = (Document) documentClass.newInstance();
          // if subclass of our own class that's cool too
          Class defaultDocClass = Class.forName(DEFAULT_DOCUMENT_CLASS_NAME);
          if (defaultDocClass.isAssignableFrom(documentClass)) {
            fDocumentImpl = (DocumentImpl) fDocument;
            // REVISIT: when DOM Level 3 is REC rely on
            //          Document.support instead of specific class
            // set DOM error checking off
            fDocumentImpl.setStrictErrorChecking(false);
          }
        } catch (ClassNotFoundException e) {
          // won't happen we already checked that earlier
        } catch (Exception e) {
          // REVISIT: Localize this message.
          throw new RuntimeException(
            "Failed to create document object of class: " + fDocumentClassName);
        }
      }
      fCurrentNode = fDocument;
    } else {
      fDeferredDocumentImpl = new DeferredDocumentImpl(fNamespaceAware);
      fDocument = fDeferredDocumentImpl;
      fDocumentIndex = fDeferredDocumentImpl.createDeferredDocument();
      fCurrentNodeIndex = fDocumentIndex;
    }

  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(QName, Augmentations)
   */
  public void endElement(QName element, Augmentations augs) throws XNIException {
    if (fCurrentNode instanceof ILocatable) {
      XMLScanner.LocationItem location = (XMLScanner.LocationItem) augs.getItem(AUGMENTATIONS);
      if (location != null) {
        ((ILocatable) fCurrentNode).setLocation(location);
      }
    }
    super.endElement(element, augs);
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(QName, XMLAttributes, Augmentations)
   */
  public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
    throws XNIException {
    super.startElement(element, attributes, augs);
    int attrCount = attributes.getLength();
    QName attrName = new QName();
    NamedNodeMap map = fCurrentNode.getAttributes();
    for (int i = 0; i < attrCount; i++) {
      attributes.getName(i, attrName);
      String rawname = attrName.rawname;
      Augmentations attrAugs = attributes.getAugmentations(i);
      if (attrAugs != null) {
        XMLScanner.LocationItem locationItem =
          (XMLScanner.LocationItem) attrAugs.getItem(AUGMENTATIONS);
        if (locationItem != null) {
          ILocatable attribute = (ILocatable) map.getNamedItem(rawname);
          attribute.setLocation(locationItem);
        }
      }
    }
  }

  /** Feature identifier: notify built-in refereces. */
  protected static final String AUGMENTATIONS =
    "http://intelligentworks.com/xml/features/augmentations-location";

  // recognized features and properties

  // property identifiers

}
