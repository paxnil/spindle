/**********************************************************************
Copyright (c) 2002 Roberto Gonzalez Rocha and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Roberto Gonzalez Rocha - Initial version
	Igor Malinin - refactoring, minor changes

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.solareclipse.ui.ColorEditor;
import net.sf.solareclipse.ui.preferences.ChainedPreferenceStore;
import net.sf.solareclipse.ui.preferences.ITextStylePreferences;
import net.sf.solareclipse.ui.preferences.OverlayPreferenceStore;
import net.sf.solareclipse.ui.preferences.PreferenceDescriptor;
import net.sf.solareclipse.xml.internal.ui.text.XMLConfiguration;
import net.sf.solareclipse.xml.ui.XMLPlugin;
import net.sf.solareclipse.xml.ui.text.IXMLSyntaxConstants;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.osgi.framework.Bundle;


/**
 * The XMLSyntaxPreferencePage is a preference page that
 * handles setting the colors used by the XML editors.
 */
public class XMLSyntaxPreferencePage
	extends PreferencePage
	implements
		IWorkbenchPreferencePage
{
	public final PreferenceDescriptor[] fKeys =
		new PreferenceDescriptor[] {
			new PreferenceDescriptor(PreferenceDescriptor.BOOLEAN,
				AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_DEFAULT +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_DEFAULT +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_TAG +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_TAG +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_ATT_NAME +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_ATT_NAME +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_ATT_VALUE +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_ATT_VALUE +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_ENTITY +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_ENTITY +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_CDATA +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_CDATA +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_PI +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_PI +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_COMMENT +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_COMMENT +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_DECL +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.XML_DECL +
				ITextStylePreferences.SUFFIX_STYLE),

			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.DTD_CONDITIONAL +
				ITextStylePreferences.SUFFIX_FOREGROUND),
			new PreferenceDescriptor(PreferenceDescriptor.STRING,
				IXMLSyntaxConstants.DTD_CONDITIONAL +
				ITextStylePreferences.SUFFIX_STYLE),
		};

	OverlayPreferenceStore overlay;

	final String[][] fSyntaxColorListModel = new String[][] {
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.others"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_DEFAULT},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.Tag"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_TAG},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.AttName"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_ATT_NAME},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.AttValue"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_ATT_VALUE},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.Entity"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_ENTITY},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.CDATA"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_CDATA},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.PI"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_PI},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.Comment"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_COMMENT},
		{XMLPlugin.getResourceString(
				"XmlSyntaxPreferencePage.Declaration"), //$NON-NLS-1$
			IXMLSyntaxConstants.XML_DECL},
		{XMLPlugin.getResourceString(	
				"XmlSyntaxPreferencePage.Conditional"), //$NON-NLS-1$
			IXMLSyntaxConstants.DTD_CONDITIONAL},
	};

	private XMLTextTools xmlTextTools;

	private Color  bgColor;

	Button bgDefault;
	Button bgCustom;

	ColorEditor bgColorEditor;

	List colors;

	ColorEditor fgColorEditor;
	Button fgBold;

	SourceViewer preview;

	/**
	 * Constructor for XMLSyntaxPreferencePage.
	 */
	public XMLSyntaxPreferencePage() {
		setDescription(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.description")); //$NON-NLS-1$

		setPreferenceStore(XMLPlugin.getDefault().getPreferenceStore());

		overlay = new OverlayPreferenceStore(getPreferenceStore(), fKeys);
	}

	protected Control createContents(Composite parent) {
		overlay.load();
		overlay.start();

		Composite colorComposite = new Composite(parent, SWT.NULL);
		colorComposite.setLayout(new GridLayout());

		Group backgroundComposite = new Group(
			colorComposite, SWT.SHADOW_ETCHED_IN);

		backgroundComposite.setLayout(new RowLayout());
		backgroundComposite.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.backgroundColor")); //$NON-NLS-1$

		SelectionListener backgroundSelectionListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean custom = bgCustom.getSelection();
				bgColorEditor.getButton().setEnabled(custom);
				overlay.setValue(AbstractTextEditor
					.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, !custom);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		};

		bgDefault = new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		bgDefault.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.systemDefault")); //$NON-NLS-1$
		bgDefault.addSelectionListener(backgroundSelectionListener);

		bgCustom = new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		bgCustom.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.custom")); //$NON-NLS-1$
		bgCustom.addSelectionListener(backgroundSelectionListener);

		bgColorEditor = new ColorEditor(backgroundComposite);

		Label label = new Label(colorComposite, SWT.LEFT);
		label.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite = new Composite(colorComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);

		colors = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		colors.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fgColorEditor = new ColorEditor(stylesComposite);

		Button fgColorButton = fgColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		fgColorButton.setLayoutData(gd);

		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.bold")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fgBold = new Button(stylesComposite, SWT.CHECK);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		fgBold.setLayoutData(gd);

		label = new Label(colorComposite, SWT.LEFT);
		label.setText(XMLPlugin.getResourceString(
			"XmlSyntaxPreferencePage.preview")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control previewer = createPreviewer(colorComposite);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(20);
		gd.heightHint = convertHeightInCharsToPixels(5);
		previewer.setLayoutData(gd);

		colors.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				handleSyntaxColorListSelection();
			}
		});

		bgColorEditor.getButton().addSelectionListener(
			new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {}

				public void widgetSelected(SelectionEvent e) {
					PreferenceConverter.setValue(overlay,
						AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
						bgColorEditor.getColorValue());
				}
			}
		);

		fgColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				int i = colors.getSelectionIndex();

				String key = fSyntaxColorListModel[i][1];

				PreferenceConverter.setValue(overlay,
					key + ITextStylePreferences.SUFFIX_FOREGROUND,
					fgColorEditor.getColorValue());
			}
		});

		fgBold.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				int i = colors.getSelectionIndex();

				String key = fSyntaxColorListModel[i][1];

				String value = (fgBold.getSelection())
					? ITextStylePreferences.STYLE_BOLD
					: ITextStylePreferences.STYLE_NORMAL;

				overlay.setValue(
					key + ITextStylePreferences.SUFFIX_STYLE, value);
			}
		});

		initialize();

		return colorComposite;
	}

	private Control createPreviewer(Composite parent) {
		xmlTextTools = new XMLTextTools(overlay); // REVISIT: DTD

		preview = new SourceViewer(parent, null,
			SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		preview.configure(new XMLConfiguration(xmlTextTools));
		preview.getTextWidget().setFont(
			JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		preview.setEditable(false);

		initializeViewerColors(preview);

		String content = loadPreviewContentFromFile("preview.xml"); //$NON-NLS-1$
		IDocument document = new Document(content);

		IDocumentPartitioner partitioner = xmlTextTools.createXMLPartitioner(); // REVISIT: DTD

		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);

		preview.setDocument(document);

		overlay.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String p = event.getProperty();
				if (p.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND) ||
					p.equals(AbstractTextEditor
						.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				) {
					initializeViewerColors(preview);
				}

				preview.invalidateTextPresentation();
			}
		});

		return preview.getControl();
	}

	/**
	 * Initializes the given viewer's colors.
	 * 
	 * @param viewer the viewer to be initialized
	 */
	void initializeViewerColors(ISourceViewer viewer) {
		if (overlay != null) {
			StyledText styledText = viewer.getTextWidget();

			// ---------- background color ----------------------
			Color color = null;
			if (
				!overlay.getBoolean(AbstractTextEditor
					.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
			) {
				color = createColor(
					overlay,
					AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
					styledText.getDisplay());
			}

			styledText.setBackground(color);

			if (bgColor != null) {
				bgColor.dispose();
			}

			bgColor = color;
		}
	}

	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(
		IPreferenceStore store, String key, Display display
	) {
		RGB rgb = null;

		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb = PreferenceConverter.getDefaultColor(store, key);
			} else {
				rgb = PreferenceConverter.getColor(store, key);
			}

			if (rgb != null) {
				return new Color(display, rgb);
			}
		}

		return null;
	}

	void handleSyntaxColorListSelection() {
		int i = colors.getSelectionIndex();

		String key = fSyntaxColorListModel[i][1];

		RGB rgb = PreferenceConverter
			.getColor(overlay, key + ITextStylePreferences.SUFFIX_FOREGROUND);

		fgColorEditor.setColorValue(rgb);

		// REVISIT
		fgBold.setSelection(
			overlay.getString(key + ITextStylePreferences.SUFFIX_STYLE)
				.indexOf(ITextStylePreferences.STYLE_BOLD) >= 0);
	}

	private String loadPreviewContentFromFile(String filename) {
		StringBuffer string = new StringBuffer(512);

		try {
			char[] buf = new char[512];
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				XMLSyntaxPreferencePage.class.getResourceAsStream(filename)));

			try {
				while (true) {
					int n = reader.read(buf);
					if (n < 0) {
						break;
					}

					string.append(buf, 0, n);
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {}

		return string.toString();
	}

	/**
	 * 
	 */
	private void initialize() {
		initializeFields();

		for (int i = 0; i < fSyntaxColorListModel.length; i++) {
			colors.add(fSyntaxColorListModel[i][0]);
		}

		colors.getDisplay().asyncExec(new Runnable() {
			public void run() {
				colors.select(0);
				handleSyntaxColorListSelection();
			}
		});
	}

	private void initializeFields() {
		RGB rgb= PreferenceConverter.getColor(overlay,
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		bgColorEditor.setColorValue(rgb);		

		boolean def = overlay.getBoolean(
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		bgDefault.setSelection(def);
		bgCustom.setSelection(!def);
		bgColorEditor.getButton().setEnabled(!def);
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		overlay.loadDefaults();
		//initializeFields();
		handleSyntaxColorListSelection();

		super.performDefaults();

		preview.invalidateTextPresentation();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		overlay.propagate();
		XMLPlugin.getDefault().savePluginPreferences();

		return true;
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if (xmlTextTools != null) {
			xmlTextTools.dispose();
			xmlTextTools = null;
		}

		if (overlay != null) {
			overlay.stop();
			overlay = null;
		}

		super.dispose();
	}

	public static void initializeDefaultPreferences(IPreferenceStore store) {
		// REVISIT: start of common preferences

		PreferenceConverter.setDefault(store,
			AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,
			Display.getDefault().getSystemColor(
				SWT.COLOR_LIST_FOREGROUND).getRGB());

		store.setDefault(
			AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT,
			true);

		PreferenceConverter.setDefault(store,
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
			Display.getDefault().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND).getRGB());

		store.setDefault(
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT,
			true);

		Bundle plugin = Platform.getBundle("org.eclipse.ui.editors"); //$NON-NLS-1$
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin = (AbstractUIPlugin) plugin;
			IPreferenceStore uiStore = uiPlugin.getPreferenceStore();
			if (uiStore != null) {
				Set keys = new HashSet(Arrays.asList(new String[] {
					AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END,
					AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN,
//					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION,
//					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR,
//					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER,
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR,
				}));

				Iterator i = new MarkerAnnotationPreferences()
					.getAnnotationPreferences().iterator();

				while (i.hasNext()) {
					AnnotationPreference info = (AnnotationPreference) i.next();
					keys.add(info.getColorPreferenceKey());
					keys.add(info.getTextPreferenceKey());
					keys.add(info.getOverviewRulerPreferenceKey());
				}

				ChainedPreferenceStore.startPropagating(uiStore, store, keys);
			}
		}

//		WorkbenchChainedTextFontFieldEditor
//			.startPropagate(store, JFaceResources.TEXT_FONT);

		plugin = Platform.getBundle("org.eclipse.ui.workbench"); //$NON-NLS-1$
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin = (AbstractUIPlugin) plugin;
			IPreferenceStore uiStore = uiPlugin.getPreferenceStore();
			if (uiStore != null) {
				ChainedPreferenceStore.startPropagating(uiStore, store,
					new String[] {JFaceResources.TEXT_FONT}
				);
			}
		}

//		store.setDefault(
//			DefaultSourceViewerConfiguration.PREFERENCE_TAB_WIDTH, 4);

		// REVISIT: end of common preferences

		setDefault(store, IXMLSyntaxConstants.XML_DEFAULT,
			"0,0,0", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_TAG,
			"127,0,127", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_ATT_NAME,
			"0,127,0", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_ATT_VALUE,
			"0,0,255", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_ENTITY,
			"127,127,0", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_CDATA,
			"127,127,0", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_PI,
			"127,127,127", ITextStylePreferences.STYLE_BOLD);

		setDefault(store, IXMLSyntaxConstants.XML_COMMENT,
			"127,0,0", ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_DECL,
			"127,0,127", ITextStylePreferences.STYLE_BOLD);

		setDefault(store, IXMLSyntaxConstants.DTD_CONDITIONAL,
			"127,127,0", ITextStylePreferences.STYLE_BOLD);
	}

	private static void setDefault(
		IPreferenceStore store, String constant, String color, String style
	) {
		store.setDefault(
			constant + ITextStylePreferences.SUFFIX_FOREGROUND, color);

		store.setDefault(constant + ITextStylePreferences.SUFFIX_STYLE, style);
	}
}
