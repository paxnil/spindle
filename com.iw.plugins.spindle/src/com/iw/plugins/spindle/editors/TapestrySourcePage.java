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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
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

  public void becomesVisible(IFormPage previousPage) {
    super.becomesVisible(previousPage);

    try {

      ResourceMarkerAnnotationModel annotationModel = (ResourceMarkerAnnotationModel) getSourceViewer().getAnnotationModel();
      if (annotationModel != null) {
        annotationModel.updateMarkers(getDocumentProvider().getDocument(getEditorInput()));
      }
    } catch (CoreException e) {
    }

  }

  //  public void checkProblemMarkers() {
  //
  //    IEditorInput input = getEditor().getEditorInput();
  //    IResource file = (IResource) input.getAdapter(IResource.class);
  //
  //    if (file == null || file.isReadOnly()) {
  //
  //      return;
  //
  //    }
  //
  //    IMarker[] badStringMarkers = findBadStringMarkers(file);
  //
  //    if (badStringMarkers.length > 0) {
  //
  //      transmorgifyMarkers(file, badStringMarkers);
  //
  //    }
  //
  //    return;
  //  }
  //
  //  private IMarker[] findBadStringMarkers(IResource resource) {
  //    try {
  //      return (resource.findMarkers("com.iw.plugins.spindle.badwordproblem", false, IResource.DEPTH_ONE));
  //    } catch (CoreException corex) {
  //    }
  //
  //    return new IMarker[0];
  //  }

  //  private void transmorgifyMarkers(IResource resource, IMarker[] markers) {
  //
  //    IDocument document = getDocumentProvider().getDocument(getEditorInput());
  //
  //    for (int i = 0; i < markers.length; i++) {
  //
  //      int searchOffset = 0;
  //      int currentOffset;
  //      String invalidString;
  //      try {
  //
  //        invalidString = (String) markers[i].getAttribute("invalidString");
  //
  //        currentOffset = document.search(searchOffset, invalidString, true, true, true);
  //
  //      } catch (Exception e) {
  //
  //        continue;
  //
  //      }
  //
  //      while (currentOffset > 0) {
  //
  //        searchOffset = currentOffset + invalidString.length();
  //
  //        try {
  //
  //          ((ResourceMarkerAnnotationModel) getSourceViewer().getAnnotationModel()).updateMarker(
  //            markers[i],
  //            document,
  //            new Position(currentOffset));
  //
  //        } catch (CoreException e) {
  //        }
  //
  //        try {
  //
  //          currentOffset = document.search(searchOffset, invalidString, true, true, true);
  //
  //        } catch (BadLocationException e) {
  //
  //          break;
  //        }
  //
  //      }
  //
  //    }
  //  }

  /**
   * @see org.eclipse.pde.internal.ui.editor.PDESourcePage#createContentOutlinePage()
   */
  public IContentOutlinePage createContentOutlinePage() {
    return null;
  }

}