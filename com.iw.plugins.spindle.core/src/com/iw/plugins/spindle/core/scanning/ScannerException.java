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

package com.iw.plugins.spindle.core.scanning;

import com.iw.plugins.spindle.core.source.ISourceLocation;

/**
 * Exception type thrown by Processors
 * 
 * @author glongman@gmail.com
 * 
 */
public class ScannerException extends Exception
{

  ISourceLocation location;
  boolean fTemporary = false;

  /**
   * @param arg0
   */
  public ScannerException(String message, boolean temporary)
  {
    super(message);
    fTemporary = temporary;
  }

  /**
   * @param arg0
   * @param arg1
   */
  public ScannerException(String message, Throwable exception, boolean temporary)
  {
    super(message, exception);
    fTemporary = temporary;
  }

  public ScannerException(String message, ISourceLocation location, boolean temporary)
  {
    super(message);
    this.location = location;
    fTemporary = temporary;
  }

  public ISourceLocation getLocation()
  {
    return this.location;
  }

  public boolean isTemporary()
  {
    return fTemporary;
  }

}