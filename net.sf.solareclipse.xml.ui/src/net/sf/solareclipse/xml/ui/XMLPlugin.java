/**********************************************************************
 Copyright (c) 2002  Widespace, OU  and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://solareclipse.sourceforge.net/legal/cpl-v10.html

 Contributors:
 Igor Malinin - initial contribution

 $Id$
 **********************************************************************/
package net.sf.solareclipse.xml.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.solareclipse.xml.internal.ui.preferences.CSSSyntaxPreferencePage;
import net.sf.solareclipse.xml.internal.ui.preferences.XMLSyntaxPreferencePage;
import net.sf.solareclipse.xml.ui.text.CSSTextTools;
import net.sf.solareclipse.xml.ui.text.DTDTextTools;
import net.sf.solareclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class XMLPlugin extends AbstractUIPlugin
{
  // The shared instance.
  private static XMLPlugin plugin;

  // Resource bundle.
  private ResourceBundle resources;

  private XMLTextTools xmlTextTools;
  private DTDTextTools dtdTextTools;
  private CSSTextTools cssTextTools;

  public XMLPlugin()
  {
    super();
    try
    {
      resources = ResourceBundle.getBundle("net.sf.solareclipse.xml.ui.XMLPluginResources");
    } catch (MissingResourceException x)
    {
    }
  }
  /**
   * The constructor.
   */
  public XMLPlugin(IPluginDescriptor descriptor)
  {
    super(descriptor);

    plugin = this;

    try
    {
      resources = ResourceBundle.getBundle("net.sf.solareclipse.xml.ui.XMLPluginResources");
    } catch (MissingResourceException x)
    {
    }
  }

  /*
   * @see org.eclipse.core.runtime.Plugin#shutdown()
   */
  public void shutdown() throws CoreException
  {
    super.shutdown();

    if (xmlTextTools != null)
    {
      xmlTextTools.dispose();
      xmlTextTools = null;
    }

    if (dtdTextTools != null)
    {
      dtdTextTools.dispose();
      dtdTextTools = null;
    }
  }

  /**
   * Returns the shared instance.
   */
  public static XMLPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = XMLPlugin.getDefault().getResourceBundle();

    try
    {
      return bundle.getString(key);
    } catch (MissingResourceException e)
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle.
   */
  public ResourceBundle getResourceBundle()
  {
    return resources;
  }

  static public void log(Throwable ex)
  {
    ILog log = getDefault().getLog();
    StringWriter stringWriter = new StringWriter();
    ex.printStackTrace(new PrintWriter(stringWriter));
    String msg = stringWriter.getBuffer().toString();

    Status status = new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IStatus.ERROR, msg, null);
    log.log(status);
  }

  /*
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(IPreferenceStore)
   */
  protected void initializeDefaultPluginPreferences()
  {
    XMLSyntaxPreferencePage.initializeDefaultPreferences(getPreferenceStore());
    CSSSyntaxPreferencePage.initDefaults(getPreferenceStore());
  }

  /**
   * Returns instance of text tools for XML.
   */
  public XMLTextTools getXMLTextTools()
  {
    if (xmlTextTools == null)
    {
      xmlTextTools = new XMLTextTools(getPreferenceStore());
    }

    return xmlTextTools;
  }

  /**
   * Returns instance of text tools for DTD.
   */
  public DTDTextTools getDTDTextTools()
  {
    if (dtdTextTools == null)
    {
      dtdTextTools = new DTDTextTools(getPreferenceStore());
    }

    return dtdTextTools;
  }

  /**
   * Returns instance of text tools for DTD.
   */
  public CSSTextTools getCSSTextTools()
  {
    if (cssTextTools == null)
    {
      cssTextTools = new CSSTextTools(getPreferenceStore());
    }

    return cssTextTools;
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
   */
  public IPreferenceStore getPreferenceStore()
  {
    // TODO Auto-generated method stub
    return super.getPreferenceStore();
  }

}