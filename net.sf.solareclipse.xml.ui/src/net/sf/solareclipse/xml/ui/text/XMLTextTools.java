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
package net.sf.solareclipse.xml.ui.text;

import java.util.Map;

import net.sf.solareclipse.text.AbstractTextTools;
import net.sf.solareclipse.xml.internal.ui.text.DeclScanner;
import net.sf.solareclipse.xml.internal.ui.text.DocumentPartitioner;
import net.sf.solareclipse.xml.internal.ui.text.SingleTokenScanner;
import net.sf.solareclipse.xml.internal.ui.text.TextScanner;
import net.sf.solareclipse.xml.internal.ui.text.XMLCDATAScanner;
import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;
import net.sf.solareclipse.xml.internal.ui.text.XMLTagScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLTextTools extends AbstractTextTools
{
  /** Text Attributes for XML editors */
  private static final String[] TOKENS = { IXMLSyntaxConstants.XML_DEFAULT,
      IXMLSyntaxConstants.XML_TAG, IXMLSyntaxConstants.XML_ATT_NAME,
      IXMLSyntaxConstants.XML_ATT_VALUE, IXMLSyntaxConstants.XML_ENTITY,
      IXMLSyntaxConstants.XML_PI, IXMLSyntaxConstants.XML_CDATA,
      IXMLSyntaxConstants.XML_COMMENT, IXMLSyntaxConstants.XML_DECL, };

  /** Content types for XML editors */
  private static final String[] TYPES = { XMLPartitionScanner.XML_PI,
      XMLPartitionScanner.XML_COMMENT, XMLPartitionScanner.XML_DECL,
      XMLPartitionScanner.XML_TAG, XMLPartitionScanner.XML_ATTRIBUTE,
      XMLPartitionScanner.XML_CDATA, XMLPartitionScanner.DTD_INTERNAL,
      XMLPartitionScanner.DTD_INTERNAL_PI, XMLPartitionScanner.DTD_INTERNAL_COMMENT,
      XMLPartitionScanner.DTD_INTERNAL_DECL, };

  /** The XML partitions scanner */
  private XMLPartitionScanner xmlPartitionScanner;

  /** The XML text scanner */
  private TextScanner xmlTextScanner;

  /** The DTD text scanner */
  private TextScanner dtdTextScanner;

  /** The XML tags scanner */
  private XMLTagScanner xmlTagScanner;

  /** The XML attributes scanner */
  private TextScanner xmlAttributeScanner;

  /** The XML CDATA sections scanner */
  private XMLCDATAScanner xmlCDATAScanner;

  /** The XML processing instructions scanner */
  private SingleTokenScanner xmlPIScanner;

  /** The XML comments scanner */
  private SingleTokenScanner xmlCommentScanner;

  /** The XML declarations scanner */
  private DeclScanner xmlDeclScanner;

  /**
   * Creates a new XML text tools collection.
   */
  public XMLTextTools(IPreferenceStore store)
  {
    super(store, TOKENS);

    xmlPartitionScanner = new XMLPartitionScanner(false);

    Map tokens = getTokens();

    xmlTextScanner = new TextScanner(tokens, '&', IXMLSyntaxConstants.XML_DEFAULT);

    dtdTextScanner = new TextScanner(tokens, '%', IXMLSyntaxConstants.XML_DEFAULT);

    xmlPIScanner = new SingleTokenScanner(tokens, IXMLSyntaxConstants.XML_PI);

    xmlCommentScanner = new SingleTokenScanner(tokens, IXMLSyntaxConstants.XML_COMMENT);

    xmlDeclScanner = new DeclScanner(tokens);

    xmlTagScanner = new XMLTagScanner(tokens);

    xmlAttributeScanner = new TextScanner(tokens, '&', IXMLSyntaxConstants.XML_ATT_VALUE);

    xmlCDATAScanner = new XMLCDATAScanner(tokens);
  }

  /**
   *  
   */
  public IDocumentPartitioner createXMLPartitioner()
  {
    return new DocumentPartitioner(xmlPartitionScanner, TYPES)
    {
      public String[] getManagingPositionCategories()
      {
        return new String[] { IDocumentExtension3.DEFAULT_PARTITIONING };
      }
    };
  }

  /**
   *  
   */
  public IPartitionTokenScanner getXMLPartitionScanner()
  {
    return xmlPartitionScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML text.
   * 
   * @return an XML text scanner
   */
  public RuleBasedScanner getXMLTextScanner()
  {
    return xmlTextScanner;
  }

  /**
   * Returns a scanner which is configured to scan DTD text.
   * 
   * @return an DTD text scanner
   */
  public RuleBasedScanner getDTDTextScanner()
  {
    return dtdTextScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML tags.
   * 
   * @return an XML tag scanner
   */
  public RuleBasedScanner getXMLTagScanner()
  {
    return xmlTagScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML tag attributes.
   * 
   * @return an XML tag attribute scanner
   */
  public RuleBasedScanner getXMLAttributeScanner()
  {
    return xmlAttributeScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML CDATA sections.
   * 
   * @return an XML CDATA section scanner
   */
  public ITokenScanner getXMLCDATAScanner()
  {
    return xmlCDATAScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML processing instructions.
   * 
   * @return an XML processing instruction scanner
   */
  public RuleBasedScanner getXMLPIScanner()
  {
    return xmlPIScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML comments.
   * 
   * @return an XML comment scanner
   */
  public RuleBasedScanner getXMLCommentScanner()
  {
    return xmlCommentScanner;
  }

  /**
   * Returns a scanner which is configured to scan XML declarations.
   * 
   * @return an XML declaration scanner
   */
  public RuleBasedScanner getXMLDeclScanner()
  {
    return xmlDeclScanner;
  }
}