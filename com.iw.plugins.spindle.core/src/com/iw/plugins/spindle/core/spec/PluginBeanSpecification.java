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

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.IBeanSpecification;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.extensions.BeanSpecificationValidators;
import com.iw.plugins.spindle.core.extensions.IBeanSpecificationValidator;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * Spindle aware concrete implementation of IBeanSpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: PluginBeanSpecification.java,v 1.5 2004/05/17 02:31:49 glongman
 *                     Exp $
 */
public class PluginBeanSpecification extends BasePropertyHolder
    implements
      IBeanSpecification
{
  protected String fClassName;
  protected BeanLifecycle fLifecycle;

  /** @since 1.0.9 * */
  private String fDescription;

  /**
   * A List of {@link IBeanInitializer}.
   *  
   */

  protected List fInitializers;
  /**
   * @param type
   */
  public PluginBeanSpecification()
  {
    super(BaseSpecification.BEAN_SPEC);
  }

  public PluginBeanSpecification(String className, BeanLifecycle lifecycle)
  {
    this();
    fClassName = className;
    fLifecycle = lifecycle;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#getClassName()
   */
  public String getClassName()
  {
    return fClassName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#getLifecycle()
   */
  public BeanLifecycle getLifecycle()
  {
    return fLifecycle;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#addInitializer(org.apache.tapestry.bean.IBeanInitializer)
   */
  public void addInitializer(IBeanInitializer initializer)
  {
    if (fInitializers == null)
      fInitializers = new ArrayList();

    fInitializers.add(initializer);
  }

  public void removeInitializer(IBeanInitializer initializer)
  {
    remove(fInitializers, initializer);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#getInitializers()
   */
  public List getInitializers()
  {
    return fInitializers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#getDescription()
   */
  public String getDescription()
  {
    return fDescription;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#setDescription(java.lang.String)
   */
  public void setDescription(String desc)
  {
    fDescription = desc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#setClassName(java.lang.String)
   */
  public void setClassName(String className)
  {
    this.fClassName = className;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tapestry.spec.IBeanSpecification#setLifecycle(org.apache.tapestry.spec.BeanLifecycle)
   */
  public void setLifecycle(BeanLifecycle lifecycle)
  {
    fLifecycle = lifecycle;
  }

  public void validate(Object parent, IScannerValidator validator)
  {

    PluginComponentSpecification component = (PluginComponentSpecification) parent;

    ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

    try
    {
      IType type = validator.validateTypeName((IResourceWorkspaceLocation) component
          .getSpecificationLocation(), fClassName, IProblem.ERROR, (fClassName != null ? sourceInfo
          .getAttributeSourceLocation("class") : sourceInfo.getTagNameLocation()));

      if (type != null)
      {
        IBeanSpecificationValidator beanValidator = new BeanSpecificationValidators();
        if (beanValidator.canValidate(this))
        {
          IStatus status = beanValidator.validate(this);
          if (!status.isOK())
          {
            validator.addProblem(status, sourceInfo.getStartTagSourceLocation(), false);
          }
        }
      }
    } catch (ScannerException e)
    {
      TapestryCore.log(e);
      e.printStackTrace();
    }

  }

}