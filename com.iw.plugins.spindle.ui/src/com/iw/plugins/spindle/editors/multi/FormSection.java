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

package com.iw.plugins.spindle.editors.multi;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.ScrollableSectionForm;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class FormSection
    extends org.eclipse.update.ui.forms.internal.FormSection //implements IModelChangedListener
{
    private FormPage fFormPage;
    private Composite fContainer;

    public FormSection(FormPage formPage)
    {
        this.fFormPage = formPage;
        setCollapsable(true);
    }
    
    public FormPage getFormPage()
    {
        return fFormPage;
    }
    //    public void modelChanged(IModelChangedEvent e)
    //    {}

    protected void reflow()
    {
        super.reflow();
        AbstractSectionForm form = fFormPage.getForm();
        if (form instanceof ScrollableSectionForm)
        {
            ((ScrollableSectionForm) form).updateScrollBars();
        }
    }

    public final Composite createClient(Composite parent, FormWidgetFactory factory)
    {
        fContainer = createClientContainer(parent, factory);
        return fContainer;
    }

    public Composite createClientContainer(Composite parent, FormWidgetFactory factory)
    {
        throw new Error("implement in subclasses!");
    }
}
