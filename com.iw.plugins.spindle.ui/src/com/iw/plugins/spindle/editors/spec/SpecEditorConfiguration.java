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

package com.iw.plugins.spindle.editors.spec;

import net.sf.solareclipse.xml.internal.ui.text.AttValueDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.SimpleDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.TagDoubleClickStrategy;
import net.sf.solareclipse.xml.internal.ui.text.XMLPartitionScanner;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.BaseSourceConfiguration;
import com.iw.plugins.spindle.editors.DefaultDoubleClickStrategy;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.formatter.FormattingPreferences;
import com.iw.plugins.spindle.editors.spec.assist.AttributeCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.DefaultCompletionProcessor;
import com.iw.plugins.spindle.editors.spec.assist.TagCompletionProcessor;
import com.iw.plugins.spindle.editors.util.CDATACompletionProcessor;
import com.iw.plugins.spindle.editors.util.CommentCompletionProcessor;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;
import com.iw.plugins.spindle.editors.util.DeclCompletionProcessor;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * SourceViewerConfiguration for the TemplateEditor
 * 
 * @author glongman@intelligentworks.com
 *  
 */
public class SpecEditorConfiguration extends BasicSpecConfiguration
{
  public static final boolean DEBUG = false;

  private ITextDoubleClickStrategy fDefaultDoubleClick;
  private ITextDoubleClickStrategy dcsSimple;
  private ITextDoubleClickStrategy dcsTag;
  private ITextDoubleClickStrategy dcsAttValue;

  /**
   * @param colorManager
   * @param editor
   */
  public SpecEditorConfiguration(XMLTextTools tools, Editor editor,
      IPreferenceStore preferenceStore)
  {
    super(tools, editor, preferenceStore);
   
    fDefaultDoubleClick = new DefaultDoubleClickStrategy();
    dcsSimple = new SimpleDoubleClickStrategy();
    dcsTag = new TagDoubleClickStrategy();
    dcsAttValue = new AttValueDoubleClickStrategy();
  }

  /*
   * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer,
   *              String)
   */
  public ITextDoubleClickStrategy getDoubleClickStrategy(
      ISourceViewer sourceViewer,
      String contentType)
  {
    if (XMLPartitionScanner.XML_COMMENT.equals(contentType))
      return dcsSimple;

    if (XMLPartitionScanner.XML_PI.equals(contentType))
      return dcsSimple;

    if (XMLPartitionScanner.XML_TAG.equals(contentType))
      return dcsTag;

    if (XMLPartitionScanner.XML_ATTRIBUTE.equals(contentType))
      return dcsAttValue;

    if (XMLPartitionScanner.XML_CDATA.equals(contentType))
      return dcsSimple;

    if (contentType.startsWith(XMLPartitionScanner.DTD_INTERNAL))
      return dcsSimple;

    return fDefaultDoubleClick;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentFormatter(org.eclipse.jface.text.source.ISourceViewer)
   */
  public IContentFormatter getContentFormatter(ISourceViewer sourceViewer)
  {
    return UIUtils.createXMLContentFormatter(new FormattingPreferences());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer,
   *              java.lang.String)
   */
  public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
  {
    if (getEditor() == null)
      return super.getTextHover(sourceViewer, contentType);

    if (DEBUG)
    {
      return new ITextHover()
      {
        public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
        {
          try
          {
            IDocumentProvider provider = getEditor().getDocumentProvider();
            IDocument doc = provider.getDocument(getEditor().getEditorInput());
            return doc.getPartition(hoverRegion.getOffset()).getType();
          } catch (BadLocationException e)
          {
            return "bad location: " + hoverRegion;
          }
        }

        public IRegion getHoverRegion(ITextViewer textViewer, int offset)
        {
          return new Region(offset, 1);
        }
      };
    }
    return super.getTextHover(sourceViewer, contentType);
  }

  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
  {
    if (getEditor() == null)
      return super.getContentAssistant(sourceViewer);

    ContentAssistant assistant = getEditor().getContentAssistant();
    ContentAssistProcessor tagProcessor = new TagCompletionProcessor(fEditor);
    ContentAssistProcessor commentProcessor = new CommentCompletionProcessor(fEditor);
    ContentAssistProcessor attributeProcessor = new AttributeCompletionProcessor(fEditor);
    ContentAssistProcessor declProcessor = new DeclCompletionProcessor(fEditor);
    ContentAssistProcessor defaultProcessor = new DefaultCompletionProcessor(fEditor);
    ContentAssistProcessor cdataProcessor = new CDATACompletionProcessor(fEditor);

    assistant.setContentAssistProcessor(tagProcessor, XMLPartitionScanner.XML_TAG);
    assistant
        .setContentAssistProcessor(commentProcessor, XMLPartitionScanner.XML_COMMENT);
    assistant.setContentAssistProcessor(
        attributeProcessor,
        XMLPartitionScanner.XML_ATTRIBUTE);
    assistant.setContentAssistProcessor(declProcessor, XMLPartitionScanner.XML_DECL);
    assistant.setContentAssistProcessor(defaultProcessor, IDocument.DEFAULT_CONTENT_TYPE);
    assistant.setContentAssistProcessor(cdataProcessor, XMLPartitionScanner.XML_CDATA);
    assistant.enableAutoActivation(true);
    assistant.setProposalSelectorBackground(UIPlugin
        .getDefault()
        .getSharedTextColors()
        .getColor(new RGB(254, 241, 233)));
    assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
    assistant.install(sourceViewer);

    return assistant;
  }

  /**
   * Returns the xml outline presenter control creator. The creator is a factory
   * creating outline presenter controls for the given source viewer. This
   * implementation always returns a creator for
   * <code>XMLOutlineInformationControl</code> instances.
   * 
   * @param sourceViewer the source viewer to be configured by this
   *                     configuration
   * @return an information control creator
   */
  private IInformationControlCreator getXMLOutlinePresenterControlCreator(
      ISourceViewer sourceViewer)
  {

    return new IInformationControlCreator()
    {
      public IInformationControl createInformationControl(Shell parent)
      {
        int shellStyle = SWT.RESIZE;
        int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
        return new XMLOutlineInformationControl(
            parent,
            shellStyle,
            treeStyle,
            (SpecEditor) getEditor());
      }
    };
  }

  /**
   * Returns the structure outline presenter control creator. The creator is a
   * factory creating outline presenter controls for the given source viewer.
   * This implementation always returns a creator for
   * <code>XMLOutlineInformationControl</code> instances.
   * 
   * @param sourceViewer the source viewer to be configured by this
   *                     configuration
   * @return an information control creator
   */
  private IInformationControlCreator getStructureOutlinePresenterControlCreator(
      ISourceViewer sourceViewer)
  {
    return new IInformationControlCreator()
    {
      public IInformationControl createInformationControl(Shell parent)
      {
        int shellStyle = SWT.RESIZE;
        int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
        return new StructureOutlineInformationControl(
            parent,
            shellStyle,
            treeStyle,
            (SpecEditor) getEditor());
      }
    };
  }

  /**
   * Returns the asset choosercontrol creator. The creator is a factory creating
   * presenter controls for the given source viewer. This implementation always
   * returns a creator for <code>AssetChooserInformationControl</code>
   * instances.
   * 
   * @param sourceViewer the source viewer to be configured by this
   *                     configuration
   * @return an information control creator
   */
  private IInformationControlCreator getAssetChooserControlCreator(
      ISourceViewer sourceViewer)
  {
    return new IInformationControlCreator()
    {
      public IInformationControl createInformationControl(Shell parent)
      {
        int shellStyle = SWT.RESIZE;
        int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
        return new ResourceChooserInformationControl(
            parent,
            shellStyle,
            treeStyle,
            (SpecEditor) getEditor());
      }
    };
  }

  /**
   * Returns the outline presenter which will determine and shown information
   * requested for the current cursor position.
   * 
   * @param sourceViewer the source viewer to be configured by this
   *                     configuration
   * @return an information presenter
   */
  public IInformationPresenter getXMLOutlinePresenter(ISourceViewer sourceViewer)
  {
    InformationPresenter presenter = new InformationPresenter(
        getXMLOutlinePresenterControlCreator(sourceViewer));
    presenter.setAnchor(InformationPresenter.ANCHOR_GLOBAL);
    IInformationProvider provider = new SpecEditor.SpecEditorInformationProvider(
        (SpecEditor) getEditor(),
        false);
    presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_TAG);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_COMMENT);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_ATTRIBUTE);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_DECL);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_CDATA);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_PI);
    presenter.setSizeConstraints(40, 20, true, false);
    return presenter;
  }

  /**
   * Returns the outline presenter which will determine and shown information
   * requested for the current cursor position.
   * 
   * @param sourceViewer the source viewer to be configured by this
   *                     configuration
   * @return an information presenter
   */
  public IInformationPresenter getStructureOutlinePresenter(ISourceViewer sourceViewer)
  {
    InformationPresenter presenter = new InformationPresenter(
        getStructureOutlinePresenterControlCreator(sourceViewer));
    presenter.setAnchor(InformationPresenter.ANCHOR_GLOBAL);
    IInformationProvider provider = new SpecEditor.SpecEditorInformationProvider(
        (SpecEditor) getEditor(),
        true);
    presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_TAG);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_COMMENT);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_ATTRIBUTE);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_DECL);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_CDATA);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_PI);
    presenter.setSizeConstraints(40, 20, true, true);
    return presenter;
  }

  public IInformationPresenter getAssetChooserPresenter(ISourceViewer sourceViewer)
  {
    InformationPresenter presenter = new InformationPresenter(
        getAssetChooserControlCreator(sourceViewer));
    presenter.setAnchor(InformationPresenter.ANCHOR_BOTTOM);
    IInformationProvider provider = new SpecEditor.SpecEditorInformationProvider(
        (SpecEditor) getEditor(),
        false);
    presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_TAG);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_COMMENT);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_ATTRIBUTE);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_DECL);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_CDATA);
    presenter.setInformationProvider(provider, XMLPartitionScanner.XML_PI);
    presenter.setSizeConstraints(40, 20, true, true);
    return presenter;
  }

}