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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SharedTextColors implements ISharedTextColors
{

  private Map fColorMap;

  public SharedTextColors()
  {
    super();
  }

  public Color getColor(RGB rgb)
  {
    if (rgb == null)
      return null;

    if (fColorMap == null)
      fColorMap = new HashMap(2);

    Display display = Display.getCurrent();

    Map colorTable = (Map) fColorMap.get(display);
    if (colorTable == null)
    {
      colorTable = new HashMap(10);
      fColorMap.put(display, colorTable);
    }

    Color color = (Color) colorTable.get(rgb);
    if (color == null || color.isDisposed())
    {
      color = new Color(display, rgb);
      colorTable.put(rgb, color);
    }

    return color;
  }

  /*
   * @see ISharedTextColors#dispose()
   */
  public void dispose()
  {
    if (fColorMap != null)
    {
      Iterator j = fColorMap.values().iterator();
      while (j.hasNext())
      {
        Iterator i = ((Map) j.next()).values().iterator();
        while (i.hasNext())
          ((Color) i.next()).dispose();
      }
    }
  }

}