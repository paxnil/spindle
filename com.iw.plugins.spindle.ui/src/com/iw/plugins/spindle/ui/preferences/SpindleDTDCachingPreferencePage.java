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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
public class SpindleDTDCachingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    private BooleanFieldEditor fToggleDTDCaching;

    /**
     * Constructor for SpindleRefactorPreferencePage.
     * @param style
     */
    public SpindleDTDCachingPreferencePage()
    {
        super("Spindle 3.0", Images.getImageDescriptor("applicationDialog.gif"));
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

        Composite top = new Composite(parent, SWT.LEFT);
        top.setFont(font);

        // Sets the layout data for the top composite's 
        // place in its parent's layout.
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        createVerticalSpacer(top, 1);

        fToggleDTDCaching =
            new BooleanFieldEditor(
                TapestryCore.CACHE_GRAMMAR_PREFERENCE,
                UIPlugin.getString("preference-dtd-caching"),
                BooleanFieldEditor.DEFAULT,
                top);

        fToggleDTDCaching.setPreferencePage(this);
        fToggleDTDCaching.setPreferenceStore(TapestryCore.getDefault().getPreferenceStore());
        fToggleDTDCaching.load();
        
        createVerticalSpacer(top, 1);

        GridData gd;

        Composite clearCacheComp = new Composite(top, SWT.NONE);
        GridLayout clearCacheLayout = new GridLayout();
        clearCacheLayout.numColumns = 2;
        clearCacheLayout.marginHeight = 0;
        clearCacheLayout.marginWidth = 0;
        clearCacheComp.setLayout(clearCacheLayout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        clearCacheComp.setLayoutData(gd);
        clearCacheComp.setFont(font);

        Label clearCacheLabel = new Label(clearCacheComp, SWT.NONE);
        clearCacheLabel.setText(UIPlugin.getString("preference-clear-dtd-cache-label"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        clearCacheLabel.setLayoutData(gd);
        clearCacheLabel.setFont(font);

        Button clearCacheButton = new Button(clearCacheComp, SWT.NULL);
        clearCacheButton.setText(UIPlugin.getString("preference-clear-dtd-cache-button"));
        gd = new GridData(GridData.FILL);
        clearCacheButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent evt)
            {
                TapestryCore.getDefault().clearDTDCache();
            }
        });

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
        fToggleDTDCaching.loadDefault();

        super.performDefaults();
    }

    public boolean performOk()
    {
        fToggleDTDCaching.store();

        return super.performOk();
    }

}
