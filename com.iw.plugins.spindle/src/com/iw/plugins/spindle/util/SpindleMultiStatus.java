package com.iw.plugins.spindle.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class SpindleMultiStatus extends SpindleStatus {
	
  private List subStatii = new ArrayList();

  /**
   * Constructor for SpindleMultiStatus.
   */
  public SpindleMultiStatus() {
    super();
  }

  /**
   * Constructor for SpindleMultiStatus.
   * @param severity
   * @param message
   */
  public SpindleMultiStatus(int severity, String message) {
    super(severity, message);
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getChildren()
   */
  public IStatus[] getChildren() {
  	
    return (IStatus []) subStatii.toArray(new IStatus[subStatii.size()]);
    
  }
  
  public void addStatus(IStatus status) {
  	
  	if (!subStatii.contains(status)) {
  		subStatii.add(status);
  	}
  	
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#isMultiStatus()
   */
  public boolean isMultiStatus() {
    return true;
  }

}
