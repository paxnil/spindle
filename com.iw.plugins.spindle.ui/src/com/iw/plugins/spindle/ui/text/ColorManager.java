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
package com.iw.plugins.spindle.ui.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.iw.plugins.spindle.UIPlugin;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ColorManager implements IColorManager, IColorConstants {

  private static Map fColorTable = new HashMap(10);
  private static int counter = 0;

  public ColorManager() {
    initialize();
    counter++;
  }

  public static void initializeDefaults(IPreferenceStore store) {
    PreferenceConverter.setDefault(store, P_JWCID, JWCID);
    PreferenceConverter.setDefault(store, P_XML_COMMENT, XML_COMMENT);
    PreferenceConverter.setDefault(store, P_PROC_INSTR, PROC_INSTR);
    PreferenceConverter.setDefault(store, P_STRING, STRING);
    PreferenceConverter.setDefault(store, P_DEFAULT, DEFAULT);
    PreferenceConverter.setDefault(store, P_TAG, TAG);
  }

  private void initialize() {
    IPreferenceStore pstore = UIPlugin.getDefault().getPreferenceStore();

    putColor(pstore, P_JWCID);
    putColor(pstore, P_XML_COMMENT);
    putColor(pstore, P_PROC_INSTR);
    putColor(pstore, P_STRING);
    putColor(pstore, P_DEFAULT);
    putColor(pstore, P_TAG);

  }

  public void dispose() {
    counter--;
    if (counter == 0) {
      Iterator e = fColorTable.values().iterator();
      while (e.hasNext())
         ((Color) e.next()).dispose();
    }
  }

  private void putColor(IPreferenceStore pstore, String property) { 
    RGB setting = PreferenceConverter.getColor(pstore, property);
    Color color = new Color(Display.getCurrent(), setting);
    fColorTable.put(property, color);
  }

  public Color getColor(String key) {
    Color color = (Color) fColorTable.get(key);
    if (color == null) {
      color = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
    }
    return color;
  }

  public Color getColor(RGB rgb) {
    Color result = (Color) fColorTable.get(rgb);
    if (result == null) {
        bindColor(rgb, rgb);
        result = (Color) fColorTable.get(rgb);
    }
    return result;
        
  }
  
  public void bindColor( Object key, RGB rgb ) {
      Object value = fColorTable.get( key );
      if ( value != null ) {
          throw new UnsupportedOperationException();
      }

      Color color = new Color( Display.getCurrent(), rgb );

      fColorTable.put( key, color );
  }

  public void unbindColor( String key ) {
      Color color = (Color) fColorTable.remove( key );
      if ( color != null ) {
          color.dispose();
      }
  }

}
