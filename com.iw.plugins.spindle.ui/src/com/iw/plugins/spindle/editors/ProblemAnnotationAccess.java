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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

/**
 * Used by callers to access info about IAnnotations.
 * 
 * @author glongman@gmail.com
 */
public class ProblemAnnotationAccess extends DefaultMarkerAnnotationAccess
{

  /*
   * @see org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
   */
  public Object getType(Annotation annotation)
  {
    if (annotation instanceof IProblemAnnotation)
    {
      IProblemAnnotation tapAnnotation = (IProblemAnnotation) annotation;
      if (tapAnnotation.isRelevant())
        return tapAnnotation.getType();
    }
    return super.getType(annotation);
  }

  /*
   * @see org.eclipse.jface.text.source.IAnnotationAccess#isMultiLine(org.eclipse.jface.text.source.Annotation)
   */
  public boolean isMultiLine(Annotation annotation)
  {
    return true;
  }

  /*
   * @see org.eclipse.jface.text.source.IAnnotationAccess#isTemporary(org.eclipse.jface.text.source.Annotation)
   */
  public boolean isTemporary(Annotation annotation)
  {
    if (annotation instanceof IProblemAnnotation)
    {
      IProblemAnnotation tapAnnotation = (IProblemAnnotation) annotation;
      if (tapAnnotation.isRelevant())
        return tapAnnotation.isTemporary();
    }
    return super.isTemporary(annotation);
  }
};



