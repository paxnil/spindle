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
package com.iw.plugins.spindle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 */
public class ModelUtils {
  /**
   * returns a readonly model, if found!
   * the model called root is used only to figure out which project to search
   */
  public static TapestryApplicationModel findApplication(
    String specificationPath,
    ITapestryModel root) {
    Assert.isNotNull(specificationPath);
    Assert.isNotNull(root);
    
    if (!specificationPath.endsWith(".application")) {
      return null;
    }
    
    TapestryLookup lookup = new TapestryLookup();
    
    try {
    	
      IJavaProject jproject =
        TapestryPlugin.getDefault().getJavaProjectFor(root.getUnderlyingStorage());
        
      lookup.configure(jproject);
      
      IStorage[] results = lookup.findApplication(specificationPath);
      
      if (results.length == 0) {
        return null;
      }
      
      return (TapestryApplicationModel) TapestryPlugin.getTapestryModelManager().getReadOnlyModel(
        results[0]);
        
    } catch (JavaModelException jmex) {
      return null;
    }
  }



  public static TapestryComponentModel findComponentWithHTML(IStorage storage) {
    List componentModels = TapestryPlugin.getTapestryModelManager().getAllModels(storage, "jwc");
    if (componentModels != null && !componentModels.isEmpty()) {
      IPath htmlPath = storage.getFullPath().removeFileExtension();
      IPath jwcPath = new Path(htmlPath.toString() + ".jwc");
      Iterator iter = componentModels.iterator();
      while (iter.hasNext()) {
        TapestryComponentModel model = (TapestryComponentModel) iter.next();
        IStorage underlier = model.getUnderlyingStorage();
        IPath underlierPath = underlier.getFullPath();
        if (underlierPath.equals(jwcPath)) {
          if (storage instanceof IResource && underlier instanceof IResource) {
            return model;
          } else if (storage instanceof IStorage && underlier instanceof IStorage) {
            return model;
          }
        }
      }
    }
    return null;
  }

  /**
   * returns a readonly model, if found!
   * the model called root is used only to figure out which project to search
   */
  public static TapestryComponentModel findComponent(
    String specificationPath,
    ITapestryModel root) {
    Assert.isNotNull(specificationPath);
    Assert.isNotNull(root);
    
    if (!specificationPath.endsWith(".jwc")) {
      return null;
    }
    
    TapestryLookup lookup = new TapestryLookup();
    
    try {
    	
      IJavaProject jproject =
        TapestryPlugin.getDefault().getJavaProjectFor(root.getUnderlyingStorage());
        
      lookup.configure(jproject);
      
      IStorage[] results = lookup.findComponent(specificationPath);
      
      if (results.length == 0) {
        return null;
      }
      
      return (TapestryComponentModel) TapestryPlugin.getTapestryModelManager().getReadOnlyModel(
        results[0]);
        
    } catch (JavaModelException jmex) {
      return null;
    }
  }

  public static List findComponentsUsingAlias(String alias) {
    ArrayList result = new ArrayList();
    List componentModels = TapestryPlugin.getTapestryModelManager().getAllModels(null, "jwc");
    Iterator iter = componentModels.iterator();
    while (iter.hasNext()) {
      TapestryComponentModel model = (TapestryComponentModel) iter.next();
      PluginComponentSpecification componentSpec = model.getComponentSpecification();
      if (componentSpec == null) {
        continue;
      }
      if (componentSpec.usesAlias(alias)) {
        result.add(model);
      }
    }
    return result;
  }

  public static Iterator getComponentModels() {
    return TapestryPlugin.getTapestryModelManager().getAllModels(null, "jwc").iterator();
  }

  public static Iterator getApplicationModels() {
    return TapestryPlugin.getTapestryModelManager().getAllModels(null, "application").iterator();
  }

}
