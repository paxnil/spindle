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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.Markers;

/**
 *  New Incremental Build - this one revalidates specs if thier underlying resource has not changed.
 *  Note: templates are not revalidated, they are parsed as per a regular build.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class IncrementalProjectBuild extends IncrementalApplicationBuild
{
    private IProblemCollector fProblemCollector = new ProblemCollector();

    public IncrementalProjectBuild(TapestryBuilder builder, IResourceDelta projectDelta)
    {
        super(builder, projectDelta);
    }

    protected IApplicationSpecification resolveApplication(
        Parser parser,
        IStorage storage,
        IResourceLocation location,
        String encoding)
    {
        IResourceWorkspaceLocation useLocation = (IResourceWorkspaceLocation) location;
        PluginApplicationSpecification result = null;

        if (!checkStorage(location, storage))
            return null;

        // pull the preexisting spec (if it exists) from the last build.       
        result = (PluginApplicationSpecification) fLastState.getSpecificationMap().get(storage);

        IFile file = null;

        if (storage instanceof IFile)
        {
            file = (IFile) storage;
            if (result == null || fileChanged(file))
            {
                Markers.removeProblemsFor(file);
                return super.resolveApplication(parser, storage, location, encoding);
            }
        } else
        {
            //this can only happen if somebody added a library tag to the .application file
            return super.resolveApplication(parser, storage, location, encoding);
        }

        try
        {
            //revalidate the spec.
            IScannerValidator useValidator = new BuilderValidator(this, fTypeFinder, true);
            useValidator.addListener(this);

            fProblemCollector.beginCollecting();
            useValidator.setProblemCollector(fProblemCollector);

            Markers.removeTemporaryProblemsForResource(file);
            try
            {
                result.validate(useValidator);
            } finally
            {
                fProblemCollector.endCollecting();
                useValidator.removeListener(this);
            }

            Markers.recordProblems(useLocation, fProblemCollector.getProblems());
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } finally
        {
            finished(useLocation);
        }

        rememberSpecification(storage, result);
        return result;
    }

    private boolean checkStorage(IResourceLocation location, IStorage storage)
    {
        if (storage == null)
        {
            finished(location);
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
      * @see com.iw.plugins.spindle.core.builder.Build#parseLibrary(com.iw.plugins.spindle.core.parser.Parser, org.apache.tapestry.IResourceLocation, java.lang.String)
      */
    protected ILibrarySpecification resolveLibrarySpecification(
        Parser parser,
        IStorage storage,
        IResourceLocation location,
        String encoding)
    {

        IResourceWorkspaceLocation useLocation = (IResourceWorkspaceLocation) location;
        PluginLibrarySpecification result = null;

        // to avoid double processing of specs that are accessible
        // by multiple means in Tapestry
        if (fProcessedLocations.containsKey(useLocation))
            return (ILibrarySpecification) fProcessedLocations.get(useLocation);

        if (!checkStorage(location, storage))
            return null;

        // pull the preexisting spec (if it exists) from the last build.
        result = (PluginLibrarySpecification) fLastState.getSpecificationMap().get(storage);

        IFile file = null;

        if (storage instanceof IFile)
        {
            file = (IFile) storage;
            if (result == null || fileChanged(file))
            {
                Markers.removeProblemsFor(file);
                return super.resolveLibrarySpecification(parser, storage, location, encoding);
            }
        } else
        {
            //this can only happen if somebody added a library tag to the .application file
            return super.resolveLibrarySpecification(parser, storage, location, encoding);
        }

        try
        {
            //revalidate the spec.
            IScannerValidator useValidator = new BuilderValidator(this, fTypeFinder, true);
            useValidator.addListener(this);

            fProblemCollector.beginCollecting();
            useValidator.setProblemCollector(fProblemCollector);

            Markers.removeTemporaryProblemsForResource(file);
            try
            {
                result.validate(useValidator);
            } finally
            {
                fProblemCollector.endCollecting();
                useValidator.removeListener(this);
            }

            Markers.recordProblems(useLocation, fProblemCollector.getProblems());
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } finally
        {
            finished(useLocation);
        }
        rememberSpecification(storage, result);
        fProcessedLocations.put(location, result);

        return result;

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.Build#resolveIComponentSpecification(com.iw.plugins.spindle.core.parser.Parser, com.iw.plugins.spindle.core.namespace.ICoreNamespace, com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation, java.lang.String)
     */
    protected IComponentSpecification resolveIComponentSpecification(
        Parser parser,
        ICoreNamespace namespace,
        IStorage storage,
        IResourceWorkspaceLocation location,
        String encoding)
    {
        PluginComponentSpecification result = null;

        // to avoid double processing of specs that are accessible
        // by multiple means in Tapestry
        if (fProcessedLocations.containsKey(location))
            return (IComponentSpecification) fProcessedLocations.get(location);

        if (!checkStorage(location, storage))
            return null;

        // pull the preexisting spec (if it exists) from the last build.
        result = (PluginComponentSpecification) fLastState.getSpecificationMap().get(storage);

        IFile file = null;

        if (storage instanceof IFile)
        {
            file = (IFile) storage;
            if (result == null || fileChanged(file))
            {
                Markers.removeProblemsFor(file);
                return super.resolveIComponentSpecification(parser, namespace, file, location, encoding);
            }
        } else
        {
            //this can only happen if somebody added a library tag to the .application file
            return super.resolveIComponentSpecification(parser, namespace, file, location, encoding);
        }

        try
        {
            //revalidate the spec.
            IScannerValidator useValidator = new BuilderValidator(this, fTypeFinder, true);
            useValidator.addListener(this);

            fProblemCollector.beginCollecting();
            useValidator.setProblemCollector(fProblemCollector);

            Markers.removeTemporaryProblemsForResource(file);
            try
            {
                result.validate(useValidator);
                result.setTemplateLocations(TapestryBuilder.scanForTemplates(result, fProblemCollector));
                for (Iterator iter = result.getTemplateLocations().iterator(); iter.hasNext();)
                {
                    IResourceWorkspaceLocation template = (IResourceWorkspaceLocation) iter.next();
                    try
                    {
                        IFile templateFile = (IFile) template.getStorage();
                        Markers.removeProblemsFor(templateFile);
                    } catch (ClassCastException e1)
                    {
                        //Ignore -  its a binary resource;
                    }
                }

            } finally
            {
                fProblemCollector.endCollecting();
                useValidator.removeListener(this);
            }

            Markers.recordProblems(location, fProblemCollector.getProblems());
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } finally
        {
            finished(location);
        }
        rememberSpecification(storage, result);
        fProcessedLocations.put(location, result);

        return result;
    }

    /* (non-Javadoc)
      * @see com.iw.plugins.spindle.core.builder.Build#parseTemplates(com.iw.plugins.spindle.core.spec.PluginComponentSpecification)
      */
    protected void parseTemplates(PluginComponentSpecification spec)
    {

        List locs = spec.getTemplateLocations();
        int count = locs.size();

        for (int i = 0; i < count; i++)
        {
            IResourceWorkspaceLocation template = (IResourceWorkspaceLocation) locs.get(i);
            IStorage templateStorage = template.getStorage();
            if (templateStorage != null && templateStorage instanceof IResource)
            {
                Markers.removeProblemsFor((IResource) templateStorage);
            }
        }

        super.parseTemplates(spec);
    }

    /**
      *  Here we override to check
      *  if the template really needs to be parsed.
      * 
      *  template must be clean
      * and not have changed
      * and its owner spec must no have changed
      * 
      * @param ownerSpec the specification that owns this template
      * @param template the IStorage for the template
      * @return true iff the template should be parsed (expensive)
      */
    protected boolean shouldParseTemplate(IStorage ownerSpec, IStorage template)
    {
        boolean mustParse = false;
        boolean cleanLastBuild = fLastState.fCleanTemplates.contains(template);

        if (template instanceof IFile)
        {
            IFile file = (IFile) template;
            if (!cleanLastBuild || fileChanged(file))
            {
                mustParse = true;
            } else if (ownerSpec instanceof IFile)
            {
                mustParse = fileChanged((IFile) ownerSpec);
            }
        }

        if (!mustParse)
            fCleanTemplates.add(template);

        return mustParse;
    }

    /**
     * @param file
     * @return
     */
    private boolean fileChanged(IFile file)
    {
        IResourceDelta specDelta = fProjectDelta.findMember(file.getProjectRelativePath());

        if (specDelta != null)
            return specDelta.getKind() != IResourceDelta.NO_CHANGE;

        return false;
    }

    class ProblemCollector implements IProblemCollector
    {

        private List fProblems = new ArrayList();

        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.source.IProblemCollector#addProblem(int, com.iw.plugins.spindle.core.source.ISourceLocation, java.lang.String, boolean)
        */
        public void addProblem(int severity, ISourceLocation location, String message, boolean isTemporary)
        {

            addProblem(
                new DefaultProblem(
                    ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
                    severity,
                    message,
                    location.getLineNumber(),
                    location.getCharStart(),
                    location.getCharEnd(),
                    isTemporary));

        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.source.IProblemCollector#addProblem(com.iw.plugins.spindle.core.source.IProblem)
         */
        public void addProblem(IProblem problem)
        {
            fProblems.add(problem);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.source.IProblemCollector#beginCollecting()
         */
        public void beginCollecting()
        {
            fProblems.clear();
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.source.IProblemCollector#endCollecting()
         */
        public void endCollecting()
        {
            // do nothing
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.source.IProblemCollector#getProblems()
         */
        public IProblem[] getProblems()
        {
            return (IProblem[]) fProblems.toArray(new IProblem[fProblems.size()]);
        }

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.Build#recordBuildMiss(int, org.eclipse.core.resources.IResource)
     */
    protected void recordBuildMiss(int missPriority, IResource resource)
    {
        Markers.removeProblemsFor(resource);
        super.recordBuildMiss(missPriority, resource);
    }

}
