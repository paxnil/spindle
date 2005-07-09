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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.documentsAndModels;

import java.io.StringWriter;
import java.util.List;

import org.eclipse.jface.text.IDocument;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryPluginException;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.PluginComponentTypeDeclaration;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.spec.PluginPageDeclaration;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.IndentingWriter;
import com.iw.plugins.spindle.core.util.XMLPublicIDUtil;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.wutka.dtd.DTD;

/**
 * LibraryEdits - helper for adding page and component declarations to a Library
 * (Application) document
 * 
 * @author glongman@gmail.com
 *  
 */
public class LibraryEdits extends SpecificationEdits
{

  PluginLibrarySpecification fSpec;
  EditFactory fPageEditFactory;
  EditFactory fComponentEditFactory;
  boolean fUseTabs;
  int fTabSize;

  /**
   * @param document
   */
  public LibraryEdits(PluginLibrarySpecification spec, IDocument document)
  {
    super(document);
    Assert.isNotNull(spec);
    fSpec = spec;
    fUseTabs = UIPlugin.getDefault().getPreferenceStore().getBoolean(
        PreferenceConstants.FORMATTER_TAB_CHAR);
    fTabSize = UIPlugin.getDefault().getPreferenceStore().getInt(
        PreferenceConstants.FORMATTER_TAB_SIZE);
  }

  public void addPageDeclaration(String name, String resourcePath) throws TapestryPluginException
  {
    if (fPageEditFactory == null)
      createPageFactory();

    StringWriter writer = new StringWriter();
    IndentingWriter indenter = new IndentingWriter(
        writer,
        true,
        fUseTabs,
        fTabSize,
        0,
        fLineDelimiter);

    XMLUtil.writeLibraryPage(1, indenter, name, resourcePath);

    String content = writer.toString();

    setEdit(fPageEditFactory.getTextEdit(content));
  }

  public void addComponentDeclaration(String id, String resourcePath) throws TapestryPluginException
  {
    if (fComponentEditFactory == null)
      createComponentFactory();

    StringWriter writer = new StringWriter();
    IndentingWriter indenter = new IndentingWriter(
        writer,
        true,
        fUseTabs,
        fTabSize,
        0,
        fLineDelimiter);

    XMLUtil.writeLibraryComponent(1, indenter, id, resourcePath);

    String content = writer.toString();

    setEdit(fComponentEditFactory.getTextEdit(content));
  }

  private void createPageFactory() throws TapestryPluginException
  {

    if (fSpec == null)
    {
      fPageEditFactory = getFactoryFromDocumentPartitioning("page");
      return;
    }
    List declarations = fSpec.getPageDeclarations();
    if (declarations != null)
    {

      PluginPageDeclaration decl = (PluginPageDeclaration) declarations.get(0);
      ISourceLocationInfo location = (ISourceLocationInfo) decl.getLocation();

      fPageEditFactory = new EditFactory();
      fPageEditFactory.offset = getDocumentInsertBeforeOffset(location.getOffset());
    }

    if (fPageEditFactory == null)
      fPageEditFactory = getFactoryFromDocumentPartitioning("page");
  }

  private void createComponentFactory() throws TapestryPluginException
  {
    if (fSpec == null)
    {
      fComponentEditFactory = getFactoryFromDocumentPartitioning("component-type");
      return;
    }
    List declarations = fSpec.getComponentTypeDeclarations();
    if (declarations != null && !declarations.isEmpty())
    {
      PluginComponentTypeDeclaration decl = (PluginComponentTypeDeclaration) declarations
          .get(0);
      ISourceLocationInfo location = (ISourceLocationInfo) decl.getLocation();

      fComponentEditFactory = new EditFactory();
      fComponentEditFactory.offset = getDocumentInsertBeforeOffset(location.getOffset());
    }

    if (fComponentEditFactory == null)
      fComponentEditFactory = getFactoryFromDocumentPartitioning("component-type");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.SpecificationEdits#getDTD()
   */
  protected DTD getDefaultDTD()
  {
    return DOMValidator.getDTD(XMLPublicIDUtil.getPublicId(XMLPublicIDUtil.DTD_3_0));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.SpecificationEdits#getExpectedRootNodeName()
   */
  protected String getExpectedRootNodeName()
  {
    return "library-specification";
  }

}