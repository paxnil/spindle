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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

import com.iw.plugins.spindle.core.source.IProblem;

/**
 * Annotation representating an <code>IProblem</code>.
 */
public class ProblemAnnotation extends Annotation implements IProblemAnnotation
{

  /**
   * The layer in which task problem annotations are located.
   */
  private static final int TASK_LAYER;
  /**
   * The layer in which info problem annotations are located.
   */
  private static final int INFO_LAYER;
  /**
   * The layer in which warning problem annotations representing are located.
   */
  private static final int WARNING_LAYER;
  /**
   * The layer in which error problem annotations representing are located.
   */
  private static final int ERROR_LAYER;

  static
  {
    AnnotationPreferenceLookup lookup = EditorsUI.getAnnotationPreferenceLookup();
    TASK_LAYER = computeLayer(ProblemMarkerAnnotation.TASK_ANNOTATION_TYPE, lookup);
    INFO_LAYER = computeLayer(ProblemMarkerAnnotation.INFO_ANNOTATION_TYPE, lookup);
    WARNING_LAYER = computeLayer(ProblemMarkerAnnotation.WARNING_ANNOTATION_TYPE, lookup);
    ERROR_LAYER = computeLayer(ProblemMarkerAnnotation.ERROR_ANNOTATION_TYPE, lookup);
  }

  private static int computeLayer(String annotationType, AnnotationPreferenceLookup lookup)
  {
    Annotation annotation = new Annotation(annotationType, false, null);
    AnnotationPreference preference = lookup.getAnnotationPreference(annotation);
    if (preference != null)
      return preference.getPresentationLayer() + 1;
    else
      return IAnnotationAccessExtension.DEFAULT_LAYER + 1;
  }

  private List fOverlaids;
  private IProblem fProblem;
  private Image fImage;
  private boolean fQuickFixImagesInitialized = false;
  private String fType;

  public ProblemAnnotation(IProblem problem)
  {

    fProblem = problem;
    //        setLayer(MarkerAnnotation.PROBLEM_LAYER + 1);

    int severity = fProblem.getSeverity();
    switch (severity)
    {
      case IMarker.SEVERITY_INFO :
        fType = ProblemMarkerAnnotation.INFO_ANNOTATION_TYPE;
        break;
      case IMarker.SEVERITY_WARNING :
        fType = ProblemMarkerAnnotation.WARNING_ANNOTATION_TYPE;
        break;
      case IMarker.SEVERITY_ERROR :
        fType = ProblemMarkerAnnotation.ERROR_ANNOTATION_TYPE;
        break;

      default :
        fType = ProblemMarkerAnnotation.TASK_ANNOTATION_TYPE;
        break;
    }
  }

  private void initializeImages()
  {

    // TODO remove // http://bugs.eclipse.org/bugs/show_bug.cgi?id=18936
    //        if (!fQuickFixImagesInitialized)
    //        {
    //            if (indicateQuixFixableProblems() &&
    // JavaCorrectionProcessor.hasCorrections(fProblem.getID()))
    //            {
    //                if (!fgQuickFixImagesInitialized)
    //                {
    //                    fgQuickFixImage =
    // JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
    //                    fgQuickFixErrorImage =
    // JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_ERROR);
    //                    fgQuickFixImagesInitialized = true;
    //                }
    //                if (fType == AnnotationType.ERROR)
    //                    fImage = fgQuickFixErrorImage;
    //                else
    //                    fImage = fgQuickFixImage;
    //            }
    //            fQuickFixImagesInitialized = true;
    //        }
  }

  //    private boolean indicateQuixFixableProblems() TODO remove
  //    {
  //        return
  // PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_CORRECTION_INDICATION);
  //    }
  //
  /*
   * @see Annotation#paint
   */
  //  public void paint(GC gc, Canvas canvas, Rectangle r)
  //  {
  //    initializeImages();
  //    if (fImage != null)
  //      ImageUtilities.drawImage(fImage, gc, canvas, r, SWT.CENTER, SWT.TOP);
  //  }
  //
  //  public Image getImage(Display display)
  //  {
  //    initializeImages();
  //    return fImage;
  //  }
  public String getMessage()
  {
    return fProblem.getMessage();
  }

  public boolean isTemporary()
  {
    return true;
  }

  public String[] getArguments()
  {
    return null;
  }

  public int getId()
  {
    return -1;
  }

  public boolean isProblem()
  {
    String type = getType();
    return ProblemMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(type)
        || ProblemMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(type);
  }

  public boolean isRelevant()
  {
    return true;
  }

  public boolean hasOverlay()
  {
    return false;
  }

  public IProblemAnnotation getOverlay()
  {
    return null;
  }

  public void addOverlaid(IProblemAnnotation annotation)
  {
    if (fOverlaids == null)
      fOverlaids = new ArrayList(1);
    fOverlaids.add(annotation);
  }

  public void removeOverlaid(IProblemAnnotation annotation)
  {
    if (fOverlaids != null)
    {
      fOverlaids.remove(annotation);
      if (fOverlaids.size() == 0)
        fOverlaids = null;
    }
  }

  public Iterator getOverlaidIterator()
  {
    if (fOverlaids != null)
      return fOverlaids.iterator();
    return null;
  }

  //  /*
  //   * (non-Javadoc)
  //   *
  //   * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
  //   */
  //  public int getLayer()
  //  {
  //    // TODO Auto-generated method stub
  //    return 0;
  //  }
};
