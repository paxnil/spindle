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

package com.iw.plugins.spindle.editors.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Support for showing a Busy Cursor during a long running process.
 */
public class BusyIndicatorSpindle
{

  static int nextBusyId = 1;
  static final String BUSYID_NAME = "Spindle Busy";

  /**
   * Runs the given <code>Runnable</code> while providing busy feedback using
   * this busy indicator.
   * 
   * @param display the display on which the busy feedback should be displayed.
   *          If the display is null, the Display for the current thread will be
   *          used. If there is no Display for the current thread, the runnable
   *          code will be executed and no busy feedback will be displayed.
   * @param runnable the runnable for which busy feedback is to be shown. Must
   *          not be null.
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
   *              </ul>
   * 
   * @see #showWhile
   */

  public static void showWhile(Display display, Runnable runnable)
  {
    if (runnable == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (display == null)
    {
      display = Display.getCurrent();
      if (display == null)
      {
        runnable.run();
        return;
      }
    }

    Integer busyId = new Integer(nextBusyId);
    nextBusyId++;
    Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);

    Shell[] shells = display.getShells();
    for (int i = 0; i < shells.length; i++)
    {
      Integer id = (Integer) shells[i].getData(BUSYID_NAME);
      if (id == null)
      {
        shells[i].setCursor(cursor);
        shells[i].setData(BUSYID_NAME, busyId);
      }
    }

    try
    {
      runnable.run();
    } finally
    {
      shells = display.getShells();
      for (int i = 0; i < shells.length; i++)
      {
        Integer id = (Integer) shells[i].getData(BUSYID_NAME);
        if (id == busyId)
        {
          shells[i].setCursor(null);
          shells[i].setData(BUSYID_NAME, null);
        }
      }
      if (cursor != null && !cursor.isDisposed())
      {
        cursor.dispose();
      }
    }
  }
}