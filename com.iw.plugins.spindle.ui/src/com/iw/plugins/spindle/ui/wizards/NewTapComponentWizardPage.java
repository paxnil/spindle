/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@intelligentworks.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.tapestry.INamespace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.actions.RequiredSaveEditorAction;
import com.iw.plugins.spindle.core.ProjectPreferenceStore;
import com.iw.plugins.spindle.core.TapestryException;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.core.util.Files;
import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;
import com.iw.plugins.spindle.editors.documentsAndModels.ApplicationEdits;
import com.iw.plugins.spindle.editors.documentsAndModels.LibraryEdits;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.util.UIUtils;
import com.iw.plugins.spindle.ui.widgets.PreferenceTemplateSelector;
import com.iw.plugins.spindle.ui.wizards.factories.ComponentFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TapestryTemplateFactory;
import com.iw.plugins.spindle.ui.wizards.fields.ComponentLocationChooserField;
import com.iw.plugins.spindle.ui.wizards.fields.ComponentNameField;
import com.iw.plugins.spindle.ui.wizards.fields.ContainerDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.NamespaceDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.PackageDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.TapestryProjectDialogField;

public class NewTapComponentWizardPage extends TapestryWizardPage
{

  public static final String P_GENERATE_HTML = "new.component.generate.html";
  /** @deprecated */
  public static final String P_HTML_TO_GENERATE = "new.component.html.to.generate";
  /** @deprecated */
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

  String LIBRARY_CONTAINER;
  String LIBRARY_PACKAGE;

  String APPLICATION_CONTAINER;

  protected IWorkspaceRoot fRoot;

  protected TapestryProjectDialogField fTapestryProjectDialogField;
  protected NamespaceDialogField fNamespaceDialogField;
  protected ComponentNameField fComponentNameDialogField;
  protected Button fGenerateHTML;
  protected DialogField fNextLabel;
  protected IFile fComponentFile = null;
  protected IFile fTemplateFile = null;
  protected ProjectPreferenceStore fPreferenceStore;

  protected PreferenceTemplateSelector fComponentTemplateSelector;
  protected PreferenceTemplateSelector fTapestryTemplateSelector;

  private Group fTemplatesGroup;
  private PageBook fPageBook;
  private Group fLibraryLocationGroup;
  private Group fApplicationLocationGroup;
  private Control fBlankPage;

  private ContainerDialogField fLibraryContainerField;
  private PackageDialogField fLibraryPackageField;
  private ComponentLocationChooserField fApplicationLocationField;

  private FieldEventsAdapter fListener;
  private boolean fBroken = false;

  public static void initializeDefaultPreferences(IPreferenceStore pstore)
  {
    pstore.setDefault(P_GENERATE_HTML, true);
    //    pstore.setDefault(P_HTML_TO_GENERATE,
    // UIPlugin.getString("TAPESTRY.xmlComment")
    //        + UIPlugin.getString("TAPESTRY.genHTMLSource"));

  }

  /**
   * Constructor for NewTapAppWizardPage1
   */
  public NewTapComponentWizardPage(IWorkspaceRoot root, String pageName)
  {
    super(UIPlugin.getString(pageName + ".title"));
    fRoot = root;
    PAGE_NAME = pageName;
    PROJECT = PAGE_NAME + ".project";
    NAMESPACE = PAGE_NAME + ".namespace";
    SPEC_CLASS = PAGE_NAME + ".specclass";
    COMPONENTNAME = PAGE_NAME + ".componentname";

    GENERATE_HTML = PAGE_NAME + ".generateHTML";
    OPEN_ALL = PAGE_NAME + ".openAll";

    LIBRARY_CONTAINER = PAGE_NAME + ".libraryContainer";
    LIBRARY_PACKAGE = PAGE_NAME + ".libraryPackage";
    APPLICATION_CONTAINER = PAGE_NAME + ".applicationContainer";

    this.setImageDescriptor(ImageDescriptor.createFromURL(Images.getImageURL(UIPlugin
        .getString(PAGE_NAME + ".image"))));

    this.setDescription(UIPlugin.getString(PAGE_NAME + ".description"));

    fListener = new FieldEventsAdapter();

    fTapestryProjectDialogField = new TapestryProjectDialogField(
        PROJECT,
        fRoot,
        LABEL_WIDTH);
    connect(fTapestryProjectDialogField);
    fTapestryProjectDialogField.addListener(fListener);

    fNamespaceDialogField = new NamespaceDialogField(NAMESPACE, LABEL_WIDTH + 40);
    connect(fNamespaceDialogField);
    fNamespaceDialogField.addListener(fListener);

    fComponentNameDialogField = new ComponentNameField(
        COMPONENTNAME,
        getClass() == NewTapComponentWizardPage.class);
    connect(fComponentNameDialogField);
    fComponentNameDialogField.addListener(fListener);

    fNextLabel = new DialogField(UIPlugin.getString(PAGE_NAME + ".nextPage"));

    fComponentTemplateSelector = createComponentTemplateSelector();

    fTapestryTemplateSelector = createTemplateSelector(
        XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE,
        PreferenceConstants.TAP_TEMPLATE_TEMPLATE);

    fLibraryContainerField = new ContainerDialogField(LIBRARY_CONTAINER, fRoot, 0, true);
    connect(fLibraryContainerField);
    fLibraryContainerField.addListener(fListener);
    fLibraryPackageField = new PackageDialogField(LIBRARY_PACKAGE);
    connect(fLibraryPackageField);
    fLibraryPackageField.addListener(fListener);
    fApplicationLocationField = new ComponentLocationChooserField(APPLICATION_CONTAINER);
    connect(fApplicationLocationField);
    fApplicationLocationField.addListener(fListener);

  }

  protected PreferenceTemplateSelector createComponentTemplateSelector()
  {
    return createTemplateSelector(
        XMLFileContextType.COMPONENT_FILE_CONTEXT_TYPE,
        PreferenceConstants.COMPONENT_TEMPLATE);
  }

  protected final PreferenceTemplateSelector createTemplateSelector(
      String templateContextId,
      String preferenceKey)
  {
    PreferenceTemplateSelector result = new PreferenceTemplateSelector(
        templateContextId,
        preferenceKey,
        null);
    result.setReadOnly(true);
    return result;
  }

  /**
   * Should be called from the wizard with the input element.
   */
  public void init(IJavaElement jelem, String prepopulateName)
  {
    WizardDialog container = (WizardDialog) getWizard().getContainer();
    IRunnableContext context = (IRunnableContext) container;

    fTapestryProjectDialogField.init(jelem, context);
    if (fTapestryProjectDialogField.isProjectBroken())
    {
      updateStatus(fTapestryProjectDialogField.getStatus());
      setCompositeEnabled((Composite) getControl(), false);
      return;
    }
    findPreferenceStore();
    if (prepopulateName != null)
    {
      fComponentNameDialogField.setTextValue(prepopulateName);
      setCompositeEnabled(fTemplatesGroup, false);
      setCompositeEnabled(fPageBook, false);
      //      fGenerateHTML.setSelection(false);
      //      fGenerateHTML.setEnabled(false);
    } else
    {
      fComponentNameDialogField.setTextValue("");
    }
    //    fComponentNameDialogField.init(null);
    fNamespaceDialogField.init(
        fTapestryProjectDialogField,
        fComponentNameDialogField,
        getWizard().getClass() == NewTapComponentWizard.class);

    fLibraryContainerField.init(jelem, context);
    fLibraryPackageField.init(fComponentNameDialogField, fLibraryContainerField, context);
    IPackageFragment pack = null;
    if (jelem != null)
    {
      pack = (IPackageFragment) CoreUtils.findElementOfKind(
          jelem,
          IJavaElement.PACKAGE_FRAGMENT);
    }
    fLibraryPackageField.setPackageFragment(pack);

    fApplicationLocationField.init(
        fComponentNameDialogField,
        fTapestryProjectDialogField,
        context);

    namespaceChanged();

    updateStatus();
  }

  /**
   *  
   */
  private void findPreferenceStore()
  {
    fPreferenceStore = null;
    TapestryProject tproject = fTapestryProjectDialogField.getTapestryProject();
    if (tproject != null && tproject.getProject() != null)
    {
      IProject project = tproject.getProject();
      if (project.exists())
      {
        fPreferenceStore = ProjectPreferenceStore.getStore(fTapestryProjectDialogField
            .getTapestryProject()
            .getProject(), UIPlugin.SPINDLEUI_PREFS_FILE, UIPlugin
            .getDefault()
            .getPreferenceStore());
      }
    }

    fComponentTemplateSelector.setPreferenceStore(fPreferenceStore);
    fTapestryTemplateSelector.setPreferenceStore(fPreferenceStore);
  }

  protected void setCompositeEnabled(Composite composite, boolean flag)
  {
    Control[] children = composite.getChildren();
    for (int i = 0; i < children.length; i++)
    {
      children[i].setEnabled(flag);
      if (children[i] instanceof Composite)
        setCompositeEnabled((Composite) children[i], flag);
    }
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

    Control labelControl = fNextLabel.getControl(composite);
    fTemplatesGroup = createGroup(composite);
    fPageBook = createLocationPageBook(composite);

    addControl(nameFieldControl, composite, 10);

    Control separator = createSeparator(composite, nameFieldControl);

    addControl(projectFieldControl, separator, 4);
    addControl(namespaceFieldControl, projectFieldControl, 10);

    separator = createSeparator(composite, namespaceFieldControl);

    addControl(fTemplatesGroup, separator, 10);

    addControl(fPageBook, fTemplatesGroup, 10);

    addControl(labelControl, fPageBook, 20);

    setControl(composite);
    setFocus();

    fNamespaceDialogField.updateStatus();
    IPreferenceStore pstore = UIPlugin.getDefault().getPreferenceStore();
    fGenerateHTML.setSelection(pstore.getBoolean(P_GENERATE_HTML));

  }

  private Group createGroup(Composite container)
  {
    Font font = container.getFont();
    Group group = new Group(container, SWT.NONE);
    GridLayout layout = new GridLayout();
    int columnCount = 3;
    layout.numColumns = columnCount;
    group.setLayout(layout);
    //    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setFont(font);
    group.setText("File Generation: ");

    fComponentTemplateSelector.createControl(group, columnCount);
    //    fComponentTemplateSelector.addSelectionChangedListener(f);

    fGenerateHTML = new Button(group, SWT.CHECK);
    fGenerateHTML.setText(UIPlugin.getString(GENERATE_HTML + ".label"));
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.horizontalSpan = columnCount;
    fGenerateHTML.setLayoutData(data);
    fGenerateHTML.addSelectionListener(fListener);

    fTapestryTemplateSelector.createControl(group, columnCount);
    //    fTapestryTemplateSelector.addSelectionChangedListener(fListener);

    return group;
  }

  private PageBook createLocationPageBook(Composite parent)
  {
    GridData data;
    PageBook book = new PageBook(parent, SWT.NULL);
    book.setFont(parent.getFont());
    data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    data.heightHint = convertHeightInCharsToPixels(4);
    book.setLayoutData(data);

    fBlankPage = new Composite(book, SWT.NULL);
    data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    //    data.widthHint = 0;
    //    data.heightHint = 0;
    fBlankPage.setLayoutData(data);

    fLibraryLocationGroup = createLibraryLocationGroup(book);
    fApplicationLocationGroup = createApplicationLocationGroup(book);

    book.showPage(fLibraryLocationGroup);

    return book;
  }

  private Group createLibraryLocationGroup(PageBook book) 
  {

    Font font = book.getFont();
    Group group = new Group(book, SWT.NONE);
    GridLayout layout = new GridLayout();
    int columnCount = 5;
    layout.numColumns = columnCount;
    group.setLayout(layout);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.heightHint = convertHeightInCharsToPixels(3);
    group.setLayoutData(data);
    group.setFont(font);
    group.setText("Location: ");

    fLibraryContainerField.fillIntoGrid(group, 5);

    fLibraryPackageField.fillIntoGrid(group, 5);

    return group;
  }
  /**
   * @param book
   * @return
   */
  private Group createApplicationLocationGroup(PageBook book)
  {
    Font font = book.getFont();
    Group group = new Group(book, SWT.NONE);
    GridLayout layout = new GridLayout();
    int columnCount = 5;
    layout.numColumns = columnCount;
    group.setLayout(layout);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.heightHint = convertHeightInCharsToPixels(2);
    group.setLayoutData(data);
    group.setFont(font);
    group.setText("Location: ");

    fApplicationLocationField.fillIntoGrid(group, 5);

    return group;
  }

  public INamespace getSelectedNamespace()
  {
    return fNamespaceDialogField.getSelectedNamespace();
  }

  /**
   * create the specification and the template (if required)
   */
  public IRunnableWithProgress getRunnable(Object specClass)
  {
    final IType useClass = (IType) specClass;
    return new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        try
        {
          if (monitor == null)
          {
            monitor = new NullProgressMonitor();
          }

          createSpecificationResource(monitor, useClass);

          if (fGenerateHTML.getSelection())
          {
            TapestryTemplateFactory factory = new TapestryTemplateFactory();
            Template template = fTapestryTemplateSelector.getSelectedTemplate();

            fTemplateFile = factory.createTapestryTemplate(
                getTemplateFile(),
                template,
                new SubProgressMonitor(monitor, 1));
          }
          monitor.done();
        } catch (CoreException e)
        {
          throw new InvocationTargetException(e);
        }
      }
    };
  }

  // page wizard overrides
  protected void createSpecificationResource(
      IProgressMonitor monitor,
      final IType specClass) throws CoreException, InterruptedException
  {
    ComponentFactory factory = new ComponentFactory();
    Template template = fComponentTemplateSelector.getSelectedTemplate();

    fComponentFile = factory.createComponent((IFile) getResource(), template, specClass
        .getFullyQualifiedName(), monitor);
  }

  public IRunnableWithProgress getAutoAddRunnable() throws IOException, CoreException
  {

    final INamespace namespace = fNamespaceDialogField.getSelectedNamespace();

    if (namespace == null)
      return null;

    final boolean addingNewComponent = getWizard().getClass() == NewTapComponentWizard.class;
    final String name = fComponentNameDialogField.getTextValue();
    final Shell useShell = getShell();

    return new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        if (monitor == null)
          monitor = new NullProgressMonitor();

        String specificationPath;
        IDocument document;
        IEditorPart editor = null;
        PluginLibrarySpecification library;
        IFile file = null;

        library = (PluginLibrarySpecification) namespace.getSpecification();
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) namespace
            .getSpecificationLocation();

        file = (IFile) location.getStorage();
        editor = UIUtils.getEditorFor(location);

        if (namespace.isApplicationNamespace()
            && !((CoreNamespace) namespace).isOnClassPath())
        {
          //      if (!mustModifyApplicationNamespace())
          return;
        } else
        {
          IPackageFragment fragment = fLibraryPackageField.getPackageFragment();

          specificationPath = ("/" + fragment.getElementName().replace('.', '/')) + "/"
              + name + (addingNewComponent ? ".jwc" : ".page");

        }

        if (editor != null && editor.isDirty())
        {

          RequiredSaveEditorAction saver = new RequiredSaveEditorAction(editor);
          if (!saver.save())
            throw new InterruptedException("ack!");

          try
          {
            file.getProject().build(TapestryBuilder.INCREMENTAL_BUILD, monitor);
          } catch (CoreException e1)
          {
            UIPlugin.log(e1);
            throw new InvocationTargetException(e1);
          }

          Map specMap = TapestryArtifactManager.getTapestryArtifactManager().getSpecMap(
              file.getProject(),
              false);

          library = specMap == null ? null : (PluginLibrarySpecification) specMap
              .get(file);

        }

        if (editor != null && editor instanceof AbstractTextEditor)
        {
          AbstractTextEditor textEditor = (AbstractTextEditor) editor;
          document = textEditor.getDocumentProvider().getDocument(
              textEditor.getEditorInput());

        } else
        {
          // we turf the editor if its not null 'cuz we can't get a document
          // from it.
          try
          {
            editor = null;
            document = new Document();
            document.set(Files.readFileToString(file.getContents(), null));
          } catch (IOException e1)
          {
            throw new InvocationTargetException(e1);
          } catch (CoreException e1)
          {
            throw new InvocationTargetException(e1);
          }
        }

        LibraryEdits helper = addingNewComponent
            ? new LibraryEdits(library, document) : new ApplicationEdits(
                (PluginApplicationSpecification) library,
                document);
        try
        {
          if (addingNewComponent)
          {
            helper.addComponentDeclaration(name, specificationPath);
          } else
          {
            helper.addPageDeclaration(name, specificationPath);
          }

          helper.apply();
        } catch (MalformedTreeException e)
        {
          UIPlugin.log(e);
          throw new InvocationTargetException(e);
        } catch (BadLocationException e)
        {
          UIPlugin.log(e);
          throw new InvocationTargetException(e);
        } catch (TapestryException e)
        {
          UIPlugin.log(e);
          throw new InvocationTargetException(e);
        }

        if (editor != null)
        {
          editor.doSave(monitor);
        } else
        {
          ByteArrayInputStream b = new ByteArrayInputStream(document.get().getBytes());
          try
          {
            file.setContents(b, true, true, monitor);
          } catch (CoreException e1)
          {
            UIPlugin.log(e1);
            throw new InvocationTargetException(e1);
          }
        }
      }
    };
  }

  public boolean performFinish()
  {
    return true;
  }

  /** may not exist yet * */
  public IFile getTemplateFile()
  {

    if (!fGenerateHTML.getSelection())
      return null;

    if (fTemplateFile == null)
      fTemplateFile = findResource(
          fComponentNameDialogField.getTextValue() + ".html",
          fNamespaceDialogField.getSelectedNamespace());

    return fTemplateFile;
  }

  void clearTemplateFile()
  {
    fTemplateFile = null;
  }

  /** may not exist yet * */
  public IResource getResource()
  {
    if (fComponentFile == null)
    {
      String fileName = fComponentNameDialogField.getTextValue()
          + (getClass() == NewTapComponentWizardPage.class ? ".jwc" : ".page");

      fComponentFile = findResource(fileName, fNamespaceDialogField
          .getSelectedNamespace());
    }
    return fComponentFile;
  }

  void clearResource()
  {
    fComponentFile = null;
  }

  private IFile findResource(String fileName, INamespace namespace)
  {
    IFile result = null;
    if (namespace.isApplicationNamespace()
        && !((CoreNamespace) namespace).isOnClassPath())
    {
      result = ((IFolder) fApplicationLocationField.getLocation()).getFile(fileName);
    } else
    {
      try
      {
        IPackageFragment fragment = fLibraryPackageField.getPackageFragment();
        IFolder folder = (IFolder) fragment.getUnderlyingResource();
        result = folder.getFile(fileName);
      } catch (JavaModelException e)
      {
        UIPlugin.log(e);
      }
    }
    return result;
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
    setCompositeEnabled(fTemplatesGroup, flag);
    setCompositeEnabled(fPageBook, flag);
    if (fGenerateHTML.isEnabled())
      fTapestryTemplateSelector.setEnabled(fGenerateHTML.getSelection());
  }

  public void updateStatus()
  {
    super.updateStatus();
    checkEnabled(fComponentNameDialogField.getStatus());
  }

  class FieldEventsAdapter implements IDialogFieldChangedListener, SelectionListener
  {

    public void dialogFieldChanged(DialogField field)
    {
      updateStatus();
      if (field == fTapestryProjectDialogField)
        findPreferenceStore();

      if (field == fNamespaceDialogField)
        namespaceChanged();
    }
    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field)
    {
    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus,
     *              DialogField)
     */
    public void dialogFieldStatusChanged(IStatus status, DialogField field)
    {
      updateStatus();
    }

    public void widgetDefaultSelected(SelectionEvent e)
    {
      //do nothing
    }
    public void widgetSelected(SelectionEvent e)
    {
      if (e.getSource() == fGenerateHTML)
        fTapestryTemplateSelector.setEnabled(fGenerateHTML.isEnabled()
            && fGenerateHTML.getSelection());
    }
  }

  /**
   * @see IWizardPage#canFlipToNextPage()
   */
  public boolean canFlipToNextPage()
  {
    return getCurrentStatus().getSeverity() != IStatus.ERROR;

  }

  private void namespaceChanged()
  {
    INamespace namespace = fNamespaceDialogField.getSelectedNamespace();
    if (namespace == null)
    {
      fPageBook.showPage(fBlankPage);
    } else if (namespace.isApplicationNamespace()
        && !((CoreNamespace) namespace).isOnClassPath())
    {
      fPageBook.showPage(fApplicationLocationGroup);
      fApplicationLocationField.setTextValue(fApplicationLocationField.getTextValue());
    } else
    {
      fPageBook.showPage(fLibraryLocationGroup);
      fLibraryContainerField.setTextValue(fLibraryContainerField.getTextValue());
      fLibraryPackageField.setTextValue(fLibraryPackageField.getTextValue());
    }

    ((Composite) getControl()).layout(true);

    updateStatus();

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
   * 
   * @return TapestryProjectDialogField
   */
  public TapestryProjectDialogField getProjectField()
  {
    return fTapestryProjectDialogField;
  }

  public ContainerDialogField getContainerField()
  {
    return fLibraryContainerField;
  }

  public PackageDialogField getPackageField()
  {
    return fLibraryPackageField;
  }

  /**
   * Method getChosenContainer.
   * 
   * @return String
   */
  public TapestryProject getChosenProject()
  {
    return fTapestryProjectDialogField.getTapestryProject();
  }
}