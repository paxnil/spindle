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

package com.iw.plugins.spindle.editors.template;

import java.util.Map;

import net.sf.solareclipse.text.AbstractTextTools;
import net.sf.solareclipse.xml.internal.ui.text.DeclScanner;
import net.sf.solareclipse.xml.internal.ui.text.DocumentPartitioner;
import net.sf.solareclipse.xml.internal.ui.text.SingleTokenScanner;
import net.sf.solareclipse.xml.internal.ui.text.TextScanner;
import net.sf.solareclipse.xml.internal.ui.text.XMLCDATAScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * TextTools for Template Editors - extended to partition and syntax color jwcid
 * tags.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TemplateTextTools extends AbstractTextTools
{
  /** Content types for Template editors */
  private static final String[] TYPES = {TemplatePartitionScanner.XML_PI,
      TemplatePartitionScanner.XML_COMMENT, TemplatePartitionScanner.XML_DECL,
      TemplatePartitionScanner.XML_TAG, TemplatePartitionScanner.XML_ATTRIBUTE,
      TemplatePartitionScanner.XML_CDATA,
      TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE,
      TemplatePartitionScanner.DTD_INTERNAL, TemplatePartitionScanner.DTD_INTERNAL_PI,
      TemplatePartitionScanner.DTD_INTERNAL_COMMENT,
      TemplatePartitionScanner.DTD_INTERNAL_DECL,};

  private static final String[] TOKENS = {ITemplateSyntaxConstants.XML_DEFAULT,
      ITemplateSyntaxConstants.XML_TAG, ITemplateSyntaxConstants.XML_ATT_NAME,
      ITemplateSyntaxConstants.XML_ATT_VALUE, ITemplateSyntaxConstants.XML_ENTITY,
      ITemplateSyntaxConstants.XML_PI, ITemplateSyntaxConstants.XML_CDATA,
      ITemplateSyntaxConstants.XML_COMMENT, ITemplateSyntaxConstants.XML_DECL,
      ITemplateSyntaxConstants.TAPESTRY_ATT_NAME,
      ITemplateSyntaxConstants.TAPESTRY_ATT_VALUE};

  /** The Template partitions scanner */
  private TemplatePartitionScanner fTemplatePartitionScanner;

  /** The XML text scanner */
  private TextScanner fXmlTextScanner;

  /** The DTD text scanner */
  private TextScanner fDtdTextScanner;

  /** The XML tags scanner */
  private TemplateTagScanner fTemplateTagScanner;

  /** The XML attributes scanner */
  private TextScanner fXmlAttributeScanner;

  /** The XML CDATA sections scanner */
  private XMLCDATAScanner fXmlCDATAScanner;

  /** The XML processing instructions scanner */
  private SingleTokenScanner fXmlPIScanner;

  /** The XML comments scanner */
  private SingleTokenScanner fXmlCommentScanner;

  /** JWCID attribute value scanner */
  private SingleTokenScanner fJwcidAttributeScanner;

  /** The XML declarations scanner */
  private DeclScanner fXmlDeclScanner;
  /**
   * @param store
   */
  public TemplateTextTools(IPreferenceStore store)
  {
    super(store, TOKENS);

    fTemplatePartitionScanner = new TemplatePartitionScanner(false);

    Map tokens = getTokens();

    fXmlTextScanner = new TextScanner(tokens, '&', ITemplateSyntaxConstants.XML_DEFAULT);

    fDtdTextScanner = new TextScanner(tokens, '%', ITemplateSyntaxConstants.XML_DEFAULT);

    fXmlPIScanner = new SingleTokenScanner(tokens, ITemplateSyntaxConstants.XML_PI);

    fXmlCommentScanner = new SingleTokenScanner(
        tokens,
        ITemplateSyntaxConstants.XML_COMMENT);

    fXmlDeclScanner = new DeclScanner(tokens);

    fTemplateTagScanner = new TemplateTagScanner(tokens);

    fXmlAttributeScanner = new TextScanner(
        tokens,
        '&',
        ITemplateSyntaxConstants.XML_ATT_VALUE);

    fJwcidAttributeScanner = new SingleTokenScanner(
        tokens,
        ITemplateSyntaxConstants.TAPESTRY_ATT_VALUE);

    fXmlCDATAScanner = new XMLCDATAScanner(tokens);
  }

  public DefaultPartitioner createXMLPartitioner()
  {
    return new DocumentPartitioner(getTemplatePartitionScanner(), TYPES);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.sf.solareclipse.xml.ui.text.XMLTextTools#getTemplatePartitionScanner()
   */
  public IPartitionTokenScanner getTemplatePartitionScanner()
  {
    return fTemplatePartitionScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML text.
   * 
   * @return an XML text scanner
   */
  public RuleBasedScanner getXMLTextScanner()
  {
    return fXmlTextScanner;
  }

  /**
   * Returns a scanner which is configured to scan DTD text.
   * 
   * @return an DTD text scanner
   */
  public RuleBasedScanner getDTDTextScanner()
  {
    return fDtdTextScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML tags. Is special in that
   * it handles attribute named "jwcid' differently
   * 
   * @return an Template (XML) tag scanner
   */
  public RuleBasedScanner getTemplateTagScanner()
  {
    return fTemplateTagScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML tag attributes.
   * 
   * @return an XML tag attribute scanner
   */
  public RuleBasedScanner getXMLAttributeScanner()
  {
    return fXmlAttributeScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML CDATA sections.
   * 
   * @return an XML CDATA section scanner
   */
  public ITokenScanner getXMLCDATAScanner()
  {
    return fXmlCDATAScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML processing instructions.
   * 
   * @return an XML processing instruction scanner
   */
  public RuleBasedScanner getXMLPIScanner()
  {
    return fXmlPIScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML comments.
   * 
   * @return an XML comment scanner
   */
  public RuleBasedScanner getXMLCommentScanner()
  {
    return fXmlCommentScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML declarations.
   * 
   * @return an XML declaration scanner
   */
  public RuleBasedScanner getXMLDeclScanner()
  {
    return fXmlDeclScanner;
  }

  /**
   * @return
   */
  public SingleTokenScanner getJwcidAttributeScanner()
  {
    return fJwcidAttributeScanner;
  }

}