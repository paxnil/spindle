package com.iw.plugins.spindle.editors;

import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;
import org.eclipse.update.ui.forms.internal.IFormPage;

import com.iw.plugins.spindle.model.BaseTapestryModel;

public class SpindleFormPage extends PDEFormPage {

  /**
   * Constructor for TapestryFormPage
   */
  public SpindleFormPage(SpindleMultipageEditor arg0, String arg1) {
    super(arg0, arg1);
  }
 
 /** must override
   * @see PDEFormPage#createForm()
   */
  protected AbstractSectionForm createForm() {
  	return null;
  }
  
 /** 
   * @see PDEFormPage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return null;
  }  

  public void update() {
  	if (((BaseTapestryModel)getEditor().getModel()).isLoaded()) {
  		super.update();
  	}
  } 
  
  /**
   * @see PDEFormPage#becomesInvisible(IFormPage)
   */
  public boolean becomesInvisible(IFormPage arg0) {
  	super.becomesInvisible(arg0);
  	((SpindleMultipageEditor)getEditor()).resynchDocument(false);
  	BaseTapestryModel model = (BaseTapestryModel)getModel();
   	model.setDirty(false);
  	return true;
  }

}

