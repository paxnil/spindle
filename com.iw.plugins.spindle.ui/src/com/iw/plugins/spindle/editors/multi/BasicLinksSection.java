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
package com.iw.plugins.spindle.editors.multi;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.IHyperlinkListener;

public abstract class BasicLinksSection extends FormSection //implements IModelChangedListener
{

    protected FormWidgetFactory useFactory;
    protected boolean updateNeeded = true;
    private Composite linksParent;
    protected Button moreButton;

    /**
     * Constructor for TapestryPageSection
     */
    public BasicLinksSection(FormPage page, String headerText, String description)
    {
        super(page);
        setHeaderText(headerText);
        if (description != null)
        {
            setDescription(description);
        }
    }

    /**
     * @see FormSection#createClient(Composite, FormWidgetFactory)
     */
    public Composite createClientContainer(Composite parent, FormWidgetFactory factory)
    {
        useFactory = factory;
        Composite container = useFactory.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 2;

        linksParent = useFactory.createComposite(container);

        RowLayout rlayout = new RowLayout();
        rlayout.wrap = true;
        linksParent.setLayout(layout);

        GridData gd = new GridData(GridData.FILL_BOTH);
        linksParent.setLayoutData(gd);

        Composite buttonContainer = useFactory.createComposite(container);
        gd = new GridData(GridData.FILL_VERTICAL);
        buttonContainer.setLayoutData(gd);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        //layout.numColumns = 2;
        buttonContainer.setLayout(layout);

        createButtons(factory, buttonContainer);

        return container;
    }

    protected void createButtons(FormWidgetFactory factory, Composite parent)
    {
        //        GridData gd;
        //        moreButton = factory.createButton(parent, "More", SWT.PUSH);
        //        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
        //        moreButton.setLayoutData(gd);
        //
        //        final IMultiPage targetPage = getGotoPage();
        //        if (targetPage == null)
        //        {
        //            return;
        //        }
        //        moreButton.setToolTipText(((IFormPage) targetPage).getTitle());
        //        moreButton.addSelectionListener(new SelectionAdapter()
        //        {
        //            public void widgetSelected(SelectionEvent e)
        //            {
        //                getFormPage().getEditor().openTo(targetPage, );
        //            }
        //        });
    }

    //    protected Button getMoreButton()
    //    {
    //        return moreButton;
    //    }

    //    protected IMultiPage getGotoPage()
    //    {
    //        return getFormPage().getEditor().getSourcePage();
    //    }

    protected void addHyperLink(String key, String value, Image image, IHyperlinkListener listener)
    {
        Label imageLabel = useFactory.createLabel(linksParent, " ");
        Label hyperlink = useFactory.createHyperlinkLabel(linksParent, value, listener);
        imageLabel.setImage(image);
        hyperlink.setData(key);
    }


    public void initialize(Object input)
    {
        update(true);
    }

    public void dispose()
    {
        linksParent.dispose();
        moreButton.dispose();
        //        getModel().removeModelChangedListener(this);
        super.dispose();
    }

    public void update()
    {
        
        this.update(true);        
    }

    public void update(boolean removePrevious)
    {
        afterUpdate(removePrevious);
    }

    protected void removeAll()
    {
        Control[] children = linksParent.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            children[i].dispose();
        }
    }

    protected void afterUpdate(boolean removePrevious)
    {
        if (removePrevious)
        {
            linksParent.layout(true);
            linksParent.redraw();
        }
        updateNeeded = false;
        reflow();
    }

}