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

package com.iw.plugins.spindle.core.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for Spec classes that have properties
 * 
 * @author glongman@gmail.com
 * 
 */
public abstract class BasePropertyHolder extends DescribableSpecification
    implements
      IPluginPropertyHolder
{
  Map fProperties;

  /**
   * The locations and values of all reserved property declarations in a the
   * document for this holder. Immutable after a parse/scan episode.
   */
  List fPropertyDeclarations;

  /**
   *  
   */
  public BasePropertyHolder(int type)
  {
    super(type);

  }

  public void addPropertyDeclaration(PluginPropertyDeclaration declaration)
  {
    if (fPropertyDeclarations == null)
    {
      fPropertyDeclarations = new ArrayList();
      fProperties = new HashMap();
    }

    fPropertyDeclarations.add(declaration);

    if (!fProperties.containsKey(declaration.getKey()))
      fProperties.put(declaration.getKey(), declaration);

  }

  public List getPropertyDeclarations()
  {
    if (fPropertyDeclarations == null)
      return Collections.EMPTY_LIST;
    return fPropertyDeclarations;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.util.IPropertyHolder#getPropertyNames()
   */
  public List getPropertyNames()
  {
    if (fProperties == null)
      return Collections.EMPTY_LIST;

    List result = new ArrayList(fProperties.keySet());

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.util.IPropertyHolder#setProperty(java.lang.String,
   *      java.lang.String)
   */
  public void setProperty(String name, String value)
  {
    throw new IllegalStateException("not used in SPindle!");
    //        if (value == null)
    //        {
    //            removeProperty(name);
    //            return;
    //        }
    //
    //        if (fProperties == null)
    //            fProperties = new PropertyFiringMap(this, "properties");
    //
    //        fProperties.put(name, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.util.IPropertyHolder#removeProperty(java.lang.String)
   */
  public void removeProperty(String name)
  {
    throw new IllegalStateException("not used in SPindle!");
    //        remove(fProperties, name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.util.IPropertyHolder#getProperty(java.lang.String)
   */
  public String getProperty(String name)
  {
    if (fProperties == null)
      return null;

    PluginPropertyDeclaration declaration = (PluginPropertyDeclaration) fProperties
        .get(name);
    if (declaration == null)
      return null;
    return declaration.getValue();
  }

  PluginPropertyDeclaration getPropertyDeclaration(String name)
  {
    if (fProperties == null)
      return null;

    return (PluginPropertyDeclaration) fProperties.get(name);
  }

}