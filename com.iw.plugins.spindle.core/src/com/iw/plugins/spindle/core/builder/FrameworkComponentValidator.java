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

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.eclipse.core.resources.IResource;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.PageSpecificationResolver;
import com.iw.plugins.spindle.core.parser.DefaultProblem;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.parser.template.TagEventInfo;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Markers;
import com.iw.plugins.spindle.core.util.Utils;

/**
 *  Additional validation for Framework components.
 *  These are recorded in the Tapestry builder as IBuildActions.
 *  The TapestryBuilder will execute them last!
 * 
 *  This is because some validations here involve pages
 *  and pages are not resolved by the builder until after all
 *  of the components are.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class FrameworkComponentValidator
{

    public static void validate(
        IResourceWorkspaceLocation putErrorsHere,
        INamespace requestorNamespace,
        String frameworkComponentName,
        IComponentSpecification frameworkComponentSpecification,
        IContainedComponent contained,
        Object sourceInfo)
    {

        IResource putProblemsResource = Utils.toResource(putErrorsHere);
        if (putProblemsResource == null)
            return;

        PluginComponentSpecification frameworkSpec = (PluginComponentSpecification) frameworkComponentSpecification;
        //check to see if we are really talking about a framework component.
        if (TapestryCore.getTapestryString("Namespace.framework-namespace").equals(frameworkSpec.getNamespace().getNamespaceId()))
        {
            //TODO clean this up
            if ("PageLink".equals(frameworkComponentName))
            {
                TapestryBuilder.fDeferredActions.add(
                    new PageLinkComponentValidation(
                        putProblemsResource,
                        (ICoreNamespace) requestorNamespace,
                        contained,
                        sourceInfo));
            }
        }

    }

    abstract static class BaseAction implements IBuildAction
    {
        IResource fPutProblemsHere;
        ICoreNamespace fReqNamespace;
        IContainedComponent fContainedComponent;
        Object fSourceInfo;

        public BaseAction(
            IResource putPorblemsHere,
            ICoreNamespace requestorNamespace,
            IContainedComponent contained,
            Object sourceInfo)
        {
            fPutProblemsHere = putPorblemsHere;
            fReqNamespace = requestorNamespace;
            fContainedComponent = contained;
            fSourceInfo = sourceInfo;

        }
        
        protected boolean isTemplate() {
            return fSourceInfo instanceof TagEventInfo;
        }
        
        protected ISourceLocation getAttributeSourceLocation(String attrName) {
            if (isTemplate())
                return (ISourceLocation)((TagEventInfo)fSourceInfo).getAttributeMap().get(attrName);
                
            return ((ISourceLocationInfo)fSourceInfo).getAttributeSourceLocation(attrName);
        }

    }

    static class PageLinkComponentValidation extends BaseAction implements IBuildAction
    {

        public PageLinkComponentValidation(
            IResource putPorblemsHere,
            ICoreNamespace requestorNamespace,
            IContainedComponent contained,
            Object sourceInfo)
        {
            super(putPorblemsHere, requestorNamespace, contained, sourceInfo);
        }

        public void run()
        {
            //look for static bindings for the 'page' parameter
            IBindingSpecification pageBinding = fContainedComponent.getBinding("page");
            if (pageBinding != null && pageBinding.getType() == BindingType.STATIC)
            {
                String value = pageBinding.getValue();
                if (value != null)
                {
                    PageSpecificationResolver resolver = fReqNamespace.getPageResolver();
                    IComponentSpecification pageSpec = resolver.resolve(pageBinding.getValue());
                    ISourceLocation location;
                    if (isTemplate()) {                    
                        location = getAttributeSourceLocation("page");
                    } else {
                        ISourceLocationInfo bindingInfo = (ISourceLocationInfo)pageBinding.getLocation();
                        location = bindingInfo.getAttributeSourceLocation("value");
                    }
                    if (pageSpec == null)
                    {
                        String namespaceId = fReqNamespace.getNamespaceId();
                        if (fReqNamespace.isApplicationNamespace())
                            namespaceId = "application namespace";
                            
                        Markers.addTapestryProblemMarkerToResource(
                            fPutProblemsHere,
                            new DefaultProblem(
                                ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
                                IProblem.ERROR,
                                TapestryCore.getTapestryString("Namespace.no-such-page", value, namespaceId),
                                location.getLineNumber(),
                                location.getCharStart(),
                                location.getCharEnd()));
                    }
                }
            }

        }

    }

}
