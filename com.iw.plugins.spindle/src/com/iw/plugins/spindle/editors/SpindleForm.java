package com.iw.plugins.spindle.editors;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public class SpindleForm extends EditorForm {

  boolean hasBeenInitialized = false;
  /**
   * Constructor for TapestryForm
   */
  public SpindleForm(SpindleFormPage page) {
    super(page);
  }

  /**
   * Once the form is committed, set the model to not dirty.
   * unless the model is out of sync. 
   * i.e. a change was made directly to the model from outside the
   * current editor.
   * 
   * @see Form#commitChanges(boolean)
   */
  public void commitChanges(boolean onSave) {
    super.commitChanges(onSave);   
  }

  /**
   * will call super.initialize() only if the model was
   * parsed without error.
   * @see Form#initialize(Object)
   */
  public void initialize(Object model) {
  	BaseTapestryModel tmodel = (BaseTapestryModel)model;
  	if (tmodel.isLoaded()) {
    	super.initialize(model);
    	hasBeenInitialized = true;
  	}
  }
  public boolean hasBeenInitialized() {
  	return hasBeenInitialized;
  }
  
  /**
   * If an update call comes, check to see if the form has been initialized.
   * It might not have been if there were parse errors when the editor opened.
   * If this instance was never initialized, try to do so now.
   * If this instance was initialized, call super.update()
   * @see Form#update()
   */
  public void update() {  	
  	if (hasBeenInitialized) {
    	super.update();
    	return;
  	} 
	BaseTapestryModel model = (BaseTapestryModel)getPage().getModel();
	if (model.isLoaded()) {
		initialize(model);
		hasBeenInitialized = true;
	}
  }

}

