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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;

public abstract class ProblemAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemCollector
{

    /**
     * Internal structure for mapping positions to some value. 
     * The reason for this specific structure is that positions can
     * change over time. Thus a lookup is based on value and not
     * on hash value.
     */
    protected static class ReverseMap
    {

        static class Entry {
                  Position fPosition;
                  Object fValue;
              };
            
              private List fList= new ArrayList(2);
              private int fAnchor= 0;
            
              public ReverseMap() {
              }
            
              public Object get(Position position) {
                
                  Entry entry;
                
                  // behind anchor
                  int length= fList.size();
                  for (int i= fAnchor; i < length; i++) {
                      entry= (Entry) fList.get(i);
                      if (entry.fPosition.equals(position)) {
                          fAnchor= i;
                          return entry.fValue;
                      }
                  }
                
                  // before anchor
                  for (int i= 0; i < fAnchor; i++) {
                      entry= (Entry) fList.get(i);
                      if (entry.fPosition.equals(position)) {
                          fAnchor= i;
                          return entry.fValue;
                      }
                  }
                
                  return null;
              }
            
              private int getIndex(Position position) {
                  Entry entry;
                  int length= fList.size();
                  for (int i= 0; i < length; i++) {
                      entry= (Entry) fList.get(i);
                      if (entry.fPosition.equals(position))
                          return i;
                  }
                  return -1;
              }
            
              public void put(Position position,  Object value) {
                  int index= getIndex(position);
                  if (index == -1) {
                      Entry entry= new Entry();
                      entry.fPosition= position;
                      entry.fValue= value;
                      fList.add(entry);
                  } else {
                      Entry entry= (Entry) fList.get(index);
                      entry.fValue= value;
                  }
              }
            
              public void remove(Position position) {
                  int index= getIndex(position);
                  if (index > -1)
                      fList.remove(index);
              }
            
              public void clear() {
                  fList.clear();
              }
    };

    protected IFileEditorInput fInput;
    protected List fCollectedProblems;
    private List fGeneratedAnnotations;
    private IProgressMonitor fProgressMonitor;
    private boolean fIsActive = false;

    private ReverseMap fReverseMap = new ReverseMap();
    private List fPreviouslyOverlaid = null;
    private List fCurrentlyOverlaid = new ArrayList();

    public ProblemAnnotationModel(IFileEditorInput input)
    {
        super(input.getFile());
        fInput = input;
    }

    protected MarkerAnnotation createMarkerAnnotation(IMarker marker)
    {
        return new ProblemMarkerAnnotation(marker);
    }

    protected Position createPositionFromProblem(IProblem problem)
    {
        int start= problem.getCharStart();
        int end= problem.getCharEnd();
        
        if (start > end) {
            end= start + end;
            start= end - start;
            end= end - start;
        }
        
        if (start == -1 && end == -1) {
            // line number is 1-based
            int line= problem.getLineNumber();
            if (line > 0 && fDocument != null) {
                try {
                    start= fDocument.getLineOffset(line - 1);
                    end= start;
                } catch (BadLocationException x) {
                }
            }
        }
        
        if (start > -1 && end > -1)
            return new Position(start, end - start);
        
        return null;
    }

    /*
     * @see IProblemRequestor#endReporting()
     */
    public void endCollecting()
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

                fCollectedProblems.clear();
            }

            removeMarkerOverlays(isCanceled);
            fPreviouslyOverlaid.clear();
            fPreviouslyOverlaid = null;
        }

        if (temporaryProblemsChanged)
            fireModelChanged(new AnnotationModelEvent(this));
    }

    private void removeMarkerOverlays(boolean isCanceled)
    {
        if (isCanceled)
        {
            fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
        } else if (fPreviouslyOverlaid != null)
        {
            Iterator e = fPreviouslyOverlaid.iterator();
            while (e.hasNext())
            {
                ProblemMarkerAnnotation annotation = (ProblemMarkerAnnotation) e.next();
                annotation.setOverlay(null);
            }
        }
    }

    /**
     * Overlays value with problem annotation.
     * @param problemAnnotation
     */
    private void setOverlay(Object value, ProblemAnnotation problemAnnotation)
    {
        if (value instanceof ProblemMarkerAnnotation)
        {
            ProblemMarkerAnnotation annotation = (ProblemMarkerAnnotation) value;
            if (annotation.isProblem())
            {
                annotation.setOverlay(problemAnnotation);
                fPreviouslyOverlaid.remove(annotation);
                fCurrentlyOverlaid.add(annotation);
            }
        }
    }

    private void overlayMarkers(Position position, ProblemAnnotation problemAnnotation)
    {
        Object value = getAnnotations(position);
        if (value instanceof List)
        {
            List list = (List) value;
            for (Iterator e = list.iterator(); e.hasNext();)
                setOverlay(e.next(), problemAnnotation);
        } else
        {
            setOverlay(value, problemAnnotation);
        }
    }

    /**
     * Tells this annotation model to collect temporary problems from now on.
     */
    private void startCollectingProblems()
    {
        fCollectedProblems = new ArrayList();
        fGeneratedAnnotations = new ArrayList();
    }

    /**
     * Tells this annotation model to no longer collect temporary problems.
     */
    private void stopCollectingProblems()
    {
        if (fGeneratedAnnotations != null)
        {
            removeAnnotations(fGeneratedAnnotations, true, true);
            fGeneratedAnnotations.clear();
        }
        fCollectedProblems = null;
        fGeneratedAnnotations = null;
    }

    /*
     * @see AnnotationModel#fireModelChanged()
     */
    protected void fireModelChanged()
    {
        fireModelChanged(new AnnotationModelEvent(this));
    }

    /*
     * @see IProblemRequestor#isActive()
     */
    public boolean isActive()
    {
        return fIsActive && (fCollectedProblems != null);
    }

    /*
     * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
     */
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        fProgressMonitor = monitor;
    }

    public void setIsActive(boolean isActive)
    {
        if (fIsActive != isActive)
        {
            fIsActive = isActive;
            if (fIsActive)
                startCollectingProblems();
            else
                stopCollectingProblems();
        }
    }

    private Object getAnnotations(Position position)
    {
        return fReverseMap.get(position);
    }

    /*
     * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
     */
    protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged)
    {
        super.addAnnotation(annotation, position, fireModelChanged);

        Object cached = fReverseMap.get(position);
        if (cached == null)
            fReverseMap.put(position, annotation);
        else if (cached instanceof List)
        {
            List list = (List) cached;
            list.add(annotation);
        } else if (cached instanceof Annotation)
        {
            List list = new ArrayList(2);
            list.add(cached);
            list.add(annotation);
            fReverseMap.put(position, list);
        }
    }

    /*
     * @see AnnotationModel#removeAllAnnotations(boolean)
     */
    protected void removeAllAnnotations(boolean fireModelChanged)
    {
        super.removeAllAnnotations(fireModelChanged);
        fReverseMap.clear();
    }

    /*
     * @see AnnotationModel#removeAnnotation(Annotation, boolean)
     */
    protected void removeAnnotation(Annotation annotation, boolean fireModelChanged)
    {
        Position position = getPosition(annotation);
        Object cached = fReverseMap.get(position);
        if (cached instanceof List)
        {
            List list = (List) cached;
            list.remove(annotation);
            if (list.size() == 1)
            {
                fReverseMap.put(position, list.get(0));
                list.clear();
            }
        } else if (cached instanceof Annotation)
        {
            fReverseMap.remove(position);
        }

        super.removeAnnotation(annotation, fireModelChanged);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblemCollector#addProblem(int, com.iw.plugins.spindle.core.parser.ISourceLocation, java.lang.String)
     */
    public void addProblem(int severity, ISourceLocation location, String message)
    {
        addProblem(
            new DefaultProblem(
                ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
                severity,
                message,
                location.getLineNumber(),
                location.getCharStart(),
                location.getCharEnd()));
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblemCollector#addProblem(com.iw.plugins.spindle.core.parser.IProblem)
     */
    public void addProblem(IProblem problem)
    {
        if (isActive())
            fCollectedProblems.add(problem);

    }

    

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblemCollector#getProblems()
     */
    public IProblem[] getProblems()
    {
        // TODO Auto-generated method stub
        return null;
    }

};
