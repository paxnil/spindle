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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.builder.FrameworkComponentValidator;
import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.I18NResourceAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * A completely functional validator for scanning Tapestry specs.
 * 
 * @author glongman@intelligentworks.com
 */
public class SpecificationValidator extends BaseValidator
{
  TapestryProject fTapestryProject;

  ContextRootLocation fContextRoot;

  ClasspathRootLocation fClasspathRoot;

  boolean fPeformDeferredValidations = true;

  TypeFinder fTypeFinder;

  public SpecificationValidator(TapestryProject project,
      boolean performDeferredValidations) throws CoreException
  {
    Assert.isNotNull(project);
    fTapestryProject = project;
    fContextRoot = fTapestryProject.getWebContextLocation();
    fClasspathRoot = fTapestryProject.getClasspathRoot();
    Assert.isNotNull(fContextRoot);
    Assert.isNotNull(fClasspathRoot);
  }

  public TypeFinder getTypeFinder() throws CoreException
  {
    if (fTypeFinder == null)
      fTypeFinder = new TypeFinder(fTapestryProject.getJavaProject());

    return fTypeFinder;
  }

  public void setTypeFinder(TypeFinder finder)
  {
    fTypeFinder = finder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.scanning.BaseValidator#findType(java.lang.String)
   */
  public IType findType(IResourceWorkspaceLocation dependant, String fullyQualifiedName)
  {
    IType result = null;
    try
    {
      result = getTypeFinder().findType(fullyQualifiedName);
    } catch (CoreException e)
    {
      TapestryCore.log(e);
    }
    fireTypeDependency(dependant, fullyQualifiedName, result);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification,
   *      org.apache.tapestry.spec.IContainedComponent,
   *      com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
   */
  public boolean validateContainedComponent(
      IComponentSpecification specification,
      IContainedComponent component,
      ISourceLocationInfo info) throws ScannerException
  {
    ICoreNamespace use_namespace = (ICoreNamespace) ((PluginComponentSpecification) specification)
        .getNamespace();
    String type = component.getType();

    if (TapestryCore.isNull(type))
      // already caught by the scanner
      return true;

    if (type.startsWith(getDummyStringPrefix()))
      return true;

    if (use_namespace == null)
    {
      addProblem(IProblem.ERROR, info.getAttributeSourceLocation("type"), TapestryCore
          .getTapestryString("Namespace.no-such-component-type", type, "unknown"), true);

      return false;
    }

    ComponentSpecificationResolver resolver = use_namespace.getComponentResolver();
    IComponentSpecification containedSpecification = resolver.resolve(type);

    if (containedSpecification == null)
    {
      int colonx = type.indexOf(':');
      String namespaceId = null;

      if (colonx > 0)
      {
        namespaceId = type.substring(0, colonx);
        type = type.substring(colonx + 1);
      }

      if (!TapestryCore.isNull(namespaceId))
      {
        ICoreNamespace sub_namespace = (ICoreNamespace) use_namespace
            .getChildNamespace(namespaceId);
        if (sub_namespace == null)
        {
          addProblem(
              IProblem.ERROR,
              info.getAttributeSourceLocation("type"),
              "Unable to resolve "
                  + TapestryCore.getTapestryString(
                      "Namespace.nested-namespace",
                      namespaceId),
              true);

        } else
        {
          addProblem(
              IProblem.ERROR,
              info.getAttributeSourceLocation("type"),
              TapestryCore.getTapestryString(
                  "Namespace.no-such-component-type",
                  type,
                  namespaceId),
              true);

        }

      } else
      {

        addProblem(IProblem.ERROR, info.getAttributeSourceLocation("type"), TapestryCore
            .getTapestryString("Namespace.no-such-component-type", type, use_namespace
                .getNamespaceId()), true);

      }
      return false;
    }
    validateContainedComponentBindings(
        specification,
        containedSpecification,
        component,
        info);

    // if the contained is a framework component, extra validation might
    // occur
    // at the end of the
    // entire build!
    FrameworkComponentValidator.validateContainedComponent(
        (IResourceWorkspaceLocation) specification.getSpecificationLocation(),
        ((PluginComponentSpecification) specification).getNamespace(),
        type,
        containedSpecification,
        component,
        info,
        containedSpecification.getPublicId());

    return true;
  }

  protected void validateContainedComponentBindings(
      IComponentSpecification containerSpecification,
      IComponentSpecification containedSpecification,
      IContainedComponent component,
      ISourceLocationInfo sourceInfo) throws ScannerException
  {
    Collection bindingNames = component.getBindingNames();
    List required = findRequiredParameterNames(containedSpecification);
    required.removeAll(bindingNames);
    if (!required.isEmpty())
    {
      addProblem(
          IProblem.ERROR,
          sourceInfo.getTagNameLocation(),
          TapestryCore.getTapestryString(
              "PageLoader.required-parameter-not-bound",
              required.toString(),
              containedSpecification.getSpecificationLocation().getName()),
          true);
    }

    boolean formalOnly = !containedSpecification.getAllowInformalParameters();

    boolean containerFormalOnly = !containerSpecification.getAllowInformalParameters();

    String containerName = containerSpecification.getSpecificationLocation().getName();
    String containedName = containedSpecification.getSpecificationLocation().getName();

    //        if (contained.getInheritInformalParameters())
    //        {
    //            if (formalOnly)
    //            {
    //
    //                reportProblem(
    //                    IProblem.ERROR,
    //                    location,
    //                    TapestryCore.getTapestryString(
    //                        "PageLoader.inherit-informal-invalid-component-formal-only",
    //                        containedName));
    //                return false;
    //            }
    //
    //            if (containerFormalOnly)
    //            {
    //                reportProblem(
    //                    IProblem.ERROR,
    //                    location,
    //                    TapestryCore.getTapestryString(
    //                        "PageLoader.inherit-informal-invalid-container-formal-only",
    //                        containerName,
    //                        containedName));
    //                return false;
    //            }
    //        }

    Iterator i = bindingNames.iterator();

    while (i.hasNext())
    {
      String name = (String) i.next();

      boolean isFormal = containedSpecification.getParameter(name) != null;

      IBindingSpecification bspec = component.getBinding(name);

      ISourceLocationInfo bindingSrcInfo = (ISourceLocationInfo) bspec.getLocation();
      ISourceLocation location = name.startsWith(getDummyStringPrefix()) ? bindingSrcInfo
          .getTagNameLocation() : bindingSrcInfo.getAttributeSourceLocation("name");

      name = name.startsWith(getDummyStringPrefix()) ? "'name not found'" : name;

      // If not allowing informal parameters, check that each binding
      // matches
      // a formal parameter.

      if (formalOnly && !isFormal)
      {
        addProblem(IProblem.ERROR, location, TapestryCore.getTapestryString(
            "PageLoader.formal-parameters-only",
            containedName,
            name), true);

        continue;
      }

      // If an informal parameter that conflicts with a reserved name,
      // then
      // skip it.

      if (!isFormal && containedSpecification.isReservedParameterName(name))
      {

        addProblem(IProblem.WARNING, location, "ignoring binding '" + name
            + "'. trying to bind to reserved parameter.", true);

        continue;
      }

    }

  }

  private List findRequiredParameterNames(IComponentSpecification spec)
  {
    //        List result = new ArrayList();
    //        for (Iterator iter = spec.getParameterNames().iterator();
    // iter.hasNext();)
    //        {
    //            String name = (String) iter.next();
    //            IParameterSpecification pspec = spec.getParameter(name);
    //            if (pspec.isRequired())
    //                result.add(name);
    //        }
    //        return result;
    ArrayList result = new ArrayList();
    result.addAll(((PluginComponentSpecification) spec).getRequiredParameterNames());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification,
   *      org.apache.tapestry.spec.IAssetSpecification,
   *      com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
   */
  public boolean validateAsset(
      IComponentSpecification specification,
      IAssetSpecification asset,
      ISourceLocationInfo sourceLocation) throws ScannerException
  {

    PluginAssetSpecification pAsset = (PluginAssetSpecification) asset;
    IResourceWorkspaceLocation specLoc = (IResourceWorkspaceLocation) specification
        .getSpecificationLocation();
    AssetType type = asset.getType();
    String assetPath = asset.getPath();
    String assetSpecName = pAsset.getIdentifier();

    IResourceWorkspaceLocation root = null;
    ISourceLocation errorLoc;
    if (type == AssetType.CONTEXT)
    {
      errorLoc = sourceLocation.getAttributeSourceLocation("path");
      root = fContextRoot;
    } else
    {
      errorLoc = sourceLocation.getAttributeSourceLocation("resource-path");
      if (specLoc.isOnClasspath())
      {
        root = specLoc;
      } else
      {
        root = fClasspathRoot;
      }
    }

    if (root == null)
      return true;

    if (errorLoc == null)
      errorLoc = sourceLocation.getTagNameLocation();

    if (assetPath == null && type != AssetType.EXTERNAL)
    {
      addProblem(IProblem.ERROR, errorLoc, TapestryCore.getString(
          "scan-component-missing-asset",
          (assetSpecName == null || assetSpecName.startsWith(getDummyStringPrefix()))
              ? "not specified" : assetSpecName,
          root.toString()), true);
      return false;
    }

    if (ITemplateSource.TEMPLATE_ASSET_NAME.equals(pAsset.getIdentifier()))
    {
      return checkTemplateAsset(specification, asset);
    }

    if (type == AssetType.EXTERNAL)
    {
      if (assetPath == null || assetPath.trim().length() == 0)
      {
        errorLoc = sourceLocation.getAttributeSourceLocation("URL");
        if (errorLoc == null)
          errorLoc = sourceLocation.getAttributeSourceLocation("url");
        addProblem(IProblem.ERROR, errorLoc, TapestryCore.getString(
            "scan-component-missing-external-url",
            assetSpecName.startsWith(getDummyStringPrefix())
                ? "not specified" : assetSpecName), true);
        return false;
      }
      
      return true;
    }

    IResourceWorkspaceLocation relative = (IResourceWorkspaceLocation) root
        .getRelativeLocation(assetPath);
    String fileName = relative.getName();

    if (relative.getStorage() == null)
    {
      IResourceWorkspaceLocation[] I18NEquivalents = getI18NAssetEquivalents(
          relative,
          fileName);

      if (I18NEquivalents.length > 0)
      {
        int handleI18NPriority = TapestryCore
            .getDefault()
            .getHandleAssetProblemPriority();
        if (handleI18NPriority >= 0)
        {
          addProblem(handleI18NPriority, errorLoc, TapestryCore.getString(
              "scan-component-missing-asset-but-has-i18n",
              assetSpecName.startsWith(getDummyStringPrefix())
                  ? "not specified" : assetSpecName,
              relative.toString()), true);
        }
      } else
      {
        addProblem(IProblem.ERROR, errorLoc, TapestryCore.getString(
            "scan-component-missing-asset",
            assetSpecName.startsWith(getDummyStringPrefix())
                ? "not specified" : assetSpecName,
            relative.toString()), true);

      }
      return false;
    }

    return true;
  }

  private I18NResourceAcceptor fI18NAcceptor = new I18NResourceAcceptor();

  private IResourceWorkspaceLocation[] getI18NAssetEquivalents(
      IResourceWorkspaceLocation baseLocation,
      String name)
  {
    try
    {
      fI18NAcceptor.configure(name);
      baseLocation.lookup(fI18NAcceptor);
      return fI18NAcceptor.getResults();
    } catch (CoreException e)
    {
      TapestryCore.log(e);
    }
    return new IResourceWorkspaceLocation[]{};
  }

  private boolean checkTemplateAsset(
      IComponentSpecification specification,
      IAssetSpecification templateAsset) throws ScannerException
  {
    AssetType type = templateAsset.getType();
    String templatePath = templateAsset.getPath();

    //set relative to the context by default!
    IResourceWorkspaceLocation templateLocation = (IResourceWorkspaceLocation) fContextRoot
        .getRelativeLocation(templatePath);
    ;
    if (type == AssetType.EXTERNAL)
    {
      addProblem(
          IProblem.WARNING,
          ((ISourceLocationInfo) templateAsset.getLocation()).getTagNameLocation(),
          "Spindle can't resolve templates from external assets",
          true);
      return false;
    }
    if (type == AssetType.CONTEXT)
    {
      if (fTapestryProject.getProjectType() != TapestryProject.APPLICATION_PROJECT_TYPE)
      {
        addProblem(
            IProblem.WARNING,
            ((ISourceLocationInfo) templateAsset.getLocation()).getTagNameLocation(),
            "Spindle can't resolve templates from context assets in Library projects",
            true);
        return false;
      }

      //            templateLocation = (IResourceWorkspaceLocation)
      // fContextRoot.getRelativeLocation(templatePath);

      //            if (templateLocation == null || !templateLocation.exists())
      //            {
      //                reportProblem(
      //                    IProblem.ERROR,
      //                    ((ISourceLocationInfo)
      // templateAsset.getLocation()).getAttributeSourceLocation("path"),
      //                    TapestryCore.getTapestryString("DefaultTemplateSource.unable-to-read-template",
      // templatePath));
      //                return false;
      //            }
    }
    if (type == AssetType.PRIVATE)
    {
      templateLocation = (IResourceWorkspaceLocation) fClasspathRoot
          .getRelativeLocation(templatePath);
      //            if (templateLocation == null || !templateLocation.exists())
      //            {
      //                reportProblem(
      //                    IProblem.ERROR,
      //                    ((ISourceLocationInfo)
      // templateAsset.getLocation()).getAttributeSourceLocation("resource-path"),
      //                    TapestryCore.getTapestryString("DefaultTemplateSource.unable-to-read-template",
      // templatePath));
      //                return false;
      //            }
    }

    if (templateLocation.getStorage() == null)
    {
      String fileName = templateLocation.getName();
      // find the attribute source location for the error
      ISourceLocationInfo sourceLocation = (ISourceLocationInfo) templateAsset
          .getLocation();
      ISourceLocation errorLoc;
      if (type == AssetType.CONTEXT)
      {
        errorLoc = sourceLocation.getAttributeSourceLocation("path");
      } else
      {
        errorLoc = sourceLocation.getAttributeSourceLocation("resource-path");
      }

      String assetSpecName = ((PluginAssetSpecification) templateAsset).getIdentifier();
      IResourceWorkspaceLocation[] I18NEquivalents = getI18NAssetEquivalents(
          templateLocation,
          fileName);

      if (I18NEquivalents.length > 0)
      {
        int handleI18NPriority = TapestryCore
            .getDefault()
            .getHandleAssetProblemPriority();
        if (handleI18NPriority >= 0)
        {
          addProblem(handleI18NPriority, errorLoc, TapestryCore.getString(
              "scan-component-missing-asset-but-has-i18n",
              assetSpecName.startsWith(getDummyStringPrefix())
                  ? "not specified" : assetSpecName,
              templateLocation.toString()), true);
        }
      } else
      {
        addProblem(IProblem.ERROR, errorLoc, TapestryCore.getString(
            "scan-component-missing-asset",
            assetSpecName.startsWith(getDummyStringPrefix())
                ? "not specified" : assetSpecName,
            templateLocation.toString()), true);

      }
      return false;
    }
    return true;
  }

  public boolean validateLibraryResourceLocation(
      IResourceLocation specLocation,
      String path,
      String errorKey,
      ISourceLocation source) throws ScannerException
  {
    IResourceWorkspaceLocation useLocation = (IResourceWorkspaceLocation) specLocation;

    if (!useLocation.isOnClasspath())
      useLocation = fClasspathRoot;

    return validateResourceLocation(useLocation, path, errorKey, source);
  }

  /**
   * 
   * A Utility class used to lookup types.
   * 
   * If this object is created during a build, the TapestryBuilder's type cache
   * is used.
   * 
   * If not, instances of this object will use thier own Map to cache.
   * 
   * Note, one the cache Map is set, you can't change it.
   * 
   * @author glongman@intelligentworks.com
   */
  public static class TypeFinder
  {
    IJavaProject project;

    Map cache;

    /**
     *  
     */
    public TypeFinder(IJavaProject project)
    {
      Assert.isNotNull(project);
      this.project = project;
      cache = TapestryBuilder.getTypeCache();
      if (cache == null)
        cache = new HashMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.BaseValidator#findType(java.lang.String)
     */
    public IType findType(String fullyQualifiedName)
    {
      IType result = null;

      if (cache.containsKey(fullyQualifiedName))
        return (IType) cache.get(fullyQualifiedName);

      try
      {
        result = project.getJavaProject().findType(fullyQualifiedName);
      } catch (CoreException e)
      {
        TapestryCore.log(e);
      }

      cache.put(fullyQualifiedName, result);

      return result;
    }
  }
}