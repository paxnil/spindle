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
package com.iw.plugins.spindle.editors.multi.lib;

import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.multi.FormPage;
import com.iw.plugins.spindle.editors.multi.FormSection;

public class OverviewLibGeneralSection extends FormSection
//implements IModelChangedListener
{

    private Text dtdText;
    private FormEntry nameText;
    private boolean updateNeeded;

    public OverviewLibGeneralSection(FormPage page)
    {
        super(page);
        setHeaderText(UIPlugin.getString("overview-lib-general-header"));
        setDescription(UIPlugin.getString("overview-lib-general-description"));
    }

    public void initialize(Object input)
    {
        //        TapestryLibraryModel model = (TapestryLibraryModel) input;
        //        update(input);
        //        dtdText.setEditable(false);
        //        if (model.isEditable() == false)
        //        {
        //            nameText.getControl().setEditable(false);
        //        }
        //        model.addModelChangedListener(this);
    }

    public void dispose()
    {
        dtdText.dispose();
        //        getModel().removeModelChangedListener(this);
        super.dispose();
    }

    public void update()
    {
        this.update(getFormPage().getModel());
    }

    public void update(Object input)
    {
        ILibrarySpecification library = (ILibrarySpecification) getFormPage().getModel();
        if (library == null)
        {
            dtdText.setText("");
        } else
        {
            dtdText.setText(library.getPublicId());
        }

        IEditorInput editorInput = getFormPage().getEditor().getEditorInput();
        IStorage storage = (IStorage) editorInput.getAdapter(IStorage.class);
        nameText.setValue(storage.getName(), true);
        updateNeeded = false;
    }

    /**
     * @see FormSection#createClient(Composite, FormWidgetFactory)
     */
    public Composite createClientContainer(Composite parent, FormWidgetFactory factory)
    {
        Composite container = factory.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.verticalSpacing = 7;
        layout.horizontalSpacing = 6;
        container.setLayout(layout);

        final ILibrarySpecification spec = (ILibrarySpecification) getFormPage().getModel();
        String labelName = UIPlugin.getString("overview-lib-general-dtd-label");
        dtdText = createText(container, labelName, factory);
        dtdText.setText(spec == null ? UIPlugin.getString("overview-lib-general-dtd-problems") : spec.getPublicId());
        dtdText.setEnabled(false);

        labelName = UIPlugin.getString("overview-lib-general-libraryName-name-label");
        nameText = new FormEntry(createText(container, labelName, factory));
        ((Text) nameText.getControl()).setEditable(false);

        factory.paintBordersFor(container);
        return container;
    }

    public boolean isDirty()
    {
        return nameText.isDirty();
    }

    //    public void modelChanged(IModelChangedEvent event)
    //    {
    //        int eventType = event.getChangeType();
    //        if (eventType == IModelChangedEvent.WORLD_CHANGED)
    //        {
    //            updateNeeded = true;
    //            return;
    //        }
    //        if (eventType == IModelChangedEvent.CHANGE)
    //        {
    //            updateNeeded = true;
    //        }
    //    }

}