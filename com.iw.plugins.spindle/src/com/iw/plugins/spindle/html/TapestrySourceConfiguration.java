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
package com.iw.plugins.spindle.html;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.RuleBasedDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.iw.plugins.spindle.ui.text.CommentScanner;
import com.iw.plugins.spindle.ui.text.DefaultScanner;
import com.iw.plugins.spindle.ui.text.IColorConstants;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;
import com.iw.plugins.spindle.ui.text.JWCIDTagScanner;
import com.iw.plugins.spindle.ui.text.JWCTagScanner;
import com.iw.plugins.spindle.ui.text.TagAttributeScanner;

public class TapestrySourceConfiguration extends SourceViewerConfiguration {

  private JWCTagScanner jwcTagScanner;
  private JWCIDTagScanner jwcidTagScanner;
  private TagAttributeScanner tagScanner;
  private CommentScanner commentScanner;
  private DefaultScanner defaultScanner;
  private ISpindleColorManager colorManager;

  public TapestrySourceConfiguration(ISpindleColorManager colorManager) {
    this.colorManager = colorManager;
  }
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[] {
      IDocument.DEFAULT_CONTENT_TYPE,
      TapestryPartitionScanner.HTML_COMMENT,
      TapestryPartitionScanner.JWCID_TAG,
      TapestryPartitionScanner.JWC_TAG,
      TapestryPartitionScanner.HTML_TAG };
  }
  public ITextDoubleClickStrategy getDoubleClickStrategy(
    ISourceViewer sourceViewer,
    String contentType) {
    return super.getDoubleClickStrategy(sourceViewer, contentType);
  }

  protected CommentScanner getCommentScanner() {
    if (commentScanner == null) {
      commentScanner = new CommentScanner(colorManager);
    }
    return commentScanner;
  }

  protected DefaultScanner getDefaultScanner() {
    if (defaultScanner == null) {
      defaultScanner = new DefaultScanner(colorManager);
    }
    return defaultScanner;
  }

  protected TagAttributeScanner getTagScanner() {
    if (tagScanner == null) {
      tagScanner = new TagAttributeScanner(colorManager);
    }
    return tagScanner;
  }

  protected JWCIDTagScanner getJWCIDTagScanner() {
    if (jwcidTagScanner == null) {
      jwcidTagScanner = new JWCIDTagScanner(colorManager);
    }
    return jwcidTagScanner;
  }
  
  protected JWCTagScanner getJWCTagScanner() {
    if (jwcTagScanner == null) {
      jwcTagScanner = new JWCTagScanner(colorManager);
    }
    return jwcTagScanner;
  }  

  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();

    RuleBasedDamagerRepairer dr = 
      new RuleBasedDamagerRepairer(
        getDefaultScanner(),
        new TextAttribute(colorManager.getColor(IColorConstants.P_DEFAULT)));
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    dr =
      new RuleBasedDamagerRepairer(
        getJWCTagScanner(),
        new TextAttribute(colorManager.getColor(IColorConstants.P_TAG)));
    reconciler.setDamager(dr, TapestryPartitionScanner.JWC_TAG);
    reconciler.setRepairer(dr, TapestryPartitionScanner.JWC_TAG);
    
    dr =
      new RuleBasedDamagerRepairer(
        getJWCIDTagScanner(),
        new TextAttribute(colorManager.getColor(IColorConstants.P_TAG)));
    reconciler.setDamager(dr, TapestryPartitionScanner.JWCID_TAG);
    reconciler.setRepairer(dr, TapestryPartitionScanner.JWCID_TAG);

    dr =
      new RuleBasedDamagerRepairer(
        getTagScanner(),
        new TextAttribute(colorManager.getColor(IColorConstants.P_TAG)));
    reconciler.setDamager(dr, TapestryPartitionScanner.HTML_TAG);
    reconciler.setRepairer(dr, TapestryPartitionScanner.HTML_TAG);

    dr =
      new RuleBasedDamagerRepairer(
        null,
        new TextAttribute(colorManager.getColor(IColorConstants.P_XML_COMMENT)));
    reconciler.setDamager(dr, TapestryPartitionScanner.HTML_COMMENT);
    reconciler.setRepairer(dr, TapestryPartitionScanner.HTML_COMMENT);

    return reconciler;
  }
}