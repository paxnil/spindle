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
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.util.CoreLookup;
import com.iw.plugins.spindle.core.builder.util.ILookupRequestor;
import com.iw.plugins.spindle.core.scanning.*;
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

    BuilderQueue applicationQueue;
    BuilderQueue libraryQueue;
    BuilderQueue pageQueue;
    BuilderQueue componentQueue;
    BuilderQueue htmlQueue;
    BuilderQueue scriptQueue;

    /**
     * Constructor for FullBuilder.
     */
    public FullBuild(TapestryBuilder builder)
    {
        super(builder);
        this.tapestryServletType = getType("org.apache.tapestry.ApplicationServlet");

    }

    public void build()
    {
        if (TapestryBuilder.DEBUG)
            System.out.println("FULL Tapestry build");

        try
        {
            notifier.subTask("Tapestry builder starting");
            Markers.removeProblemsFor(tapestryBuilder.currentProject);

            if (tapestryServletType == null)
            {
                Markers.addTapestryProblemMarkerToResource(
                    tapestryBuilder.currentProject,
                    "ignoring applications in project because '"
                        + tapestryBuilder.currentProject.getName()
                        + "', type 'org.apache.tapestry.ApplicationServlet' not found in project build path!",
                    IMarker.SEVERITY_WARNING,
                    0,
                    0,
                    0);
            } else
            {
                findDeclaredApplications();
            }
            notifier.updateProgressDelta(0.1f);

            notifier.subTask("locating Tapestry artifacts");
            applicationQueue = new BuilderQueue();
            libraryQueue = new BuilderQueue();
            pageQueue = new BuilderQueue();
            componentQueue = new BuilderQueue();
            htmlQueue = new BuilderQueue();
            scriptQueue = new BuilderQueue();

            List found = findAllTapestryArtifacts();
            notifier.updateProgressDelta(0.15f);
            //      int total = getTotalWaitingCount();
            int total = found.size();
            if (total > 0)
            {
                //        String[] allSourceFiles = new String[locations.size()];
                //        locations.toArray(allSourceFiles);
                //        String[] initialTypeNames = new String[typeNames.size()];
                //        typeNames.toArray(initialTypeNames);
                //
                notifier.setProcessingProgressPer(0.75f / (total * 2));

            }
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } finally
        {
            cleanUp();
        }
    }

    /**
     * Method findAllTapestryArtifacts.
     */
    protected List findAllTapestryArtifacts() throws CoreException
    {
        CoreLookup lookup = new CoreLookup();
        Set names = knownValidServlets.keySet();
        String[] servletNames = (String[]) names.toArray(new String[names.size()]);
        lookup.configure(tapestryBuilder.tapestryProject, servletNames);
        lookup.findAll(new ArtifactCollector());
        ArrayList found = new ArrayList();
        //    findAllArtifactsInProjectProper(found);
        //    findAllArtifactsInBinaryClasspath(found);
        return found;
    }

    /**
     * Method findAllArtifactsInBinaryClasspath.
     */
    private void findAllArtifactsInBinaryClasspath(ArrayList found)
    {}

    /**
     * Method findAllArtifactsInProjectProper.
     */
    private void findAllArtifactsInProjectProper(ArrayList found)
    {
        try
        {
            tapestryBuilder.getProject().accept(new BuilderResourceVisitor(this, found), IResource.DEPTH_INFINITE, false);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }
    }

    public void cleanUp()
    {}

    protected int getTotalWaitingCount()
    {
        return applicationQueue.getWaitingCount()
            + libraryQueue.getWaitingCount()
            + pageQueue.getWaitingCount()
            + componentQueue.getWaitingCount()
            + htmlQueue.getWaitingCount()
            + scriptQueue.getWaitingCount();
    }

    protected void findDeclaredApplications()
    {
        if (tapestryBuilder.webXML != null && tapestryBuilder.webXML.exists())
        {
            // TODO need to pull any IProblems out and make them into Markers!
            Node wxmlElement = null;
            try
            {
                wxmlElement = parseToNode(tapestryBuilder.webXML);
            } catch (IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (CoreException e1)
            {
                // TODO Auto-generated catch block
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
                servletInfos = wscanner.getServletInformation(wxmlElement);
                Markers.addTapestryProblemMarkersToResource(tapestryBuilder.webXML, wscanner.getProblems());
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
                    "Ignoring applications: " + definedWebRoot + " does not exist",
                    IMarker.SEVERITY_WARNING,
                    0,
                    0,
                    0);
            }
        }

    }

    private final class ArtifactCollector implements ILookupRequestor
    {
        public boolean isCancelled()
        {
            try
            {
                tapestryBuilder.notifier.checkCancel();
            } catch (OperationCanceledException e)
            {
                return true;
            }
            return false;
        }
        public void accept(IStorage storage, Object parent)
        {
            System.out.println(storage);
        }
        /**
         * @see com.iw.plugins.spindle.core.processing.ILookupRequestor#markBadLocation(IStorage, Object, ILookupRequestor, String)
         */
        public void markBadLocation(IStorage s, Object parent, ILookupRequestor requestor, String message)
        {
            System.err.println(s + " " + message);
        }
    }

    public class ServletInfo
    {
        String name;
        String classname;
        Map parameters = new HashMap();
        boolean isServletSubclass;
        public String toString()
        {
            StringBuffer buffer = new StringBuffer("ServletInfo(");
            buffer.append(name);
            buffer.append(")::");
            buffer.append("classname = ");
            buffer.append(classname);
            buffer.append(", params = ");
            buffer.append(parameters);
            return buffer.toString();
        }
    }

}
