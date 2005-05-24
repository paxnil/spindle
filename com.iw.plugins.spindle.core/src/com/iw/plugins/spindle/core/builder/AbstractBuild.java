package com.iw.plugins.spindle.core.builder;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hivemind.Resource;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.w3c.dom.Document;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.IJavaType;
import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.properties.ComponentPropertySource;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.templates.ITemplateFinderListener;
import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;
import com.iw.plugins.spindle.core.scanning.ApplicationScanner;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidatorListener;
import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.SpecificationValidator;
import com.iw.plugins.spindle.core.scanning.TemplateScanner;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.spec.BaseSpecification;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * Abstract base class for full and incremental builds
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractBuild implements IBuild, IScannerValidatorListener,
        ITemplateFinderListener
{

    private static ThreadLocal DEPENDENCY_LISTENER_HOLDER;

    protected static void setDependencyListener(BuilderDependencyListener listener)
    {
        if (DEPENDENCY_LISTENER_HOLDER == null)
            DEPENDENCY_LISTENER_HOLDER = new ThreadLocal();

        DEPENDENCY_LISTENER_HOLDER.set(listener);
    }

    public static IDependencyListener getDependencyListener()
    {
        if (DEPENDENCY_LISTENER_HOLDER == null)
            return null;

        return (IDependencyListener) DEPENDENCY_LISTENER_HOLDER.get();
    }

    protected ICoreNamespace fApplicationNamespace;

    protected BuilderQueue fBuildQueue;

    protected List fFoundTypes;

    protected ICoreNamespace fFrameworkNamespace;

  
    protected IPropertySource fProjectPropertySource;

    protected State fLastState;

    protected List fMissingTypes;

    protected State fNewState;

    protected BuildNotifier fNotifier;

    protected Map fProcessedLocations;

    protected List fSeenTemplateExtensions;

    protected Map fTemplateMap;

    protected Map fFileSpecificationMap;

    protected Map fBinarySpecificationMap; //binary specs (from jars) never

    // change
    protected AbstractBuildInfrastructure fInfrastructure;

    protected List fCleanTemplates; //templates that contain no implicit

    // components

    protected IJavaType fTapestryServletType;

    public AbstractBuild(AbstractBuildInfrastructure infrastructure)
    {
        fTapestryServletType = fInfrastructure.fTapestryProject.findType(CoreMessages
                .format(AbstractBuildInfrastructure.STRING_KEY + "applicationServletClassname"));

        fInfrastructure = infrastructure;
        fNewState = new State(infrastructure); 
        fBuildQueue = new BuilderQueue();
        fNotifier = infrastructure.fNotifier;
        fFoundTypes = new ArrayList();
        fMissingTypes = new ArrayList();
        fProcessedLocations = new HashMap();
        fSeenTemplateExtensions = new ArrayList();
        fTemplateMap = new HashMap();
        fFileSpecificationMap = new HashMap();
        fBinarySpecificationMap = new HashMap();
        TapestryArtifactManager.getTapestryArtifactManager().addTemplateFinderListener(this);
        fCleanTemplates = new ArrayList();
    }

    public void build() throws BuilderException
    {

        Parser parser = new Parser(false);

        preBuild();

        fNotifier.updateProgressDelta(0.1f);

        fNotifier.subTask(CoreMessages.format(AbstractBuildInfrastructure.STRING_KEY + "locating-artifacts"));

        fBuildQueue.addAll(findAllTapestryArtifacts());

        resolveFramework(parser);
        try
        {
            doBuild(parser);

            postBuild();

        }
        finally
        {
            setDependencyListener(null);
        }

        fNotifier.updateProgressDelta(0.15f);
        if (fBuildQueue.hasWaiting())
        {
            int missPriority = TapestryCore.getDefault().getBuildMissPriority();
            fNotifier.setProcessingProgressPer(0.75f / fBuildQueue.getWaitingCount());
            while (fBuildQueue.getWaitingCount() > 0)
            {

                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) fBuildQueue
                        .peekWaiting();
                fNotifier.processed(location);
                fBuildQueue.finished(location);

                if (missPriority >= 0 && location.exists() && !location.isBinaryResource())
                    recordBuildMiss(missPriority, location);

            }
        }
        //TODO remove
        System.out.println("template count:" + fTemplateMap.keySet().size());
        System.out.println(" file specification count" + fFileSpecificationMap.keySet().size());
        System.out.println(" binary specification count" + fBinarySpecificationMap.keySet().size());
        saveState();
    }

    protected void recordBuildMiss(int missPriority, Resource resource)
    {
        if ("package.html".equals(resource.getName())) //TODO add real filter
            // capability.
            return;

        fInfrastructure.fProblemPersister.recordProblem(resource, new DefaultProblem(
                IProblem.TAPESTRY_PROBLEM_MARKER, missPriority, CoreMessages.format(
                        "builder-missed-file-message",
                        resource.getName()), 0, 0, 0, false, IProblem.NOT_QUICK_FIXABLE));
    }

    protected abstract void saveState();

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.builder.IBuild#cleanUp()
     */
    public void cleanUp()
    {
        TapestryArtifactManager.getTapestryArtifactManager().removeTemplateFinderListener(this);
        fLastState = null;
        fFoundTypes = null;
        fMissingTypes = null;
        fProcessedLocations = null;
        fSeenTemplateExtensions = null;
        fApplicationNamespace = null;
        fFrameworkNamespace = null;
        fBuildQueue = null;
        fNotifier = null;
        fInfrastructure = null;
        fProcessedLocations = null;
        fTemplateMap = null;
        fBinarySpecificationMap = null;
        fFileSpecificationMap = null;
        fCleanTemplates = null;
    }

    protected ICoreNamespace getPreBuiltNamespace(IResourceWorkspaceLocation location)
    {
        if (fLastState != null)
        {
            if (location.isBinaryResource() && fLastState.fBinaryNamespaces.containsKey(location))
                return (ICoreNamespace) fLastState.fBinaryNamespaces.get(location);

        }
        return null;
    }

    protected ICoreNamespace createNamespace(Parser parser, String id,
            IResourceWorkspaceLocation location, String encoding)
    {

        ICoreNamespace result = null;

        ILibrarySpecification lib = null;

        String name = location.getName();
        if (name.endsWith(".application"))
        {
            lib = resolveApplication(parser, location, encoding);
        }
        else if (name.endsWith(".library"))
        {
            lib = resolveLibrarySpecification(parser, location, encoding);
        }
        if (lib != null)
            result = new CoreNamespace(id, lib);

        ((BaseSpecLocatable) lib).setNamespace(result);

        return result;
    }

    /**
     * Perform the build.
     */
    protected abstract void doBuild(Parser parser);

    /**
     * Find all files in the project that have Tapestry extensions.
     * 
     * @return List a list containing all the Tapestry files in the project
     */
    protected List findAllTapestryArtifacts()
    {
        ArrayList found = new ArrayList();
        fInfrastructure.findAllTapestryArtifactsInWebContext(found);
        fInfrastructure.findAllTapestryArtifactsInClasspath(found);
        return found;
    }

    /**
     * Completely process an application specification file, recording any problems encountered.
     * <p>
     * If the entity referred to by the location is really a file in the workspace, the problems are
     * recorded as resource markers. Otherwise, the problem is logged.
     */
    protected IApplicationSpecification resolveApplication(Parser parser, Resource location,
            String encoding)
    {
        IResourceWorkspaceLocation useLocation = (IResourceWorkspaceLocation) location;
        IApplicationSpecification result = null;
        try
        {
            Document document = parseToDocument(parser, useLocation, encoding == null ? "UTF-8"
                    : encoding);
            //            Node node = parseToNode(location);
            if (document != null)
            {
                ApplicationScanner scanner = new ApplicationScanner();
                scanner.setResourceLocation(useLocation);

                IScannerValidator useValidator = new SpecificationValidator( 
                        (IJavaTypeFinder) this,  fInfrastructure.fTapestryProject);

                useValidator.addListener(this);

                try
                {
                    result = (IApplicationSpecification) scanner.scan(document, useValidator);
                    fInfrastructure.fProblemPersister.recordProblems(useLocation, scanner
                            .getProblems());
                }
                finally
                {
                    useValidator.removeListener(this);
                }

            }
            else
            {
                PluginApplicationSpecification dummy = new PluginApplicationSpecification();
                dummy.makePlaceHolder();
                dummy.setSpecificationLocation(location);
                result = dummy;
            }
            rememberSpecification(useLocation, (BaseSpecification) result);
        }
        catch (IOException e)
        {
            TapestryCore.log(e);
        }
        //        catch (CoreException e)
        //        {
        //            TapestryCore.log(e);
        //        }
        catch (ScannerException e)
        {
            recordFatalProblem(location, e);
        }
        finally
        {
            finished(useLocation);
        }
        return result;
    }

    protected void rememberSpecification(IResourceWorkspaceLocation location,
            BaseSpecification result)
    {
        if (!location.exists())
        {
            Throwable t = new Throwable();
            t.fillInStackTrace();
            TapestryCore.log("unexpected null storage", t);
            return;
        }
        if (location.isBinaryResource())
            fBinarySpecificationMap.put(location, result);
        else
            fFileSpecificationMap.put(location, result);

    }

    protected void recordFatalProblem(Resource location, ScannerException e)
    {
        fInfrastructure.fProblemPersister.recordProblem(location, new DefaultProblem(
                IProblem.TAPESTRY_FATAL_PROBLEM_MARKER, IProblem.ERROR, e.getMessage(), 1, 0, 0,
                false, IProblem.NOT_QUICK_FIXABLE));
    }

    /**
     * Completely process a library specification file, recording any problems encountered.
     * <p>
     * If the entity referred to by the location is really a resource in the workspace, the problems
     * are recorded as resource markers. Otherwise, the problem is logged.
     */
    protected ILibrarySpecification resolveLibrarySpecification(Parser parser, Resource location,
            String encoding)
    {
        IResourceWorkspaceLocation useLocation = (IResourceWorkspaceLocation) location;
        ILibrarySpecification result = null;
        if (fProcessedLocations.containsKey(useLocation))
            return (ILibrarySpecification) fProcessedLocations.get(useLocation);

        try
        {
            Document document = parseToDocument(parser, useLocation, encoding == null ? "UTF-8"
                    : encoding);

            if (document != null)
            {
                LibraryScanner scanner = new LibraryScanner();
                scanner.setResourceLocation(useLocation);

                IScannerValidator useValidator = new SpecificationValidator(
                        (IJavaTypeFinder) this,  fInfrastructure.fTapestryProject);
                try
                {
                    useValidator.addListener(this);
                    result = (ILibrarySpecification) scanner.scan(document, useValidator);

                }
                finally
                {
                    useValidator.removeListener(this);
                }
                rememberSpecification(useLocation, (BaseSpecification) result);

                fInfrastructure.fProblemPersister.recordProblems(useLocation, scanner
                        .getProblems());

            }
            else
            {
                PluginLibrarySpecification dummy = new PluginLibrarySpecification();
                dummy.setSpecificationLocation(location);
                dummy.makePlaceHolder();
                result = dummy;
            }
            rememberSpecification(useLocation, (BaseSpecification) result);
        }
        catch (IOException e)
        {
            TapestryCore.log(e);
        }
        //        catch (CoreException e)
        //        {
        //            TapestryCore.log(e);
        //        }
        catch (ScannerException e)
        {
            recordFatalProblem(location, e);
        }
        finally
        {
            finished(useLocation);
        }
        if (result != null)
            fProcessedLocations.put(useLocation, result);

        return result;
    }

    /**
     * Process completely all the templates found for a component or page. Problems encountered are
     * recorded.
     * <p>
     * If the entity referred to by the specification location is really a resource in the
     * workspace, the problems are recorded as resource markers. Otherwise, the problem is logged.
     */
    protected void parseTemplates(PluginComponentSpecification spec)
    {
        ComponentPropertySource source = new ComponentPropertySource(fProjectPropertySource, spec);
        String componentAttributeName = source
                .getPropertyValue("org.apache.tapestry.jwcid-attribute-name");

        TemplateScanner scanner = new TemplateScanner();
        for (Iterator iter = spec.getTemplateLocations().iterator(); iter.hasNext();)
        {
            IResourceWorkspaceLocation templateLocation = (IResourceWorkspaceLocation) iter.next();

            if (fProcessedLocations.containsKey(templateLocation))
                continue;
            try
            {
                if (!templateLocation.exists())
                    return;

                fTemplateMap.put(templateLocation, spec);

                if (shouldParseTemplate(spec.getSpecificationLocation(), templateLocation))
                {
                    IScannerValidator validator = new SpecificationValidator(
                            (IJavaTypeFinder) this, fInfrastructure.fTapestryProject);
                    try
                    {
                        validator.addListener(this);
                        scanner.scanTemplate(
                                spec,
                                templateLocation,
                                componentAttributeName,
                                validator);
                    }
                    finally
                    {
                        validator.removeListener(this);
                    }

                    fInfrastructure.fProblemPersister.recordProblems(templateLocation, scanner
                            .getProblems());

                    if (!scanner.containsImplicitComponents()
                            && !templateLocation.isBinaryResource()) // we
                        // don't
                        // care
                        // if
                        // specs
                        // in
                        // jars
                        // are
                        // clean
                        fCleanTemplates.add(templateLocation);
                }
                fProcessedLocations.put(templateLocation, null);
            }
            catch (ScannerException e)
            {
                TapestryCore.log(e);
            }
            //            catch (CoreException e)
            //            {
            //                TapestryCore.log(e);
            //            }
            catch (RuntimeException e)
            {
                TapestryCore.log(e);
                throw e;
            }
            finally
            {
                finished(templateLocation);
            }

        }
    }

    /**
     * Always returns true here. Incremental builds may override to check if the template really
     * needs to be parsed.
     * 
     * @param ownerSpec
     *            the Resource for the specification that owns this template
     * @param template
     *            the Resource for the template
     * @return true iff the template should be parsed (expensive)
     */
    protected boolean shouldParseTemplate(Resource ownerSpec, Resource template)
    {
        return true;
    }

    /**
     * Invoke the Parser on an xml file given an instance of IStorage. Problems encountered by the
     * Parser are recorded.
     * <p>
     * If the IStorage is really a resource in the workspace, the problems are recorded as resource
     * markers. Otherwise, the problem is logged.
     */
    protected Document parseToDocument(Parser parser, IResourceWorkspaceLocation location,
            String encoding) throws IOException
    {
        if (!location.exists())
            throw new IOException(CoreMessages.format("core-resource-does-not-exist", location));
        Document result = null;
        try
        {
            result = parser.parse(location.getContents(), encoding);

        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
            throw e;
        }
        catch (IOException e)
        {
            TapestryCore.log(e);
            throw e;
        }

        fInfrastructure.fProblemPersister.recordProblems(location, parser.getProblems());

        if (parser.getHasFatalErrors())
            return null;

        return result;
    }

    /**
     * Perform any work that needs to be done before the build proper can begin. For example the
     * application builder parses web.xml here.
     */
    protected abstract void preBuild();

    /**
     * Perform any work that need to happen after doBuild()
     * 
     * @param parser
     */
    protected abstract void postBuild();

    /**
     * Resolve the Tapestry Framework namespace here. Library builds check to see if the library
     * being built *is* the framework library. If so it skips this step.
     */
    protected abstract void resolveFramework(Parser parser);

    protected IComponentSpecification resolveIComponentSpecification(Parser parser,
            ICoreNamespace namespace, IResourceWorkspaceLocation location,
            String templateExtension, String encoding)

    {
        // to avoid double parsing specs that are accessible
        // by multiple means in Tapestry
        if (fProcessedLocations.containsKey(location))
            return (IComponentSpecification) fProcessedLocations.get(location);

        Assert.isNotNull(templateExtension);
        templateExtensionSeen(templateExtension);

        PluginComponentSpecification result = null;
        if (location.exists())
            try
            {
                Document document = parseToDocument(parser, location, encoding == null ? "UTF-8"
                        : encoding);
                // while editor reconcilers can re-use the scanner, we can't here
                //because scanning may invoke the scanning of another, nested,
                // component.
                ComponentScanner scanner = new ComponentScanner();

                if (document != null)
                {
                    scanner.setResourceLocation(location);
                    scanner.setNamespace(namespace);

                    IScannerValidator useValidator = new SpecificationValidator(
                            (IJavaTypeFinder) this, fInfrastructure.fTapestryProject);
                    useValidator.addListener(this);
                    try
                    {
                        result = (PluginComponentSpecification) scanner
                                .scan(document, useValidator);
                        if (result != null)
                            result.setTemplateLocations(TemplateFinder.scanForTemplates(
                                    result,
                                    templateExtension,
                                    fInfrastructure.fTapestryProject,
                                    scanner));

                    }
                    finally
                    {
                        useValidator.removeListener(this);
                    }

                    fInfrastructure.fProblemPersister.recordProblems(location, scanner
                            .getProblems());

                    rememberSpecification(location, result);
                }
                else
                {
                    PluginComponentSpecification dummy = new PluginComponentSpecification();
                    dummy.makePlaceHolder();
                    dummy.setSpecificationLocation(location);
                    dummy.setNamespace(namespace);
                    dummy.setTemplateLocations(TemplateFinder.scanForTemplates(
                            dummy,
                            templateExtension,
                            fInfrastructure.fTapestryProject,
                            null));
                    result = dummy;
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            //            catch (CoreException e)
            //            {
            //                e.printStackTrace();
            //            }
            catch (ScannerException e)
            {
                recordFatalProblem(location, e);

            }
            finally
            {
                finished(location);
            }

        if (result != null)
            fProcessedLocations.put(location, result);
        return result;
    }

    protected void finished(Resource location)
    {
        if (fBuildQueue.isWaiting(location))
        {
            fBuildQueue.finished(location);
            fNotifier.processed(location);
        }
    }

    /**
     * Template extensions seen during a build are stored in the State Used by Incremental builds.
     */
    public void templateExtensionSeen(String extension)
    {
        if (!fSeenTemplateExtensions.contains(extension))
            fSeenTemplateExtensions.add(extension);
    }

    /**
     * Trigger fired whenever a type check occurs during a build. Type names are stored in the State
     * and used to by incremental builds
     */
    public void typeChecked(String fullyQualifiedName, IJavaType result)
    {
        if (result == null)
        {
            if (!fMissingTypes.contains(fullyQualifiedName))
                fMissingTypes.add(fullyQualifiedName);
        }
        else if (!result.isBinary() && !fFoundTypes.contains(result))
            fFoundTypes.add(result);

    }

    /**
     * A depenency listener that does nothing.
     * 
     * @author glongman@gmail.com
     */
    class NullDependencyListener implements IDependencyListener
    {
        /*
         * (non-Javadoc)
         * 
         * @see com.iw.plugins.spindle.core.builder.IDependencyListener#foundResourceDependency(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation,
         *      com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
         */
        public void foundResourceDependency(IResourceWorkspaceLocation dependant,
                IResourceWorkspaceLocation dependancy)
        {
            //do nothing
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.iw.plugins.spindle.core.builder.IDependencyListener#foundTypeDependency(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation,
         *      java.lang.String)
         */
        public void foundTypeDependency(IResourceWorkspaceLocation dependant,
                String fullyQualifiedTypeName)
        {
            //do nothing
        }
    }

    protected class BuilderDependencyListener implements IDependencyListener
    {

        Map dependencyMap;

        /*
         * (non-Javadoc)
         * 
         * @see com.iw.plugins.spindle.core.builder.IDependencyListener#foundResourceDependency(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation,
         *      com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
         */
        public void foundResourceDependency(IResourceWorkspaceLocation dependant,
                IResourceWorkspaceLocation dependancy)
        {
            if (dependant.isBinaryResource())
                return;

            if (dependencyMap == null)
                dependencyMap = new HashMap();

            getInfo(dependant).resourceDeps.add(dependancy);

        }

        private DependencyInfo getInfo(IResourceWorkspaceLocation dependant)
        {
            DependencyInfo info = (DependencyInfo) dependencyMap.get(dependant);
            if (info == null)
            {
                info = new DependencyInfo();
                dependencyMap.put(dependant, info);
            }
            return info;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.iw.plugins.spindle.core.builder.IDependencyListener#foundTypeDependency(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation,
         *      java.lang.String)
         */
        public void foundTypeDependency(IResourceWorkspaceLocation dependant,
                String fullyQualifiedTypeName)
        {
            if (dependant.isBinaryResource())
                return;

            if (dependencyMap == null)
                dependencyMap = new HashMap();

            getInfo(dependant).typeDeps.add(fullyQualifiedTypeName);
        }

        public void clear()
        {
            if (dependencyMap != null)
                dependencyMap.clear();
        }

        public void dump()
        {
            if (dependencyMap == null)
            {
                System.out.println("no deps found");
            }
            else
            {
                for (Iterator iter = dependencyMap.keySet().iterator(); iter.hasNext();)
                {
                    IResourceWorkspaceLocation element = (IResourceWorkspaceLocation) iter.next();
                    System.out.println("Deps for: " + element);

                    DependencyInfo info = getInfo(element);
                    for (Iterator iterator = info.resourceDeps.iterator(); iterator.hasNext();)
                    {
                        System.err.println(iterator.next());
                    }
                    for (Iterator iterator = info.typeDeps.iterator(); iterator.hasNext();)
                    {
                        System.err.println(iterator.next());
                    }
                }
            }
        }
    }

    protected class DependencyInfo
    {
        Set resourceDeps = new HashSet();

        Set typeDeps = new HashSet();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.IJavaTypeFinder#findType(java.lang.String)
     */
    public IJavaType findType(String fullyQualifiedName)
    {
        return fInfrastructure.findType(fullyQualifiedName);       
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.IJavaTypeFinder#isCachingJavaTypes()
     */
    public boolean isCachingJavaTypes()
    {       
        return fInfrastructure.isCachingJavaTypes();
    }

}