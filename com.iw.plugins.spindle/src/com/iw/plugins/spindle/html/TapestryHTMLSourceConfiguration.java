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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.pde.internal.ui.editor.text.NonRuleBasedDamagerRepairer;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import com.iw.plugins.spindle.ui.text.CommentScanner;
import com.iw.plugins.spindle.ui.text.DefaultScanner;
import com.iw.plugins.spindle.ui.text.IColorConstants;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;
import com.iw.plugins.spindle.ui.text.JWCIDTagScanner;
import com.iw.plugins.spindle.ui.text.JWCTagScanner;
import com.iw.plugins.spindle.ui.text.TagAttributeScanner;

public class TapestryHTMLSourceConfiguration extends SourceViewerConfiguration implements IColorConstants {

  private JWCTagScanner jwcTagScanner;
  private JWCIDTagScanner jwcidTagScanner;
  private TagAttributeScanner tagScanner;
  private CommentScanner commentScanner;
  private DefaultScanner defaultScanner;
  private ISpindleColorManager colorManager;

  private ITextDoubleClickStrategy doubleClickStrategy;

  public TapestryHTMLSourceConfiguration(ISpindleColorManager colorManager) {
    this.colorManager = colorManager;
  }
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[] {
      IDocument.DEFAULT_CONTENT_TYPE,
      TapestryHTMLPartitionScanner.HTML_COMMENT,
      TapestryHTMLPartitionScanner.JWCID_TAG,
      TapestryHTMLPartitionScanner.JWC_TAG,
      TapestryHTMLPartitionScanner.HTML_TAG };
  }

  protected TagAttributeScanner getTagScanner() {
    if (tagScanner == null) {
      tagScanner = new TagAttributeScanner(colorManager);
      tagScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(P_TAG))));
    }
    return tagScanner;
  }

  protected JWCIDTagScanner getJWCIDTagScanner() {
    if (jwcidTagScanner == null) {
      jwcidTagScanner = new JWCIDTagScanner(colorManager);
      jwcidTagScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(P_TAG))));
    }
    return jwcidTagScanner;
  }

  protected JWCTagScanner getJWCTagScanner() {
    if (jwcTagScanner == null) {
      jwcTagScanner = new JWCTagScanner(colorManager);
      jwcTagScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(P_TAG))));
    }
    return jwcTagScanner;
  }

  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();

    NonRuleBasedDamagerRepairer dr = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(P_DEFAULT)));
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    dr = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(P_XML_COMMENT)));
    reconciler.setDamager(dr, TapestryHTMLPartitionScanner.HTML_COMMENT);
    reconciler.setRepairer(dr, TapestryHTMLPartitionScanner.HTML_COMMENT);

    DefaultDamagerRepairer ddr = new DefaultDamagerRepairer(getJWCTagScanner());
    reconciler.setDamager(ddr, TapestryHTMLPartitionScanner.JWC_TAG);
    reconciler.setRepairer(ddr, TapestryHTMLPartitionScanner.JWC_TAG);

    ddr = new DefaultDamagerRepairer(getJWCIDTagScanner());
    reconciler.setDamager(ddr, TapestryHTMLPartitionScanner.JWCID_TAG);
    reconciler.setRepairer(ddr, TapestryHTMLPartitionScanner.JWCID_TAG);

    ddr = new DefaultDamagerRepairer(getTagScanner());
    reconciler.setDamager(ddr, TapestryHTMLPartitionScanner.HTML_TAG);
    reconciler.setRepairer(ddr, TapestryHTMLPartitionScanner.HTML_TAG);

    return reconciler;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
   */
  public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {

    return new IAnnotationHover() {
      public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
        ResourceMarkerAnnotationModel model = (ResourceMarkerAnnotationModel) sourceViewer.getAnnotationModel();
        Iterator e = model.getAnnotationIterator();
        ArrayList list = new ArrayList();
        while (e.hasNext()) {
          MarkerAnnotation element = (MarkerAnnotation) e.next();
          IMarker marker = element.getMarker();
          if (lineNumber == marker.getAttribute(IMarker.LINE_NUMBER, -1)) {
            list.add(marker.getAttribute(IMarker.MESSAGE, "no message found"));
          }
        }
        if (!list.isEmpty()) {
          if (list.size() == 1) {
            return (String) list.get(0);
          } else {
            StringBuffer buffer = new StringBuffer("multiple markers found on this line:\n\n");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
              buffer.append((String) iter.next());
              if (iter.hasNext()) {
                buffer.append("\n");
              }

            }
          }
        }
        return null;
      }
    };
  }

}