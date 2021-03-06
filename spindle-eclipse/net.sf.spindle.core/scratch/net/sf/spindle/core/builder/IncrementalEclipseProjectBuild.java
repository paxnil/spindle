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

package net.sf.spindle.core.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.spindle.core.build.templates.TemplateFinder;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.eclipse.IEclipseResource;
import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.scanning.SpecificationValidator;
import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.IProblemCollector;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.spec.PluginApplicationSpecification;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.spec.PluginLibrarySpecification;
import net.sf.spindle.core.types.IJavaTypeFinder;
import net.sf.spindle.core.util.eclipse.Markers;

import org.apache.hivemind.Resource;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;


/**
 * New Incremental AbstractBuild - this one revalidates specs if thier underlying resource has not
 * changed. Note: templates are not revalidated, they are parsed as per a regular build.
 * 
 * @author glongman@gmail.com
 */
public class IncrementalEclipseProjectBuild extends AbstractIncrementalEclipseBuild
{
    private IProblemCollector fProblemCollector = new ProblemCollector();

    public IncrementalEclipseProjectBuild(EclipseBuildInfrastructure builder,
            IResourceDelta projectDelta)
    {
        super(builder, projectDelta);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuild#resolveApplication(core.parser.Parser,
     *      org.eclipse.core.resources.IStorage, org.apache.hivemind.Resource, java.lang.String)
     */
    protected IApplicationSpecification parseApplicationSpecification(Resource location, String encoding)
    {
        IEclipseResource useLocation = (IEclipseResource) location;
        PluginApplicationSpecification result = null;

        if (!checkResource(useLocation))
            return null;

        // pull the preexisting spec (if it exists) from the last build.
        result = (PluginApplicationSpecification) lastState.getSpecificationMap().get(location);

        IStorage storage = useLocation.getStorage();
        IFile file = null;

        if (storage instanceof IFile)
        {
            file = (IFile) storage;
            if (result == null || result.isPlaceholder() || fileChanged(file))
            {
                Markers.removeProblemsFor(file);
                return super.parseApplicationSpecification(location, encoding);
            }
        }
        else
        {
            // this can only happen if somebody added a library tag to the
            // .application file
            return super.parseApplicationSpecification(location, encoding);
        }

        try
        {
            // revalidate the spec.
            IScannerValidator useValidator = new SpecificationValidator(
                    tapestryProject);
            useValidator.addListener(this);

            fProblemCollector.beginCollecting();
            useValidator.setProblemCollector(fProblemCollector);

            problemPersister.removeTemporaryProblemsFor(file);
            try
            {
                result.validate(useValidator);
            }
            finally
            {
                fProblemCollector.endCollecting();
                useValidator.removeListener(this);
            }

            problemPersister.recordProblems(useLocation, fProblemCollector.getProblems());
        }
        finally
        {
            finished(useLocation);
        }

        rememberSpecification(useLocation, result);
        return result;
    }

    private boolean checkResource(ICoreResource location)
    {
        if (!location.exists())
        {
            finished(location);
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuild#resolveLibrarySpecification(core.parser.Parser,
     *      org.eclipse.core.resources.IStorage, org.apache.hivemind.Resource, java.lang.String)
     */
    protected ILibrarySpecification parseLibrarySpecification(Resource location, String encoding)
    {

        IEclipseResource useLocation = (IEclipseResource) location;
        PluginLibrarySpecification result = null;

        // to avoid double processing of specs that are accessible
        // by multiple means in Tapestry
        if (processedLocations.containsKey(useLocation))
            return (ILibrarySpecification) processedLocations.get(useLocation);

        if (!checkResource(useLocation))
            return null;

        // pull the preexisting spec (if it exists) from the last build.
        result = (PluginLibrarySpecification) lastState.getSpecificationMap().get(useLocation);

        IStorage storage = useLocation.getStorage();

        IFile file = null;

        if (storage instanceof IFile)
        {
            file = (IFile) storage;
            if (result == null || result.isPlaceholder() || fileChanged(file))
            {
                problemPersister.removeAllProblemsFor(file);
                return super.parseLibrarySpecification(location, encoding);
            }
        }
        else
        {
            // this can only happen if somebody added a library tag to the
            // .application file
            return super.parseLibrarySpecification(location, encoding);
        }

        try
        {
            // revalidate the spec.
            IScannerValidator useValidator = new SpecificationValidator((IJavaTypeFinder) this,
                    tapestryProject);
            useValidator.addListener(this);

            fProblemCollector.beginCollecting();
            useValidator.setProblemCollector(fProblemCollector);

            problemPersister.removeTemporaryProblemsFor(file);
            try
            {
                result.validate(useValidator);
            }
            finally
            {
                fProblemCollector.endCollecting();
                useValidator.removeListener(this);
            }

            problemPersister.recordProblems(useLocation, fProblemCollector.getProblems());
        }
        finally
        {
            finished(useLocation);
        }
        rememberSpecification(useLocation, result);
        processedLocations.put(location, result);

        return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuild#resolveIComponentSpecification(core.parser.Parser,
     *      core.namespace.ICoreNamespace,
     *      org.eclipse.core.resources.IStorage,
     *      core.resources.ICoreResource, java.lang.String)
     */
    protected IComponentSpecification parseComponentSpecification(ICoreNamespace namespace,
            ICoreResource location, String encoding)
    {
        PluginComponentSpecification result = null;

        // to avoid double processing of specs that are accessible
        // by multiple means in Tapestry
        if (processedLocations.containsKey(location))
            return (IComponentSpecification) processedLocations.get(location);

        if (!checkResource(location))
            return null;

        // pull the preexisting spec (if it exists) from the last build.
        result = (PluginComponentSpecification) lastState.getSpecificationMap().get(location);

        IFile file = (IFile) (((IEclipseResource) location).getStorage()).getAdapter(IFile.class);
        if (file != null)
        {
            if (result == null || result.isPlaceholder() || fileChanged(file))
            {
                Markers.removeProblemsFor(file);
                return super.parseComponentSpecification(
                        namespace,
                        location,
                        encoding);
            }
        }
        else
        {
            // this can only happen if somebody added a library tag to the
            // .application file
            // so the resolve is the same as the full build and we stop here.
            return super.parseComponentSpecification(
                    namespace,
                    location,
                    encoding);
        }

        try
        {
            // revalidate the spec.
            IScannerValidator useValidator = new SpecificationValidator((IJavaTypeFinder) this,
                    tapestryProject);
            useValidator.addListener(this);

            fProblemCollector.beginCollecting();
            useValidator.setProblemCollector(fProblemCollector);

            problemPersister.removeTemporaryProblemsFor(file);
            try
            {
                result.validate(useValidator);
                List<Resource> oldTemplates = new ArrayList<Resource>(result.getTemplateLocations());
                IPropertySource source = infrastructure.createPropertySource(result);

                String seek_extension = source
                        .getPropertyValue("org.apache.tapestry.template-extension");
                
                result.setTemplateLocations(TemplateFinder.scanForTemplates(
                        result,
                        seek_extension,
                        tapestryProject,
                        fProblemCollector));

                oldTemplates.addAll(result.getTemplateLocations());
                for (Iterator iter = oldTemplates.iterator(); iter.hasNext();)
                {
                    IEclipseResource template = (IEclipseResource) iter.next();
                    try
                    {
                        IFile templateFile = (IFile) template.getStorage();
                        Markers.removeProblemsFor(templateFile);
                    }
                    catch (ClassCastException e1)
                    {
                        // Ignore - its a binary resource;
                    }
                }

            }
            finally
            {
                fProblemCollector.endCollecting();
                useValidator.removeListener(this);
            }

            problemPersister.recordProblems(location, fProblemCollector.getProblems());
        }
        finally
        {
            finished(location);
        }
        rememberSpecification(location, result);
        processedLocations.put(location, result);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuild#parseTemplates(core.spec.PluginComponentSpecification)
     */
    protected void parseTemplates(PluginComponentSpecification spec)
    {
        List locs = spec.getTemplateLocations();
        int count = locs.size();

        for (int i = 0; i < count; i++)
        {
            IEclipseResource template = (IEclipseResource) locs.get(i);
            IStorage templateStorage = template.getStorage();
            if (templateStorage != null && templateStorage instanceof IResource)
                Markers.removeProblemsFor((IResource) templateStorage);
        }
        super.parseTemplates(spec);
    }

    /**
     * Here we override to check if the template really needs to be parsed. template must be clean
     * and not have changed and its owner spec must no have changed
     * 
     * @param ownerSpec
     *            the specification that owns this template
     * @param template
     *            the IStorage for the template
     * @return true iff the template should be parsed (expensive)
     */
    protected boolean shouldParseTemplate(Resource ownerSpec, Resource template)
    {
        IFile specFile = (IFile) (((IEclipseResource) ownerSpec).getStorage())
                .getAdapter(IFile.class);
        IFile templateFile = (IFile) (((IEclipseResource) ownerSpec).getStorage())
                .getAdapter(IFile.class);

        boolean mustParse = false;
        boolean cleanLastBuild = lastState.fCleanTemplates.contains(template);

        if (templateFile != null)
        {
            if (!cleanLastBuild || fileChanged(templateFile))
            {
                mustParse = true;
            }
            else if (specFile != null)
            {
                mustParse = fileChanged(specFile);
            }
        }

        if (!mustParse)
            cleanTemplates.add(template);

        return mustParse;
    }

    /**
     * @param file
     * @return
     */
    private boolean fileChanged(IFile file)
    {
        IResourceDelta specDelta = projectDelta.findMember(file.getProjectRelativePath());

        if (specDelta != null)
            return specDelta.getKind() != IResourceDelta.NO_CHANGE;

        return false;
    }

    class ProblemCollector implements IProblemCollector
    {

        private List<IProblem> fProblems = new ArrayList<IProblem>();

        /*
         * (non-Javadoc)
         * 
         * @see core.source.IProblemCollector#addProblem(int,
         *      core.source.ISourceLocation, java.lang.String, boolean)
         */
        public void addProblem(int severity, ISourceLocation location, String message,
                boolean isTemporary, int code)
        {

            addProblem(new DefaultProblem(severity, message, location, isTemporary, code));

        }

        /*
         * (non-Javadoc)
         * 
         * @see core.source.IProblemCollector#addProblem(core.source.IProblem)
         */
        public void addProblem(IProblem problem)
        {
            fProblems.add(problem);
        }

        /*
         * (non-Javadoc)
         * 
         * @see core.source.IProblemCollector#beginCollecting()
         */
        public void beginCollecting()
        {
            fProblems.clear();
        }

        /*
         * (non-Javadoc)
         * 
         * @see core.source.IProblemCollector#endCollecting()
         */
        public void endCollecting()
        {
            // do nothing
        }

        /*
         * (non-Javadoc)
         * 
         * @see core.source.IProblemCollector#getProblems()
         */
        public IProblem[] getProblems()
        {
            return (IProblem[]) fProblems.toArray();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.AbstractBuild#recordBuildMiss(int,
     *      org.eclipse.core.resources.IResource)
     */
    protected void recordBuildMiss(int missPriority, Resource resource)
    {
        problemPersister.removeTemporaryProblemsForResource(resource);
        super.recordBuildMiss(missPriority, resource);
    }

}