package com.iw.plugins.spindle.refactor.components;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class RefactorWizardDialog extends WizardDialog {

  /**
   * Constructor for RefactorWizardDialog.
   * @param parentShell
   * @param newWizard
   */
  public RefactorWizardDialog(Shell parentShell, IWizard newWizard) {
    super(parentShell, newWizard);
  }

}
