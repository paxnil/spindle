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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iw.plugins.spindle.editors.BaseSourceConfiguration;
import com.iw.plugins.spindle.ui.text.CommentScanner;
import com.iw.plugins.spindle.ui.text.DefaultScanner;
import com.iw.plugins.spindle.ui.text.IColorConstants;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;
import com.iw.plugins.spindle.ui.text.JWCIDTagScanner;
import com.iw.plugins.spindle.ui.text.JWCTagScanner;
import com.iw.plugins.spindle.ui.text.NonRuleBasedDamagerRepairer;
import com.iw.plugins.spindle.ui.text.TagAttributeScanner;

public class TemplateSourceConfiguration extends BaseSourceConfiguration implements IColorConstants
{

    private JWCTagScanner fJwcTagScanner;
    private JWCIDTagScanner fJwcidTagScanner;
    private TagAttributeScanner fTagScanner;
    private CommentScanner fCommentScanner;
    private DefaultScanner fDefaultScanner;

    private ITextDoubleClickStrategy doubleClickStrategy;

    public TemplateSourceConfiguration(ISpindleColorManager colorManager, ITextEditor editor)
    {
        super(colorManager, editor);
    }
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
    {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            TemplatePartitionScanner.HTML_COMMENT,
            TemplatePartitionScanner.JWCID_TAG,
            TemplatePartitionScanner.JWC_TAG,
            TemplatePartitionScanner.HTML_TAG };
    }

    protected TagAttributeScanner getTagScanner()
    {
        if (fTagScanner == null)
        {
            fTagScanner = new TagAttributeScanner(getColorManager());
            fTagScanner.setDefaultReturnToken(new Token(new TextAttribute(getColorManager().getColor(P_TAG))));
        }
        return fTagScanner;
    }

    protected JWCIDTagScanner getJWCIDTagScanner()
    {
        if (fJwcidTagScanner == null)
        {
            fJwcidTagScanner = new JWCIDTagScanner(getColorManager());
            fJwcidTagScanner.setDefaultReturnToken(new Token(new TextAttribute(getColorManager().getColor(P_TAG))));
        }
        return fJwcidTagScanner;
    }

    protected JWCTagScanner getJWCTagScanner()
    {
        if (fJwcTagScanner == null)
        {
            fJwcTagScanner = new JWCTagScanner(getColorManager());
            fJwcTagScanner.setDefaultReturnToken(new Token(new TextAttribute(getColorManager().getColor(P_TAG))));
        }
        return fJwcTagScanner;
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
    {
        PresentationReconciler reconciler = new PresentationReconciler();

        NonRuleBasedDamagerRepairer dr =
            new NonRuleBasedDamagerRepairer(new TextAttribute(getColorManager().getColor(P_DEFAULT)));
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new NonRuleBasedDamagerRepairer(new TextAttribute(getColorManager().getColor(P_XML_COMMENT)));
        reconciler.setDamager(dr, TemplatePartitionScanner.HTML_COMMENT);
        reconciler.setRepairer(dr, TemplatePartitionScanner.HTML_COMMENT);

        DefaultDamagerRepairer ddr = new DefaultDamagerRepairer(getJWCTagScanner());
        reconciler.setDamager(ddr, TemplatePartitionScanner.JWC_TAG);
        reconciler.setRepairer(ddr, TemplatePartitionScanner.JWC_TAG);

        ddr = new DefaultDamagerRepairer(getJWCIDTagScanner());
        reconciler.setDamager(ddr, TemplatePartitionScanner.JWCID_TAG);
        reconciler.setRepairer(ddr, TemplatePartitionScanner.JWCID_TAG);

        ddr = new DefaultDamagerRepairer(getTagScanner());
        reconciler.setDamager(ddr, TemplatePartitionScanner.HTML_TAG);
        reconciler.setRepairer(ddr, TemplatePartitionScanner.HTML_TAG);

        return reconciler;
    }

}