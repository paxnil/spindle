package com.iw.plugins.spindle.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class TapestryModelException extends CoreException {

  public TapestryModelException(IStatus status) {
    super(status);
  }

}
