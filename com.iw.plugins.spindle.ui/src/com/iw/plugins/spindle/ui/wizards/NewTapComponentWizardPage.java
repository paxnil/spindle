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
package com.iw.plugins.spindle.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.actions.RequiredSaveEditorAction;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.ContextResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.ui.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.wizards.factories.ComponentFactory;
import com.iw.plugins.spindle.ui.wizards.fields.ComponentNameField;
import com.iw.plugins.spindle.ui.wizards.fields.NamespaceDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.TapestryProjectDialogField;

public class NewTapComponentWizardPage extends TapestryWizardPage
{

    public static final String P_GENERATE_HTML = "new.component.generate.html";
    public static final String P_HTML_TO_GENERATE = "new.component.html.to.generate";
    public static final String P_OPENALL = "new.component.open.all";

    String PAGE_NAME;

    private static int LABEL_WIDTH = 64;

    String PROJECT;
    String NAMESPACE;
    String SPEC_CLASS;
    String COMPONENTNAME;
    String AUTO_ADD;
    String GENERATE_HTML;
    String OPEN_ALL;

    protected TapestryProjectDialogField fTapestryProjectDialogField;
    protected NamespaceDialogField fNamespaceDialogField;
    protected ComponentNameField fComponentNameDialogField;
    protected CheckBoxField fGenerateHTML;
    protected CheckBoxField fOpenAllField;
    protected DialogField fNextLabel;
    protected IFile fComponentFile = null;
    protected IFile fGeneratedHTMLFile = null;

    public static void initializeDefaults(IPreferenceStore pstore)
    {
        pstore.setDefault(P_GENERATE_HTML, true);
        pstore.setDefault(
            P_HTML_TO_GENERATE,
            UIPlugin.getString("TAPESTRY.xmlComment") + UIPlugin.getString("TAPESTRY.genHTMLSource"));
    }

    /**
     * Constructor for NewTapAppWizardPage1
     */
    public NewTapComponentWizardPage(IWorkspaceRoot root, String pageName)
    {
        super(UIPlugin.getString(pageName + ".title"));
        PAGE_NAME = pageName;
        PROJECT = PAGE_NAME + ".project";
        NAMESPACE = PAGE_NAME + ".namespace";
        SPEC_CLASS = PAGE_NAME + ".specclass";
        COMPONENTNAME = PAGE_NAME + ".componentname";

        GENERATE_HTML = PAGE_NAME + ".generateHTML";
        OPEN_ALL = PAGE_NAME + ".openAll";

        this.setImageDescriptor(ImageDescriptor.createFromURL(Images.getImageURL("componentDialog.gif")));

        this.setDescription(UIPlugin.getString(PAGE_NAME + ".description"));

        IDialogFieldChangedListener listener = new FieldEventsAdapter();

        fTapestryProjectDialogField = new TapestryProjectDialogField(PROJECT, root, LABEL_WIDTH);
        connect(fTapestryProjectDialogField);
        fTapestryProjectDialogField.addListener(listener);

        fNamespaceDialogField = new NamespaceDialogField(NAMESPACE, LABEL_WIDTH + 40);
        connect(fNamespaceDialogField);
        fNamespaceDialogField.addListener(listener);

        fComponentNameDialogField = new ComponentNameField(COMPONENTNAME);
        connect(fComponentNameDialogField);
        fComponentNameDialogField.addListener(listener);

        fGenerateHTML = new CheckBoxField(UIPlugin.getString(GENERATE_HTML + ".label"));
        fOpenAllField = new CheckBoxField(UIPlugin.getString(OPEN_ALL + ".label"));
        fNextLabel = new DialogField("Choose a class for the specification on the next page..."); //TODO I10N

    }

    /**
     * Should be called from the wizard with the input element. 
     */
    public void init(IJavaElement jelem, String prepopulateName)
    {
        WizardDialog container = (WizardDialog) getWizard().getContainer();
        IRunnableContext context = (IRunnableContext) container;

        fTapestryProjectDialogField.init(jelem, context);
        if (prepopulateName != null)
        {
            fComponentNameDialogField.setTextValue(prepopulateName);
            fGenerateHTML.setCheckBoxValue(false);
            fGenerateHTML.setEnabled(false);
        } else
        {
            fComponentNameDialogField.setTextValue("");
        }
        fComponentNameDialogField.init(null);
        fNamespaceDialogField.init(
            fTapestryProjectDialogField,
            fComponentNameDialogField,
            getWizard().getClass() == NewTapComponentWizard.class);
    }

    /**
     * @see DialogPage#createControl(Composite)
     */
    public void createControl(Composite container)
    {

        Composite composite = new Composite(container, SWT.NONE);

        FormLayout layout = new FormLayout();
        layout.marginWidth = 4;
        layout.marginHeight = 4;
        composite.setLayout(layout);

        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 0);
        formData.width = 400;
        composite.setLayoutData(formData);

        Control projectFieldControl = fTapestryProjectDialogField.getControl(composite);
        Control namespaceFieldControl = fNamespaceDialogField.getControl(composite);
        Control nameFieldControl = fComponentNameDialogField.getControl(composite);
        Control genHTML = fGenerateHTML.getControl(composite);
        Control openAll = fOpenAllField.getControl(composite);
        Control labelControl = fNextLabel.getControl(composite);

        addControl(nameFieldControl, composite, 10);

        Control separator = createSeparator(composite, nameFieldControl);

        addControl(projectFieldControl, separator, 4);
        addControl(namespaceFieldControl, projectFieldControl, 4);

        separator = createSeparator(composite, namespaceFieldControl);

        addControl(genHTML, separator, 10);

        addControl(openAll, genHTML, 10);

        addControl(labelControl, openAll, 50);

        setControl(composite);
        setFocus();

        fNamespaceDialogField.updateStatus();
        IPreferenceStore pstore = UIPlugin.getDefault().getPreferenceStore();
        fGenerateHTML.setCheckBoxValue(pstore.getBoolean(P_GENERATE_HTML));
        fOpenAllField.setCheckBoxValue(pstore.getBoolean(P_OPENALL));
        updateStatus();

    }

    /**
     * create the specification and the template (if required)
     */
    public IRunnableWithProgress getRunnable(Object specClass)
    {
        final IType useClass = (IType) specClass;
        return new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try
                {
                    if (monitor == null)
                    {
                        monitor = new NullProgressMonitor();
                    }
                    createComponentResource(monitor, useClass);

                    if (fGenerateHTML.getCheckBoxValue())
                    {
                        createHTMLResource(new SubProgressMonitor(monitor, 1));
                    }
                    monitor.done();
                } catch (CoreException e)
                {
                    throw new InvocationTargetException(e);
                }
            }
        };
    }

    //    public void createComponentResource(IProgressMonitor monitor, IType specClass)
    //        throws InterruptedException, CoreException
    //    {
    //        fComponentFile =
    //                                ComponentFactory.createComponent(
    //                                    location,
    //                                    fComponentNameDialogField.getTextValue(),
    //                                    useClass,
    //                                    new SubProgressMonitor(monitor, 1));
    //    }

    protected void createComponentResource(IProgressMonitor monitor, final IType specClass)
        throws CoreException, InterruptedException
    {
        INamespace useNamespace = fNamespaceDialogField.getSelectedNamespace();
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) useNamespace.getSpecificationLocation();

        if (!location.exists() || location.getStorage() == null)
        {
            ContextResourceWorkspaceLocation ctxLoc = (ContextResourceWorkspaceLocation) location;
            IContainer container = (IContainer) ctxLoc.getResource();
            fComponentFile =
                ComponentFactory.createComponent(
                    container,
                    fComponentNameDialogField.getTextValue(),
                    specClass,
                    new SubProgressMonitor(monitor, 1));
        } else
        {

            fComponentFile =
                ComponentFactory.createComponent(
                    location,
                    fComponentNameDialogField.getTextValue(),
                    specClass,
                    new SubProgressMonitor(monitor, 1));
        }
    }

    // TODO stubbed out for now
    public IRunnableWithProgress getAutoAddRunnable()
    {
        //        final boolean addingNewComponent = getWizard().getClass() == NewTapComponentWizard.class;
        //
        //        String extension = "page";
        //        if (addingNewComponent)
        //        {
        //
        //            extension = "jwc";
        //
        //        }
        //        IPackageFragment frag = fPackageDialogField.getPackageFragment();
        //        String componentName = fComponentNameDialogField.getTextValue();
        //        String componentTapestryPath = null;
        //        if (frag.isDefaultPackage())
        //        {
        //            componentTapestryPath = "/" + componentName + "." + extension;
        //        } else
        //        {
        //            componentTapestryPath =
        //                ("/" + frag.getElementName() + "/").replace('.', '/') + componentName + "." + extension;
        //        }
        //        final boolean doAutoAdd = false;
        //        //        final TapestryLibraryModel useSelectedModel = fAutoAddField.getContainerModel();
        //        final Object useSelectedModel = null;
        //        final String useTapestryPath = componentTapestryPath;
        //        final String useComponentName = componentName;
        //        final Shell shell = this.getShell();
        return new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                //                if (monitor == null)
                //                {
                //                    monitor = new NullProgressMonitor();
                //                }
                //                if (doAutoAdd && useSelectedModel != null)
                //                {
                //                    //                    SpindleMultipageEditor targetEditor =
                //                    //                        (SpindleMultipageEditor) Utils.getEditorFor(useSelectedModel.getUnderlyingStorage());
                //                    Object targetEditor = null;
                //
                //                    if (targetEditor != null)
                //                    {
                //                        try
                //                        {
                //                            doAddInEditor(targetEditor, monitor);
                //                        } catch (InterruptedException e)
                //                        {
                //                            //do nothing
                //                        }
                //                    } else
                //                    {
                //                        doAddInWorkspace(monitor);
                //                    }
                //
                //                }

            }

            //            private void doAddInEditor(SpindleMultipageEditor targetEditor, IProgressMonitor monitor)
            //                throws InterruptedException
            //            {
            //                TapestryLibraryModel useModel = (TapestryLibraryModel) targetEditor.getModel();
            //                if (!checkSaveEditor(targetEditor))
            //                {
            //                    MessageDialog.openInformation(shell, UIPlugin.getString(PAGE_NAME + ".autoAddNotPossible"),
            //                    //"AutoAdd not possible",
            //                    UIPlugin.getString(
            //                        PAGE_NAME + ".autoAddParseError",
            //                        useSelectedModel.getUnderlyingStorage().getName())
            //                    //              "A parse error occured while saving "
            //                    //                + useSelectedModel.getUnderlyingStorage().getName()                          
            //                    //                + ".\n The component will be created without adding it to the app."
            //                    );
            //                    return;
            //                }
            //                ILibrarySpecification spec = (ILibrarySpecification) useModel.getSpecification();
            //
            //                if (componentAlreadyExists(spec))
            //                {
            //                    return;
            //                }
            //                performAddToModel(spec);
            //                useSelectedModel.setOutOfSynch(true);
            //                targetEditor.doSave(monitor);
            //                targetEditor.showPage(SpindleMultipageEditor.SOURCE_PAGE);
            //            }

            private void doAddInWorkspace(IProgressMonitor monitor) throws InterruptedException
            {
                //                String consumer = "WizardAutoAddToAppInWorkspace";
                //
                //                try
                //                {
                //                    ITapestryProject tproject = TapestryPlugin.getDefault().getTapestryProjectFor(useSelectedModel);
                //
                //                    TapestryProjectModelManager mgr = tproject.getModelManager();
                //                    mgr.connect(useSelectedModel.getUnderlyingStorage(), consumer, true);
                //                    TapestryLibraryModel useModel =
                //                        (TapestryLibraryModel) mgr.getEditableModel(useSelectedModel.getUnderlyingStorage(), consumer);
                //
                //                    IPluginLibrarySpecification spec = (IPluginLibrarySpecification) useModel.getSpecification();
                //
                //                    if (componentAlreadyExists(spec))
                //                    {
                //                        return;
                //                    }
                //                    performAddToModel(spec);
                //                    Utils.saveModel(useModel, monitor);
                //                    //TapestryPlugin.openTapestryEditor(useModel.getUnderlyingStorage());
                //                    mgr.disconnect(useModel.getUnderlyingStorage(), consumer);
                //                } catch (CoreException e)
                //                {
                //
                //                    throw new InterruptedException("AutoAdd failed");
                //                }
            }

            private boolean componentAlreadyExists(ILibrarySpecification spec)
            {
                //                boolean result = false;
                //                if (addingNewComponent)
                //                {
                //                    result = spec.getComponentSpecificationPath(useComponentName) != null;
                //                } else
                //                {
                //                    result = spec.getPageSpecificationPath(useComponentName) != null;
                //                }
                //                if (result)
                //                {
                //                    MessageDialog
                //                        .openInformation(
                //                            shell,
                //                            UIPlugin.getString(PAGE_NAME + ".autoAddNotPossible"),
                //                            UIPlugin.getString(
                //                                PAGE_NAME + "autoAddAlreadyExisits",
                //                                new Object[] { useComponentName, useSelectedModel.getUnderlyingStorage().getName()})
                //                    //              "The component "
                //                    //                + useComponentName
                //                    //                + " already exists in "
                //                    //                + useSelectedModel.getUnderlyingStorage().getName()
                //                    //                + ".\n The component will be created without adding it to the app."
                //                    );
                //                }
                //                return result;
                return false; //TODO rewrite the new way!
            }

            private void performAddToModel(ILibrarySpecification spec)
            {
                //                if (addingNewComponent)
                //                {
                //                    spec.setComponentSpecificationPath(useComponentName, useTapestryPath);
                //                } else
                //                {
                //                    spec.setPageSpecificationPath(useComponentName, useTapestryPath);
                //                }
            }

            private boolean checkSaveEditor(IEditorPart targetEditor) throws InterruptedException
            {
                if (targetEditor != null && targetEditor.isDirty())
                {

                    RequiredSaveEditorAction saver = new RequiredSaveEditorAction(targetEditor);
                    if (!saver.save())
                    {
                        throw new InterruptedException();
                    }
                }
                return true;
            }
        };
    };
    
    public boolean getOpenAll() {
        return fOpenAllField.getCheckBoxValue();
    }

    public boolean performFinish()
    {
        UIPlugin.getDefault().getPreferenceStore().setValue(P_OPENALL, fOpenAllField.getCheckBoxValue());
        return true;
    }

    public void createHTMLResource(IProgressMonitor monitor) throws InterruptedException, CoreException
    {
        monitor.beginTask("", 4);

        String fileName = fComponentNameDialogField.getTextValue() + ".html";
        IResourceWorkspaceLocation namespaceLocation =
            (IResourceWorkspaceLocation) fNamespaceDialogField.getSelectedNamespace().getSpecificationLocation();
        IFile namespaceFile = (IFile) namespaceLocation.getStorage();
        IContainer container = null;
        if (namespaceFile == null)
        {
            ContextResourceWorkspaceLocation ctxLoc = (ContextResourceWorkspaceLocation) namespaceLocation;
            container = (IContainer) ctxLoc.getResource();
        } else
        {
            container = (IContainer) namespaceFile.getParent();
        }
        IFile newFile = container.getFile(new Path("/" + fileName));

        if (newFile.exists())
            return;

        monitor.worked(1);

        IPreferenceStore pstore = UIPlugin.getDefault().getPreferenceStore();
        String source = pstore.getString(P_HTML_TO_GENERATE);
        String comment = UIPlugin.getString("TAPESTRY.xmlComment");
        if (source == null)
        {
            source = comment + UIPlugin.getString("TAPESTRY.genHTMLSource");
        }
        if (!source.trim().startsWith(comment))
        {
            source = comment + source;
        }
        InputStream contents = new ByteArrayInputStream(source.getBytes());
        monitor.worked(1);
        newFile.create(contents, false, new SubProgressMonitor(monitor, 1));
        monitor.worked(1);
        monitor.done();
        fGeneratedHTMLFile = newFile;
    }
    
    public IFile getGeneratedTemplate() {
        return fGeneratedHTMLFile;
    }

    public IResource getResource()
    {
        return fComponentFile;
    }

    protected void setFocus()
    {
        fComponentNameDialogField.setFocus();
    }

    private void checkEnabled(IStatus status)
    {
        boolean flag = fComponentNameDialogField.getStatus().getSeverity() != IStatus.ERROR;
        fTapestryProjectDialogField.setEnabled(flag);
        fNamespaceDialogField.setEnabled(flag);
        fGenerateHTML.setEnabled(flag);
        fOpenAllField.setEnabled(flag);
    }

    public void updateStatus()
    {
        super.updateStatus();
        checkEnabled(fComponentNameDialogField.getStatus());
    }

    private class FieldEventsAdapter implements IDialogFieldChangedListener
    {

        public void dialogFieldChanged(DialogField field)
        {
            updateStatus();
        }
        /**
         * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
         */
        public void dialogFieldButtonPressed(DialogField field)
        {}

        /**
         * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
         */
        public void dialogFieldStatusChanged(IStatus status, DialogField field)
        {
            if (field == fNamespaceDialogField)
                updateStatus();
        }
    }

    /**
     * @see IWizardPage#canFlipToNextPage()
     */
    public boolean canFlipToNextPage()
    {
        return getCurrentStatus().getSeverity() != IStatus.ERROR;

    }

    public String getChosenComponentName()
    {
        return fComponentNameDialogField.getTextValue();
    }

    public DialogField getComponentNameField()
    {
        return fComponentNameDialogField;
    }

    /**
     * Method getComponentContainerField.
     * @return DialogField
     */
    public DialogField getProjectField()
    {
        return fTapestryProjectDialogField;
    }

    /**
     * Method getChosenContainer.
     * @return String
     */
    public TapestryProject getChosenProject()
    {
        return fTapestryProjectDialogField.getTapestryProject();
    }
}