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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.groovy;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.apache.tapestry.bean.AbstractBeanInitializer;
import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.spec.IBeanSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;

import com.iw.plugins.spindle.core.extensions.IBeanSpecificationValidator;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.core.spec.bean.PluginMessageBeanInitializer;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.core.util.SpindleStatusWithLocation;

/**
 * Validates that a ScriptBean is ok.
 * 
 * @author glongman@gmail.com
 *  
 */
public class ScriptBeanValidator implements IBeanSpecificationValidator
{

  public boolean canValidate(IBeanSpecification bean)
  {
    if ("org.apache.tapestry.contrib.script.ScriptBean".equals(bean.getClassName()))
      return true;
    return false;
  }
  public IStatus validate(IBeanSpecification bean)
  {

    ISourceLocationInfo beanInfo = (ISourceLocationInfo) bean.getLocation();
    SpindleStatusWithLocation status = new SpindleStatusWithLocation(beanInfo
        .getTagNameLocation());
    if (!canValidate(bean))
    {
      status
          .setError("expected bean class: 'org.apache.tapestry.contrib.script.ScriptBean'");
    }

    List initializers = bean.getInitializers();
    IBeanInitializer scriptProperty = null;
    if (initializers != null)
    {
      for (Iterator iter = initializers.iterator(); iter.hasNext();)
      {
        IBeanInitializer initializer = (IBeanInitializer) iter.next();
        if ("script".equals(initializer.getPropertyName()))
        {
          scriptProperty = initializer;
          break;
        }
      }
    }
    if (scriptProperty == null)
    {
      status.setError("required property 'script' is not set");
      return status;
    }

    ISourceLocationInfo scriptInfo = (ISourceLocationInfo) scriptProperty.getLocation();

    if (scriptProperty instanceof PluginMessageBeanInitializer)
    {
      status.setWarning("Spindle can't resolve <set-message>", scriptInfo
          .getTagNameLocation());
      return status;
    }

    PluginExpressionBeanInitializer expressionBeanInitializer = (PluginExpressionBeanInitializer) scriptProperty;

    String expression = expressionBeanInitializer.getExpression();

    if (expression == null || expression.trim().length() == 0)
    {
      status.setError("no value set for property 'script'", scriptInfo
          .getAttributeSourceLocation("name"));
      return status;
    }

    ISourceLocation expressionLocation = scriptInfo
        .getAttributeSourceLocation("expression");
    expressionLocation = expressionLocation == null ? scriptInfo
        .getContentSourceLocation() : expressionLocation;

    IStatus expressionStatus = validateExpression(expression);
    if (!expressionStatus.isOK())
    {
      return new SpindleStatusWithLocation(expressionLocation, expressionStatus
          .getSeverity(), expressionStatus.getMessage());
    }

    String scriptName = null;
    try
    {
      scriptName = (String) Ognl
          .getValue(expression, Collections.EMPTY_MAP, new Object());
    } catch (OgnlException e)
    {
      status.setError(
          "Spindle could not evaluate value for 'script': " + e.getMessage(),
          expressionLocation);
      return status;
    }

    if (scriptName == null)
    {
      status.setWarning("Spindle can't resolve the value for 'script'");
      return status;
    }

    IResourceWorkspaceLocation base = (IResourceWorkspaceLocation) bean
        .getLocation()
        .getResourceLocation();
    IResourceWorkspaceLocation script = (IResourceWorkspaceLocation) base
        .getRelativeLocation(scriptName + ".groovy");

    IStorage storage = script.getStorage();

    if (storage == null)
      status.setError(script.toString() + " does not exist.", expressionLocation);

    return status;
  }

  /**
   * @param expression
   * @return
   */
  private IStatus validateExpression(String expression)
  {
    SpindleStatus status = new SpindleStatus();
    Object parsed = null;
    try
    {
      Ognl.parseExpression(expression);

      if (!Ognl.isConstant(expression))
      {
        status
            .setWarning("Spindle could not evaluate value for 'script': the ognl expression must be constant");
        return status;
      }
    } catch (OgnlException e)
    {
      status.setError("Spindle could not evaluate value for 'script': " + e.getMessage());
      return status;
    }

    return status;
  }
}