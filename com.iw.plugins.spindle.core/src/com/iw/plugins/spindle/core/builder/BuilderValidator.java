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

package com.iw.plugins.spindle.core.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  A validator that knows about the project
 *  <p>
 *  i.e. it can resolve type names in the project buildpath
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BuilderValidator extends BaseValidator
{
    Build fBuild;
    ICoreNamespace fFramework;

    public BuilderValidator(Build build)
    {
        super();
        this.fBuild = build;
    }

    public BuilderValidator(Build build, ICoreNamespace framework)
    {
        this(build);
        Assert.isNotNull(framework);
        this.fFramework = framework;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.BaseValidator#findType(java.lang.String)
     */
    protected Object findType(String fullyQualifiedName)
    {
        return fBuild.fTapestryBuilder.getType(fullyQualifiedName);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.spec.IContainedComponent, com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
     */
    public boolean validateContainedComponent(
        IComponentSpecification specification,
        IContainedComponent component,
        ISourceLocationInfo info)
        throws ScannerException
    {
        ICoreNamespace use_namespace = (ICoreNamespace) ((PluginComponentSpecification) specification).getNamespace();
        String type = component.getType();

        if (TapestryCore.isNull(type))
            // already caught by the scanner
            return true;

        if (type.startsWith(getDummyStringPrefix()))
            return true;

        if (use_namespace == null)
        {
            reportProblem(
                IProblem.ERROR,
                info.getAttributeSourceLocation("type"),
                TapestryCore.getTapestryString("Namespace.no-such-component-type", type, "unknown"));

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
                ICoreNamespace sub_namespace = (ICoreNamespace) use_namespace.getChildNamespace(namespaceId);
                if (sub_namespace == null)
                {
                    reportProblem(
                        IProblem.ERROR,
                        info.getAttributeSourceLocation("type"),
                        "Unable to resolve "
                            + TapestryCore.getTapestryString("Namespace.nested-namespace", namespaceId));

                }

            } else
            {

                reportProblem(
                    IProblem.ERROR,
                    info.getAttributeSourceLocation("type"),
                    TapestryCore.getTapestryString(
                        "Namespace.no-such-component-type",
                        type,
                        use_namespace.getNamespaceId()));

            }
            return false;
        }
        validateContainedComponentBindings(specification, containedSpecification, component, info);

        return true;
    }

    protected void validateContainedComponentBindings(
        IComponentSpecification containerSpecification,
        IComponentSpecification containedSpecification,
        IContainedComponent component,
        ISourceLocationInfo sourceInfo)
        throws ScannerException
    {
        Collection bindingNames = component.getBindingNames();
        List required = findRequiredParameterNames(containedSpecification);
        required.removeAll(bindingNames);
        if (!required.isEmpty())
        {
            reportProblem(
                IProblem.ERROR,
                sourceInfo.getStartTagSourceLocation(),
                TapestryCore.getTapestryString(
                    "PageLoader.required-parameter-not-bound",
                    required.toString(),
                    containedSpecification.getSpecificationLocation().getName()));
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

            ISourceLocationInfo bindingSrcInfo = (ISourceLocationInfo)bspec.getLocation();
            ISourceLocation location =
                name.startsWith(getDummyStringPrefix())
                    ? bindingSrcInfo.getStartTagSourceLocation()
                    : bindingSrcInfo.getAttributeSourceLocation("name");

            name = name.startsWith(getDummyStringPrefix()) ? "'name not found'" : name;


            // If not allowing informal parameters, check that each binding matches
            // a formal parameter.

            if (formalOnly && !isFormal)
            {
                reportProblem(
                    IProblem.ERROR,
                    location,
                    TapestryCore.getTapestryString("PageLoader.formal-parameters-only", containedName, name));

                continue;
            }

            // If an informal parameter that conflicts with a reserved name, then
            // skip it.

            if (!isFormal && containedSpecification.isReservedParameterName(name))
            {

                reportProblem(
                    IProblem.WARNING,
                    location,
                    "ignoring binding '" + name + "'. trying to bind to reserved parameter.");

                continue;
            }

        }

    }

    private List findRequiredParameterNames(IComponentSpecification spec)
    {
        List result = new ArrayList();
        for (Iterator iter = spec.getParameterNames().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IParameterSpecification pspec = spec.getParameter(name);
            if (pspec.isRequired())
                result.add(name);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateAsset(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.spec.IAssetSpecification, com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
     */
    public boolean validateAsset(
        IComponentSpecification specification,
        IAssetSpecification asset,
        ISourceLocationInfo sourceLocation)
        throws ScannerException
    {

        PluginAssetSpecification pAsset = (PluginAssetSpecification) asset;
        IResourceWorkspaceLocation specLoc = (IResourceWorkspaceLocation) specification.getSpecificationLocation();
        String assetPath = asset.getPath();
        if (ITemplateSource.TEMPLATE_ASSET_NAME.equals(pAsset.getIdentifier()))
        {
            return checkTemplateAsset(specification, asset);
        }
        AssetType type = asset.getType();
        IResourceWorkspaceLocation root = null;
        if (type == AssetType.CONTEXT)
        {
            root = fBuild.fTapestryBuilder.fContextRoot;
        } else if (type == AssetType.PRIVATE)
        {
            if (specLoc.isOnClasspath())
            {
                root = specLoc;
            } else
            {
                root = fBuild.fTapestryBuilder.fClasspathRoot;
            }
        }

        if (root == null)
            return true;

        IResourceWorkspaceLocation relative = (IResourceWorkspaceLocation) root.getRelativeLocation(assetPath);

        if (!relative.exists())
        {
            // find the attribute source location for the error
            ISourceLocation errorLoc;
            if (type == AssetType.CONTEXT)
            {
                errorLoc = sourceLocation.getAttributeSourceLocation("path");
            } else
            {
                errorLoc = sourceLocation.getAttributeSourceLocation("resource-path");
            }

            String name = pAsset.getIdentifier();

            reportProblem(
                IProblem.ERROR,
                errorLoc,
                TapestryCore.getString(
                    "scan-component-missing-asset",
                    name.startsWith(getDummyStringPrefix()) ? "not specified" : name,
                    relative.toString()));

            return false;
        }

        return true;
    }

    private boolean checkTemplateAsset(IComponentSpecification specification, IAssetSpecification templateAsset)
        throws ScannerException
    {
        AssetType type = templateAsset.getType();
        String templatePath = templateAsset.getPath();

        IResourceWorkspaceLocation templateLocation;
        if (type == AssetType.EXTERNAL)
        {
            reportProblem(
                IProblem.WARNING,
                ((ISourceLocationInfo) templateAsset.getLocation()).getStartTagSourceLocation(),
                "Spindle can't resolve templates from external assets");
            return false;
        }
        if (type == AssetType.CONTEXT)
        {
            IResourceWorkspaceLocation context = fBuild.fTapestryBuilder.fContextRoot;
            if (fBuild.fTapestryBuilder.fTapestryProject.getProjectType() != TapestryProject.APPLICATION_PROJECT_TYPE)
            {
                reportProblem(
                    IProblem.WARNING,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getStartTagSourceLocation(),
                    "Spindle can't resolve templates from context assets in Library projects");
                return false;
            }

            templateLocation = (IResourceWorkspaceLocation) context.getRelativeLocation(templatePath);

            if (templateLocation == null || !templateLocation.exists())
            {
                reportProblem(
                    IProblem.ERROR,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getAttributeSourceLocation("path"),
                    TapestryCore.getTapestryString("DefaultTemplateSource.unable-to-read-template", templatePath));
                return false;
            }
        }
        if (type == AssetType.PRIVATE)
        {
            IResourceWorkspaceLocation classpath = fBuild.fTapestryBuilder.fClasspathRoot;
            templateLocation = (IResourceWorkspaceLocation) classpath.getRelativeLocation(templatePath);
            if (templateLocation == null || !templateLocation.exists())
            {
                reportProblem(
                    IProblem.ERROR,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getAttributeSourceLocation("resource-path"),
                    TapestryCore.getTapestryString("DefaultTemplateSource.unable-to-read-template", templatePath));
                return false;
            }
        }
        return true;
    }


    public boolean validateLibraryResourceLocation(String path, String errorKey, ISourceLocation source) throws ScannerException
    {
        // TODO Auto-generated method stub
        return validateResourceLocation(fBuild.fTapestryBuilder.fClasspathRoot, path, errorKey, source);
    }

}
