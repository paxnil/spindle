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
package com.iw.plugins.spindle.model.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.pde.core.IModel;

import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.ITapestryModel;

/**
 * @author gwl
 * @version $Id$
 *
 */
public abstract class AbstractDelegate implements ITapestryModelManagerDelegate {

  ArrayList models = new ArrayList();
  
  /**
   * Constructor for DefaultDelegate.
   */
  public AbstractDelegate() {
    super();
  }
  

  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#getFirstLoadedModel()
   */
  public ITapestryModel getFirstLoadedModel() {
    for (Iterator iter = models.iterator(); iter.hasNext();) {
      ITapestryModel model = (ITapestryModel) iter.next();
      if (model.isLoaded()) {
      	return model;
      }
    }
    return null;
  }

  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#addModel(ITapestryModel)
   */
  public void addModel(ITapestryModel model) {
  	if (!models.contains(model)) {
  		models.add(model);
  	} else {
  		throw new IllegalArgumentException("tried to add duplicate model for "+model.getUnderlyingStorage().getFullPath());
  	}  	
  	
  }
  
  public void clear() {  	
  	models.clear();
  }

  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#removeModel(IModel)
   */
  public void removeModel(IModel nuked) {
  	models.remove(nuked);
  }

  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#createModel(IStorage)
   */
  public abstract BaseTapestryModel createModel(IStorage storage);

  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#getAllModels()
   */
  public List getAllModels() {
    return models;
  }


  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#registerParserFor(String)
   */
  public abstract void registerParserFor(String extension);

}
