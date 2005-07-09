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

package com.iw.plugins.spindle.core.builder;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.eclipse.core.resources.IResource;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.PageSpecificationResolver;
import com.iw.plugins.spindle.core.parser.template.TagEventInfo;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.core.util.eclipse.EclipseUtils;
import com.iw.plugins.spindle.core.util.eclipse.Markers;
import com.iw.plugins.spindle.messages.DefaultTapestryMessages;

/**
 * Additional validation for Framework components. These are recorded in the Tapestry builder as
 * IBuildActions. The TapestryBuilder will execute them last! This is because some validations here
 * involve pages and pages are not resolved by the builder until after all of the components are. In
 * the scope of Picasso, this class does not build and has been left ths way on purpose.
 * 
 * @deprecated - to be replaced
 * @author glongman@gmail.com
 */
public class FrameworkComponentValidator
{

    public static void validateContainedComponent(ICoreResource putErrorsHere,
            INamespace requestorNamespace, String frameworkComponentName,
            IComponentSpecification frameworkComponentSpecification, IContainedComponent contained,
            Object sourceInfo, String publicId)
    {

        IResource putProblemsResource = EclipseUtils.toResource(putErrorsHere);
        if (putProblemsResource == null)
            return;

        PluginComponentSpecification frameworkSpec = (PluginComponentSpecification) frameworkComponentSpecification;
        // check to see if we are really talking about a framework component.
        if (DefaultTapestryMessages.format("Namespace.framework-namespace").equals(
                frameworkSpec.getNamespace().getNamespaceId()))
        {
            if ("PageLink".equals(frameworkComponentName))
            {
                AbstractBuildInfrastructure.fDeferredActions.add(new PageLinkComponentValidation(
                        putProblemsResource, (ICoreNamespace) requestorNamespace, contained,
                        sourceInfo, publicId));

            }
            else if ("Script".equals(frameworkComponentName))
            {

                // if (putErrorsHere.getName().endsWith("html")) {
                // // its an specless page, need to get the Tapestry generated spec
                // // in order to have valid location to base the script file lookup on.
                // IProject project = putProblemsResource.getProject();
                // TapestryArtifactManager manager =
                // TapestryArtifactManager.getTapestryArtifactManager();
                // Map specMap = manager.getSpecMap(project);
                // if (specMap == null)
                // return;
                // BaseSpecLocatable tapestryGeneratedSpec =
                // (BaseSpecLocatable)specMap.get(putProblemsResource);
                // if (tapestryGeneratedSpec == null)
                // return;
                // putErrorsHere = (ICoreResource)
                // tapestryGeneratedSpec.getSpecificationLocation();
                // }

                AbstractBuildInfrastructure.fDeferredActions.add(new ScriptComponentValidation(
                        putErrorsHere, putProblemsResource, (ICoreNamespace) requestorNamespace,
                        contained, sourceInfo, publicId));
            }
        }

    }

    public static void validateImplictComponent(ICoreResource specificationLocation,
            ICoreResource putErrorsHere, INamespace requestorNamespace,
            String frameworkComponentName, IComponentSpecification frameworkComponentSpecification,
            IContainedComponent contained, Object sourceInfo, String publicId)
    {
        if ("PageLink".equals(frameworkComponentName))
        {
            validateContainedComponent(
                    putErrorsHere,
                    requestorNamespace,
                    frameworkComponentName,
                    frameworkComponentSpecification,
                    contained,
                    sourceInfo,
                    publicId);
        }
        else
        {

            IResource putProblemsResource = EclipseUtils.toResource(putErrorsHere);
            if (putProblemsResource == null)
                return;

            PluginComponentSpecification frameworkSpec = (PluginComponentSpecification) frameworkComponentSpecification;
            // check to see if we are really talking about a framework component.
            if (DefaultTapestryMessages.format("Namespace.framework-namespace").equals(
                    frameworkSpec.getNamespace().getNamespaceId()))
            {
                if ("Script".equals(frameworkComponentName))
                {

                    // if (putErrorsHere.getName().endsWith("html")) {
                    // // its an specless page, need to get the Tapestry generated spec
                    // // in order to have valid location to base the script file lookup
                    // on.
                    // IProject project = putProblemsResource.getProject();
                    // TapestryArtifactManager manager =
                    // TapestryArtifactManager.getTapestryArtifactManager();
                    // Map specMap = manager.getSpecMap(project);
                    // if (specMap == null)
                    // return;
                    // BaseSpecLocatable tapestryGeneratedSpec =
                    // (BaseSpecLocatable)specMap.get(putProblemsResource);
                    // if (tapestryGeneratedSpec == null)
                    // return;
                    // putErrorsHere = (ICoreResource)
                    // tapestryGeneratedSpec.getSpecificationLocation();
                    // }

                    AbstractBuildInfrastructure.fDeferredActions.add(new ScriptComponentValidation(
                            specificationLocation, putProblemsResource,
                            (ICoreNamespace) requestorNamespace, contained, sourceInfo, publicId));
                }
            }
        }

    }

    abstract static class BaseAction implements IBuildAction
    {
        IResource fPutProblemsHere;

        ICoreNamespace fReqNamespace;

        IContainedComponent fContainedComponent;

        Object fSourceInfo;

        String fPublicId;

        public BaseAction(IResource putProblemsHere, ICoreNamespace requestorNamespace,
                IContainedComponent contained, Object sourceInfo, String publicId)
        {
            fPutProblemsHere = putProblemsHere;
            fReqNamespace = requestorNamespace;
            fContainedComponent = contained;
            fSourceInfo = sourceInfo;
            fPublicId = publicId;

        }

        protected boolean isTemplate()
        {
            return fSourceInfo instanceof TagEventInfo;
        }

        protected ISourceLocation getAttributeSourceLocation(String attrName)
        {
            if (isTemplate())
                return (ISourceLocation) ((TagEventInfo) fSourceInfo).getAttributeMap().get(
                        attrName);

            return ((ISourceLocationInfo) fSourceInfo).getAttributeSourceLocation(attrName);
        }

    }

    static class PageLinkComponentValidation extends BaseAction implements IBuildAction
    {

        public PageLinkComponentValidation(IResource putProblemsHere,
                ICoreNamespace requestorNamespace, IContainedComponent contained,
                Object sourceInfo, String publicId)
        {
            super(putProblemsHere, requestorNamespace, contained, sourceInfo, publicId);
        }

        public void run()
        {
            // look for static bindings for the 'page' parameter
            IBindingSpecification pageBinding = fContainedComponent.getBinding("page");
            if (pageBinding != null)// && pageBinding.getType() == BindingType.STATIC)
            {
                String value = pageBinding.getValue();
                if (value != null)
                {
                    PageSpecificationResolver resolver = fReqNamespace.getPageResolver();
                    IComponentSpecification pageSpec = resolver.resolve(pageBinding.getValue());
                    ISourceLocation location;
                    if (isTemplate())
                    {
                        location = getAttributeSourceLocation("page");
                    }
                    else
                    {
                        ISourceLocationInfo bindingInfo = (ISourceLocationInfo) pageBinding
                                .getLocation();
                        location = bindingInfo.getAttributeSourceLocation("value");
                    }
                    if (pageSpec == null)
                    {
                        String namespaceId = fReqNamespace.getNamespaceId();
                        if (fReqNamespace.isApplicationNamespace())
                            namespaceId = "application namespace";
                        if (location != null)
                        {

                            Markers.addTapestryProblemMarkerToResource(
                                    fPutProblemsHere,
                                    new DefaultProblem(IProblem.ERROR, DefaultTapestryMessages
                                            .format("Namespace.no-such-page", value, namespaceId),
                                            location, true, IProblem.TAP_NAMESPACE_NO_SUCH_PAGE));
                        }
                        else
                        {
                            StringWriter swriter = new StringWriter();
                            PrintWriter pwriter = new PrintWriter(swriter);
                            pwriter.println("FCV - page - no location");
                            pwriter.println(DefaultTapestryMessages.format(
                                    "Namespace.no-such-page",
                                    value,
                                    namespaceId));
                            pwriter.println(fPutProblemsHere == null ? "null" : fPutProblemsHere
                                    .toString());
                            pwriter.println();
                            try
                            {
                                XMLUtil.writeBinding("page", pageBinding, pwriter, 0, fPublicId);
                            }
                            catch (RuntimeException e)
                            {
                                // do nothing
                            }
                            TapestryCore.log(swriter.toString());
                        }
                    }
                }
            }
        }
    }

    static class ScriptComponentValidation extends BaseAction implements IBuildAction
    {
        ICoreResource scriptOwnerLocation;

        public ScriptComponentValidation(ICoreResource scriptOwnerLocation,
                IResource putProblemsHere, ICoreNamespace requestorNamespace,
                IContainedComponent contained, Object sourceInfo, String publicId)
        {
            super(putProblemsHere, requestorNamespace, contained, sourceInfo, publicId);
            this.scriptOwnerLocation = scriptOwnerLocation;
        }

        public void run()
        {
            // look for static bindings for the 'script' parameter
            IBindingSpecification scriptBinding = fContainedComponent.getBinding("script");
            if (scriptBinding != null)// && scriptBinding.getType() == BindingType.STATIC)
            {
                String value = scriptBinding.getValue();
                if (value != null && value.trim().length() > 0
                        && !value.startsWith(BaseValidator.DefaultDummyString))
                {
                    ICoreResource scriptLocation = (ICoreResource) scriptOwnerLocation
                            .getRelativeResource(value);
                    if (scriptLocation.exists())
                    {
                        ISourceLocation location;
                        if (isTemplate())
                        {
                            location = getAttributeSourceLocation("script");
                        }
                        else
                        {
                            ISourceLocationInfo bindingInfo = (ISourceLocationInfo) scriptBinding
                                    .getLocation();
                            location = bindingInfo.getAttributeSourceLocation("value");
                            if (location == null)
                                location = bindingInfo.getContentSourceLocation();
                        }

                        if (location != null)
                        {

                            Markers.addTapestryProblemMarkerToResource(
                                    fPutProblemsHere,
                                    new DefaultProblem(IProblem.ERROR, "Unable to find script: "
                                            + scriptLocation.toString(), location, true,
                                            IProblem.SPINDLE_MISSING_SCRIPT));
                        }
                        else
                        {
                            StringWriter swriter = new StringWriter();
                            PrintWriter pwriter = new PrintWriter(swriter);
                            pwriter.println("FCV - script - no location");
                            pwriter.println("Unable to find script: " + scriptLocation.toString());
                            pwriter.println(fPutProblemsHere == null ? "null" : fPutProblemsHere
                                    .toString());
                            pwriter.println();
                            try
                            {
                                XMLUtil
                                        .writeBinding(
                                                "script",
                                                scriptBinding,
                                                pwriter,
                                                0,
                                                fPublicId);
                            }
                            catch (RuntimeException e)
                            {
                                // do nothing
                            }
                            TapestryCore.log(swriter.toString());
                        }
                    }

                }
            }

        }

    }

}