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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.xmen.internal.ui.text.XMLReconciler;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.documentsAndModels.BaseDocumentSetupParticipant;
import com.iw.plugins.spindle.editors.documentsAndModels.SpecDocumentSetupParticipant;
import com.iw.plugins.spindle.editors.formatter.FormattingPreferences;
import com.iw.plugins.spindle.editors.spec.BasicSpecConfiguration;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * SpindleFormatterPreferencePage
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: SpindleFormatterPreferencePage.java,v 1.1.2.1 2004/07/14
 *                     21:15:47 glongman Exp $
 */
public class SpindleFormatterPreferencePage extends AbstractPreferencePage
{

  private SourceViewer fPreviewViewer;
  private PreviewerUpdater fPreviewerUpdater;
  private IDocument fDocument;
  private BasicSpecConfiguration fSpecConfiguration;
  private IContentFormatter fFormatter;
  private String fContent;
  private MarginPainter fMarginPainter;

  /**
   *  
   */
  public SpindleFormatterPreferencePage()
  {
    super("preference-formatter-title", "applicationDialog.gif");
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent)
  {
    initializeDialogUnits(parent);
    getOverlayStore().load();
    getOverlayStore().start();
    int numColumns = 2;
    Composite result = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    result.setLayout(layout);

    Group indentationGroup = createGroup(numColumns, result, UIPlugin
        .getString("preference-formatter-indentation-group"));

    String labelText = UIPlugin.getString("preference-formatter-tab-size");
    String[] errorMessages = new String[]{
        UIPlugin.getString("preference-formatter-tab-size-error-1"),
        UIPlugin.getString("preference-formatter-tab-size-error-2")};

    addTextField(
        indentationGroup,
        labelText,
        PreferenceConstants.FORMATTER_TAB_SIZE,
        3,
        0,
        errorMessages);

    labelText = UIPlugin.getString("preference-formatter-use-tabs");
    addCheckBox(indentationGroup, labelText, PreferenceConstants.FORMATTER_TAB_CHAR, 1);

    Group wrappingGroup = createGroup(numColumns, result, UIPlugin
        .getString("preference-formatter-wrap-group"));

    labelText = UIPlugin.getString("preference-formatter-max-line-width");
    errorMessages = new String[]{
        UIPlugin.getString("preference-formatter-max-line-width-error-1"),
        UIPlugin.getString("preference-formatter-max-line-width-error-2")};

    addTextField(
        wrappingGroup,
        labelText,
        PreferenceConstants.FORMATTER_MAX_LINE_LENGTH,
        3,
        0,
        errorMessages);

    labelText = UIPlugin.getString("preference-formatter-wrap-long");
    addCheckBox(wrappingGroup, labelText, PreferenceConstants.FORMATTER_WRAP_LONG, 1);

    labelText = UIPlugin.getString("preference-formatter-collapse-to-one-line");
    addCheckBox(
        wrappingGroup,
        labelText,
        PreferenceConstants.FORMATTER_PRESERVE_BLANK_LINES,
        1);

    Label label = new Label(result, SWT.LEFT);
    labelText = UIPlugin.getString("preference-formatter-preview");
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Control previewer = createPreviewer(result);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = convertWidthInCharsToPixels(20);
    gd.heightHint = convertHeightInCharsToPixels(5);
    previewer.setLayoutData(gd);

    initializeFields();

    applyDialogFont(result);

    return result;
  }
  protected OverlayPreferenceStore createOverlayStore()
  {
    List overlayKeys = new ArrayList();
    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.BOOLEAN,
        PreferenceConstants.FORMATTER_WRAP_LONG));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.BOOLEAN,
        PreferenceConstants.FORMATTER_ALIGN));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.INT,
        PreferenceConstants.FORMATTER_MAX_LINE_LENGTH));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.BOOLEAN,
        PreferenceConstants.FORMATTER_TAB_CHAR));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.INT,
        PreferenceConstants.FORMATTER_TAB_SIZE));

    overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
        OverlayPreferenceStore.BOOLEAN,
        PreferenceConstants.FORMATTER_PRESERVE_BLANK_LINES));

    OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys
        .size()];
    overlayKeys.toArray(keys);

    return new OverlayPreferenceStore(getPreferenceStore(), keys);
  }
  private Control createPreviewer(Composite parent)
  {
    fPreviewViewer = new SourceViewer(parent, null, null, false, SWT.BORDER
        | SWT.V_SCROLL | SWT.H_SCROLL);

    fSpecConfiguration = new BasicSpecConfiguration(UIPlugin
        .getDefault()
        .getXMLTextTools(), null, getOverlayStore())
    {

      public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
      {
        return null;
      }

      public IReconciler getReconciler(ISourceViewer sourceViewer)
      {
        return null;
      }

      public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
      {
        return null;
      }

      public IInformationControlCreator getInformationControlCreator(
          ISourceViewer sourceViewer)
      {
        return null;
      }

      public IAutoIndentStrategy getAutoIndentStrategy(
          ISourceViewer sourceViewer,
          String contentType)
      {
        return null;
      }

      public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType)
      {
        return new String[]{"\t", "    ", ""};
      }
    };

    fPreviewViewer.configure(fSpecConfiguration);
    fPreviewViewer.setEditable(false);
    Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
    fPreviewViewer.getTextWidget().setFont(font);
    fMarginPainter = installMarginPainter(fPreviewViewer);

    fPreviewerUpdater = new PreviewerUpdater(fPreviewViewer, getOverlayStore());

    fContent = loadPreviewContentFromFile("FormatPreview.txt");

    fDocument = new Document(fContent);
    XMLReconciler model = new SpecDocumentSetupParticipant().setup(fDocument);
    BaseDocumentSetupParticipant.removeModel(fDocument);
    fPreviewViewer.setDocument(fDocument);
    format(getOverlayStore());
    return fPreviewViewer.getControl();
  }

  /**
   * Preference key for print margin ruler color.
   */
  private final static String PRINT_MARGIN_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
  /**
   * @param previewViewer
   * @return
   */
  private MarginPainter installMarginPainter(SourceViewer previewViewer)
  {
    MarginPainter painter = new MarginPainter(previewViewer);
    EditorsPlugin ePlugin = EditorsPlugin.getDefault();

    IPreferenceStore store = ePlugin.getPreferenceStore();
    RGB rgb = PreferenceConverter.getColor(store, PRINT_MARGIN_COLOR);

    painter.setMarginRulerColor(ePlugin.getSharedTextColors().getColor(rgb));
    painter.setMarginRulerColumn(getOverlayStore().getInt(
        PreferenceConstants.FORMATTER_MAX_LINE_LENGTH));
    ITextViewerExtension2 extension = (ITextViewerExtension2) previewViewer;
    extension.addPainter(painter);
    return painter;

  }
  private void format(final IPreferenceStore store)
  {
    if (fFormatter == null)
    {
      FormattingPreferences prefs = new FormattingPreferences();
      prefs.setPreferenceStore(store);
      fFormatter = UIUtils.createXMLContentFormatter(prefs);
    }
    fDocument.set(fContent);
    fFormatter.format(fDocument, new Region(0, fDocument.getLength()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ant.internal.ui.preferences.AbstractAntEditorPreferencePage#handleDefaults()
   */
  protected void handleDefaults()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose()
  {
    super.dispose();
    if (fPreviewerUpdater != null)
    {
      fPreviewerUpdater.dispose();
    }
  }
  class PreviewerUpdater
  {

    public PreviewerUpdater(final SourceViewer viewer,
        final IPreferenceStore preferenceStore)
    {

      final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
      {
        /*
         * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event)
        {
          if (FormattingPreferences.affectsFormatting(event))
          {

            if (PreferenceConstants.FORMATTER_MAX_LINE_LENGTH.equals(event.getProperty()))
              fMarginPainter.setMarginRulerColumn(getOverlayStore().getInt(
                  PreferenceConstants.FORMATTER_MAX_LINE_LENGTH));

            //            if
            // (PreferenceConstants.FORMATTER_TAB_SIZE.equals(event.getProperty()))
//            viewer.setRedraw(false);
            format(preferenceStore);
//            viewer.setSelection(TextSelection.emptySelection());
//            viewer.setRedraw(true);
          }
        }
      };

      viewer.getTextWidget().addDisposeListener(new DisposeListener()
      {
        /*
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e)
        {
          preferenceStore.removePropertyChangeListener(propertyChangeListener);

        }
      });
      preferenceStore.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     *  
     */
    public void dispose()
    {

    }
  }
}