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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */



package com.iw.plugins.spindle;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps with managing messages.
 */
public class MessageUtil {

  private static final String RESOURCE_BUNDLE = "com.iw.plugins.spindle.messages";

  private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

  static private HashMap resourceFiles = new HashMap();

  private MessageUtil() {
    // prevent instantiation of class
  }
  /**
   * Returns the formatted message for the given key in
   * the resource bundle. 
   *
   * @param key the resource name
   * @param args the message arguments
   * @return the string
   */
  public static String format(String key, Object[] args) {
    return MessageFormat.format(getString(key), args);
  }
  /**
   * Returns the resource object with the given key in
   * the resource bundle. If there isn't any value under
   * the given key, the key is returned, surrounded by '!'s.
   *
   * @param key the resource name
   * @return the string
   */
  public static String getString(String key) {
    try {
      return fgResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      return "!" + key + "!"; //$NON-NLS-2$ 
    }
  }

  /**
   * Gets a string from the resource bundle and formats it with arguments
   */
  public static String getFormattedString(String key, Object[] args) {
    return MessageFormat.format(getString(key), args);
  }
  /**
   * Gets a string from the resource bundle and formats it with the argument
   * 
   * @param key	the string used to get the bundle value, must not be null
   */
  public static String getFormattedString(String key, Object arg) {
    return MessageFormat.format(getString(key), new Object[] { arg });
  }

  public static String getResourceFile(String key) {
    if (key == null) {
      return null;
    }
    String result = (String) resourceFiles.get(key);
    InputStream in = null;
    if (result != null) {
      return result;
    }
    in = TapestryPlugin.class.getResourceAsStream(getString(key));
    if (in == null) {
      in = TapestryPlugin.class.getResourceAsStream(key);
    }
    if (in == null) {
      return null;
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringWriter writer = new StringWriter();
    try {
      String line = reader.readLine();
      while (line != null) {
        writer.write(line);
        line = reader.readLine();
        if (line != null) {
          writer.write("\n");
        }
      }
    } catch (IOException ioex) {
      TapestryPlugin.getDefault().logException(ioex);
      return null;
    }
    result = writer.toString();
    try {
      reader.close();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

}