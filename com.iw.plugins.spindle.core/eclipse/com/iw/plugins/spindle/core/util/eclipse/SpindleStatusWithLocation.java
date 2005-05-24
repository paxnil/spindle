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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.util.eclipse;

import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * SpindleStatusWithLocation
 * 
 * @author glongman@gmail.com
 *  
 */
public class SpindleStatusWithLocation extends SpindleStatus implements ISourceLocation
{

  private ISourceLocation fLocation;

  public SpindleStatusWithLocation(ISourceLocation defaultLocation)
  {
    super();
    Assert.isNotNull(defaultLocation);
    fLocation = defaultLocation;
  }
  /**
   * @param severity
   * @param message
   */
  public SpindleStatusWithLocation(ISourceLocation baseLocation, int severity,
      String message)
  {
    super(severity, message);
    Assert.isNotNull(baseLocation);
    fLocation = baseLocation;
  }
  /**
   * @param exception
   */
  public SpindleStatusWithLocation(ISourceLocation baseLocation, Throwable exception)
  {
    super(exception);
    Assert.isNotNull(baseLocation);
    fLocation = baseLocation;
  }

  public void setError(String errorMessage, ISourceLocation location)
  {
    Assert.isNotNull(location);
    fLocation = location;
    super.setError(errorMessage);
  }
  public void setInfo(String infoMessage, ISourceLocation location)
  {
    Assert.isNotNull(location);
    fLocation = location;
    super.setInfo(infoMessage);
  }
  public void setWarning(String warningMessage, ISourceLocation location)
  {
    Assert.isNotNull(location);
    fLocation = location;
    super.setWarning(warningMessage);
  }

  public boolean contains(int cursorPosition)
  {
    // TODO Auto-generated method stub
    return fLocation.contains(cursorPosition);
  }
  public int getCharEnd()
  {
    return fLocation.getCharEnd();
  }
  public int getCharStart()
  {
    return fLocation.getCharStart();
  }
  public int getLength()
  {
    return fLocation.getLength();
  }
  public int getLineNumber()
  {
    return fLocation.getLineNumber();
  }
  public ISourceLocation getLocationOffset(int cursorPosition)
  {
    return fLocation.getLocationOffset(cursorPosition);
  }
}