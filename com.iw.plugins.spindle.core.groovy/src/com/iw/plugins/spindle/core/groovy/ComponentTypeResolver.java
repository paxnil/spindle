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
package com.iw.plugins.spindle.core.groovy;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;

/**
 *  Resolve groovy files for Groovestry Pages and Components
 * 
 * @author glongman@gmail.com
 * 
 */
public class ComponentTypeResolver implements IComponentTypeResourceResolver
{
  
  private IStorage fStorage;

  /* (non-Javadoc)
   * @see com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver#canResolve(org.eclipse.jdt.core.IType)
   */
  public boolean canResolve(IType type)
  {
    fStorage = null;
    try
    {
      if (type.isInterface())
      return false;
      
      String name = type.getFullyQualifiedName();
      //the quick and easy checks
      if ("org.apache.tapestry.contrib.script.ScriptPage".equals(name) || "org.apache.tapestry.contrib.script.ScriptBaseComponent".equals(name) ) 
        return true;
      
      //otherwise see if the type extends ScriptPage, ScriptBaseComponent , or  ScriptAbstractComponent in that order.
      
      // I don't think the following is right - subclasses may override something (I don't know what) that changes where the groovy script is found!
      // plus, hierarchy lookups are expensive
      
      // I think eventually the IComponentTypeResourceResolver interface will become an abstract subclass
      // that will cache hierachy lookup results.
      
//      if (CoreUtils.extendsType(type, "org.apache.tapestry.contrib.script.ScriptPage"))
//        return true;
//      
//      if (CoreUtils.extendsType(type, "org.apache.tapestry.contrib.script.ScriptBaseComponent"))
//        return true;
//      
//      if (CoreUtils.extendsType(type, "org.apache.tapestry.contrib.script.ScriptAbstractComponent"))
//        return true;
    } catch (JavaModelException e)
    {
      TapestryCore.log(e);    
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver#doResolve(com.iw.plugins.spindle.core.resources.ICoreResource, org.apache.tapestry.spec.IComponentSpecification)
   */
  public IStatus doResolve(
      ICoreResource specificationLocation,
      IComponentSpecification componentSpec)
  {
    SpindleStatus status = new SpindleStatus();
    
    IStorage specStorage = specificationLocation.getStorage();
    if (specStorage == null) {
      status.setError("Groovestry could not find the file for '"+specificationLocation.toString());
      return status;
    }
    
    String scriptName = specStorage.getFullPath().removeFileExtension().lastSegment();
    
    ICoreResource scriptLocation = (ICoreResource) specificationLocation.getRelativeResource(scriptName+".groovy");
    
    fStorage = scriptLocation.getStorage();
    
    if (fStorage == null) 
      status.setError(scriptLocation.toString()+" does not exist.");      
      
    return status;
  }

  /* (non-Javadoc)
   * @see com.iw.plugins.spindle.core.extensions.IComponentTypeResourceResolver#getStorage()
   */
  public IStorage getStorage()
  {
    return fStorage;
  }

}
