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

import org.apache.hivemind.Location;
import org.apache.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * Record <component>tags in a document
 * 
 * @author glongman@gmail.com
 */
public class PluginComponentTypeDeclaration extends DescribableSpecification
{

  String fResourcePath;

  public PluginComponentTypeDeclaration(String id, String resourcePath, Location location)
  {
    super(BaseSpecification.COMPONENT_TYPE_DECLARATION);
    setIdentifier(id);
    fResourcePath = resourcePath;
    setLocation(location);
  }

  public String getId()
  {
    return getIdentifier();
  }

  public String getResourcePath()
  {
    return fResourcePath;
  }

  /**
   * Revalidate this declaration. Note that some validations, like duplicate
   * ids, are only possible during a parse/scan cycle. But that's ok 'cuz those
   * kinds of problems would have already been caught.
   * 
   * @param parent the object holding this
   * @param validator a validator helper
   */
  public void validate(Object parent, IScannerValidator validator)
  {
    ISourceLocationInfo info = (ISourceLocationInfo) getLocation();

    try
    {

      ILibrarySpecification parentLib = (ILibrarySpecification) parent;

      validator.validateResource(
          parentLib.getSpecificationLocation(),
          fResourcePath,
          "scan-library-missing-component",
          info.getAttributeSourceLocation("specification-path"));

    } catch (ScannerException e)
    {
      TapestryCore.log(e);
    }

  }

}