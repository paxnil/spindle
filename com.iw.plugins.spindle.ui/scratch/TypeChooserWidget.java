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

package com.iw.plugins.spindle.ui.widgets;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ISearchPattern;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.CoreUtils;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class TypeChooserWidget extends TwoListSearchWidget
{

    Object[] empty = new Object[0];

    private SearchFilter fSearchFilter;
    private List fSearchResults;
    private IType fResultType;
    private IType fHierarchyRoot;

    public TypeChooserWidget(IJavaProject project, IType hierarchyRoot, IRunnableContext context)
    {
        super();
        Assert.isNotNull(hierarchyRoot);
        fHierarchyRoot = hierarchyRoot;
        configure(project, context);
        fSearchFilter = new SearchFilter();
        TypeProvider typeProvider = new TypeProvider();
        setUpperListLabelProvider(typeProvider);
        setUpperListContentProvider(typeProvider);
        setLowerListLabel(UIPlugin.getString("fix me"));
        PackageProvider packageProvider = new PackageProvider();
        setLowerListLabelProvider(packageProvider);
        setLowerListContentProvider(packageProvider);
    }

    public ISelection getSelection()
    {
        IStructuredSelection selection = (IStructuredSelection) super.getSelection();
        if (selection == null || selection.isEmpty())
        {
            return selection;
        }
        Object[] selectionData = selection.toArray();

        String name = (String) selectionData[0];
        IPackageFragment fragment = (IPackageFragment) selectionData[1];

        fResultType = (IType) fragment.getCompilationUnit(name);
        if (fResultType == null)
            fResultType = (IType) fragment.getClassFile(name);
        return new StructuredSelection(fResultType);
    }

    public void refresh()
    {
        if (fSearchFilter == null)
        {
            return;

        } else
        {
            super.refresh();
        }
    }

    public void configure(final IJavaProject project, final IRunnableContext context)
    {
        try
        {
            final boolean baseIsInterface = fHierarchyRoot.isInterface();
            fSearchResults = new ArrayList();
            IRunnableWithProgress runnable = new IRunnableWithProgress()
            {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    IJavaSearchScope scope = null;
                    try
                    {
                        // we want to rip out the JRE entry as there's no chance of finding a tapestry class there.
                        ArrayList roots = new ArrayList();
                        IClasspathEntry[] classpath = project.getRawClasspath();
                        for (int i = 0; i < classpath.length; i++)
                        {
                            if (classpath[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
                            {
                                IPath cpPath = classpath[i].getPath();
                                if (JavaRuntime.JRE_CONTAINER.equals(cpPath.segment(0)))
                                    continue;
                            }
                            roots.add(project.findPackageFragmentRoots(classpath[i]));
                        }

                        scope =
                            SearchEngine.createJavaSearchScope(
                                (IJavaElement[]) roots.toArray(new IJavaElement[roots.size()]),
                                true);

                    } catch (JavaModelException e1)
                    {
                        scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, true);
                    }
                    ISearchPattern pattern =
                        SearchEngine.createSearchPattern(
                            "*",
                            IJavaSearchConstants.TYPE,
                            IJavaSearchConstants.DECLARATIONS,
                            true);

                    SearchEngine engine = new SearchEngine();
                    IJavaSearchResultCollector collector = new IJavaSearchResultCollector()
                    {
                        public void aboutToStart()
                        {}

                        public void accept(
                            IResource resource,
                            int start,
                            int end,
                            IJavaElement enclosingElement,
                            int accuracy)
                            throws CoreException
                        {
                            if (accuracy != EXACT_MATCH)
                                return;

                            if (enclosingElement.getElementType() != IJavaElement.TYPE)
                                return;

                            IType type = (IType) enclosingElement;
                            System.out.println(type.getFullyQualifiedName());

                            if (baseIsInterface)
                            {
                                if (!CoreUtils.implementsInterface(type, fHierarchyRoot.getElementName()))
                                    return;
                            } else
                            {
                                if (!CoreUtils.extendsType(type, fHierarchyRoot))
                                    return;
                            }

                            System.out.println(type.getFullyQualifiedName());

                            fSearchResults.add(type);
                        }

                        public void done()
                        {}

                        public IProgressMonitor getProgressMonitor()
                        {
                            return monitor;
                        }
                    };
                    try
                    {
                        engine.search(ResourcesPlugin.getWorkspace(), pattern, scope, collector);
                    } catch (JavaModelException e)
                    {
                        UIPlugin.log(e);
                    }
                }
            };

            context.run(false, true, runnable);
        } catch (InvocationTargetException e)
        {
            UIPlugin.log(e);
        } catch (InterruptedException e)
        {
            //do nothing;
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }
    }

    public IType getResultType()
    {
        return fResultType;
    }

    class TypeProvider extends JavaElementLabelProvider implements IStructuredContentProvider
    {
        public Object[] getElements(Object inputElement)
        {
            String searchFilter = (String) inputElement;
            if (searchFilter == null || searchFilter.trim().length() == 0)
            {
                return empty;
            }
            fSearchFilter.scan(searchFilter.trim());
            return fSearchFilter.getMatchingTypes();
        }

        public void dispose()
        {}

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {}

    }

    class PackageProvider extends JavaElementLabelProvider implements IStructuredContentProvider
    {
        public Object[] getElements(Object inputElement)
        {
            String selectedName = (String) inputElement;
            if (selectedName == null)
            {
                return empty;
            }
            return fSearchFilter.getPackagesFor(selectedName);
        }
        public void dispose()
        {}
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {}
    }

    class SearchFilter
    {
        String matchString;

        List types;
        Map packageLookup;

        public SearchFilter()
        {
            types = new ArrayList();
            packageLookup = new HashMap();
        }

        public void reset()
        {
            types.clear();
            packageLookup.clear();
        }

        public List getTypes()
        {
            return types;
        }

        private void scan(String matchString)
        {
            if (matchString == null)
            {
                reset();
            } else
            {
                if (matchString.equals(this.matchString))
                    return;
            }

            this.matchString = matchString;

            for (Iterator iter = fSearchResults.iterator(); iter.hasNext();)
            {
                IType type = (IType) iter.next();
                String typeName = type.getElementName();
                if (match(typeName))
                {
                    types.add(type);
                    IPackageFragment fragment = (IPackageFragment) type.getPackageFragment();
                    Set namePackages = (Set) packageLookup.get(typeName);
                    if (namePackages == null)
                    {
                        namePackages = new HashSet();
                        namePackages.add(fragment);
                        packageLookup.put(typeName, namePackages);
                    } else
                    {
                        namePackages.add(fragment);
                    }
                }

            }
        }

        private boolean match(String name)
        {
            if ("*".equals(matchString))
            {
                return true;
            }
            return name.startsWith(matchString);
        }

        public Object[] getMatchingTypes()
        {
            if (types == null)
            {
                return empty;
            }
            return new TreeSet(packageLookup.keySet()).toArray();
        }

        public Object[] getPackagesFor(String name)
        {
            Set packages = (Set) packageLookup.get(name);
            if (packages == null)
            {
                return empty;
            }
            return packages.toArray();
        }

    }

}