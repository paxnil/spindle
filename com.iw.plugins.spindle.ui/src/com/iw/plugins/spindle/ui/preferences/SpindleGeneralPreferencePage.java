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
package com.iw.plugins.spindle.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;

/**
 * @author GWL
 * @version $Id$
 *
 * Copryright 2002, Intelligent Works Inc.
 * All Rights Reserved
 */
public class SpindleGeneralPreferencePage
    extends PreferencePage
    implements IWorkbenchPreferencePage, IPropertyChangeListener
{
    private static final String EDITOR_DISPLAY_TAB_WIDTH = PreferenceConstants.EDITOR_DISPLAY_TAB_WIDTH;
    private static final String FORMATTER_PRESERVE_BLANK_LINES = PreferenceConstants.FORMATTER_PRESERVE_BLANK_LINES;
    private static final String FORMATTER_USE_TABS_TO_INDENT = PreferenceConstants.FORMATTER_USE_TABS_TO_INDENT;
    private static final String BUILD_MISS = TapestryCore.BUILDER_MARKER_MISSES;
    private static final String[][] BUILD_MISS_OPTIONS =
        new String[][] {
            new String[] { TapestryCore.BUILDER_MARKER_MISSES_INFO, TapestryCore.BUILDER_MARKER_MISSES_INFO },
            new String[] { TapestryCore.BUILDER_MARKER_MISSES_WARN, TapestryCore.BUILDER_MARKER_MISSES_WARN },
            new String[] { TapestryCore.BUILDER_MARKER_MISSES_ERROR, TapestryCore.BUILDER_MARKER_MISSES_ERROR },
            new String[] { TapestryCore.BUILDER_MARKER_MISSES_IGNORE, TapestryCore.BUILDER_MARKER_MISSES_IGNORE }
    };

    private RadioGroupFieldEditor fBuildMisses;
    private IntegerFieldEditor fDisplayTabWidth;
    private BooleanFieldEditor fPreserveBlankLines;
    private BooleanFieldEditor fUseTabsForIndentation;
    //    private BooleanFieldEditor fToggleDTDCaching;

    /**
     * Constructor for SpindleRefactorPreferencePage.
     * @param style
     */
    public SpindleGeneralPreferencePage()
    {
        super(UIPlugin.getString("preference-general-title"), Images.getImageDescriptor("applicationDialog.gif"));
        setDescription(UIPlugin.getString("preference-general-settings"));
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench)
    {}

    protected Control createContents(Composite parent)
    {
        Font font = parent.getFont();
        GridData gd;

        Composite top = new Composite(parent, SWT.LEFT);
        top.setFont(font);

        // Sets the layout data for the top composite's 
        // place in its parent's layout.
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createVerticalSpacer(top, 1);

        fBuildMisses =
            new RadioGroupFieldEditor(
                BUILD_MISS,
                UIPlugin.getString("preference-build-miss"),
                4,
                BUILD_MISS_OPTIONS,
                top);

        fBuildMisses.setPreferencePage(this);
        fBuildMisses.setPreferenceStore(TapestryCore.getDefault().getPreferenceStore());
        fBuildMisses.load();
        setValid(fBuildMisses.isValid());
        fBuildMisses.setPropertyChangeListener(this);
        
        createVerticalSpacer(top, 1);

        Composite displayComp = new Composite(top, SWT.NONE);
        GridLayout displayCompLayout = new GridLayout();
        displayCompLayout.numColumns = 2;
        displayCompLayout.marginHeight = 0;
        displayCompLayout.marginWidth = 0;
        displayComp.setLayout(displayCompLayout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        displayComp.setLayoutData(gd);
        displayComp.setFont(font);

        fDisplayTabWidth =
            new IntegerFieldEditor(
                EDITOR_DISPLAY_TAB_WIDTH,
                UIPlugin.getString("preference-editor-tab-display-width"),
                displayComp,
                4)
        {
            public void showErrorMessage(String message)
            {
                super.showErrorMessage(UIPlugin.getString("preference-editor-tab-display-width-error"));
            }
        };
        fDisplayTabWidth.setValidRange(1, 10);
        fDisplayTabWidth.setPreferencePage(this);
        fDisplayTabWidth.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
        fDisplayTabWidth.load();
        setValid(fDisplayTabWidth.isValid());
        fDisplayTabWidth.setPropertyChangeListener(this);

        createVerticalSpacer(top, 1);

        fPreserveBlankLines =
            new BooleanFieldEditor(
                FORMATTER_PRESERVE_BLANK_LINES,
                UIPlugin.getString("preference-formatter-preserve-blank-lines"),
                BooleanFieldEditor.DEFAULT,
                top);

        fPreserveBlankLines.setPreferencePage(this);
        fPreserveBlankLines.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
        fPreserveBlankLines.load();
        fPreserveBlankLines.setPropertyChangeListener(this);

        fUseTabsForIndentation =
            new BooleanFieldEditor(
                FORMATTER_USE_TABS_TO_INDENT,
                UIPlugin.getString("preference-formatter-use-tabs-for-indent"),
                BooleanFieldEditor.DEFAULT,
                top);

        fUseTabsForIndentation.setPreferencePage(this);
        fUseTabsForIndentation.setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
        fUseTabsForIndentation.load();
        fUseTabsForIndentation.setPropertyChangeListener(this);

        //        createVerticalSpacer(top, 1);

        //        fToggleDTDCaching =
        //            new BooleanFieldEditor(
        //                TapestryCore.CACHE_GRAMMAR_PREFERENCE,
        //                UIPlugin.getString("preference-dtd-caching"),
        //                BooleanFieldEditor.DEFAULT,
        //                top);
        //
        //        fToggleDTDCaching.setPreferencePage(this);
        //        fToggleDTDCaching.setPreferenceStore(TapestryCore.getDefault().getPreferenceStore());
        //        fToggleDTDCaching.load();
        //
        //        createVerticalSpacer(top, 1);
        //
        //        Composite clearCacheComp = new Composite(top, SWT.NONE);
        //        GridLayout clearCacheLayout = new GridLayout();
        //        clearCacheLayout.numColumns = 2;
        //        clearCacheLayout.marginHeight = 0;
        //        clearCacheLayout.marginWidth = 0;
        //        clearCacheComp.setLayout(clearCacheLayout);
        //        gd = new GridData(GridData.FILL_HORIZONTAL);
        //        clearCacheComp.setLayoutData(gd);
        //        clearCacheComp.setFont(font);
        //
        //        Label clearCacheLabel = new Label(clearCacheComp, SWT.NONE);
        //        clearCacheLabel.setText(UIPlugin.getString("preference-clear-dtd-cache-label"));
        //        gd = new GridData(GridData.FILL_HORIZONTAL);
        //        gd.horizontalSpan = 1;
        //        clearCacheLabel.setLayoutData(gd);
        //        clearCacheLabel.setFont(font);
        //
        //        Button clearCacheButton = new Button(clearCacheComp, SWT.NULL);
        //        clearCacheButton.setText(UIPlugin.getString("preference-clear-dtd-cache-button"));
        //        gd = new GridData(GridData.FILL);
        //        clearCacheButton.addSelectionListener(new SelectionAdapter()
        //        {
        //            public void widgetSelected(SelectionEvent evt)
        //            {
        //                TapestryCore.getDefault().clearDTDCache();
        //            }
        //        });

        return top;
    }

    /**
     * Create some empty space.
     */
    protected void createVerticalSpacer(Composite comp, int colSpan)
    {
        Label label = new Label(comp, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = colSpan;
        label.setLayoutData(gd);
    }

    protected void performDefaults()
    {
        fBuildMisses.loadDefault();
        fDisplayTabWidth.loadDefault();
        fPreserveBlankLines.loadDefault();
        fUseTabsForIndentation.loadDefault();
        //        fToggleDTDCaching.loadDefault();

        super.performDefaults();
    }

    public boolean performOk()
    {
        fBuildMisses.store();
        fDisplayTabWidth.store();
        fPreserveBlankLines.store();
        fUseTabsForIndentation.store();
        //        fToggleDTDCaching.store();
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#isValid()
     */
    public boolean isValid()
    {
        return fDisplayTabWidth.isValid();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(FieldEditor.IS_VALID))
        {
            boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
            // If the new value is true then we must check all field editors.
            // If it is false, then the page is invalid in any case.
            if (newValue)
            {
                setValid(
                    fDisplayTabWidth.isValid() && fPreserveBlankLines.isValid() && fUseTabsForIndentation.isValid());
            } else
            {
                setValid(newValue);
            }
        }
    }

}
