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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import com.iw.plugins.spindle.core.parser.IProblem;

/**
 * Annotation representating an <code>IProblem</code>.
 */
public class ProblemAnnotation extends Annotation implements IAnnotation
{

    private List fOverlaids;
    private IProblem fProblem;
    private Image fImage;
    private boolean fQuickFixImagesInitialized = false;
    private AnnotationType fType;

    public ProblemAnnotation(IProblem problem)
    {

        fProblem = problem;
        setLayer(MarkerAnnotation.PROBLEM_LAYER + 1);

        if (fProblem.getSeverity() == IMarker.SEVERITY_WARNING)
            fType = AnnotationType.WARNING;
        else
            fType = AnnotationType.ERROR;
    }

    private void initializeImages()
    {
        //        // http://bugs.eclipse.org/bugs/show_bug.cgi?id=18936
        //        if (!fQuickFixImagesInitialized)
        //        {
        //            if (indicateQuixFixableProblems() && JavaCorrectionProcessor.hasCorrections(fProblem.getID()))
        //            {
        //                if (!fgQuickFixImagesInitialized)
        //                {
        //                    fgQuickFixImage = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
        //                    fgQuickFixErrorImage = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_ERROR);
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

//    private boolean indicateQuixFixableProblems()
//    {
//        return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_CORRECTION_INDICATION);
//    }
//
    /*
     * @see Annotation#paint
     */
    public void paint(GC gc, Canvas canvas, Rectangle r)
    {
        initializeImages();
        if (fImage != null)
            drawImage(fImage, gc, canvas, r, SWT.CENTER, SWT.TOP);
    }

    public Image getImage(Display display)
    {
        initializeImages();
        return fImage;
    }

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
        return fType == AnnotationType.WARNING || fType == AnnotationType.ERROR;
    }

    public boolean isRelevant()
    {
        return true;
    }

    public boolean hasOverlay()
    {
        return false;
    }

    public void addOverlaid(IAnnotation annotation)
    {
        if (fOverlaids == null)
            fOverlaids = new ArrayList(1);
        fOverlaids.add(annotation);
    }

    public void removeOverlaid(IAnnotation annotation)
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

    public AnnotationType getAnnotationType()
    {
        return fType;
    }
};
