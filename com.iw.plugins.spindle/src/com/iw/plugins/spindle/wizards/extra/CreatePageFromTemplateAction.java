package com.iw.plugins.spindle.wizards.extra;

import com.iw.plugins.spindle.wizards.NewTapPageWizard;
import com.iw.plugins.spindle.wizards.NewTapestryElementWizard;

/**
 * Copyright 2002 Intelligent Works Inc.
 * All rights reserved
 * 
 * @author gwl
 * @version $Id$
 */
public class CreatePageFromTemplateAction extends AbstractCreateFromTemplateAction {

  /**
   * Constructor for CreatePageFromTemplateAction.
   */
  public CreatePageFromTemplateAction() {
    super();
  }

  /**
   * @see com.iw.plugins.spindle.wizards.extra.AbstractCreateFromTemplateAction#getWizard()
   */
  protected NewTapestryElementWizard getWizard() {
    return new NewTapPageWizard();
  }

}
