/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package com.iw.plugins.spindle.editors;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class ProblemMarkerAnnotation extends MarkerAnnotation
    implements
      IProblemAnnotation
{

  public static final String TAPESTRY_MARKER_TYPE_PREFIX = "com.iw.plugins.spindle";
  public static final String ERROR_ANNOTATION_TYPE = TAPESTRY_MARKER_TYPE_PREFIX
      + ".ui.error";
  public static final String WARNING_ANNOTATION_TYPE = TAPESTRY_MARKER_TYPE_PREFIX
      + ".ui.warning";
  public static final String INFO_ANNOTATION_TYPE = TAPESTRY_MARKER_TYPE_PREFIX
      + ".ui.info";
  public static final String TASK_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.task";

  private IProblemAnnotation fOverlay;
  private boolean fNotRelevant = false;

  public ProblemMarkerAnnotation(IMarker marker)
  {
    super(marker);
    //    initialize();
  }

  //  protected String getUnknownImageName(IMarker marker)
  //  {
  //    return "unknown";
  //  }

  /**
   * Initializes the annotation's icon representation and its drawing layer
   * based upon the properties of the underlying marker.
   */
  //  protected void initialize()
  //  {
  //    IMarker marker = getMarker();
  //
  //    fType = ProblemAnnotationType.UNKNOWN;
  //    if (marker.exists())
  //    {
  //      try
  //      {
  //        if (marker.isSubtypeOf(IMarker.PROBLEM))
  //        {
  //          int severity = marker.getAttribute(IMarker.SEVERITY, -1);
  //          switch (severity)
  //          {
  //            case IMarker.SEVERITY_ERROR :
  //              fType = ProblemAnnotationType.ERROR;
  //              break;
  //            case IMarker.SEVERITY_WARNING :
  //              fType = ProblemAnnotationType.WARNING;
  //              break;
  //          }
  //        }
  //
  //        else if (marker.isSubtypeOf(IMarker.TASK))
  //          fType = ProblemAnnotationType.TASK;
  //        else if (marker.isSubtypeOf(SearchUI.SEARCH_MARKER))
  //          fType = ProblemAnnotationType.SEARCH;
  //        else if (marker.isSubtypeOf(IMarker.BOOKMARK))
  //          fType = ProblemAnnotationType.BOOKMARK;
  //
  //      } catch (CoreException e)
  //      {
  //        UIPlugin.log(e);
  //      }
  //    }
  //  }
  public String getMessage()
  {
    IMarker marker = getMarker();
    if (marker == null || !marker.exists())
      return "";
    else
      return marker.getAttribute(IMarker.MESSAGE, "");
  }

  public boolean isTemporary()
  {
    return false;
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
    return WARNING_ANNOTATION_TYPE.equals(type) || ERROR_ANNOTATION_TYPE.equals(type);
  }

  public boolean isRelevant()
  {
    return !fNotRelevant;
  }

  /**
   * Overlays this annotation with the given annotation.
   * 
   * @param annotation that is overlaid by this annotation
   */
  public void setOverlay(IProblemAnnotation annotation)
  {
    if (fOverlay != null)
      fOverlay.removeOverlaid(this);

    fOverlay = annotation;
    fNotRelevant = (fNotRelevant || fOverlay != null);

    if (annotation != null)
      annotation.addOverlaid(this);
  }

  public IProblemAnnotation getOverlay()
  {
    return fOverlay;
  }

  public boolean hasOverlay()
  {
    return fOverlay != null;
  }

  //    public Image getImage(Display display)
  //    {
  //        int newImageType = NO_IMAGE;
  //
  //        if (hasOverlay())
  //            newImageType = OVERLAY_IMAGE;
  //        else if (isRelevant())
  //        {
  //            // if (mustShowQuickFixIcon())
  //            // {
  //            // if (fType == AnnotationType.ERROR)
  //            // newImageType = QUICKFIX_ERROR_IMAGE;
  //            // else
  //            // newImageType = QUICKFIX_IMAGE;
  //            // } else
  //            newImageType = ORIGINAL_MARKER_IMAGE;
  //        } else
  //            newImageType = GRAY_IMAGE;
  //
  //        if (fImageType == newImageType && newImageType != OVERLAY_IMAGE)
  //            // Nothing changed - simply return the current image
  //            return super.getImage(display);
  //
  //        Image newImage = null;
  //        switch (newImageType)
  //        {
  //            case ORIGINAL_MARKER_IMAGE :
  //                newImage = null;
  //                break;
  //            case OVERLAY_IMAGE :
  //                newImage = fOverlay.getImage(display);
  //                break;
  //                // case QUICKFIX_IMAGE :
  //                // newImage = getQuickFixImage();
  //                // break;
  //                // case QUICKFIX_ERROR_IMAGE :
  //                // newImage = getQuickFixErrorImage();
  //                // break;
  //            case GRAY_IMAGE :
  //                if (fImageType != ORIGINAL_MARKER_IMAGE)
  //                    setImage(null);
  //                Image originalImage = super.getImage(display);
  //                if (originalImage != null)
  //                {
  //                    ImageRegistry imageRegistry = getGrayMarkerImageRegistry(display);
  //                    if (imageRegistry != null)
  //                    {
  //                        String key = Integer.toString(originalImage.hashCode());
  //                        Image grayImage = imageRegistry.get(key);
  //                        if (grayImage == null)
  //                        {
  //                            grayImage = new Image(display, originalImage, SWT.IMAGE_GRAY);
  //                            imageRegistry.put(key, grayImage);
  //                        }
  //                        newImage = grayImage;
  //                    }
  //                }
  //                break;
  //            default :
  //                Assert.isLegal(false);
  //        }
  //
  //        fImageType = newImageType;
  //        setImage(newImage);
  //        return super.getImage(display);
  //    }
  //
  //    private ImageRegistry getGrayMarkerImageRegistry(Display display)
  //    {
  //        if (fGrayMarkersImageRegistry == null)
  //            fGrayMarkersImageRegistry = new ImageRegistry(display);
  //        return fGrayMarkersImageRegistry;
  //    }

  public void addOverlaid(IProblemAnnotation annotation)
  {
    // not supported
  }

  public void removeOverlaid(IProblemAnnotation annotation)
  {
    // not supported
  }

  public Iterator getOverlaidIterator()
  {
    // not supported
    return null;
  }

}