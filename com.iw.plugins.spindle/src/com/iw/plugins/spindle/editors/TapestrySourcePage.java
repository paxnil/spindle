/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.ui.forms.internal.IFormPage;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.BaseTapestryModel;

/**
 * This class exists only to override the method <code>becomesInvisible</code> in 
 * <p>
 * <code>org.eclipse.pde.internal.editor.PDESourcePage</code>.
 * <p>
 * The superclass handles incorrect source badly in that it will warn the
 * user once that the source page cannot be parsed. At that point the
 * user is prevented from bringing up another page.
 * <p>
 * But after the first warning, the superclass will allow other pages to
 * be selected. so if the source is bad, the superclass returns false to calls
 * to <code>becomesInvisible</code> only the first time its called.
 * <p>
 * We override to ensure the source is *always* good before allowing
 * <code>becomesInvisible</code> to return true. Its the return value that
 * determines if a page switch is allowed.
 */

public abstract class TapestrySourcePage extends PDESourcePage {

  public TapestrySourcePage(SpindleMultipageEditor editor) { 
    super(editor);
  }

  // we need to add this listener so that document changes
  // will inform the editor that a save is needed.
  // Its a mystery to me why the IBM folks aren't doing this
  // already.
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    IDocument document = getDocumentProvider().getDocument(getEditorInput());
    document.addDocumentListener(new IDocumentListener() {
      public void documentAboutToBeChanged(DocumentEvent e) {
      }
      public void documentChanged(DocumentEvent e) {
        firePropertyChange(PROP_DIRTY);
      }
    });
  }

  public boolean becomesInvisible(IFormPage newPage) {
    if (super.becomesInvisible(newPage)) {
      boolean modelIsGood = ((BaseTapestryModel) getEditor().getModel()).isLoaded();
      if (!modelIsGood) {
        warnErrorsInSource();
      }
      return modelIsGood;

    }
    return false;
  }

  private void warnErrorsInSource() {
    Display.getCurrent().beep();
    MessageDialog.openError(
      TapestryPlugin.getDefault().getActiveWorkbenchShell(),
      "Source Error:",
      "The Source page has errors. Other pages cannot be used until these errors are corrected.");
  }
}