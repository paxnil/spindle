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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.spec;

import org.apache.tapestry.spec.IApplicationSpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * Spindle implementation of IApplicationSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: PluginApplicationSpecification.java,v 1.3 2004/05/17 02:31:49
 *          glongman Exp $
 */
public class PluginApplicationSpecification extends PluginLibrarySpecification
    implements
      IApplicationSpecification
{
  private String fName;
  private String fEngineClassName;

  public PluginApplicationSpecification()
  {
    super(BaseSpecification.APPLICATION_SPEC);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IApplicationSpecification#getName()
   */
  public String getName()
  {
    return fName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IApplicationSpecification#setEngineClassName(java.lang.String)
   */
  public void setEngineClassName(String value)
  {
    this.fEngineClassName = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IApplicationSpecification#getEngineClassName()
   */
  public String getEngineClassName()
  {
    return fEngineClassName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IApplicationSpecification#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.fName = name;
  }

  public void validateSelf(IScannerValidator validator) 
  {
    ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

    if (fEngineClassName != null)
    {
      try
      {
        validator.validateTypeName(
            (IResourceWorkspaceLocation) getSpecificationLocation(),
            fEngineClassName,
            IProblem.ERROR,
            sourceInfo.getAttributeSourceLocation("engine-class"));

      } catch (ScannerException e)
      {
        TapestryCore.log(e);
        e.printStackTrace();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.spec.PluginLibrarySpecification#validate(com.iw.plugins.spindle.core.scanning.IScannerValidator)
   */
  public void validate(IScannerValidator validator)
  {
    validateSelf(validator);
    super.validate(validator);
  }

}