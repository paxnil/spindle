/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Intelligent Works
 * Incorporated. Portions created by the Initial Developer are Copyright (C)
 * 2003 the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@intelligentworks.com
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.iw.plugins.spindle.editors.spec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.editors.ProblemAnnotation;
import com.iw.plugins.spindle.editors.ProblemAnnotationModel;

/**
 * Model for Spec annotations - of course only files and not jar entries will
 * have an annotation model.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: SpecAnnotationModel.java,v 1.2 2003/09/21 19:49:32 glongman
 *          Exp $
 */
public class SpecAnnotationModel extends ProblemAnnotationModel
{
    private static final int STAGE_INACTIVE = -1;
    /** accept parser problems* */
    private static final int STAGE_PARSER = 0;
    /** accept scanner problems* */
    private static final int STAGE_SCANNER = 1;

    private int fStage = STAGE_INACTIVE;
    private List fCollectedParserProblems;
    private List fGeneratedParserAnnotations;
    private List fCurrentlyOverlaidParser;
    private List fPreviouslyOverlaidParser;

    public SpecAnnotationModel(IFileEditorInput input)
    {
        super(input);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.ProblemAnnotationModel#startCollectingProblems()
     */
    protected void startCollectingProblems()
    {
        super.startCollectingProblems();
        fCollectedParserProblems = new ArrayList();
        fGeneratedParserAnnotations = new ArrayList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.ProblemAnnotationModel#stopCollectingProblems()
     */
    protected void stopCollectingProblems()
    {
        if (fGeneratedParserAnnotations != null)
        {
            removeAnnotations(fGeneratedParserAnnotations, true, true);
            fGeneratedParserAnnotations.clear();
        }
        if (fGeneratedAnnotations != null)
        {
            removeAnnotations(fGeneratedAnnotations, true, true);
            fGeneratedAnnotations.clear();
        }
        fCollectedProblems = null;
        fCollectedParserProblems = null;
        fGeneratedAnnotations = null;
        fGeneratedParserAnnotations = null;
    }

    // Must be a resource
    public void beginCollecting()
    {
        switch (fStage)
        {
            case STAGE_INACTIVE :
                fStage = STAGE_PARSER;
                Object spec = getSpecification();
                if (spec != null)
                    setIsActive(true);
                else
                    setIsActive(false);
                break;
            case STAGE_PARSER :
                fStage = STAGE_SCANNER;
                break;
            default :
                throw new Error("invalid stage");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.source.IProblemCollector#addProblem(com.iw.plugins.spindle.core.source.IProblem)
     */
    public void addProblem(IProblem problem)
    {
        if (!isActive())
            return;

        switch (fStage)
        {
            case STAGE_PARSER :
                fCollectedParserProblems.add(problem);
                break;

            case STAGE_SCANNER :
                fCollectedProblems.add(problem);
                break;

            default :
                throw new Error("invalid stage");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.parser.IProblemCollector#endCollecting()
     */
    public void endCollecting()
    {
        switch (fStage)
        {
            case STAGE_PARSER :
                if (!fCollectedParserProblems.isEmpty())
                {
                    updateAnnotationsParser();
                    fStage = STAGE_INACTIVE;
                }
                break;

            case STAGE_SCANNER :
                updateAnnotationsNormal();
                fStage = STAGE_INACTIVE;
                break;
        }
    }

    private void updateAnnotationsParser()
    {
        // goal here is to leave non-parser annotation intact
        // and to handle parser problems as per normal.
        if (!isActive())
            return;

        if (fProgressMonitor != null && fProgressMonitor.isCanceled())
            return;

        boolean isCanceled = false;
        boolean temporaryParserProblemsChanged = false;
        fPreviouslyOverlaidParser = fCurrentlyOverlaidParser;
        fCurrentlyOverlaidParser = new ArrayList();

        synchronized (fAnnotations)
        {
            if (fGeneratedParserAnnotations.size() > 0)
            {
                temporaryParserProblemsChanged = true;
                removeAnnotations(fGeneratedParserAnnotations, false, true);
                fGeneratedParserAnnotations.clear();
            }

            if (fCollectedParserProblems != null && fCollectedParserProblems.size() > 0)
            {

                Iterator e = fCollectedParserProblems.iterator();
                while (e.hasNext())
                {

                    IProblem problem = (IProblem) e.next();

                    if (fProgressMonitor != null && fProgressMonitor.isCanceled())
                    {
                        isCanceled = true;
                        break;
                    }

                    Position position = createPositionFromProblem(problem);
                    if (position != null)
                    {

                        ProblemAnnotation annotation = new ProblemAnnotation(problem);
                        overlayMarkers(position, annotation);
                        fGeneratedParserAnnotations.add(annotation);
                        addAnnotation(annotation, position, false);

                        temporaryParserProblemsChanged = true;
                    }
                }

                fCollectedParserProblems.clear();
            }

            removeMarkerOverlays(isCanceled);
            fPreviouslyOverlaid = null;

        }

        if (temporaryParserProblemsChanged)
            fireModelChanged(new AnnotationModelEvent(this));

    }

    private void updateAnnotationsNormal()
    {
        if (!isActive())
            return;

        if (fProgressMonitor != null && fProgressMonitor.isCanceled())
            return;

        boolean isCanceled = false;
        boolean temporaryProblemsChanged = false;
        fPreviouslyOverlaid = fCurrentlyOverlaid;
        fCurrentlyOverlaid = new ArrayList();

        synchronized (fAnnotations)
        {
            fGeneratedAnnotations.addAll(fGeneratedParserAnnotations);

            if (fGeneratedAnnotations.size() > 0)
            {
                temporaryProblemsChanged = true;
                removeAnnotations(fGeneratedAnnotations, false, true);
                fGeneratedAnnotations.clear();
            }

            if (fCollectedProblems != null && fCollectedProblems.size() > 0)
            {

                Iterator e = fCollectedProblems.iterator();
                while (e.hasNext())
                {

                    IProblem problem = (IProblem) e.next();

                    if (fProgressMonitor != null && fProgressMonitor.isCanceled())
                    {
                        isCanceled = true;
                        break;
                    }

                    Position position = createPositionFromProblem(problem);
                    if (position != null)
                    {

                        ProblemAnnotation annotation = new ProblemAnnotation(problem);
                        overlayMarkers(position, annotation);
                        fGeneratedAnnotations.add(annotation);
                        addAnnotation(annotation, position, false);

                        temporaryProblemsChanged = true;
                    }
                }
                fCollectedParserProblems.clear();
                fCollectedProblems.clear();
            }

            removeMarkerOverlays(isCanceled);
            fPreviouslyOverlaid.clear();
            fPreviouslyOverlaid = null;
        }

        if (temporaryProblemsChanged)
            fireModelChanged(new AnnotationModelEvent(this));
    }

    private Object getSpecification()
    {
        IFile file = fInput.getFile();

        IProject project = file.getProject();
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map specs = manager.getSpecMap(project);
        if (specs != null)
        {
            return specs.get(file);
        }
        return null;
    }

}
