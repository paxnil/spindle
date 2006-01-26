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

package net.sf.spindle.core.spec;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.hivemind.Location;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * Record <service>tags in a document
 * 
 * @deprecated ??
 * @author glongman@gmail.com
 */
public class PluginEngineServiceDeclaration extends DescribableSpecification
{

  String fName;
  String fServiceClass;

  public PluginEngineServiceDeclaration(String name, String serviceClass,
      Location location)
  {
    super(BaseSpecification.ENGINE_SERVICE_DECLARATION);
    fName = name;
    fServiceClass = serviceClass;
    setLocation(location);
  }

  public String getIdentifier()
  {
    return getName();
  }

  public String getName()
  {
    return fName;
  }

  public String getServiceClass()
  {
    return fServiceClass;
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

      validator.validateTypeName((ICoreResource) parentLib
          .getSpecificationLocation(), fServiceClass, IProblem.ERROR, info
          .getAttributeSourceLocation("class"));

    } catch (ScannerException e)
    {
      TapestryCore.log(e);
    }

  }

}