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

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.util.Markers;

/**
 * Builds a Tapestry project from scratch.
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class FullBuild extends Build
{

    protected ServletInfo fApplicationServlet;
    /**
     * Constructor for FullBuilder.
     */
    public FullBuild(TapestryBuilder builder)
    {
        super(builder);
    }

    // used only by incremental subclasses
    protected FullBuild(TapestryBuilder builder, IResourceDelta projectDelta)
    {
        super(builder, projectDelta);
    }

    /**
     * Use the parser to find declared applications in web.xml
     * @param parser
     * @throws CoreException
     */
    protected void preBuild() throws CoreException
    {
        setDependencyListener(new BuilderDependencyListener());
        findDeclaredApplication();
    }

    protected void postBuild()
    {
        BuilderDependencyListener listener = (BuilderDependencyListener) getDependencyListener();
        if (fTapestryBuilder.DEBUG)
        {
            listener.dump();
        }
    }

    /**
     * Resolve the Tapesty framework namespace
     */
    protected void resolveFramework(Parser parser)
    {
        IResourceWorkspaceLocation frameworkLocation =
            (IResourceWorkspaceLocation) fTapestryBuilder.fClasspathRoot.getRelativeLocation(
                "/org/apache/tapestry/Framework.library");
        FrameworkResolver resolver = new FrameworkResolver(this, parser, frameworkLocation);
        fFrameworkNamespace = resolver.resolve();
        //        fFrameworkNamespace = fNSResolver.resolveFrameworkNamespace(frameworkLocation);
    }

    /**
     * Resolve the application namespace
     *
     */
    protected void doBuild(Parser parser)
    {
        ApplicationResolver resolver = new ApplicationResolver(this, parser, fFrameworkNamespace, fApplicationServlet);
        fApplicationNamespace = resolver.resolve();
    }

    public void saveState()
    {
        State newState = new State(fTapestryBuilder);
        newState.fLibraryLocation = fTapestryBuilder.fTapestryProject.getLibrarySpecPath();
        newState.fLastKnownClasspath = fTapestryBuilder.fClasspath;
        newState.fJavaDependencies = fFoundTypes;
        newState.fMissingJavaTypes = fMissingTypes;
        newState.fTemplateMap = fTemplateMap;
        newState.fSpecificationMap = fSpecificationMap;
        newState.fSeenTemplateExtensions = fSeenTemplateExtensions;
        newState.fApplicationServlet = fApplicationServlet;
        newState.fPrimaryNamespace = fApplicationNamespace;
        newState.fFrameworkNamespace = fFrameworkNamespace;

        // save the processed binary libraries
        saveBinaryLibraries(fFrameworkNamespace, fApplicationNamespace, newState);
        TapestryArtifactManager.getTapestryArtifactManager().setLastBuildState(
            fTapestryBuilder.fCurrentProject,
            newState);
    }

    protected void saveBinaryLibraries(ICoreNamespace framework, ICoreNamespace namespace, State state)
    {
        IResourceWorkspaceLocation frameworkLoc = (IResourceWorkspaceLocation) framework.getSpecificationLocation();
        if (frameworkLoc.isBinary())
            state.fBinaryNamespaces.put(frameworkLoc, framework);

        for (Iterator iter = namespace.getChildIds().iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();
            ICoreNamespace child = (ICoreNamespace) namespace.getChildNamespace(id);
            if (child != null)
            {
                IResourceWorkspaceLocation childLocation =
                    (IResourceWorkspaceLocation) child.getSpecificationLocation();
                if (childLocation.isBinary())
                {
                    state.fBinaryNamespaces.put(childLocation, child);
                }
            }

        }
    }

    public void cleanUp()
    {
        super.cleanUp();
    }

    protected void findDeclaredApplication() throws CoreException
    {
        Parser servletParser = new Parser();
        servletParser.setDoValidation(true);
        // uses a validating parser here.
        // Parser does not validate by default.
        // Scanners use the Spindle validator.

        IResourceWorkspaceLocation webXML =
            (IResourceWorkspaceLocation) fTapestryBuilder.fContextRoot.getRelativeLocation("WEB-INF/web.xml");
        //        IFile webXML = tapestryBuilder.contextRoot.getFile("WEB-INF/web.xml");
        if (webXML.exists())
        {
            Document wxmlElement = null;
            try
            {
                fTapestryBuilder.fNotifier.subTask(
                    TapestryCore.getString(TapestryBuilder.STRING_KEY + "scanning", webXML.toString()));
                wxmlElement = parseToDocument(servletParser, webXML, null);
            } catch (IOException e1)
            {
                TapestryCore.log(e1);
            } finally
            {
                servletParser = null;
            }
            if (wxmlElement == null)
                throw new BuilderException("Tapestry Build failed: could not parse web.xml");

            ServletInfo[] servletInfos = null;
            try
            {
                WebXMLScanner wscanner = new WebXMLScanner(this);
                servletInfos = wscanner.scanServletInformation(wxmlElement);
                IResource resource = (IResource) webXML.getStorage();
                Markers.addTapestryProblemMarkersToResource(resource, wscanner.getProblems());
            } catch (ScannerException e)
            {
                TapestryCore.log(e);
            }

            if (servletInfos == null || servletInfos.length == 0)
            {
                throw new BuilderException(
                    TapestryCore.getString(TapestryBuilder.STRING_KEY + "abort-no-valid-application-servlets-found"));
            }
            if (servletInfos.length > 1)
            {
                throw new BuilderException(
                    TapestryCore.getString(TapestryBuilder.STRING_KEY + "abort-too-many-valid-servlets-found"));
            }
            fApplicationServlet = servletInfos[0];

        } else
        {
            String definedWebRoot = fTapestryBuilder.fTapestryProject.getWebContext();
            if (definedWebRoot != null && !"".equals(definedWebRoot))
            {
                Markers.addTapestryProblemMarkerToResource(
                    fTapestryBuilder.getProject(),
                    TapestryCore.getString(TapestryBuilder.STRING_KEY + "missing-context", definedWebRoot),
                    IMarker.SEVERITY_WARNING,
                    0,
                    0,
                    0);
            }
        }

    }

}
