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

import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.util.Markers;
/**
 * Abstract base class for full and incremental builds
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public abstract class Build implements IBuild
{

    private static final Parser BUILD_PARSER = new Parser();
    protected TapestryBuilder tapestryBuilder;
    protected State newState;
    protected BuildNotifier notifier;
    protected Parser parser;
    protected IJavaProject javaProject;

    public Build(TapestryBuilder builder)
    {
        tapestryBuilder = builder;
        newState = new State(builder);
        this.notifier = builder.notifier;
        this.javaProject = builder.javaProject;
        this.parser = BUILD_PARSER;
    }

    protected Node parseToNode(IResourceWorkspaceLocation location) throws IOException, CoreException
    {
        IStorage storage = location.getStorage();
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
        } else
        {
            TapestryCore.logProblems(storage, parser.getProblems());
        }

        return result;
    }

    protected IStorage findInPackage(IPackageFragment pack, String filename)
    {
        IPackageFragmentRoot root = (IPackageFragmentRoot) pack.getParent();
        try
        {
            int packageFlavor = root.getKind();
            switch (packageFlavor)
            {
                case IPackageFragmentRoot.K_BINARY :

                    return findInBinaryPackage(pack, filename);
                case IPackageFragmentRoot.K_SOURCE :
                    return findInSourcePackage(pack, filename);
            }
        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return null;
    }

    protected IStorage findInBinaryPackage(IPackageFragment pack, String filename)
    {
        Object[] jarFiles = null;
        try
        {
            jarFiles = pack.getNonJavaResources();
        } catch (JavaModelException npe)
        {
            return null; // the package is not present
        }
        int length = jarFiles.length;
        for (int i = 0; i < length; i++)
        {
            JarEntryFile jarFile = null;
            try
            {
                jarFile = (JarEntryFile) jarFiles[i];
            } catch (ClassCastException ccex)
            { //skip it
                continue;
            }
            if (jarFile.getName().equals(filename))
            {
                return (IStorage) jarFile;
            }
        }
        return null;
    }

    protected IStorage findInSourcePackage(IPackageFragment pack, String filename)
    {
        Object[] files = null;
        try
        {
            files = pack.getNonJavaResources();
        } catch (CoreException npe)
        {
            return null; // the package is not present
        }
        if (files != null)
        {
            int length = files.length;
            for (int i = 0; i < length; i++)
            {
                IFile file = null;
                try
                {
                    file = (IFile) files[i];
                } catch (ClassCastException ccex)
                { // skip it
                    continue;
                }
                if (file.getName().equals(filename))
                {
                    return (IStorage) file;
                }
            }
        }
        return null;
    }

    protected ILibrarySpecification parseLibrary(IResourceWorkspaceLocation location)
    {
        try
        {
            Node node = parseToNode(location);
            LibraryScanner scanner = new LibraryScanner();
            return (ILibrarySpecification)scanner.scan(BUILD_PARSER, null, node);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ScannerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
