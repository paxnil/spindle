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

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.ApplicationScanner;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Markers;
import com.iw.plugins.spindle.core.util.Utils;
/**
 * Abstract base class for full and incremental builds
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public abstract class Build implements IBuild
{

    private static final Parser BUILD_PARSER = new Parser();
    protected IJavaProject fJavaProject;
    protected State fNewState;
    protected BuildNotifier fNotifier;
    protected Parser fParser;
    protected TapestryBuilder fTapestryBuilder;
    protected ICoreNamespace fFramework;

    public Build(TapestryBuilder builder)
    {
        fTapestryBuilder = builder;
        fNewState = new State(builder);
        this.fNotifier = builder.fNotifier;
        this.fJavaProject = builder.fJavaProject;
        this.fParser = BUILD_PARSER;
    }

    protected ICoreNamespace createNamespace(String id, IResourceWorkspaceLocation location)
    {
        ICoreNamespace result = null;

        ILibrarySpecification lib = null;
        String name = location.getName();
        if (name.endsWith(".application"))
        {
            lib = parseApplication(location);
        } else if (name.endsWith(".library"))
        {
            lib = parseLibrary(location);
        }
        if (lib != null)
            result = new CoreNamespace(id, lib);

        return result;
    }

    protected IApplicationSpecification parseApplication(IResourceLocation location)
    {
        try
        {
            Node node = parseToNode(location);
            if (node != null)
            {
                ApplicationScanner scanner = new ApplicationScanner();
                scanner.setResourceLocation(location);
                scanner.setFactory(TapestryCore.getSpecificationFactory());
                IApplicationSpecification result =
                    (IApplicationSpecification) scanner.scan(fParser, new BuilderValidator(this), node);
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

    //    protected IStorage findInPackage(IPackageFragment pack, String filename)
    //    {
    //        IPackageFragmentRoot root = (IPackageFragmentRoot) pack.getParent();
    //        try
    //        {
    //            int packageFlavor = root.getKind();
    //            switch (packageFlavor)
    //            {
    //                case IPackageFragmentRoot.K_BINARY :
    //
    //                    return findInBinaryPackage(pack, filename);
    //                case IPackageFragmentRoot.K_SOURCE :
    //                    return findInSourcePackage(pack, filename);
    //            }
    //        } catch (JavaModelException e)
    //        {
    //            TapestryCore.log(e);
    //        }
    //        return null;
    //    }
    //
    //    protected IStorage findInBinaryPackage(IPackageFragment pack, String filename)
    //    {
    //        Object[] jarFiles = null;
    //        try
    //        {
    //            jarFiles = pack.getNonJavaResources();
    //        } catch (JavaModelException npe)
    //        {
    //            return null; // the package is not present
    //        }
    //        int length = jarFiles.length;
    //        for (int i = 0; i < length; i++)
    //        {
    //            JarEntryFile jarFile = null;
    //            try
    //            {
    //                jarFile = (JarEntryFile) jarFiles[i];
    //            } catch (ClassCastException ccex)
    //            { //skip it
    //                continue;
    //            }
    //            if (jarFile.getName().equals(filename))
    //            {
    //                return (IStorage) jarFile;
    //            }
    //        }
    //        return null;
    //    }
    //
    //    protected IStorage findInSourcePackage(IPackageFragment pack, String filename)
    //    {
    //        Object[] files = null;
    //        try
    //        {
    //            files = pack.getNonJavaResources();
    //        } catch (CoreException npe)
    //        {
    //            return null; // the package is not present
    //        }
    //        if (files != null)
    //        {
    //            int length = files.length;
    //            for (int i = 0; i < length; i++)
    //            {
    //                IFile file = null;
    //                try
    //                {
    //                    file = (IFile) files[i];
    //                } catch (ClassCastException ccex)
    //                { // skip it
    //                    continue;
    //                }
    //                if (file.getName().equals(filename))
    //                {
    //                    return (IStorage) file;
    //                }
    //            }
    //        }
    //        return null;
    //    }

    protected ILibrarySpecification parseLibrary(IResourceLocation location)
    {
        try
        {
            Node node = parseToNode(location);
            if (node != null)
            {
                LibraryScanner scanner = new LibraryScanner();
                scanner.setResourceLocation(location);
                scanner.setFactory(TapestryCore.getSpecificationFactory());
                ILibrarySpecification result =
                    (ILibrarySpecification) scanner.scan(BUILD_PARSER, new BuilderValidator(this), node);
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

    protected Node parseToNode(IResourceLocation location) throws IOException, CoreException
    {
        IResourceWorkspaceLocation use_loc = (IResourceWorkspaceLocation) location;
        IStorage storage = use_loc.getStorage();
        if (storage != null)
        {
            return parseToNode(storage);
        } else
        {
            throw new IOException(TapestryCore.getString("core-resource-does-not-exist", location));
        }
    }

    protected Node parseToNode(IStorage storage) throws IOException, CoreException
    {
        Node result = null;
        try
        {
            result = fParser.parse(storage);

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
            Markers.addTapestryProblemMarkersToResource(((IResource) storage), fParser.getProblems());
        } else
        {
            TapestryCore.logProblems(storage, fParser.getProblems());
        }

        if (fParser.getHasFatalErrors())
            return null;

        return result;
    }

    protected IComponentSpecification resolveComponent(String type, ICoreNamespace namespace) throws BuilderException
    {
        IComponentSpecification result = namespace.getComponentSpecification(type);
        if (result == null)
        {
            ILibrarySpecification namespaceSpec = namespace.getSpecification();

            IResourceWorkspaceLocation location = null;
            String specPath = namespaceSpec.getPageSpecificationPath(type);
            if (specPath != null)
            {
                location =
                    (IResourceWorkspaceLocation) namespaceSpec.getSpecificationLocation().getRelativeLocation(specPath);
                if (!location.exists())
                    return null;

            }
            if (location == null)
                location = null; // find page using any funny rules!

            result = resolveIComponentSpecification(namespace, location);

            if (result != null)
            {
                if (result.isPageSpecification())
                {
                    throw new BuilderException("expected component but got page");
                } else
                {
                    namespace.installComponentSpecification(type, result);
                }
            }
        }

        return result;
    }

    protected IComponentSpecification resolveIComponentSpecification(
        ICoreNamespace namespace,
        IResourceWorkspaceLocation location)
    {
        IComponentSpecification result = null;
        if (location != null)
            if (location.exists())
            {
                try
                {
                    Node node = parseToNode(location);
                    if (node != null)
                    {
                        ComponentScanner scanner = new ComponentScanner();
                        scanner.setResourceLocation(location);
                        scanner.setFactory(TapestryCore.getSpecificationFactory());
                        try
                        {
                            result =
                                (IComponentSpecification) scanner.scan(
                                    fParser,
                                    new BuilderValidator(this, namespace),
                                    node);
                            ((PluginComponentSpecification) result).setNamespace(namespace);
                        } catch (ScannerException e1)
                        {
                            e1.printStackTrace();
                        }
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
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (CoreException e)
                {
                    e.printStackTrace();
                }

            }
        return result;
    }

    protected IComponentSpecification resolvePage(String pageName, ICoreNamespace namespace) throws BuilderException
    {
        IComponentSpecification result = namespace.getPageSpecification(pageName);
        if (result == null)
        {
            ILibrarySpecification namespaceSpec = namespace.getSpecification();

            IResourceWorkspaceLocation location = null;
            String specPath = namespaceSpec.getPageSpecificationPath(pageName);
            if (specPath != null)
            {
                location =
                    (IResourceWorkspaceLocation) namespaceSpec.getSpecificationLocation().getRelativeLocation(specPath);
                if (!location.exists())
                    return null;

            }
            if (location == null)
                location = null; // find page using any funny rules!

            result = resolveIComponentSpecification(namespace, location);
        }
        if (result != null)
        {
            if (result.isPageSpecification())
            {
                throw new BuilderException("expected page but got component");
            } else
            {
                namespace.installPageSpecification(pageName, result);
            }
        }
        return result;
    }

}
