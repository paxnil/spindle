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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.multi.application;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.ui.forms.internal.FormEntry;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import org.eclipse.update.ui.forms.internal.IFormTextListener;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.editors.NotImplementedYetAction;
import com.iw.plugins.spindle.editors.multi.FormPage;
import com.iw.plugins.spindle.editors.multi.FormSection;
import com.iw.plugins.spindle.ui.util.ToolTipHandler;

public class OverviewAppGeneralSection extends FormSection //implements IModelChangedListener
{

    private Text dtdText;
    private FormEntry nameText;
    private FormEntry engineClassText;
    private boolean updateNeeded;
    private ChooseEngineClassAction chooseEngineAction = new ChooseEngineClassAction();
    private OpenEngineClassAction openEngineClassAction = new OpenEngineClassAction();
    private String hierarchyRoot = "net.sf.tapestry.IEngine";
    private ToolTipHandler tooltipHandler;

    public OverviewAppGeneralSection(FormPage page)
    {
        super(page);
        setHeaderText(UIPlugin.getString("overview-app-general-header"));
        setDescription(UIPlugin.getString("overview-app-general-description"));
    }

    public void initialize(Object input)
    {
        //        TapestryApplicationModel model = (TapestryApplicationModel) input;
        //        update(input);
        //        dtdText.setEditable(false);
        //        if (model.isEditable() == false)
        //        {
        //            nameText.getControl().setEditable(false);
        //            engineClassText.getControl().setEditable(false);
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
        if (updateNeeded)
        {
            this.update(getFormPage().getModel());
        }
    }

    public void update(Object input)
    {
        //        TapestryApplicationModel model = (TapestryApplicationModel) input;
        //        PluginApplicationSpecification spec = (PluginApplicationSpecification) model.getSpecification();
        //        String name = spec.getName();
        //        String dtdVersion = spec.getPublicId();
        //
        //        getFormPage().getForm().setHeadingText(name);
        //        ((SpindleMultipageEditor) getFormPage().getEditor()).updateTitle();
        //        nameText.setValue(spec.getName(), true);
        //        engineClassText.setValue(spec.getEngineClassName(), true);
        //        dtdText.setText(dtdVersion);
        //        updateNeeded = false;
    }

    /**
     * @see FormSection#createClient(Composite, FormWidgetFactory)
     */
    public Composite createClientContainer(Composite parent, FormWidgetFactory factory)
    {

        tooltipHandler = new ToolTipHandler(parent.getShell());

        Composite container = factory.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.verticalSpacing = 7;
        layout.horizontalSpacing = 6;
        container.setLayout(layout);

        String labelName = UIPlugin.getString("overview-app-general-dtd-label");
        dtdText = createText(container, labelName, factory);
        dtdText.setText(UIPlugin.getString("overview-app-general-dtd-problems"));
        dtdText.setEnabled(false);

        labelName = UIPlugin.getString("overview-app-general-application-name-label");
        nameText = new FormEntry(createText(container, labelName, factory));
        nameText.addFormTextListener(new IFormTextListener()
        {
            public void textValueChanged(FormEntry text)
            {
                PluginApplicationSpecification appSpec = (PluginApplicationSpecification) getFormPage().getModel();
                String name = appSpec.getName();
                appSpec.setName(text.getValue());
                if (getFormPage().getEditor().isReadOnly())
                {
                    name = UIPlugin.getString("READ_ONLY_LABEL", name);
                }
                getFormPage().getForm().setHeadingText(name);
            }
            public void textDirty(FormEntry text)
            {
                // TODO - do something! forceDirty();
            }
        });

        labelName = UIPlugin.getString("overview-app-general-engine-class-label");
        engineClassText = new FormEntry(createText(container, labelName, factory));
        Control text = engineClassText.getControl();
        text.setData("TIP_TEXT", UIPlugin.getString("overview-app-general-engine-class-tooltip"));
        tooltipHandler.activateHoverHelp(text);

        engineClassText.addFormTextListener(new IFormTextListener()
        {
            public void textValueChanged(FormEntry text)
            {
                PluginApplicationSpecification appSpec = (PluginApplicationSpecification) getFormPage().getModel();
                if (getFormPage().getEditor().isReadOnly())
                {

                    String name = appSpec.getEngineClassName();
                    appSpec.setEngineClassName(text.getValue());
                    name = UIPlugin.getString("READ_ONLY_LABEL", name);

                    getFormPage().getForm().setHeadingText(name);
                    return;
                }
                String newValue = text.getValue();
                appSpec.setEngineClassName(newValue);
            }
            public void textDirty(FormEntry text)
            {
                // TODO - do something! forceDirty();
            }
        });
        MenuManager popupMenuManager = new MenuManager();
        IMenuListener listener = new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager mng)
            {
                fillContextMenu(mng);
            }
        };
        popupMenuManager.setRemoveAllWhenShown(true);
        popupMenuManager.addMenuListener(listener);
        Menu menu = popupMenuManager.createContextMenu(engineClassText.getControl());
        engineClassText.getControl().setMenu(menu);
        factory.paintBordersFor(container);
        return container;
    }

    protected void fillContextMenu(IMenuManager manager)
    {
        //        String engineClass = engineClassText.getValue();
        //        openEngineClassAction.setEnabled(engineClass != null && !"".equals(engineClass.trim()));
        //        TapestryApplicationModel model = (TapestryApplicationModel) getFormPage().getModel();
        //        chooseEngineAction.setEnabled(model.isEditable());
        //
        //        manager.add(openEngineClassAction);
        //        manager.add(chooseEngineAction);
    }

    private boolean checkEngineClass(String value)
    {
        return true;
    }

    public void commitChanges(boolean onSave)
    {
        nameText.commit();
        engineClassText.commit();
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

    class OpenEngineClassAction extends NotImplementedYetAction
    {}
    //    extends Action
    //    {
    //
    //        /**
    //         * Constructor for NewPropertyAction
    //         */
    //        protected OpenEngineClassAction()
    //        {
    //            super();
    //            setText("open engine class");
    //            setToolTipText("open the engine class in an editor");
    //        }
    //
    //        /**
    //        * @see Action#run()
    //        */
    //        public void run()
    //        {
    //            String engineClass = engineClassText.getValue();
    //            ITapestryModel model = (ITapestryModel) getFormPage().getModel();
    //            try
    //            {
    //                IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
    //                IType type = jproject.findType(engineClass);
    //                JavaUI.openInEditor(type);
    //            } catch (Exception e)
    //            {
    //                MessageDialog.openError(
    //                    engineClassText.getControl().getShell(),
    //                    "Error opening editor",
    //                    "could not open an editor for " + engineClass);
    //            }
    //        }
    //    }

    class ChooseEngineClassAction extends NotImplementedYetAction
    {}
    //    extends Action
    //    {
    //
    //        /**
    //         * Constructor for NewPropertyAction
    //         */
    //        protected ChooseEngineClassAction()
    //        {
    //            super();
    //            setText("choose engine class");
    //            setToolTipText("choose the engine class");
    //        }
    //
    //        /**
    //        * @see Action#run()
    //        */
    //        public void run()
    //        {
    //            IType newEngine = chooseType();
    //            if (newEngine != null)
    //            {
    //                engineClassText.setValue(newEngine.getFullyQualifiedName());
    //            }
    //        }
    //
    //        private IType chooseType()
    //        {
    //            ITapestryModel model = (ITapestryModel) getFormPage().getModel();
    //            Shell shell = engineClassText.getControl().getShell();
    //            try
    //            {
    //                IJavaProject jproject = TapestryPlugin.getDefault().getJavaProjectFor(model.getUnderlyingStorage());
    //                if (jproject == null)
    //                {
    //                    return null;
    //                }
    //                IJavaSearchScope scope = createSearchScope(jproject);
    //
    //                SelectionDialog dialog =
    //                    JavaUI.createTypeDialog(
    //                        shell,
    //                        new ProgressMonitorDialog(shell),
    //                        scope,
    //                        IJavaElementSearchConstants.CONSIDER_CLASSES,
    //                        false);
    //
    //                dialog.setTitle("Choose Engine Class");
    //                String message = "choose Engine class";
    //                dialog.setMessage(hierarchyRoot == null ? message : message + " (implements " + hierarchyRoot + ")");
    //
    //                if (dialog.open() == dialog.OK)
    //                {
    //                    return (IType) dialog.getResult()[0]; //FirstResult();
    //                }
    //            } catch (CoreException jmex)
    //            {
    //                ErrorDialog.openError(shell, "Spindle error", "unable to continue", jmex.getStatus());
    //            }
    //            return null;
    //        }
    //
    //        private IJavaSearchScope createSearchScope(IJavaProject jproject)
    //        {
    //
    //            IJavaSearchScope result = null;
    //            IType hrootElement = null;
    //            try
    //            {
    //                if (hierarchyRoot != null)
    //                {
    //                    hrootElement = jproject.findType(hierarchyRoot);
    //                }
    //                if (hrootElement != null)
    //                {
    //                    //          result = SearchEngine.createHierarchyScope(hrootElement);
    //                    // note, this is a kludge to work around bug 
    //                    //[ 621849 ] Class selection dlg searches workspace
    //                    result = new HierarchyScope(hrootElement, jproject);
    //
    //                }
    //            } catch (JavaModelException jmex)
    //            {
    //                //ignore
    //                jmex.printStackTrace();
    //            }
    //            if (result == null)
    //            {
    //                IJavaElement[] elements = new IJavaElement[] { jproject };
    //                result = SearchEngine.createJavaSearchScope(elements);
    //            }
    //            return result;
    //        }
    //
    //    }

}