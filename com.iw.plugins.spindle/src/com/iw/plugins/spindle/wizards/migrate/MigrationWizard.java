package com.iw.plugins.spindle.wizards.migrate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

import com.iw.plugins.spindle.spec.IPluginLibrarySpecification;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.ui.migrate.MigrateComponentModel;
import com.iw.plugins.spindle.ui.migrate.MigrationContext;
import com.iw.plugins.spindle.ui.migrate.MigrationWorkUnit;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MigrationWizard extends Wizard {

  MigrationContext context;
  MigrationWelcomePage welcomePage;
  MigrationScopePage scopePage;
  MigrationActionPage actionPage;
  UndefinedComponentsDescriptionPage undefinedWarningPage;
  DefinePagesComponentsPage definePage;

  private boolean canFinish = true;
  /**
   * Constructor for MigrationWizard.
   */
  public MigrationWizard(MigrationContext context) {
    super();
    this.context = context;
    setWindowTitle(context.getDescription());
  }

  protected boolean doFinish(IRunnableWithProgress runnable) {
    IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
    try {
      getContainer().run(false, true, op);
    } catch (InvocationTargetException e) {
      Shell shell = getShell();
      String title = NewWizardMessages.getString("NewElementWizard.op_error.title"); //$NON-NLS-1$
      String message = NewWizardMessages.getString("NewElementWizard.op_error.message"); //$NON-NLS-1$
      //			ExceptionHandler.handle(e, shell, title, message);
      return false;
    } catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {

    List undefinedComponents = collectUndefined();

    Map migratorMap = context.getMigrationMap();

    MigrationWorkUnit libraryWorker =
      (MigrationWorkUnit) migratorMap.get(context.getContextModel().getUnderlyingStorage());

    final ArrayList migrators = orderedMigrators(migratorMap, libraryWorker);

    IPluginLibrarySpecification librarySpec =
      (IPluginLibrarySpecification) context.getContextModel().getSpecification();

    List convertToNewPages = new ArrayList();

    if (getContainer().getCurrentPage() == definePage) {

      convertToNewPages = definePage.getNewPages();

    }

    undefinedComponents.removeAll(convertToNewPages);

    createMigratorsForUndefined(
      undefinedComponents,
      migratorMap,
      migrators,
      librarySpec,
      convertToNewPages);

    migrators.add(0, libraryWorker);

    return doFinish(new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor)
        throws InvocationTargetException, InterruptedException {

        for (Iterator iter = migrators.iterator(); iter.hasNext();) {
          MigrationWorkUnit worker = (MigrationWorkUnit) iter.next();

          try {

            worker.migrate();

          } catch (CoreException e) {

            throw new InvocationTargetException(e);
          }

        }

        for (Iterator iter = migrators.iterator(); iter.hasNext();) {
          MigrationWorkUnit worker = (MigrationWorkUnit) iter.next();

          worker.commitMigration(monitor);

        }

      }
    });
  }

  private void createMigratorsForUndefined(
    List undefinedComponents,
    Map migratorMap,
    ArrayList migrators,
    IPluginLibrarySpecification librarySpec,
    List convertToNewPages) {
    for (Iterator iter = undefinedComponents.iterator(); iter.hasNext();) {
      IStorage element = (IStorage) iter.next();

      if (!migratorMap.containsKey(element) && context.isInScope(element)) {

        boolean isPageSpec = "page".equals(element.getFullPath().getFileExtension());

        if (isPageSpec) {

          addPage(librarySpec, element, migrators);

        } else {

          addComponent(librarySpec, element, migrators);
        }

      }

    }

    for (Iterator iter = convertToNewPages.iterator(); iter.hasNext();) {
      IStorage element = (IStorage) iter.next();

      if (!migratorMap.containsKey(element) && context.isInScope(element)) {

        addPage(librarySpec, element, migrators);

      }

    }
  }

  private ArrayList orderedMigrators(Map migratorMap, MigrationWorkUnit libraryWorker) {

    ArrayList migrators = new ArrayList();

    for (Iterator iter = migratorMap.entrySet().iterator(); iter.hasNext();) {

      Map.Entry entry = (Map.Entry) iter.next();

      MigrationWorkUnit worker = (MigrationWorkUnit) entry;

      if (libraryWorker == worker) {

        migrators.add(worker);

      }

    }

    return migrators;
  }

  private void addPage(
    IPluginLibrarySpecification librarySpec,
    IStorage element,
    ArrayList migrators) {

    try {

      String path = getTapestryPath(element);
      String name = element.getFullPath().removeFileExtension().lastSegment();

      if (librarySpec.getPageSpecificationPath(name) != null) {

        int count = 1;

        name = name + count;

        if (librarySpec.getPageSpecificationPath(name) != null) {

          name = name + count++;
        }

      }

      librarySpec.setPageSpecificationPath(name, path);

      MigrateComponentModel migrator =
        new MigrateComponentModel(
          context,
          context.getModelManager().getReadOnlyModel(element),
          XMLUtil.DTD_1_3);

      migrators.add(0, migrator);

    } catch (CoreException e) {
      //skip it
    }
  }

  private void addComponent(
    IPluginLibrarySpecification librarySpec,
    IStorage element,
    ArrayList migrators) {

    try {

      String path = getTapestryPath(element);
      String name = element.getFullPath().removeFileExtension().lastSegment();

      if (librarySpec.getComponentSpecificationPath(name) != null) {

        int count = 1;

        name = name + count;

        if (librarySpec.getComponentSpecificationPath(name) != null) {

          name = name + count++;
        }

      }

      librarySpec.setComponentSpecificationPath(name, path);

      MigrateComponentModel migrator =
        new MigrateComponentModel(
          context,
          context.getModelManager().getReadOnlyModel(element),
          XMLUtil.DTD_1_3);

      migrators.add(0, migrator);

    } catch (CoreException e) {

      // skip it
    }
  }

  private String getTapestryPath(IStorage element) throws CoreException {

    IPackageFragment fragment = context.getLookup().findPackageFragment(element);

    String path = "/" + fragment.getElementName().replace('.', '/') + "/" + element.getName();

    return path;
  }

  private ArrayList collectUndefined() {
    List possibles = scopePage.getPossibleUndefinedJWCs();
    ArrayList actuals = new ArrayList();

    if (!possibles.isEmpty()) {

      context.reset();
      context.constructMigrators();

      for (Iterator iter = possibles.iterator(); iter.hasNext();) {
        IStorage element = (IStorage) iter.next();

        if (context.getMigratorFor(element) == null && context.isInScope(element)) {

          actuals.add(element);

        }
      }
    }
    return actuals;
  }

  private void contextChanged() {

    IStructuredSelection scope = (IStructuredSelection) scopePage.getSelection();
    context.setScope(scope.toList());

    IStructuredSelection constraints = (IStructuredSelection) actionPage.getSelection();
    context.setConstraints(constraints.toArray());

    context.constructMigrators();

    canFinish = definePage.setUndefined(collectUndefined());
    
    getContainer().updateButtons();

    context.reset();

  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages() {
    welcomePage = new MigrationWelcomePage("welcome", context);
    addPage(welcomePage);
    scopePage = new MigrationScopePage("scope", context);
    scopePage.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        contextChanged();
      }
    });
    addPage(scopePage);
    actionPage = new MigrationActionPage("actions", context);
    actionPage.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        contextChanged();
      }
    });

    addPage(actionPage);
    //    undefinedWarningPage = new UndefinedComponentsDescriptionPage("warning", context);
    //    addPage(undefinedWarningPage);
    definePage = new DefinePagesComponentsPage("define", context);
    addPage(definePage);

  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getNextPage(IWizardPage)
   */
  public IWizardPage getNextPage(IWizardPage page) {
    IWizardPage nextPage = super.getNextPage(page);

    if (nextPage == definePage) {

      canFinish = true;
      getContainer().updateButtons();

    }
    return nextPage;
  }



  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish() {
    boolean result = super.canFinish();
    return result & canFinish;
  }

}
