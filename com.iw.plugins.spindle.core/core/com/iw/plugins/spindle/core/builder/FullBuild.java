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
import java.util.Iterator;

import org.w3c.dom.Document;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;

/**
 * Builds a Tapestry project from scratch.
 * 
 * @author glongman@gmail.com
 */
public class FullBuild extends AbstractBuild
{

    protected ServletInfo fApplicationServlet;

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
        findDeclaredApplication();
    }

    protected void postBuild()
    {
        BuilderDependencyListener listener = (BuilderDependencyListener) getDependencyListener();
        if (fInfrastructure.DEBUG)
        {
            listener.dump();
        }
    }

    /**
     * Resolve the Tapesty framework namespace
     */
    protected void resolveFramework(Parser parser)
    {
        IResourceWorkspaceLocation frameworkLocation = (IResourceWorkspaceLocation) fInfrastructure.fClasspathRoot
                .getRelativeResource("/org/apache/tapestry/Framework.library");
        FrameworkResolver resolver = new FrameworkResolver(this, parser, frameworkLocation);
        fFrameworkNamespace = resolver.resolve();
        //        fFrameworkNamespace =
        // fNSResolver.resolveFrameworkNamespace(frameworkLocation);
    }

    /**
     * Resolve the application namespace
     */
    protected void doBuild(Parser parser)
    {
        ApplicationResolver resolver = new ApplicationResolver(this, parser, fFrameworkNamespace,
                fApplicationServlet);
        fApplicationNamespace = resolver.resolve();
    }

    public void saveState()
    {        
        State newState = new State(fInfrastructure);
        //    newState.fLibraryLocation = fTapestryBuilder.fTapestryProject.getLibrarySpecPath();
        newState.fLastKnownClasspath = fInfrastructure.getClasspathMemento();
        newState.fJavaDependencies = fFoundTypes;
        newState.fMissingJavaTypes = fMissingTypes;
        newState.fTemplateMap = fTemplateMap;
        newState.fFileSpecificationMap = fFileSpecificationMap;
        newState.fBinarySpecificationMap = fBinarySpecificationMap;
        newState.fSeenTemplateExtensions = fSeenTemplateExtensions;
        newState.fApplicationServlet = fApplicationServlet;
        newState.fWebAppDescriptor = fWebAppDescriptor;
        newState.fPrimaryNamespace = fApplicationNamespace;
        newState.fFrameworkNamespace = fFrameworkNamespace;
        newState.fCleanTemplates = fCleanTemplates;

        // save the processed binary libraries
        saveBinaryLibraries(fFrameworkNamespace, fApplicationNamespace, newState);
        fInfrastructure.persistState(newState);        
    }

    protected void saveBinaryLibraries(ICoreNamespace framework, ICoreNamespace namespace,
            State state)
    {
        saveBinaryLibraries(framework, state);
        saveBinaryLibraries(namespace, state);
    }

    private void saveBinaryLibraries(ICoreNamespace namespace, State state)
    {
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) namespace
                .getSpecificationLocation();
        if (location.isBinaryResource())
            state.fBinaryNamespaces.put(location, namespace);

        for (Iterator iter = namespace.getChildIds().iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();
            ICoreNamespace child = (ICoreNamespace) namespace.getChildNamespace(id);
            if (child != null)
                saveBinaryLibraries(child, state);
        }
    }

    public void cleanUp()
    {
        super.cleanUp();
    }

    protected void findDeclaredApplication() 
    {
        Parser servletParser = new Parser();
        servletParser.setDoValidation(fInfrastructure.fValidateWebXML);
        // Parser does not validate by default.
        // Scanners use the Spindle validator.

        IResourceWorkspaceLocation webXML = (IResourceWorkspaceLocation) fInfrastructure.fContextRoot
                .getRelativeResource("WEB-INF/web.xml");
        if (webXML.exists())
        {
            Document wxmlElement = null;
            try
            {
                fInfrastructure.fNotifier.subTask(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                        + "scanning", webXML.toString()));
                fInfrastructure.fProblemPersister.removeAllProblemsFor(webXML);
                wxmlElement = parseToDocument(servletParser, webXML, null);
            }
            catch (IOException e1)
            {
                TapestryCore.log(e1);
            }
            finally
            {
                servletParser = null;
            }
            if (wxmlElement == null)
                throw new BrokenWebXMLException(
                        "Tapestry AbstractBuild failed: could not parse web.xml. ");

            WebAppDescriptor descriptor = null;
            try
            {
                WebXMLScanner wscanner = fInfrastructure.createWebXMLScanner();
                descriptor = wscanner.scanWebAppDescriptor(wxmlElement);
                fInfrastructure.fProblemPersister.recordProblems(webXML, wscanner.getProblems());
            }
            catch (ScannerException e)
            {
                TapestryCore.log(e);
            }

            if (descriptor == null)
                throw new BrokenWebXMLException(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                        + "abort-no-valid-application-servlets-found"));

            ServletInfo[] servletInfos = descriptor.getServletInfos();
            if (servletInfos == null || servletInfos.length == 0)

                throw new BrokenWebXMLException(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                        + "abort-no-valid-application-servlets-found"));

            if (servletInfos.length > 1)
                throw new BrokenWebXMLException(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                        + "abort-too-many-valid-servlets-found"));

            fApplicationServlet = servletInfos[0];
            fWebAppDescriptor = descriptor;
            fInfrastructure.installBasePropertySource(fWebAppDescriptor);
        }
        else
        {
            IResourceWorkspaceLocation definedWebRoot = (IResourceWorkspaceLocation) fInfrastructure.fTapestryProject.getWebContextLocation();
            if (definedWebRoot != null || !definedWebRoot.exists())

                fInfrastructure.fProblemPersister.recordProblem(
                        fInfrastructure.fTapestryProject,
                        new DefaultProblem(IProblem.TAPESTRY_PROBLEM_MARKER,
                                IProblem.WARNING, CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY
                                        + "missing-context", definedWebRoot.toString()), 1, 0, 0,
                                false, IProblem.NOT_QUICK_FIXABLE));
        }
    }

}