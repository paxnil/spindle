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

import org.eclipse.jface.preference.FieldEditor;
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
    private static final String BUILD_MISS = TapestryCore.BUILDER_MARKER_MISSES;
    private static final String HANDLE_ASSETS = TapestryCore.BUILDER_HANDLE_ASSETS;
    private static final String[][] CORE_STATUS_OPTIONS =
        new String[][] {
            new String[] { TapestryCore.CORE_STATUS_INFO, TapestryCore.CORE_STATUS_INFO },
            new String[] { TapestryCore.CORE_STATUS_WARN, TapestryCore.CORE_STATUS_WARN },
            new String[] { TapestryCore.CORE_STATUS_ERROR, TapestryCore.CORE_STATUS_ERROR },
            new String[] { TapestryCore.CORE_STATUS_IGNORE, TapestryCore.CORE_STATUS_IGNORE }
    };

    private RadioGroupFieldEditor fBuildMisses;
    private RadioGroupFieldEditor fHandleAssets;
 
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
                CORE_STATUS_OPTIONS,
                top);

        fBuildMisses.setPreferencePage(this);
        fBuildMisses.setPreferenceStore(TapestryCore.getDefault().getPreferenceStore());
        fBuildMisses.load();
        setValid(fBuildMisses.isValid());
        fBuildMisses.setPropertyChangeListener(this);

        createVerticalSpacer(top, 1);

        fHandleAssets =
            new RadioGroupFieldEditor(
                HANDLE_ASSETS,
                UIPlugin.getString("preference-handle-assets"),
                4,
                CORE_STATUS_OPTIONS,
                top);

        fHandleAssets.setPreferencePage(this);
        fHandleAssets.setPreferenceStore(TapestryCore.getDefault().getPreferenceStore());
        fHandleAssets.load();
        setValid(fHandleAssets.isValid());
        fHandleAssets.setPropertyChangeListener(this);

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
        fHandleAssets.loadDefault();
 
        super.performDefaults();
    }

    public boolean performOk()
    {
        fBuildMisses.store();
        fHandleAssets.store();
        
         return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#isValid()
     */
    public boolean isValid()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(FieldEditor.IS_VALID))
        {
            boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
            setValid(newValue);
        }
    }

}