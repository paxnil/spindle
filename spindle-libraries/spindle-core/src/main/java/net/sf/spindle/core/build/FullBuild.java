package net.sf.spindle.core.build;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
import java.io.IOException;
import java.util.List;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.namespace.CoreNamespace;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.SourceLocation;
import net.sf.spindle.core.spec.PluginApplicationSpecification;

import org.apache.hivemind.Resource;

/**
 * Builds a Tapestry project from scratch.
 * 
 * @author glongman@gmail.com
 */
public class FullBuild extends AbstractBuild
{

    protected ServletInfo applicationServlet;

    protected WebAppDescriptor webAppDescriptor;

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
        new FrameworkResolver(this).resolve(frameworkNamespace);
    }

    protected void resolveApplication(String name, CoreNamespace namespace)
    {
        new ApplicationResolver(this, frameworkNamespace, name).resolve(namespace);
    }

    protected void postBuild()
    {
        super.postBuild();
        BuilderDependencyListener listener = (BuilderDependencyListener) getDependencyListener();
        if (AbstractBuildInfrastructure.DEBUG)
        {
            listener.dump();
        }
    }

    @SuppressWarnings("unchecked")
    public void saveState()
    {
        State newState = infrastructure.createEmptyState();

        newState.classpathMemento = infrastructure.getClasspathMemento();
        newState.javaTypeDependencies = foundTypes;
        newState.missingJavaTypes = missingTypes;
        newState.templateMap = templateMap;
        newState.fileSpecificationMap = fileSpecificationMap;
        newState.binarySpecificationMap = binarySpecificationMap;
        newState.seenTemplateExtensions = seenTemplateExtensions;

        newState.declaredTemplateExtensions = declaredTemplateExtensions;
        newState.declaredTemplateExtensionsClasspath = declaredTemplateExtensionsClasspath;
        
        newState.webAppDescriptor = webAppDescriptor;
        newState.primaryNamespace = applicationNamespace;
        newState.frameworkNamespace = frameworkNamespace;
        newState.fCleanTemplates = cleanTemplates;

        // save the processed binary libraries
        saveBinaryLibraries(classpathNamespaces, newState);
        infrastructure.persistState(newState);
    }

    @SuppressWarnings("unchecked")
    private void saveBinaryLibraries(List<ICoreNamespace> libs, State state)
    {
        if (libs == null)
            return;

        for (ICoreNamespace libNS : libs)
        {
            ICoreResource location = (ICoreResource) libNS.getSpecificationLocation();
            if (location.isBinaryResource())
                state.fBinaryNamespaces.put(location, libNS);
        }
    }

    public void cleanUp()
    {
        super.cleanUp();
    }

    // returns unresolved app namespace tree
    protected CoreNamespace doGetApplicationNamespace()
    {
        CoreNamespace result = null;

        ICoreResource nsLocation = applicationServlet.applicationSpecLocation;
        if (nsLocation != null)
        {
            if (!nsLocation.exists())
                throw new BuilderException(BuilderMessages.missingApplicationSpec(nsLocation));

            result = getNamespaceTree(null, nsLocation, null);
        }
        else
        {
            result = createStandinApplicationNamespace(applicationServlet);
        }

        result.setAppNameFromWebXML(applicationServlet.name);

        return result;
    }

    protected CoreNamespace createStandinApplicationNamespace(ServletInfo servlet)
    {

        PluginApplicationSpecification applicationSpec = new PluginApplicationSpecification();
        Resource virtualLocation = contextRoot.getRelativeResource("/WEB-INF/");
        applicationSpec.setSpecificationLocation(virtualLocation);
        applicationSpec.setName(servlet.name);

        CoreNamespace result = new CoreNamespace(null, applicationSpec);

        return result;
    }

    protected void findDeclaredApplications()
    {
        ICoreResource webXML = (ICoreResource) contextRoot.getRelativeResource("WEB-INF/web.xml");

        if (webXML.exists())
        {
            aboutToProcess(webXML);

            IDOMModel model = null;
            boolean ok = false;
            try
            {

                notifier.subTask(BuilderMessages.scanning(webXML));

                try
                {
                    model = getDOMModel(webXML, null, validateWebXML);
                }
                catch (IOException e)
                {
                    TapestryCore.log(e);
                }

                if (model != null)
                {

                    WebAppDescriptor descriptor = null;

                    WebXMLScanner wscanner = infrastructure.createWebXMLScanner(this);
                    try
                    {
                        descriptor = wscanner.scanWebAppDescriptor(model);
                    }
                    catch (ScannerException e)
                    {
                        TapestryCore.log(e);
                    }
                    recordProblems(webXML, wscanner.getProblems());

                    if (descriptor == null)
                        throw new BrokenWebXMLException(BuilderMessages
                                .fatalErrorNoValidTapestryServlets());

                    ServletInfo[] applicationServlets = descriptor.getServletInfos();
                    if (applicationServlets == null || applicationServlets.length == 0)

                        throw new BrokenWebXMLException(BuilderMessages
                                .fatalErrorNoValidTapestryServlets());

                    if (applicationServlets.length > 1)
                        throw new BrokenWebXMLException(BuilderMessages
                                .fatalErrorTooManyValidTapestryServlets());

                    applicationServlet = applicationServlets[0];

                    projectPropertySource = infrastructure.installBasePropertySource(descriptor);
                    webAppDescriptor = descriptor;

                    ok = true;
                }
            }
            finally
            {
                if (!ok && model == null)
                    throw new BrokenWebXMLException(BuilderMessages
                            .fatalErrorCouldNotParseWebXML(webXML));

                if (model != null)
                    model.release();

                finished(webXML);
            }
        }
        else
        {
            problemPersister.recordProblem(tapestryProject, new DefaultProblem(IProblem.WARNING,
                    BuilderMessages.webXMLDoesNotExist(webXML), SourceLocation.FOLDER_LOCATION,
                    false, IProblem.NOT_QUICK_FIXABLE));

        }
    }

}