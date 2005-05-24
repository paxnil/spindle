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
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.tapestry.INamespace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
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
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.ProjectPreferenceStore;
import com.iw.plugins.spindle.core.TapestryException;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.core.util.Files;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;
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

  public static final String SHOW_ADVANCED_OPTIONS = UIPlugin.PLUGIN_ID + "showAdvanced";

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
  String DEFAULT_LOCATION;

  protected IWorkspaceRoot fRoot;

  protected TapestryProjectDialogField fTapestryProjectDialogField;
  protected NamespaceDialogField fNamespaceDialogField;
  protected ComponentNameField fComponentNameDialogField;
  protected Button fGenerateHTML;
  protected Button fToggleAdvancedOptions;
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
  protected boolean fBroken = false;
  protected boolean fShowingAdvanced = UIPlugin
      .getDefault()
      .getPreferenceStore()
      .getBoolean(SHOW_ADVANCED_OPTIONS);

  protected DialogDefaultLocation fDefaultLocation;

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
    DEFAULT_LOCATION = ".defaultLocation";

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
    fLibraryContainerField.setAcceptSourceContainers(true);
    connect(fLibraryContainerField);
    fLibraryContainerField.addListener(fListener);
    fLibraryPackageField = new PackageDialogField(LIBRARY_PACKAGE);
    fLibraryPackageField.setAcceptSourcePackagesOnly(true);
    connect(fLibraryPackageField);
    fLibraryPackageField.addListener(fListener);
    fApplicationLocationField = new ComponentLocationChooserField(
        APPLICATION_CONTAINER,
        getClass() != NewTapComponentWizardPage.class);
    connect(fApplicationLocationField);
    fApplicationLocationField.addListener(fListener);

    fDefaultLocation = new DialogDefaultLocation(
        PAGE_NAME + DEFAULT_LOCATION,
        this instanceof NewTapComponentWizardPage);

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
  public void init(IJavaElement jelem, IResource initResource, String prepopulateName)
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
      fGenerateHTML.setSelection(true);
      fGenerateHTML.setEnabled(false);
    } else
    {
      fComponentNameDialogField.setTextValue("");
    }
    fNamespaceDialogField.init(
        fTapestryProjectDialogField,
        fComponentNameDialogField,
        jelem,
        initResource,
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
        fGenerateHTML,
        initResource,
        context);

    namespaceChanged();

    fDefaultLocation
        .init(fComponentNameDialogField, fNamespaceDialogField, fGenerateHTML);
    connect(fDefaultLocation);
    fDefaultLocation.addListener(fListener);

    updateAdvancedOptionWidgets();

    updateStatus();
  }

  /**
   *  
   */
  private void findPreferenceStore()
  {
    fPreferenceStore = null;
    ITapestryProject tproject = fTapestryProjectDialogField.getTapestryProject();
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

  private void updateAdvancedOptionWidgets()
  {
    fTemplatesGroup.setVisible(fShowingAdvanced);
    fPageBook.setVisible(fShowingAdvanced);
    fDefaultLocation.setVisible(!fShowingAdvanced);
    refreshStatus();
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

    Control labelControl = fNextLabel.getControl(composite);
    fTemplatesGroup = createTemplateGroup(composite);
    fPageBook = createLocationPageBook(composite);

    Control nameFields = createComponentNameControl(composite);

    addControl(nameFields, composite, 5);

    Control separator = createSeparator(composite, nameFields);

    addControl(projectFieldControl, separator, 4);
    addControl(namespaceFieldControl, projectFieldControl, 10);

    separator = createSeparator(composite, namespaceFieldControl);

    fGenerateHTML = new Button(composite, SWT.CHECK);
    fGenerateHTML.setText(UIPlugin.getString(GENERATE_HTML + ".label"));

    addControl(fGenerateHTML, separator, 10);

    addControl(fTemplatesGroup, fGenerateHTML, 10);

    addControl(fPageBook, fTemplatesGroup, 10);

    addControl(labelControl, fPageBook, 20);

    setControl(composite);
    setFocus();

    fNamespaceDialogField.refreshStatus();
    IPreferenceStore pstore = UIPlugin.getDefault().getPreferenceStore();
    fGenerateHTML.addSelectionListener(fListener);
    fGenerateHTML.setSelection(pstore.getBoolean(P_GENERATE_HTML));
  }

  private Composite createComponentNameControl(Composite parent)
  {

    Font font = parent.getFont();
    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    int columnCount = 5;
    layout.numColumns = columnCount;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    container.setLayout(layout);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    container.setLayoutData(data);
    container.setFont(font);

    fComponentNameDialogField.fillIntoGrid(container, 3);

    int heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
    int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

    fToggleAdvancedOptions = new Button(container, SWT.PUSH);
    fToggleAdvancedOptions.setText(fShowingAdvanced
        ? "Hide Advanced Options" : "Show Advanced Options");
    data = new GridData(GridData.HORIZONTAL_ALIGN_END);
    data.horizontalSpan = 2;
    //    data.heightHint = heightHint;
    //    data.widthHint = Math.max(widthHint, buttonControl.computeSize(
    //        SWT.DEFAULT,
    //        SWT.DEFAULT,
    //        true).x);
    fToggleAdvancedOptions.setLayoutData(data);
    fToggleAdvancedOptions.addSelectionListener(fListener);

    return container;
  }

  private Group createTemplateGroup(Composite container)
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

    fTapestryTemplateSelector.createControl(group, columnCount);

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

    if (!fShowingAdvanced)
      return null;

    final INamespace namespace = fNamespaceDialogField.getSelectedNamespace();

    if (namespace == null)
      return null;

    final boolean addingNewComponent = getWizard().getClass() == NewTapComponentWizard.class;
    final String name = fComponentNameDialogField.getTextValue();
    final Shell useShell = getShell();
    final IFolder applicationLocation = fApplicationLocationField.getSpecLocation();
    ITapestryProject tproject = fTapestryProjectDialogField.getTapestryProject();
    final IFolder webInf = tproject.getWebContextFolder().getFolder("WEB-INF");

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

          IFolder applicationSpecFolder = (IFolder) file.getParent();

          // first eliminate folders where no changes to .application are
          // needed.

          //not needed if web-inf.
          IFolder chosenFolder = fApplicationLocationField.getSpecLocation();
          if (chosenFolder.equals(webInf))
            return;

          // not needed if same folder as .application
          if (applicationSpecFolder.equals(chosenFolder))
            return;

          //not needed if same folder as WEB-INF/servletname TODO confirm
          String appName = ((PluginApplicationSpecification) library).getName();
          if (webInf.getFolder(appName).equals(chosenFolder))
            return;

          IPath webInfPath = webInf.getFullPath();
          IPath applicationFileFolderPath = applicationSpecFolder.getFullPath();
          IPath usePath = chosenFolder.getFullPath();

          //now that we have eliminated all the ones that need no mods...
          //two options, a relative path if the chosen location is a subFolder
          // of the .applciation file
          if (applicationFileFolderPath.isPrefixOf(usePath))
          {
            usePath = usePath.removeFirstSegments(
                applicationFileFolderPath.segmentCount()).makeRelative();
          } else
          {
            // or otherwise an absolute path including WEB-INF
            usePath = usePath
                .removeFirstSegments(webInfPath.segmentCount() - 1)
                .makeAbsolute();
          }

          specificationPath = usePath.toString() + "/" + name
              + (addingNewComponent ? ".jwc" : ".page");

        } else
        {
          IPackageFragment fragment = fLibraryPackageField.getPackageFragment();

          specificationPath = ("/" + fragment.getElementName().replace('.', '/')) + "/"
              + name + (addingNewComponent ? ".jwc" : ".page");

        }

        if (editor != null && editor.isDirty())
        {

          RequiredSaveEditorAction saver = new RequiredSaveEditorAction(editor);
          if (!saver.save("Save files", "It is strongly recommended you save all here."))
            throw new InterruptedException("ack!");

          try
          {
            file.getProject().build(TapestryBuilder.INCREMENTAL_BUILD, monitor);
          } catch (CoreException e1)
          {
            UIPlugin.log_it(e1);
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
          UIPlugin.log_it(e);
          throw new InvocationTargetException(e);
        } catch (BadLocationException e)
        {
          UIPlugin.log_it(e);
          throw new InvocationTargetException(e);
        } catch (TapestryException e)
        {
          UIPlugin.log_it(e);
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
            UIPlugin.log_it(e1);
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
          fNamespaceDialogField.getSelectedNamespace(),
          true);

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
          .getSelectedNamespace(), false);
    }
    return fComponentFile;
  }

  void clearResource()
  {
    fComponentFile = null;
  }

  private IFile findResource(String fileName, INamespace namespace, boolean forTemplate)
  {
    IFile result = null;

    if (!fShowingAdvanced)
      return fDefaultLocation.getResultFolder().getFile(fileName);

    if (namespace.isApplicationNamespace()
        && !((CoreNamespace) namespace).isOnClassPath())
    {
      if (forTemplate)
      {
        result = result = ((IFolder) fApplicationLocationField.getTemplateLocation())
            .getFile(fileName);
      } else
      {
        result = ((IFolder) fApplicationLocationField.getSpecLocation())
            .getFile(fileName);
      }
    } else
    {
      try
      {
        IPackageFragment fragment = fLibraryPackageField.getPackageFragment();
        IFolder folder = (IFolder) fragment.getUnderlyingResource();
        result = folder.getFile(fileName);
      } catch (JavaModelException e)
      {
        UIPlugin.log_it(e);
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
    fGenerateHTML.setEnabled(flag);
    setCompositeEnabled(fTemplatesGroup, flag);
    setCompositeEnabled(fPageBook, flag);
    fApplicationLocationField.setEnabled(flag);
    if (fGenerateHTML.isEnabled())
      fTapestryTemplateSelector.setEnabled(fGenerateHTML.getSelection());
  }

  public void updateStatus()
  {
    super.updateStatus();
    checkEnabled(fComponentNameDialogField.getStatus());
  }

  /**
   * @see IWizardPage#canFlipToNextPage()
   */
  public boolean canFlipToNextPage()
  {
    IStatus status = getCurrentStatus();
    return super.canFlipToNextPage() && status != null && status.getSeverity() != IStatus.ERROR;
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
      //      fApplicationLocationField.setTextValue(fApplicationLocationField.getTextValue());
    } else
    {
      fPageBook.showPage(fLibraryLocationGroup);
      //      fLibraryContainerField.setTextValue(fLibraryContainerField.getTextValue());
      //      fLibraryPackageField.setTextValue(fLibraryPackageField.getTextValue());
    }

    refreshStatus();

    ((Composite) getControl()).layout(true);

    updateStatus();
  }

  /**
   *  
   */
  private void simpleComponentNameChanged()
  {
    if (fComponentNameDialogField.getStatus().getSeverity() == IStatus.ERROR)
      return;
    if (fNamespaceDialogField.getStatus().getSeverity() == IStatus.ERROR)
      return;
    SpindleStatus status = (SpindleStatus) fComponentNameDialogField.getStatus();
    String name = fComponentNameDialogField.getTextValue();
    INamespace namespace = fNamespaceDialogField.getSelectedNamespace();
    IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) namespace
        .getSpecificationLocation();
    if (location.isClasspathResource())
    {
      //check if location exists!
      IFile file = (IFile) location.getStorage();
      IContainer container = file.getParent();
      //        file =
    } else
    {
      //check if exists in webinf.,
    }

  }
  public String getChosenComponentName()
  {
    return fComponentNameDialogField.getTextValue();
  }

  public DialogField getComponentNameField()
  {
    return fComponentNameDialogField;
  }

  public NamespaceDialogField getNamespaceField()
  {
    return fNamespaceDialogField;
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
  public ITapestryProject getChosenProject()
  {
    return fTapestryProjectDialogField.getTapestryProject();
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
      Object source = e.getSource();
      if (source == fGenerateHTML)
      {
        fTapestryTemplateSelector.setEnabled(fGenerateHTML.isEnabled()
            && fGenerateHTML.getSelection());
        refreshStatus();
      } else if (source == fToggleAdvancedOptions)
      {
        fShowingAdvanced = !fShowingAdvanced;
        fToggleAdvancedOptions.setText(fShowingAdvanced
            ? "Hide Advanced Options" : "Show Advanced Options");
        UIPlugin.getDefault().getPreferenceStore().setValue(
            SHOW_ADVANCED_OPTIONS,
            fShowingAdvanced);
        updateAdvancedOptionWidgets();
      }
    }
  }

  //This is a sneaky non visible field that tracks the default location
  // for artifacts.
  class DialogDefaultLocation extends DialogField
  {

    private String fieldName;
    private ComponentNameField nameField;
    private NamespaceDialogField namespaceField;
    private Button generateTemplateSwitch;
    private boolean visible;
    private IFolder resultFolder;
    private boolean addingComponent;

    public DialogDefaultLocation(String name, boolean addingComponent)
    {
      super(name);
      this.fieldName = name;
      this.addingComponent = addingComponent;
    }

    public void init(
        ComponentNameField nameField,
        NamespaceDialogField namespaceField,
        Button generateTemplateSwitch)
    {
      this.nameField = nameField;
      this.namespaceField = namespaceField;
      this.generateTemplateSwitch = generateTemplateSwitch;

      nameField.addListener(this);
      namespaceField.addListener(this);
      generateTemplateSwitch.addSelectionListener(new SelectionListener()
      {
        public void widgetSelected(SelectionEvent e)
        {
          refreshStatus();
        }
        public void widgetDefaultSelected(SelectionEvent e)
        {
          // do nothing
        }
      });
    }

    public void dialogFieldChanged(DialogField field)
    {
      if (field == fComponentNameDialogField || field == namespaceField)
        refreshStatus();
    }

    public void refreshStatus()
    {
      setStatus(somethingChanged());
    }
    /**
     * @return
     */
    private IStatus somethingChanged()
    {
      resultFolder = null;
      SpindleStatus result = new SpindleStatus();
      if (fComponentNameDialogField.getStatus().getSeverity() != IStatus.ERROR
          && namespaceField.getStatus().getSeverity() != IStatus.ERROR)
      {
        String name = fComponentNameDialogField.getTextValue();
        INamespace namespace = namespaceField.getSelectedNamespace();

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) namespace
            .getSpecificationLocation();

        IFolder folder = (IFolder) ((IFile) location.getStorage()).getParent();

        IFile specFile = folder.getFile(name + (addingComponent ? ".jwc" : ".page"));
        if (specFile.exists())
        {
          result.setError(UIPlugin.getString(
              fieldName + ".error.ComponentAlreadyExists",
              specFile.getFullPath().toString()));
          return result;
        }

        if (fGenerateHTML.getSelection())
        {
          IFile templateFile = folder.getFile(name + ".html");
          if (templateFile.exists())
          {
            result.setError(UIPlugin.getString(
                fieldName + ".error.TemplateAlreadyExists",
                templateFile.getFullPath().toString()));
            return result;
          }
        }

        resultFolder = folder;
      }
      return result;
    }

    public IFolder getResultFolder()
    {
      return resultFolder;
    }

    public boolean isEnabled()
    {
      return true;
    }
    public boolean isVisible()
    {
      return visible;
    }
    public void setVisible(boolean flag)
    {
      visible = flag;
    }

    public void setEnabled(boolean flag)
    {
      //eat it
    }
    public void fillIntoGrid(Composite parent, int numcols)
    {
      throw new IllegalStateException("not implemented");
    }
    public Control getControl(Composite parent)
    {
      throw new IllegalStateException("no implemented");
    }
  }
}