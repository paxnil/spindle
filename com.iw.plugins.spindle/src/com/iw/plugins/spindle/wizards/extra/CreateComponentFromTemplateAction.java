package com.iw.plugins.spindle.wizards.extra;

import com.iw.plugins.spindle.wizards.NewTapComponentWizard;
import com.iw.plugins.spindle.wizards.NewTapestryElementWizard;

/**
 * Copyright 2002 Intelligent Works Inc.
 * All rights reserved
 * 
 * @author gwl
 * @version $Id$
 */
public class CreateComponentFromTemplateAction extends AbstractCreateFromTemplateAction {

  /**
   * Constructor for CreateComponentFromTemplateAction.
   */
  public CreateComponentFromTemplateAction() {
    super();
  }

  /**
   * @see com.iw.plugins.spindle.wizards.extra.AbstractCreateFromTemplateAction#getWizard()
   */
  protected NewTapestryElementWizard getWizard() {
    return new NewTapComponentWizard();
  }

}
