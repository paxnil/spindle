package com.iw.plugins.spindle.project.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.util.ITapestryLookupRequestor;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;
import com.iw.plugins.spindle.wizards.project.convert.ConvertToTapestryProjectWizard;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class ConvertToTapestryProjectAction extends AbstractTapestryProjectAction {

  /**
   * Constructor for ConvertToTapestryProjectAction.
   */
  public ConvertToTapestryProjectAction() {
    super();
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {

    Shell shell = TapestryPlugin.getDefault().getActiveWorkbenchShell();
    IJavaProject jproject = (IJavaProject) selection.getFirstElement();
    IProject project = jproject.getProject();

    if (!checkHasTapestryJars(jproject)) {

      MessageDialog.openInformation(
        shell,
        "Conversion Problem",
        "Can't continue with conversion.\n\nAdd:\n\n javax.servlet.jar; and\n net.sf.tapestry.jar (2.2 or better)\n\n to the project build path.\n\nThen try converting again");

      return;
    }

    ConvertToTapestryProjectWizard wizard = new ConvertToTapestryProjectWizard();

    wizard.init(TapestryPlugin.getDefault().getWorkbench(), selection);

    WizardDialog dialog = new WizardDialog(shell, wizard);
    PixelConverter converter= new PixelConverter(TapestryPlugin.getDefault().getActiveWorkbenchShell());
		
	dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));

    dialog.create(); 
    dialog.getShell().setText("Tapestry Project Conversion");

    dialog.open();

  }

  private boolean hasExistingApplicationsOrLibraries(ITapestryProject tproject)
    throws CoreException {

    TapestryLookup lookup = tproject.getLookup();

    Requestor requestor = new Requestor();

    lookup.findAll(
      "*",
      true,
      TapestryLookup.ACCEPT_APPLICATIONS
        | TapestryLookup.ACCEPT_COMPONENTS
        | TapestryLookup.WRITEABLE
        | TapestryLookup.THIS_PROJECT_ONLY,
      requestor);

    return requestor.count >= 0;
  }

  class Requestor implements ITapestryLookupRequestor {

    public int count = 0;

    public boolean accept(IStorage storage, IPackageFragment frgament) {

      count++;

      return true;
    }

    public boolean isCancelled() {

      return false;

    }

  }

}
