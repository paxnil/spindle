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

import net.sf.tapestry.parse.SpecificationParser;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.BaseTapestryModel;
import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.TapestryPluginFactory;

/**
 * @author gwl
 * @version $Id$
 *
 */
public class ApplicationManagerDelegate extends AbstractDelegate {

  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#createModel(IStorage)
   */
  public BaseTapestryModel createModel(IStorage storage) {
  	TapestryApplicationModel model = new TapestryApplicationModel(storage);
  	try {
      model.load();
    } catch (CoreException e) {
    }
    return model;
  }
  
  /**
   * @see com.iw.plugins.spindle.model.manager.ITapestryModelManagerDelegate#registerParserFor(String)
   */
  public void registerParserFor(String extension) {  	
  	TapestryPlugin.getTapestryModelManager().registerParser(extension, TapestryPlugin.getParser());
  }

}
