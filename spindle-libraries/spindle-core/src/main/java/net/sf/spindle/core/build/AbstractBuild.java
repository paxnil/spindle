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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.CoreStatus;
import net.sf.spindle.core.IProblemPersistManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.build.templates.ITemplateFinderListener;
import net.sf.spindle.core.build.templates.TemplateFinder;
import net.sf.spindle.core.namespace.CoreNamespace;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.parser.IDOMModelSource;
import net.sf.spindle.core.properties.DefaultProperties;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.scanning.ApplicationScanner;
import net.sf.spindle.core.scanning.ComponentScanner;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.IScannerValidatorListener;
import net.sf.spindle.core.scanning.LibraryScanner;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.scanning.SpecificationValidator;
import net.sf.spindle.core.scanning.TemplateScanner;
import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.SourceLocation;
import net.sf.spindle.core.spec.BaseSpecification;
import net.sf.spindle.core.spec.PluginApplicationSpecification;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.spec.PluginLibrarySpecification;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.util.Assert;

import org.apache.hivemind.Resource;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * Abstract base class for full and incremental builds
 * <p>
 * This class is not intended to be subclassed by clients
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractBuild implements IBuild, IScannerValidatorListener,
        ITemplateFinderListener
{

    private static ThreadLocal<BuilderDependencyListener> DEPENDENCY_LISTENER_HOLDER;

    protected static void setDependencyListener(BuilderDependencyListener listener)
    {
        if (DEPENDENCY_LISTENER_HOLDER == null)
            DEPENDENCY_LISTENER_HOLDER = new ThreadLocal<BuilderDependencyListener>();

        DEPENDENCY_LISTENER_HOLDER.set(listener);
    }

    public static IDependencyListener getDependencyListener()
    {
        if (DEPENDENCY_LISTENER_HOLDER == null)
            return null;

        return (IDependencyListener) DEPENDENCY_LISTENER_HOLDER.get();
    }

    // the sole app ns allowed in a Spindle project
    protected ICoreNamespace applicationNamespace;

    // all the namespaces rooted in the context.
    // will inclide appNamespace and any libraries in the context
    protected List<ICoreNamespace> contextNamespaces;

    // all the namespaces rooted in the classpath
    // will only be libraries.
    protected List<ICoreNamespace> classpathNamespaces;

    protected BuilderQueue buildQueue;

    protected List<IJavaType> foundTypes;

    protected CoreNamespace frameworkNamespace;

    protected State lastState;

    protected List<String> missingTypes;

    protected State newState;

    protected IBuildNotifier notifier;

    protected Map<Resource, BaseSpecification> processedLocations;

    // extensions actually processed
    protected Set<String> seenTemplateExtensions;

    // extensions declared in namepaces.
    protected Set<String> declaredTemplateExtensions;

    protected Set<String> declaredTemplateExtensionsClasspath;

    protected Map<Resource, BaseSpecification> templateMap;

    protected Map<Resource, BaseSpecification> fileSpecificationMap;

    protected Map<Resource, BaseSpecification> binarySpecificationMap; // binary specs (from jars)

    // never change

    protected AbstractBuildInfrastructure infrastructure;

    protected List<Resource> cleanTemplates; // templates that contain no implicit

    // components

    protected IJavaType tapestryServletType;

    protected ClashDetector clashDetector;

    protected IProblemPersistManager problemPersister;

    protected ITapestryProject tapestryProject;

    protected IResourceRoot classpathRoot;

    protected IResourceRoot contextRoot;

    protected IDOMModelSource domModelSource;

    protected boolean validateWebXML;

    protected IPropertySource projectPropertySource;

    private List<IPostBuildRunnable> postBuildRunnables;

    /**
     * @throws BuilderException
     *             a runtime exception thrown if the Tapestry application serlvet class can't be
     *             located
     */
    public AbstractBuild(AbstractBuildInfrastructure infrastructure)
    {
        this.infrastructure = infrastructure;
        this.tapestryProject = infrastructure.tapestryProject;
        this.classpathRoot = infrastructure.classpathRoot;
        this.contextRoot = infrastructure.contextRoot;
        this.domModelSource = infrastructure.domModelSource;
        this.projectPropertySource = infrastructure.projectPropertySource;
        this.validateWebXML = infrastructure.validateWebXML;
        this.newState = infrastructure.createEmptyState();
        this.notifier = infrastructure.notifier;
        this.foundTypes = new ArrayList<IJavaType>();
        this.missingTypes = new ArrayList<String>();
        this.processedLocations = new HashMap<Resource, BaseSpecification>();
        this.seenTemplateExtensions = new HashSet<String>();
        this.declaredTemplateExtensions = new HashSet<String>();
        this.declaredTemplateExtensionsClasspath = new HashSet<String>();
        this.templateMap = new HashMap<Resource, BaseSpecification>();
        this.fileSpecificationMap = new HashMap<Resource, BaseSpecification>();
        this.binarySpecificationMap = new HashMap<Resource, BaseSpecification>();
        this.contextNamespaces = new ArrayList<ICoreNamespace>();
        this.classpathNamespaces = new ArrayList<ICoreNamespace>();
        this.cleanTemplates = new ArrayList<Resource>();
        this.clashDetector = new ClashDetector();
        this.problemPersister = infrastructure.problemPersister;
        this.buildQueue = new BuilderQueue();
        this.tapestryServletType = tapestryProject.findType(BuilderMessages
                .getApplicationServletClassname());

        if (tapestryServletType == null)
            throw new BuilderException(BuilderMessages.noTapestryServlet());
    }

    public void build() throws BuilderException
    {
        try
        {
            preBuild();

            notifier.checkCancel();

            notifier.subTask(BuilderMessages.locatingNamespaces());

            frameworkNamespace = getFrameworkNamespace();

            applicationNamespace = getApplicationNamespace();

            checkForNamspaceClashes();

            notifier.checkCancel();

            notifier.updateProgressDelta(0.05f);

            notifier.subTask(BuilderMessages.locatingArtifacts());

            buildQueue.addAll(findAllTapestrySourceFiles().toArray());

            // we need to eliminate - mark as "processed" ns locations we already visited.

            for (ICoreNamespace ns : contextNamespaces)
            {
                buildQueue.finished(ns.getSpecificationLocation());
            }

            for (ICoreNamespace ns : classpathNamespaces)
            {
                buildQueue.finished(ns.getSpecificationLocation());
            }

            notifier.checkCancel();

            notifier.updateProgressDelta(0.05f);

            notifier.setProcessingProgressPer(0.9f / buildQueue.getWaitingCount());

            resolveFramework();

            notifier.checkCancel();

            CoreNamespace application = (CoreNamespace) applicationNamespace;
            resolveApplication(application.getAppNameFromWebXML(), application);

            notifier.checkCancel();

            notifier.updateProgressDelta(0.05f);

            postBuild();

            notifier.checkCancel();
        }
        finally
        {
            setDependencyListener(null);
        }

        notifier.updateProgressDelta(0.15f);

        CoreStatus missPreference = TapestryCore.getDefault().getBuildMissPriority();

        if (buildQueue.hasWaiting() && missPreference != CoreStatus.IGNORE)
        {
            int missPriority = missPreference.getPriority();
            while (buildQueue.getWaitingCount() > 0)
            {

                Object missed = buildQueue.peekWaiting();
                buildQueue.finished(missed);

                if (missPriority >= 0)
                    recordBuildMiss(missPriority, missed);

            }
        }
        // TODO remove
        System.out.println("template count:" + templateMap.keySet().size());
        System.out.println(" file specification count" + fileSpecificationMap.keySet().size());
        System.out.println(" binary specification count" + binarySpecificationMap.keySet().size());
        saveState();
    }

    public void addPostBuildRunnable(IPostBuildRunnable runnable)
    {
        if (postBuildRunnables == null)
            postBuildRunnables = new ArrayList<IPostBuildRunnable>();

        postBuildRunnables.add(runnable);
    }

    protected void checkForNamspaceClashes()
    {
        CoreStatus namespaceClashPriority = TapestryCore.getDefault().getNamespaceClashPriority();
        if (namespaceClashPriority == CoreStatus.IGNORE)
            return;
        IProblem[] problems;
        for (ICoreNamespace ns : contextNamespaces)
        {
            problems = ClashDetector.checkNamspaceClash(
                    ns,
                    contextNamespaces,
                    "contextNSKey",
                    namespaceClashPriority);

            recordProblems(((ICoreResource) ns.getSpecificationLocation()), problems);

        }

        for (ICoreNamespace ns : classpathNamespaces)
        {
            problems = ClashDetector.checkNamspaceClash(
                    ns,
                    classpathNamespaces,
                    "classpathNSKey",
                    namespaceClashPriority);

            recordProblems((ICoreResource) ns.getSpecificationLocation(), problems);
        }
    }

    @SuppressWarnings("unchecked")
    protected void recordBuildMiss(int missPriority, Object missed)
    {

        problemPersister.recordProblem(missed, new DefaultProblem(missPriority, BuilderMessages
                .buildMissMessage(missed.toString()), SourceLocation.FOLDER_LOCATION, false,
                IProblem.NOT_QUICK_FIXABLE));
    }

    protected void recordProblem(ICoreResource resource, IProblem problem)
    {
        recordProblems(resource, new IProblem[]
        { problem });
    }

    @SuppressWarnings("unchecked")
    protected void recordProblems(ICoreResource resource, IProblem[] problems)
    {
        Assert.isNotNull(problems);
        problemPersister.recordProblems(resource.getUnderlier(), problems);
    }

    protected abstract void saveState();

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.IBuild#cleanUp()
     */
    public void cleanUp()
    {
        lastState = null;
        foundTypes = null;
        missingTypes = null;
        processedLocations = null;
        seenTemplateExtensions = null;
        applicationNamespace = null;
        contextNamespaces = null;
        classpathNamespaces = null;
        frameworkNamespace = null;
        buildQueue = null;
        notifier = null;
        infrastructure = null;
        processedLocations = null;
        templateMap = null;
        binarySpecificationMap = null;
        fileSpecificationMap = null;
        cleanTemplates = null;
        clashDetector = null;
        problemPersister = null;
        tapestryProject = null;
        classpathRoot = null;
        contextRoot = null;
        domModelSource = null;
        projectPropertySource = null;
        postBuildRunnables = null;
    }

    protected ICoreNamespace getPreBuiltNamespace(Resource location)
    {
        if (lastState != null && ((ICoreResource) location).isBinaryResource()
                && lastState.fBinaryNamespaces.containsKey(location))
            return (ICoreNamespace) lastState.fBinaryNamespaces.get(location);

        return null;
    }

    // assumes id is valid and location exists.
    protected ICoreNamespace createNamespace(String id, ICoreResource location, String encoding)
    {
        ICoreNamespace result = null;

        PluginLibrarySpecification lib = null;

        String name = location.getName();

        if (name.endsWith(".application"))
            lib = (PluginLibrarySpecification) parseApplicationSpecification(location, encoding);
        else if (name.endsWith(".library"))
            lib = (PluginLibrarySpecification) parseLibrarySpecification(location, encoding);

        if (lib != null)
            result = new CoreNamespace(id, lib);

        lib.setNamespace(result);

        if (location.isClasspathResource())
            classpathNamespaces.add(result);
        else
            contextNamespaces.add(result);

        return result;
    }

    /**
     * Find all files in the project that have standard Tapestry extensions. This is less useful in
     * that users can override the extensions allowed for templates.
     * 
     * @return List a list containing all the Tapestry files in the project
     */
    protected List<Resource> findAllTapestrySourceFiles()
    {
        ArrayList<Resource> found = new ArrayList<Resource>();
        infrastructure.findAllTapestrySourceFiles(declaredTemplateExtensions, found);
        return found;
    }

    /**
     * Completely process an application specification file, recording any problems encountered.
     * <p>
     * If the entity referred to by the location is really a file in the workspace, the problems are
     * recorded as resource markers. Otherwise, the problem is logged.
     */

    protected IApplicationSpecification parseApplicationSpecification(Resource location,
            String encoding)
    {
        ICoreResource useLocation = (ICoreResource) location;
        IApplicationSpecification result = null;
        IDOMModel model = null;
        try
        {
            aboutToProcess(location);

            model = getDOMModel(useLocation, encoding == null ? "UTF-8" : encoding, false);
            // Node node = parseToNode(location);
            if (model != null)
            {
                ApplicationScanner scanner = new ApplicationScanner();
                scanner.setResourceLocation(useLocation);

                IScannerValidator useValidator = new SpecificationValidator(this, tapestryProject);

                useValidator.addListener(this);

                try
                {
                    result = (IApplicationSpecification) scanner.scan(model, useValidator);
                    recordProblems(useLocation, scanner.getProblems());
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
        // catch (CoreException e)
        // {
        // TapestryCore.log(e);
        // }
        catch (ScannerException e)
        {
            recordFatalProblem(useLocation, e);
        }
        finally
        {
            finished(useLocation);
            if (model != null)
                model.release();
        }
        return result;
    }

    protected void rememberSpecification(Resource location, BaseSpecification result)
    {
        if (AbstractBuildInfrastructure.DEBUG)
            System.out.println(location);
        
        ICoreResource coreResource = (ICoreResource) location;
        if (!coreResource.exists())
        {
            Throwable t = new Throwable();
            t.fillInStackTrace();
            TapestryCore.log("unexpected null storage", t);
            return;
        }
        if (coreResource.isBinaryResource())
            binarySpecificationMap.put(location, result);
        else
            fileSpecificationMap.put(location, result);

    }

    @SuppressWarnings("unchecked")
    protected void recordFatalProblem(ICoreResource location, ScannerException e)
    {
        problemPersister.recordProblem(location.getUnderlier(), new DefaultProblem(
                IProblem.TAPESTRY_FATAL_PROBLEM_MARKER, IProblem.ERROR, e.getMessage(),
                SourceLocation.FILE_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));
    }

    /**
     * Completely process a library specification file, recording any problems encountered.
     * <p>
     * If the entity referred to by the location is really a resource in the workspace, the problems
     * are recorded as resource markers. Otherwise, the problem is logged.
     */
    protected ILibrarySpecification parseLibrarySpecification(Resource location, String encoding)
    {
        ICoreResource useLocation = (ICoreResource) location;
        ILibrarySpecification result = null;
        if (processedLocations.containsKey(useLocation))
            return (ILibrarySpecification) processedLocations.get(useLocation);

        aboutToProcess(location);

        IDOMModel model = null;
        try
        {
            model = getDOMModel(useLocation, encoding == null ? "UTF-8" : encoding, false);

            if (model != null)
            {
                LibraryScanner scanner = new LibraryScanner();
                scanner.setResourceLocation(useLocation);

                IScannerValidator useValidator = new SpecificationValidator(this, tapestryProject);
                try
                {
                    useValidator.addListener(this);
                    result = (ILibrarySpecification) scanner.scan(model, useValidator);

                }
                finally
                {
                    useValidator.removeListener(this);
                }
                

                recordProblems(useLocation, scanner.getProblems());

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
        catch (ScannerException e)
        {
            recordFatalProblem(useLocation, e);
        }
        finally
        {
            finished(useLocation);
            if (model != null)
                model.release();
        }
        if (result != null)
        {
            processedLocations.put(useLocation, (BaseSpecification) result);
        }

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

        String jwcidAttributeName = getJWCIDAttributeName(spec);

        TemplateScanner scanner = new TemplateScanner();
        for (Iterator iter = spec.getTemplateLocations().iterator(); iter.hasNext();)
        {
            ICoreResource templateLocation = (ICoreResource) iter.next();

            if (processedLocations.containsKey(templateLocation))
                continue;

            aboutToProcess(templateLocation);
            try
            {
                if (!templateLocation.exists())
                    return;

                templateMap.put(templateLocation, spec);

                if (shouldParseTemplate(spec.getSpecificationLocation(), templateLocation))
                {
                    SpecificationValidator validator = new SpecificationValidator(this, tapestryProject);
                    try
                    {
                        validator.addListener(this);
                        scanner.scanTemplate(spec, templateLocation, jwcidAttributeName, validator);
                    }
                    finally
                    {
                        validator.removeListener(this);
                    }

                    recordProblems(templateLocation, scanner.getProblems());

                    if (!scanner.containsImplicitComponents()
                            && !templateLocation.isBinaryResource())
                        // we don't care if templates
                        // in jars are clean.
                        cleanTemplates.add(templateLocation);
                }
                processedLocations.put(templateLocation, null);
            }
            catch (ScannerException e)
            {
                TapestryCore.log(e);
            }
            // catch (CoreException e)
            // {
            // TapestryCore.log(e);
            // }
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

    protected IDOMModel getDOMModel(ICoreResource location, String encoding, boolean validate)
            throws IOException
    {
        if (!location.exists())
            throw new IOException(CoreMessages.resourceDoesNotExist(location));

        IDOMModel result = domModelSource.parseDocument(location, encoding, validate, this);

        recordProblems(location, result.getProblems());

        if (result.getDocument() == null)
        {
            result.release();
            result = null;
        }
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
    protected void postBuild()
    {
        if (postBuildRunnables != null)
        {
            for (Iterator iter = postBuildRunnables.iterator(); iter.hasNext();)
            {
                IPostBuildRunnable runnable = (IPostBuildRunnable) iter.next();
                try
                {
                    runnable.run(this);
                }
                catch (RuntimeException e)
                {
                    // don't want one runnable to bring down the whole build!
                    TapestryCore.log(e);
                }
            }
        }
    }

    /**
     * Resolve the Tapestry Framework namespace here. Library builds check to see if the library
     * being built *is* the framework library. If so it skips this step.
     */
    protected abstract void resolveFramework();

    protected abstract void resolveApplication(String name, CoreNamespace namespace);

    protected IComponentSpecification parseComponentSpecification(ICoreNamespace namespace,
            ICoreResource location, String encoding)

    {

        // to avoid double parsing specs that are accessible
        // by multiple means in Tapestry
        if (processedLocations.containsKey(location))
        {
            PluginComponentSpecification existing = (PluginComponentSpecification) processedLocations
                    .get(location);
            INamespace claimer = existing.getNamespace();
            if (!claimer.equals(namespace))
            {
                recordProblem(location, new DefaultProblem(IProblem.WARNING,
                        "namespace clash: already claimed by "
                                + claimer.getSpecificationLocation().toString(),
                        SourceLocation.FILE_LOCATION, true, IProblem.NOT_QUICK_FIXABLE));
                return null;
            }
            return existing;
        }

        PluginComponentSpecification result = null;
        IDOMModel model = null;
        if (location.exists())
        {
            aboutToProcess(location);

            try
            {
                model = getDOMModel(location, encoding == null ? "UTF-8" : encoding, false);
                // while editor reconcilers can re-use the scanner, we can't
                // here because scanning may invoke the scanning of another,
                // nested, component.

                if (model != null)
                {
                    ComponentScanner scanner = new ComponentScanner();
                    scanner.setResourceLocation(location);
                    scanner.setNamespace(namespace);
                    scanner.setPropertySource((IPropertySource) namespace);

                    IScannerValidator scanValidator = new SpecificationValidator(this,
                            tapestryProject);
                    scanValidator.addListener(this);
                    try
                    {
                        result = (PluginComponentSpecification) scanner.scan(model, scanValidator);
                    }
                    finally
                    {
                        scanValidator.removeListener(this);
                    }

                    recordProblems(location, scanner.getProblems());

                    rememberSpecification(location, result);
                }
                else
                {
                    PluginComponentSpecification dummy = new PluginComponentSpecification();
                    dummy.makePlaceHolder();
                    dummy.setSpecificationLocation(location);
                    dummy.setNamespace(namespace);
                    result = dummy;
                }

                String templateExtension = getComponentTemplateExtension(namespace, result);

                templateExtensionSeen(templateExtension);

                result.setTemplateLocations(TemplateFinder.scanForTemplates(
                        result,
                        templateExtension,
                        tapestryProject,
                        null));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ScannerException e)
            {
                recordFatalProblem(location, e);

            }
            finally
            {
                finished(location);
                if (model != null)
                    model.release();
            }
        }

        if (result != null)
            processedLocations.put(location, result);

        return result;
    }

    protected void scanComponentSpecificationAnnotations(PluginComponentSpecification specification)
    {
        // TODO implement!
    }

    protected String getComponentTemplateExtension(ICoreNamespace namespace,
            PluginComponentSpecification spec)
    {
        String templateExtension = spec.getProperty("org.apache.tapestry.template-extension");

        if (templateExtension == null)
            templateExtension = namespace.getSpecification().getProperty(
                    "org.apache.tapestry.template-extension");

        if (templateExtension == null)
            templateExtension = DefaultProperties.getInstance().getPropertyValue(
                    "org.apache.tapestry.template-extension");

        return templateExtension;
    }

    protected String getJWCIDAttributeName(PluginComponentSpecification spec)
    {
        String result = spec.getProperty("org.apache.tapestry.jwcid-attribute-name");

        if (result == null)
            result = spec.getNamespace().getSpecification().getProperty(
                    "org.apache.tapestry.jwcid-attribute-name");

        if (result == null)
            result = DefaultProperties.getInstance().getPropertyValue(
                    "org.apache.tapestry.jwcid-attribute-name");

        return result;
    }

    protected void aboutToProcess(Resource location)
    {
        notifier.aboutToProcess(location);
    }

    protected void finished(ICoreResource location)
    {
        buildQueue.finished(location.getUnderlier());
        notifier.processed(location);
    }

    /**
     * Template extensions seen during a build are stored in the State Used by Incremental builds.
     */
    public void templateExtensionSeen(String extension)
    {
        if (extension == null)
            return;
        seenTemplateExtensions.add(extension);
    }

    public void templateExtensionDeclared(String extension, boolean isClasspath)
    {
        if (extension == null)
            return;
        if (isClasspath)
            declaredTemplateExtensionsClasspath.add(extension);
        else
            declaredTemplateExtensions.add(extension);
    }

    /**
     * Trigger fired whenever a type check occurs during a build. Type names are stored in the State
     * and used to by incremental builds
     */
    public void typeChecked(String fullyQualifiedName, IJavaType result)
    {
        if (!result.exists())
        {
            if (!missingTypes.contains(fullyQualifiedName))
                missingTypes.add(fullyQualifiedName);
        }
        else if (!result.isBinary() && !foundTypes.contains(result))
            foundTypes.add(result);

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
         * @see core.builder.IDependencyListener#foundResourceDependency(core.resources.ICoreResource,
         *      core.resources.ICoreResource)
         */
        public void foundResourceDependency(Resource dependant, Resource dependancy)
        {
            // do nothing
        }

        /*
         * (non-Javadoc)
         * 
         * @see core.builder.IDependencyListener#foundTypeDependency(core.resources.ICoreResource,
         *      java.lang.String)
         */
        public void foundTypeDependency(Resource dependant, String fullyQualifiedTypeName)
        {
            // do nothing
        }
    }

    protected class BuilderDependencyListener implements IDependencyListener
    {

        Map<Resource, DependencyInfo> dependencyMap;

        /*
         * (non-Javadoc)
         * 
         * @see core.builder.IDependencyListener#foundResourceDependency(core.resources.ICoreResource,
         *      core.resources.ICoreResource)
         */
        public void foundResourceDependency(Resource dependant, Resource dependancy)
        {

            if (((ICoreResource) dependant).isBinaryResource())
                return;

            if (dependencyMap == null)
                dependencyMap = new HashMap<Resource, DependencyInfo>();

            getInfo(dependant).resourceDeps.add(dependancy);

        }

        private DependencyInfo getInfo(Resource dependant)
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
         * @see core.builder.IDependencyListener#foundTypeDependency(core.resources.ICoreResource,
         *      java.lang.String)
         */
        public void foundTypeDependency(Resource dependant, String fullyQualifiedTypeName)
        {
            if (((ICoreResource) dependant).isBinaryResource())
                return;

            if (dependencyMap == null)
                dependencyMap = new HashMap<Resource, DependencyInfo>();

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
                    ICoreResource element = (ICoreResource) iter.next();
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

    protected CoreNamespace getFrameworkNamespace()
    {

        ICoreResource frameworkLocation = (ICoreResource) classpathRoot
                .getRelativeResource("/org/apache/tapestry/Framework.library");

        CoreNamespace result = getNamespaceTree("framework", frameworkLocation, null);

        result.installBasePropertySource(DefaultProperties.getInstance());

        ILibrarySpecification spec = result.getSpecification();

        String templateExtension = spec.getProperty("org.apache.tapestry.template-extension");
        // if its not null, it has already been registered.
        // otherwise, we must register the default.
        if (templateExtension == null)
            templateExtensionDeclared(DefaultProperties.getInstance().getPropertyValue(
                    "org.apache.tapestry.template-extension"), true);

        return result;
    }

    protected ICoreNamespace getApplicationNamespace()
    {
        CoreNamespace ns = doGetApplicationNamespace();

        if (ns != null)
        {

            ns.installBasePropertySource(projectPropertySource);

            ILibrarySpecification spec = ns.getSpecification();

            String templateExtension = spec.getProperty("org.apache.tapestry.template-extension");
            // if its not null, it has already been registered.
            // otherwise, we must register the default.
            if (templateExtension == null)
                templateExtensionDeclared(DefaultProperties.getInstance().getPropertyValue(
                        "org.apache.tapestry.template-extension"), false);
        }

        return ns;
    }

    protected abstract CoreNamespace doGetApplicationNamespace();

    // returns unresolved namespace tree assumes id is valid and location exists.
    protected CoreNamespace getNamespaceTree(String namespaceId, ICoreResource location,
            String encoding)
    {
        CoreNamespace result = (CoreNamespace) createNamespace(namespaceId, location, encoding);

        if (result == null)
            return null;

        ILibrarySpecification nsSpec = result.getSpecification();

        templateExtensionDeclared(
                nsSpec.getProperty("org.apache.tapestry.template-extension"),
                location.isClasspathResource());

        for (Iterator iter = nsSpec.getLibraryIds().iterator(); iter.hasNext();)
        {
            String childId = (String) iter.next();

            ICoreResource childLocation;

            if (location.isClasspathResource())
                childLocation = (ICoreResource) location.getRelativeResource(nsSpec
                        .getLibrarySpecificationPath(childId));
            else
                childLocation = (ICoreResource) classpathRoot.getRelativeResource(nsSpec
                        .getLibrarySpecificationPath(childId));

            if (childLocation.exists())
            {
                ICoreNamespace nsChild = getNamespaceTree(childId, childLocation, encoding);
                if (nsChild != null)
                    result.installChildNamespace(childId, nsChild);
            }
        }
        return result;
    }

    protected class DependencyInfo
    {
        Set<Resource> resourceDeps = new HashSet<Resource>();

        Set<String> typeDeps = new HashSet<String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaTypeFinder#findType(java.lang.String)
     */
    public IJavaType findType(String fullyQualifiedName)
    {
        return infrastructure.findType(fullyQualifiedName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaTypeFinder#isCachingJavaTypes()
     */
    public boolean isCachingJavaTypes()
    {
        return infrastructure.isCachingJavaTypes();
    }

}