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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.templates.ITemplateFinderListener;
import com.iw.plugins.spindle.core.scanning.ApplicationScanner;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidatorListener;
import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.TemplateScanner;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Markers;
import com.iw.plugins.spindle.core.util.Utils;
/**
 * Abstract base class for full and incremental builds
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public abstract class Build implements IIncrementalBuild, IScannerValidatorListener, ITemplateFinderListener
{

//    private static final Parser BUILD_PARSER = new Parser();
    protected IJavaProject fJavaProject;
    protected State fNewState;
    protected BuildNotifier fNotifier;
    protected BuilderQueue fBuildQueue;
//    protected Parser fParser;
    protected TapestryBuilder fTapestryBuilder;
    protected ICoreNamespace fFrameworkNamespace;
    protected ICoreNamespace fApplicationNamespace;
    protected BuilderValidator fValidator;
    protected State fLastState;

    protected List fFoundTypes;
    protected List fMissingTypes;
    protected Map fProcessedLocations;
    protected List fSeenTemplateExtensions;

    public Build(TapestryBuilder builder)
    {
        fTapestryBuilder = builder;
        fNewState = new State(builder);
        fBuildQueue = new BuilderQueue();
        fNotifier = builder.fNotifier;
        fJavaProject = builder.fJavaProject;
//        fParser = BUILD_PARSER;
        fValidator = new BuilderValidator(this);
        fValidator.addListener(this);
        fFoundTypes = new ArrayList();
        fMissingTypes = new ArrayList();
        fProcessedLocations = new HashMap();
        fSeenTemplateExtensions = new ArrayList();
        TapestryArtifactManager.getTapestryArtifactManager().addTemplateFinderListener(this);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IIncrementalBuild#canIncrementalBuild(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean canIncrementalBuild(IResourceDelta projectDelta)
    {
        fLastState = fTapestryBuilder.getLastState(fTapestryBuilder.fCurrentProject);
        if (fLastState == null || fLastState.fBuildNumber < 0 || fLastState.fVersion != State.VERSION)
            return false;

        IResourceWorkspaceLocation frameworkLocation =
            (IResourceWorkspaceLocation) fTapestryBuilder.fClasspathRoot.getRelativeLocation(
                "/org/apache/tapestry/Framework.library");
        if (!fLastState.fBinaryNamespaces.containsKey(frameworkLocation))
            return false;

        if (hasClasspathChanged())
            return false;

        return true;

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IIncrementalBuild#canIncrementalBuild(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean needsIncrementalBuild(IResourceDelta projectDelta)
    {
        fLastState = fTapestryBuilder.getLastState(fTapestryBuilder.fCurrentProject);
        final List knownTapestryExtensions = Arrays.asList(fTapestryBuilder.KnownExtensions);

        // check for java files that changed, or have been added
        try
        {
            projectDelta.accept(new IResourceDeltaVisitor()
            {
                public boolean visit(IResourceDelta delta) throws CoreException
                {
                    IResource resource = delta.getResource();

                    if (resource instanceof IContainer)
                        return true;

                    IPath path = resource.getFullPath();
                    String extension = path.getFileExtension();

                    if (fLastState.fSeenTemplateExtensions.contains(extension))
                        throw new NeedToBuildException();

                    if (fLastState.fJavaDependencies.contains(resource) || knownTapestryExtensions.contains(extension))
                    {
                        throw new NeedToBuildException();
                    } else
                    {

                        if (!"java".equals(extension))
                            return true;

                        String name = path.removeFileExtension().lastSegment();
                        IContainer container = resource.getParent();
                        IJavaElement element = (IPackageFragment) JavaCore.getJavaCore().create((IFolder) container);
                        if (element == null)
                            return true;
                        if (element instanceof IPackageFragmentRoot && fLastState.fMissingJavaTypes.contains(name))
                        {
                            throw new NeedToBuildException();
                        } else if (
                            fLastState.fMissingJavaTypes.contains(
                                ((IPackageFragment) element).getElementName() + "." + name))
                        {
                            throw new NeedToBuildException();
                        }

                    }

                    return true;
                }
            });
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } catch (NeedToBuildException e)
        {
            return true;
        }
        return false;

    }

    protected boolean hasClasspathChanged()
    {
        IClasspathEntry[] currentEntries = fTapestryBuilder.fClasspath;

        if (currentEntries.length != fLastState.fLastKnownClasspath.length)
            return true;

        List old = Arrays.asList(fLastState.fLastKnownClasspath);
        List current = Arrays.asList(currentEntries);

        return !current.containsAll(old);
    }

    protected ICoreNamespace createNamespace(Parser parser, String id, IResourceWorkspaceLocation location)
    {
        ICoreNamespace result = null;

        ILibrarySpecification lib = null;
        String name = location.getName();
        if (name.endsWith(".application"))
        {
            lib = parseApplication(parser, location);
        } else if (name.endsWith(".library"))
        {
            lib = parseLibrary(parser, location);
        }
        if (lib != null)
            result = new CoreNamespace(id, lib);

        return result;
    }

    protected IApplicationSpecification parseApplication(Parser parser, IResourceLocation location)
    {
        try
        {
            Node node = parseToNode(parser, location);
//            Node node = parseToNode(location);
            if (node != null)
            {
                ApplicationScanner scanner = new ApplicationScanner();
                scanner.setResourceLocation(location);
                scanner.setFactory(TapestryCore.getSpecificationFactory());
                scanner.setPublicId(parser.getPublicId());
//                scanner.setPublicId(fParser.getPublicId());

                IApplicationSpecification result = (IApplicationSpecification) scanner.scan(node, fValidator);
                IResource res = Utils.toResource(location);
                if (res != null)
                {
                    Markers.addTapestryProblemMarkersToResource(res, scanner.getProblems());
                } else
                {
                    TapestryCore.logProblems(
                        ((IResourceWorkspaceLocation) location).getStorage(),
                        scanner.getProblems());
                }
                return result;
            }
        } catch (IOException e)
        {
            TapestryCore.log(e);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } catch (ScannerException e)
        {
            TapestryCore.log(e);
        }
        return null;
    }

    protected ILibrarySpecification parseLibrary(Parser parser, IResourceLocation location)
    {
        if (fProcessedLocations.containsKey(location))
            return (ILibrarySpecification) fProcessedLocations.get(location);

        try
        {
            Node node = parseToNode(parser, location);
//            Node node = parseToNode(location);
            if (node != null)
            {
                LibraryScanner scanner = new LibraryScanner();
                scanner.setResourceLocation(location);
                scanner.setFactory(TapestryCore.getSpecificationFactory());
//                scanner.setPublicId(fParser.getPublicId());
                scanner.setPublicId(parser.getPublicId());
                ILibrarySpecification result = (ILibrarySpecification) scanner.scan(node, fValidator);
                if (result != null)
                    fProcessedLocations.put(location, result);

                IResource res = Utils.toResource(location);
                if (res != null)
                {
                    Markers.addTapestryProblemMarkersToResource(res, scanner.getProblems());
                } else
                {
                    TapestryCore.logProblems(
                        ((IResourceWorkspaceLocation) location).getStorage(),
                        scanner.getProblems());
                }
                return result;
            }
        } catch (IOException e)
        {
            TapestryCore.log(e);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } catch (ScannerException e)
        {
            TapestryCore.log(e);
        }
        return null;
    }

    protected Node parseToNode(Parser parser, IResourceLocation location) throws IOException, CoreException
    {
        IResourceWorkspaceLocation use_loc = (IResourceWorkspaceLocation) location;
        IStorage storage = use_loc.getStorage();
        if (storage != null)
        {
            return parseToNode(parser, storage);
        } else
        {
            throw new IOException(TapestryCore.getString("core-resource-does-not-exist", location));
        }
    }

    protected Node parseToNode(Parser parser, IStorage storage) throws IOException, CoreException
    {
        Node result = null;
        try
        {
//            result = fParser.parse(storage);
            result = parser.parse(storage);

        } catch (CoreException e)
        {
            TapestryCore.log(e);
            throw e;
        } catch (IOException e)
        {
            TapestryCore.log(e);
            throw e;
        }

        if (storage.getAdapter(IResource.class) != null)
        {
            Markers.addTapestryProblemMarkersToResource(((IResource) storage), parser.getProblems());
//            Markers.addTapestryProblemMarkersToResource(((IResource) storage), fParser.getProblems());
        } else
        {
//            TapestryCore.logProblems(storage, fParser.getProblems());
            TapestryCore.logProblems(storage, parser.getProblems());
        }

        if (parser.getHasFatalErrors())
//        if (fParser.getHasFatalErrors())
            return null;

        return result;
    }

    private ComponentScanner fComponentScanner = new ComponentScanner();

    protected IComponentSpecification resolveIComponentSpecification(
        Parser parser,
        ICoreNamespace namespace,
        IResourceWorkspaceLocation location)
    {
        // to avoid double parsing specs that are accessible
        // by multiple means in Tapestry
        if (fProcessedLocations.containsKey(location))
            return (IComponentSpecification) fProcessedLocations.get(location);

        IComponentSpecification result = null;
        if (location != null)
            if (location.exists())
            {
                try
                {
                    Node node = parseToNode(parser, location);

                    fComponentScanner.setResourceLocation(location);
                    fComponentScanner.setNamespace(namespace);
                    fComponentScanner.setPublicId(parser.getPublicId());
                    fComponentScanner.setFactory(TapestryCore.getSpecificationFactory());
                    if (node != null)
                    {
                        try
                        {
                            IScannerValidator useValidator = new BuilderValidator(this, namespace);
                            useValidator.addListener(this);
                            result = (IComponentSpecification) fComponentScanner.scan(node, useValidator);
                        } catch (ScannerException e1)
                        {
                            e1.printStackTrace();
                        }
                    } else
                    {
                        PluginComponentSpecification dummy = new PluginComponentSpecification();
                        dummy.setSpecificationLocation(location);
                        dummy.setNamespace(namespace);
                        fComponentScanner.scanForTemplates(dummy);
                        result = dummy;
                    }
                    IResource res = Utils.toResource(location);
                    if (res != null)
                    {
                        Markers.addTapestryProblemMarkersToResource(res, fComponentScanner.getProblems());
                    } else
                    {
                        TapestryCore.logProblems(
                            ((IResourceWorkspaceLocation) location).getStorage(),
                            fComponentScanner.getProblems());
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (CoreException e)
                {
                    e.printStackTrace();
                } finally
                {
                    if (fBuildQueue.isWaiting(location))
                    {
                        fBuildQueue.finished(location);
                        fNotifier.processed(location);
                    }

                }

            }
        if (result != null)
            fProcessedLocations.put(location, result);
        return result;
    }

    protected void parseTemplates(PluginComponentSpecification spec)
    {
        TemplateScanner scanner = new TemplateScanner();
        scanner.setFactory(TapestryCore.getSpecificationFactory());

        for (Iterator iter = spec.getTemplateLocations().iterator(); iter.hasNext();)
        {
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) iter.next();
            if (fProcessedLocations.containsKey(location))
                continue;
            try
            {
                scanner.scanTemplate(spec, location, fValidator);

                IResource res = Utils.toResource(location);
                if (res != null)
                {
                    Markers.addTapestryProblemMarkersToResource(res, scanner.getProblems());
                } else
                {
                    TapestryCore.logProblems(
                        ((IResourceWorkspaceLocation) location).getStorage(),
                        scanner.getProblems());
                }
                fProcessedLocations.put(location, null);
            } catch (ScannerException e)
            {
                TapestryCore.log(e);
            } finally
            {
                if (fBuildQueue.isWaiting(location))
                {
                    fBuildQueue.finished(location);
                    fNotifier.processed(location);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IBuild#cleanUp()
     */
    public void cleanUp()
    {
        fLastState = null;
        fFoundTypes.clear();
        fMissingTypes.clear();
        fProcessedLocations.clear();
        fSeenTemplateExtensions.clear();
        TapestryArtifactManager.getTapestryArtifactManager().removeTemplateFinderListener(this);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidatorListener#typeChecked(java.lang.String, org.eclipse.jdt.core.IType)
     */
    public void typeChecked(String fullyQualifiedName, IType result)
    {
        if (result == null)
        {
            if (!fMissingTypes.contains(fullyQualifiedName))
                fMissingTypes.add(fullyQualifiedName);
        } else if (!result.isBinary())
        {
            try
            {
                IResource resource = result.getUnderlyingResource();
                if (!fFoundTypes.contains(resource))
                    fFoundTypes.add(resource);
            } catch (JavaModelException e)
            {
                TapestryCore.log(e);
            }

        }

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.templates.ITemplateFinderListener#templateExtensionSeen(java.lang.String)
     */
    public void templateExtensionSeen(String extension)
    {
        if (!fSeenTemplateExtensions.contains(extension))
            fSeenTemplateExtensions.add(extension);

    }

    private static class NeedToBuildException extends RuntimeException
    {
        public NeedToBuildException()
        {
            super();
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.IBuild#build()
     */
    public void build() throws BuilderException
    {
        // TODO Auto-generated method stub

    }

}
