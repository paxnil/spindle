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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.IResourceLocation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.search.AbstractTapestrySearchAcceptor;
import com.iw.plugins.spindle.core.resources.search.ISearch;
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

    protected IType tapestryServletType;
    protected Map knownValidServlets;
    protected Map infoCache;

    BuilderQueue buildQueue;

    /**
     * Constructor for FullBuilder.
     */
    public FullBuild(TapestryBuilder builder)
    {
        super(builder);
        this.tapestryServletType = builder.getType(TapestryCore.getString(TapestryBuilder.APPLICATION_SERVLET_NAME));

    }

    public void build() throws BuilderException
    {
        if (TapestryBuilder.DEBUG)
            System.out.println("FULL Tapestry build");

        try
        {
            notifier.subTask(TapestryCore.getString(TapestryBuilder.STARTING));
            Markers.removeProblemsForProject(tapestryBuilder.currentProject);

            findDeclaredApplications();
            if (knownValidServlets == null || knownValidServlets.isEmpty())
            {
                throw new BuilderException(TapestryCore.getString(TapestryBuilder.ABORT_APPLICATION_NO_SERVLETS));
            }
            if (knownValidServlets.size() > 1)
            {
                throw new BuilderException(TapestryCore.getString(TapestryBuilder.ABORT_APPLICATION_ONE_SERVLET_ONLY));
            }
            //            goofTest();
            notifier.updateProgressDelta(0.1f);

            notifier.subTask(TapestryCore.getString(TapestryBuilder.LOCATING_ARTIFACTS));
            buildQueue = new BuilderQueue();

            buildQueue.addAll(findAllTapestryArtifacts());
            notifier.updateProgressDelta(0.15f);
            if (buildQueue.hasWaiting())
            {
                notifier.setProcessingProgressPer(0.75f / buildQueue.getWaitingCount());
                while (buildQueue.getWaitingCount() > 0)
                {

                    IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) buildQueue.peekWaiting();
                    notifier.processed(location);
                    buildQueue.finished(location);
                }
            }

        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
    }

    /**
     * 
     */
    private void goofTest()
    {
        IResourceLocation goof = tapestryBuilder.classpathRoot.getRelativeLocation("com/Framework.library");
        parseLibrary(goof);
        goof = tapestryBuilder.contextRoot.getRelativeLocation("com/ExampleLayout.application");
        parseApplication(goof);

    }

    /**
     * Method findAllTapestryArtifacts.
     */
    protected List findAllTapestryArtifacts() throws CoreException
    {
        ArrayList found = new ArrayList();
        findAllArtifactsInWebContext(found);
        findAllArtifactsInClasspath(found);
        return found;
    }

    /**
     * Method findAllArtifactsInBinaryClasspath.
     */
    private void findAllArtifactsInClasspath(final ArrayList found)
    {
        ISearch searcher = null;
        try
        {
            searcher = tapestryBuilder.classpathRoot.getSearch();
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        if (searcher != null)
        {
            searcher.search(new ArtifactCollector()
            {
                public boolean acceptTapestry(Object parent, IStorage storage)
                {
                    IPackageFragment fragment = (IPackageFragment) parent;
                    IResourceWorkspaceLocation location =
                        tapestryBuilder.classpathRoot.getRelativeLocation(fragment, storage);
                    found.add(location);
                    if (TapestryBuilder.DEBUG)
                    {
                        System.out.println(location);
                    }
                    return keepGoing();
                }
            });
        }
    }

    /**
     * Method findAllArtifactsInProjectProper.
     */
    private void findAllArtifactsInWebContext(final ArrayList found)
    {
        ISearch searcher = null;
        try
        {
            searcher = tapestryBuilder.contextRoot.getSearch();
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        if (searcher != null)
        {
            searcher.search(new ArtifactCollector()
            {
                public boolean acceptTapestry(Object parent, IStorage storage)
                {

                    IResourceWorkspaceLocation location =
                        tapestryBuilder.contextRoot.getRelativeLocation((IResource) storage);
                    found.add(location);
                    if (TapestryBuilder.DEBUG)
                    {
                        System.out.println(location);
                    }
                    return keepGoing();

                }
            });
        }
    }

    public void cleanUp()
    {}

    protected void findDeclaredApplications()
    {
        IResourceWorkspaceLocation webXML =
            (IResourceWorkspaceLocation) tapestryBuilder.contextRoot.getRelativeLocation("WEB-INF/web.xml");
        //        IFile webXML = tapestryBuilder.contextRoot.getFile("WEB-INF/web.xml");
        if (webXML.exists())
        {
            Node wxmlElement = null;
            try
            {
                tapestryBuilder.notifier.subTask(TapestryCore.getString(TapestryBuilder.SCANNING, webXML.toString()));
                wxmlElement = parseToNode(webXML);
            } catch (IOException e1)
            {
                TapestryCore.log(e1);
                e1.printStackTrace();
            } catch (CoreException e1)
            {
                TapestryCore.log(e1);
                e1.printStackTrace();
            }
            if (wxmlElement == null)
            {
                return;
            }

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
            if (servletInfos != null && servletInfos.length > 0)
            {
                knownValidServlets = new HashMap();
                for (int i = 0; i < servletInfos.length; i++)
                {
                    knownValidServlets.put(servletInfos[i].name, servletInfos[i]);
                }
            }

        } else
        {
            String definedWebRoot = tapestryBuilder.tapestryProject.getWebContext();
            if (definedWebRoot != null && !"".equals(definedWebRoot))
            {
                Markers.addTapestryProblemMarkerToResource(
                    tapestryBuilder.getProject(),
                    TapestryCore.getString(TapestryBuilder.MISSING_CONTEXT, definedWebRoot),
                    IMarker.SEVERITY_WARNING,
                    0,
                    0,
                    0);
            }
        }

    }

    private abstract class ArtifactCollector extends AbstractTapestrySearchAcceptor
    {

        public ArtifactCollector()
        {
            super(AbstractTapestrySearchAcceptor.ACCEPT_ANY);
        }

        public boolean keepGoing()
        {
            try
            {
                tapestryBuilder.notifier.checkCancel();
            } catch (OperationCanceledException e)
            {
                return false;
            }
            return true;
        }
    }

    public class ServletInfo
    {
        String name;
        String classname;
        Map parameters = new HashMap();
        boolean isServletSubclass;
        IResourceWorkspaceLocation applicationSpecLocation;
        public String toString()
        {
            StringBuffer buffer = new StringBuffer("ServletInfo(");
            buffer.append(name);
            buffer.append(")::");
            buffer.append("classname = ");
            buffer.append(classname);
            buffer.append(", params = ");
            buffer.append(parameters);
            buffer.append(" loc= ");
            buffer.append(applicationSpecLocation);
            return buffer.toString();
        }
    }

}
