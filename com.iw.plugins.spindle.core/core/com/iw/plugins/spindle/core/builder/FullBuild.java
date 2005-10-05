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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.Resource;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.dom.IDOMModel;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.SourceLocation;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;

/**
 * Builds a Tapestry project from scratch.
 * 
 * @author glongman@gmail.com
 */
public class FullBuild extends AbstractBuild
{

    protected ServletInfo [] fApplicationServlets;

    protected WebAppDescriptor fWebAppDescriptor;

    /**
     * Constructor for FullBuilder.
     */
    public FullBuild(AbstractBuildInfrastructure infrastructure)
    {
        super(infrastructure);
    }

    /**
     * Use the parser to find declared applications in web.xml
     * 
     * @param parser
     * @throws CoreException
     */
    protected void preBuild()
    {
        setDependencyListener(new BuilderDependencyListener());
        findDeclaredApplications();
    }

    /**
     * Resolve the Tapesty framework namespace
     */
    protected void resolveFramework()
    {        
        new FrameworkResolver(this).resolve(fFrameworkNamespace);
    }
    
    protected void resolveApplication(String name, CoreNamespace namespace)
    {
        new ApplicationResolver(this, fFrameworkNamespace, name).resolve(namespace);        
    }
    
    protected void postBuild()
    {
        BuilderDependencyListener listener = (BuilderDependencyListener) getDependencyListener();
        if (fInfrastructure.DEBUG)
        {
            listener.dump();
        }
    }

    public void saveState()
    {
//        State newState = new State(fInfrastructure);
//        // newState.fLibraryLocation = fTapestryBuilder.fTapestryProject.getLibrarySpecPath();
//        newState.fLastKnownClasspath = fInfrastructure.getClasspathMemento();
//        newState.fJavaDependencies = fFoundTypes;
//        newState.fMissingJavaTypes = fMissingTypes;
//        newState.fTemplateMap = fTemplateMap;
//        newState.fFileSpecificationMap = fFileSpecificationMap;
//        newState.fBinarySpecificationMap = fBinarySpecificationMap;
//        newState.fSeenTemplateExtensions = fSeenTemplateExtensions;
//        
//        newState.fDeclatedTemplateExtensions = fDeclaredTemplateExtensions;
//        newState.fDeclaredTemplateExtensionsClasspath = fDeclaredTemplateExtensionsClasspath;
//        
//        newState.fApplicationServlets = fApplicationServlets;
//        newState.fWebAppDescriptor = fWebAppDescriptor;
//        newState.fPrimaryNamespace = fApplicationNamespace;
//        newState.fFrameworkNamespace = fFrameworkNamespace;
//        newState.fCleanTemplates = fCleanTemplates;
//
//        // save the processed binary libraries
//        saveBinaryLibraries(fLibraryNamespaces, newState);
//        fInfrastructure.persistState(newState);
    }
    
    

    private void saveBinaryLibraries(List libs, State state)
    {
        for (Iterator iter = libs.iterator(); iter.hasNext();)
        {
            ICoreNamespace libNS = (ICoreNamespace) iter.next();
            ICoreResource location = (ICoreResource) libNS.getSpecificationLocation();
            if (location.isBinaryResource())
                state.fBinaryNamespaces.put(location, libNS);
        }
        
    }    

    public void cleanUp()
    {
        super.cleanUp();
    }
    
//  returns unresolved namespace tree
    protected List doGetApplicationNamespaces()
    {    
        List namespaces = new ArrayList();
        
        if (fApplicationServlets == null || fApplicationServlets.length == 0)
            return Collections.EMPTY_LIST;
        
        for (int i=0;i<fApplicationServlets.length;i++)
        {            
            
            CoreNamespace result = null;
            ICoreResource nsLocation = fApplicationServlets[i].applicationSpecLocation;
            if (nsLocation != null)
            {
                if (!nsLocation.exists())
                    throw new BuilderException(CoreMessages.format(
                            "build-failed-missing-application-spec",
                            nsLocation.toString()));

                result = getNamespaceTree(null, nsLocation, null);
            }
            else
            {
                result = createStandinApplicationNamespace(fApplicationServlets[i]);
            }
            
            
            result.setAppNameFromWebXML(fApplicationServlets[i].name);
            
            namespaces.add(result);
        }
                
        return namespaces;
    }

    protected CoreNamespace createStandinApplicationNamespace(ServletInfo servlet)
    {

        PluginApplicationSpecification applicationSpec = new PluginApplicationSpecification();
        Resource virtualLocation = fInfrastructure.fContextRoot.getRelativeResource("/WEB-INF/");
        applicationSpec.setSpecificationLocation(virtualLocation);
        applicationSpec.setName(servlet.name);

        CoreNamespace result = new CoreNamespace(null, applicationSpec);

        return result;
    }

    protected void findDeclaredApplications()
    {
        ICoreResource webXML = (ICoreResource) fInfrastructure.fContextRoot
                .getRelativeResource("WEB-INF/web.xml");

        if (webXML.exists())
        {
            IDOMModel model = null;
            try
            {
                fInfrastructure.fNotifier.subTask(CoreMessages.format(
                        AbstractBuildInfrastructure.STRING_KEY + "scanning",
                        webXML.toString()));
                fInfrastructure.fProblemPersister.removeAllProblemsFor(webXML);

                try
                {
                    model = getDOMModel(webXML, null, fInfrastructure.fValidateWebXML);
                }
                catch (IOException e)
                {
                    TapestryCore.log(e);
                }

                if (model == null)
                {
                    // fInfrastructure.fProblemPersister.recordProblems(webXML,
                    // model.getProblems());
                    throw new BrokenWebXMLException(
                            "Tapestry AbstractBuild failed: could not parse web.xml. ");
                }

                WebAppDescriptor descriptor = null;

                WebXMLScanner wscanner = fInfrastructure.createWebXMLScanner();
                try
                {
                    descriptor = wscanner.scanWebAppDescriptor(model);
                }
                catch (ScannerException e)
                {
                    TapestryCore.log(e);
                }
                fInfrastructure.fProblemPersister.recordProblems(webXML, wscanner.getProblems());

                if (descriptor == null)
                    throw new BrokenWebXMLException(CoreMessages
                            .format(AbstractBuildInfrastructure.STRING_KEY
                                    + "abort-no-valid-application-servlets-found"));

                fApplicationServlets = descriptor.getServletInfos();
                if (fApplicationServlets == null || fApplicationServlets.length == 0)

                    throw new BrokenWebXMLException(CoreMessages
                            .format(AbstractBuildInfrastructure.STRING_KEY
                                    + "abort-no-valid-application-servlets-found"));

//                if (servletInfos.length > 1)
//                    throw new BrokenWebXMLException(CoreMessages
//                            .format(AbstractBuildInfrastructure.STRING_KEY
//                                    + "abort-too-many-valid-servlets-found"));

                //fApplicationServlets = servletInfos[0];
                fWebAppDescriptor = descriptor;
                fInfrastructure.installBasePropertySource(fWebAppDescriptor);
            }
            finally
            {
                if (model != null)
                    model.release();
            }
        }
        else
        {
            ICoreResource definedWebRoot = (ICoreResource) fInfrastructure.fTapestryProject
                    .getWebContextLocation();
            if (definedWebRoot != null || !definedWebRoot.exists())

                fInfrastructure.fProblemPersister.recordProblem(
                        fInfrastructure.fTapestryProject,
                        new DefaultProblem(IProblem.WARNING, CoreMessages.format(
                                AbstractBuildInfrastructure.STRING_KEY + "missing-context",
                                definedWebRoot.toString()), SourceLocation.FOLDER_LOCATION, false,
                                IProblem.NOT_QUICK_FIXABLE));
        }
    }

}