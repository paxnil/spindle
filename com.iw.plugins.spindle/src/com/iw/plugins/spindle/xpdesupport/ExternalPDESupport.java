package com.iw.plugins.spindle.xpdesupport;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.internal.ui.editor.text.ColorManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ExternalPDESupport {

  private static ExternalPDESupport inst;

  private ResourceBundle resourceBundle;

  private java.util.Hashtable counters;

  public ExternalPDESupport() {
    inst = this;
    try {
      resourceBundle = ResourceBundle.getBundle("com.iw.plugins.spindle.xpdesupport.pderesources");
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }

  public static ExternalPDESupport getDefault() {
    if (inst == null) {
      inst = new ExternalPDESupport();
    }
    return inst;
  }
  public static String getFormattedMessage(String key, String[] args) {
    String text = getResourceString(key);
    return java.text.MessageFormat.format(text, args);
  }
  public static String getFormattedMessage(String key, String arg) {
    String text = getResourceString(key);
    return java.text.MessageFormat.format(text, new Object[] { arg });
  }
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }
  public static String getResourceString(String key) {
    ResourceBundle bundle = ExternalPDESupport.getDefault().getResourceBundle();
    if (bundle != null) {
      try {
        String bundleString = bundle.getString(key);
        //return "$"+bundleString;
        return bundleString;
      } catch (MissingResourceException e) {
        // default actions is to return key, which is OK
      }
    }
    return key;
  }

  protected void initializeDefaultPreferences(IPreferenceStore store) {
    ColorManager.initializeDefaults(store);
  }
}