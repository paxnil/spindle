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

package com.iw.plugins.spindle.core.scanning;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * Base Class for Node processors
 * <p>
 * Node Processors can find problems, but these problems do not represent a list
 * of *all* the problems with this document.
 * </p>
 * <p>
 * i.e.The Parser will hold problems for things like well-formedness and dtd
 * validation!
 * </p>
 * 
 * @author glongman@intelligentworks.com
 */
public abstract class AbstractScanner implements IProblemCollector
{

  protected IProblemCollector fExternalProblemCollector;
  protected List fProblems = new ArrayList();
  protected IScannerValidator fValidator;

  public Object scan(Object source, IScannerValidator validator) throws ScannerException
  {
    Assert.isNotNull(source);
    Object resultObject = null;
    beginCollecting();
    try
    {

      if (validator == null)
      {
        this.fValidator = new BaseValidator();
      } else
      {
        this.fValidator = validator;
      }
      this.fValidator.setProblemCollector(this);
      resultObject = beforeScan(source);
      if (resultObject == null)
        return null;

      doScan(source, resultObject);
      return afterScan(resultObject);

    } catch (ScannerException scex)
    {

      if (scex.getLocation() != null)
      {
        addProblem(IProblem.ERROR, scex.getLocation(), scex.getMessage(), scex
            .isTemporary());
      } else
      {
        addProblem(new DefaultProblem(
            ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
            IProblem.ERROR,
            scex.getMessage(),
            0,
            0,
            0,
            false));
      }
      return null;
    } catch (RuntimeException e)
    {
      TapestryCore.log(e);
      throw e;

    } finally
    {
      cleanup();
      endCollecting();
    }

  }
  protected abstract void doScan(Object source, Object resultObject) throws ScannerException;

  protected abstract Object beforeScan(Object source) throws ScannerException;

  protected abstract void cleanup();

  protected Object afterScan(Object scanResults) throws ScannerException
  {
    return scanResults;
  }

  public void beginCollecting()
  {
    if (fExternalProblemCollector != null)
      fExternalProblemCollector.beginCollecting();

    fProblems.clear();
  }

  public void endCollecting()
  {
    if (fExternalProblemCollector != null)
      fExternalProblemCollector.endCollecting();
  }

  public void addProblem(IProblem problem)
  {
    if (fExternalProblemCollector != null)
    {
      fExternalProblemCollector.addProblem(problem);
    } else
    {
      fProblems.add(problem);
    }
  }

  public void addProblem(
      int severity,
      ISourceLocation location,
      String message,
      boolean isTemporary)
  {
    addProblem(new DefaultProblem(
        ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
        severity,
        message,
        location.getLineNumber(),
        location.getCharStart(),
        location.getCharEnd(),
        isTemporary));
  }

  public void addProblem(IStatus status, ISourceLocation location, boolean isTemporary)
  {
    addProblem(new DefaultProblem(
        ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
        status,
        location.getLineNumber(),
        location.getCharStart(),
        location.getCharEnd(),
        isTemporary));
  }

  public void addProblems(IProblem[] problems)
  {
    if (problems != null)
      for (int i = 0; i < problems.length; i++)
      {
        addProblem(problems[i]);
      }
  }

  public IProblem[] getProblems()
  {
    if (fExternalProblemCollector != null)
      return fExternalProblemCollector.getProblems();
    return (IProblem[]) fProblems.toArray(new IProblem[fProblems.size()]);
  }

  public boolean isElement(Node node, String elementName)
  {
    return W3CAccess.isElement(node, elementName);
  }

  public String getValue(Node node)
  {
    return W3CAccess.getValue(node);
  }

  protected boolean isDummyString(String value)
  {
    if (value != null)
      return value.startsWith(fValidator.getDummyStringPrefix());

    return false;
  }

  protected String getAttribute(Node node, String attributeName)
  {
    return getAttribute(node, attributeName, false);
  }

  protected boolean getBooleanAttribute(Node node, String attributeName)
  {
    return W3CAccess.getBooleanAttribute(node, attributeName);
  }

  protected String getAttribute(Node node, String attributeName, boolean returnDummyIfNull)
  {
    String result = W3CAccess.getAttribute(node, attributeName);
    if (TapestryCore.isNull(result) && returnDummyIfNull)
      result = getNextDummyString();

    return result;
  }

  protected String getAttribute(
      Node node,
      String attributeName,
      boolean returnDummyIfNull,
      boolean warnIfNull)
  {
    String result = W3CAccess.getAttribute(node, attributeName);
    if (TapestryCore.isNull(result) && returnDummyIfNull)
    {
      result = getNextDummyString();
      if (warnIfNull)
        addProblem(
            IProblem.WARNING,
            getAttributeSourceLocation(node, attributeName),
            "warning, attribute value is null!",
            false);
    }

    return result;
  }

  protected ISourceLocationInfo getSourceLocationInfo(Node node)
  {
    return W3CAccess.getSourceLocationInfo(node);
  }

  protected ISourceLocation getBestGuessSourceLocation(Node node, boolean forNodeContent)
  {
    ISourceLocationInfo info = getSourceLocationInfo(node);

    if (info != null)
    {
      if (forNodeContent)
      {
        if (!info.isEmptyTag())
        {
          return info.getContentSourceLocation();
        } else
        {
          return info.getTagNameLocation();
        }
      } else
      {
        return info.getTagNameLocation();
      }
    }
    return null;
  }

  protected ISourceLocation getNodeStartSourceLocation(Node node)
  {
    ISourceLocationInfo info = getSourceLocationInfo(node);
    ISourceLocation result = null;
    if (info != null)
      result = info.getTagNameLocation();

    return result;
  }

  protected ISourceLocation getNodeEndSourceLocation(Node node)
  {
    ISourceLocationInfo info = getSourceLocationInfo(node);
    ISourceLocation result = null;
    if (info != null)
    {
      result = info.getEndTagSourceLocation();
      if (result == null)
      {
        result = info.getTagNameLocation();
      }
    }
    return result;
  }

  protected ISourceLocation getNodeBodySourceLocation(Node node)
  {
    ISourceLocationInfo info = getSourceLocationInfo(node);
    ISourceLocation result = null;

    if (info != null)
    {
      result = info.getContentSourceLocation();
    }
    return result;
  }

  protected ISourceLocation getAttributeSourceLocation(Node node, String rawname)
  {
    ISourceLocationInfo info = getSourceLocationInfo(node);
    ISourceLocation result = null;
    if (info != null)
    {
      result = info.getAttributeSourceLocation(rawname);
      if (result == null)
      {
        result = info.getTagNameLocation();
      }
    }
    return result;
  }

  protected boolean validatePattern(
      String value,
      String pattern,
      String errorKey,
      int severity) throws ScannerException
  {
    return fValidator.validatePattern(value, pattern, errorKey, severity);
  }

  protected boolean validatePattern(
      String value,
      String pattern,
      String errorKey,
      int severity,
      ISourceLocation location) throws ScannerException
  {
    return fValidator.validatePattern(value, pattern, errorKey, severity, location);
  }

  protected boolean validateExpression(String expression, int severity) throws ScannerException
  {
    return fValidator.validateExpression(expression, severity);
  }

  protected boolean validateExpression(
      String expression,
      int severity,
      ISourceLocation location) throws ScannerException
  {
    return fValidator.validateExpression(expression, severity, location);
  }

  protected IType validateTypeName(
      IResourceWorkspaceLocation dependant,
      String fullyQualifiedType,
      int severity) throws ScannerException
  {
    return fValidator.validateTypeName(dependant, fullyQualifiedType, severity);
  }

  protected IType validateTypeName(
      IResourceWorkspaceLocation dependant,
      String fullyQualifiedType,
      int severity,
      ISourceLocation location) throws ScannerException
  {
    return fValidator.validateTypeName(dependant, fullyQualifiedType, severity, location);
  }

  protected boolean validateLibraryResourceLocation(
      IResourceLocation specLocation,
      String path,
      String errorKey,
      ISourceLocation source) throws ScannerException
  {
    return fValidator.validateLibraryResourceLocation(
        specLocation,
        path,
        errorKey,
        source);

  }

  protected boolean validateResourceLocation(
      IResourceLocation location,
      String relativePath,
      String errorKey,
      ISourceLocation source) throws ScannerException
  {
    return fValidator.validateResourceLocation(location, relativePath, errorKey, source);

  }

  protected boolean validateContainedComponent(
      IComponentSpecification specification,
      IContainedComponent component,
      ISourceLocationInfo sourceLocation) throws ScannerException
  {
    return fValidator
        .validateContainedComponent(specification, component, sourceLocation);
  }

  protected boolean validateAsset(
      IComponentSpecification specification,
      IAssetSpecification asset,
      ISourceLocationInfo sourceLocation) throws ScannerException
  {
    return fValidator.validateAsset(specification, asset, sourceLocation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getNextDummyString()
   */
  protected String getNextDummyString()
  {
    return fValidator.getDummyStringPrefix();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#getDummyStringPrefix()
   */
  protected String getDummyStringPrefix()
  {
    return fValidator.getDummyStringPrefix();
  }

  /**
   * @return
   */
  public IProblemCollector getExternalProblemCollector()
  {
    return fExternalProblemCollector;
  }

  /**
   * @param collector
   */
  public void setExternalProblemCollector(IProblemCollector collector)
  {
    fExternalProblemCollector = collector;
  }

}