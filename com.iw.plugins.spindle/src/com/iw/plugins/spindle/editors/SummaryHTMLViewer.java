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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.pde.internal.ui.editor.text.PDEPartitionScanner;
import org.eclipse.swt.widgets.Composite;

import com.iw.plugins.spindle.html.TapestryHTMLPartitionScanner;
import com.iw.plugins.spindle.html.TapestryHTMLSourceConfiguration;
import com.iw.plugins.spindle.ui.text.ColorManager;

public class SummaryHTMLViewer extends SummarySourceViewer {

  /**
   * Constructor for OverviewSourceViewer
   */
  public SummaryHTMLViewer(Composite parent) {
    super(parent);
    configure(new TapestryHTMLSourceConfiguration(new ColorManager()));
    ITextHover hover = new Hover();
    setTextHover(hover, TapestryHTMLPartitionScanner.HTML_COMMENT);
    setTextHover(hover, TapestryHTMLPartitionScanner.HTML_TAG);
    setTextHover(hover, TapestryHTMLPartitionScanner.JWC_TAG);
    setTextHover(hover, TapestryHTMLPartitionScanner.JWCID_TAG);
    setTextHover(hover, PDEPartitionScanner.XML_COMMENT);
    setTextHover(hover, PDEPartitionScanner.XML_DEFAULT);
    setTextHover(hover, PDEPartitionScanner.XML_TAG);
    activatePlugins();
  }

  protected IDocumentPartitioner createDocumentPartitioner() {
    RuleBasedPartitioner partitioner =
      new RuleBasedPartitioner(
        new TapestryHTMLPartitionScanner(),
        new String[] {
          TapestryHTMLPartitionScanner.JWC_TAG,
          TapestryHTMLPartitionScanner.JWCID_TAG,
          TapestryHTMLPartitionScanner.HTML_TAG, 
          TapestryHTMLPartitionScanner.HTML_COMMENT });
    return partitioner;
  }

  protected class Hover implements ITextHover {

    /**
     * Constructor for Hover
     */
    public Hover() {
      super();
    }

    /**
    * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
    */
    public String getHoverInfo(ITextViewer viewer, IRegion region) {
      ITypedRegion useRegion = (ITypedRegion) getHoverRegion(viewer, region.getOffset());
      if (useRegion != null) {
        return useRegion.getType();
      }
      return null;
    }

    /**
     * @see ITextHover#getHoverRegion(ITextViewer, int)
     */
    public IRegion getHoverRegion(ITextViewer viewer, int offset) {
      try {
        return viewer.getDocument().getPartition(offset);
      } catch (BadLocationException blex) {
        return null;
      }
    }
  }

}