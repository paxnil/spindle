package com.iw.plugins.spindle.editors;

import org.eclipse.pde.internal.ui.editor.PDEFormSection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public abstract class SpindleFormSection extends PDEFormSection {

  /**
   * Constructor for TapestryFormSection
   */
  public SpindleFormSection(SpindleFormPage page) {
    super(page);
    setCollapsable(true);
  }
  /**
   * @see FormSection#createClient(Composite, FormWidgetFactory)
   */ 
  public abstract Composite createClient(Composite arg0, FormWidgetFactory arg1);
  
  public boolean canUpdate() {
  	return ((BaseTapestryModel)getFormPage().getModel()).isLoaded();
  }

}

