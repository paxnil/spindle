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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.parser.xml;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.validation.XMLGrammarPoolImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;

import com.iw.plugins.spindle.core.TapestryCore;

/**
 * Adds ability to use cached DTD Grammars
 * 
 * @author glongman@gmail.com
 
 */
public class TapestryXMLDTDValidator extends XMLDTDValidator
{

  private boolean fSeenRootElement;
  private String fPublicId;
  private XMLGrammarPoolImpl fGrammarPool;
  /**
   *  
   */
  public TapestryXMLDTDValidator()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(java.lang.String,
   *      java.lang.String, java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void doctypeDecl(
      String rootElement,
      String publicId,
      String systemId,
      Augmentations augs) throws XNIException
  {
    this.fPublicId = publicId;
    super.doctypeDecl(rootElement, publicId, systemId, augs);
  }

  /*
   * (non-Javadoc) If the grammar is null, the cache has it.
   * 
   * @see org.apache.xerces.impl.dtd.XMLDTDValidator#handleStartElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.XMLAttributes)
   */
  protected void handleStartElement(QName element, XMLAttributes attributes) throws XNIException
  {
    if (!fSeenRootElement)
    {
      fSeenRootElement = true;

      if (fDTDGrammar == null)
        fDTDGrammar = getGrammar(fPublicId);
    }
    super.handleStartElement(element, attributes);

  }

  /**
   * @param publicId
   * @return
   */
  private DTDGrammar getGrammar(String publicId)
  {
    if (TapestryCore.isCachingDTDGrammars() && publicId != null)
      return (DTDGrammar) fGrammarPool.getGrammar(publicId);

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.parser.XMLComponent#setProperty(java.lang.String,
   *      java.lang.Object)
   */
  public void setProperty(String propertyId, Object value) throws XMLConfigurationException
  {

    super.setProperty(propertyId, value);
    if (propertyId.equals(XMLDTDValidator.GRAMMAR_POOL))
    {
      fGrammarPool = (XMLGrammarPoolImpl) value;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.parser.XMLComponent#reset(org.apache.xerces.xni.parser.XMLComponentManager)
   */
  public void reset(XMLComponentManager componentManager) throws XMLConfigurationException
  {
    if (fGrammarPool == null)
      fGrammarPool = (XMLGrammarPoolImpl) componentManager
          .getProperty("http://apache.org/xml/properties/internal/grammar-pool");

    if (fPublicId != null && TapestryCore.isCachingDTDGrammars())
    {
      if (fGrammarPool != null && fDTDGrammar != null
          && fGrammarPool.getGrammar(fPublicId) == null)
        fGrammarPool.putGrammar(fPublicId, fDTDGrammar);
    }

    fSeenRootElement = false;
    fPublicId = null;
    super.reset(componentManager);
  }

}