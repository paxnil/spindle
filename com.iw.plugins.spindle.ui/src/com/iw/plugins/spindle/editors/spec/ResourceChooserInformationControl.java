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

package com.iw.plugins.spindle.editors.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.resources.AbstractRootLocation;
import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.editors.spec.assist.ChooseResourceProposal;
import com.iw.plugins.spindle.editors.util.BusyIndicatorSpindle;

/**
 * InformationControl for choosing Assets
 * 
 * @author glongman@gmail.com
 */
public class ResourceChooserInformationControl extends TreeInformationControl
{

    class Sorter extends ViewerSorter
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
         */
        public int category(Object element)
        {
            if (element instanceof IContainer)
                return 1;

            if (element instanceof IPackageFragment)
            {
                IProject current = fRootLocations[0].getProject();
                try
                {
                    IPackageFragment fragment = (IPackageFragment) element;
                    IProject fproject = fragment.getJavaProject().getProject();
                    boolean isSource = fragment.getKind() == IPackageFragmentRoot.K_SOURCE;
                    boolean sameProject = current.equals(fproject);

                    if (isSource)
                    {
                        if (sameProject)
                            return 2;
                        else
                            return 4;
                    }
                    else
                    {
                        if (sameProject)
                            return 3;
                        else
                            return 5;

                    }
                }
                catch (JavaModelException e)
                {
                    UIPlugin.log(e);
                }
            }
            return 0;
        }

    }

    class UserFilter extends NamePatternFilter
    {

        public boolean select(Viewer viewer, Object parentElement, Object element)
        {
            if (element instanceof IStorage)
            {
                String extension = ((IStorage) element).getFullPath().getFileExtension();

                if (extension != null)
                {
                    ChooseResourceProposal.Filter exclusionFilter = fProposal
                            .getExtensionExclusionFilter();
                    if (exclusionFilter.matches(extension))
                        return false;
                    ChooseResourceProposal.Filter inclusionFilter = fProposal
                            .getExtensionlnclusionFilter();
                    return inclusionFilter.matches(extension)
                            && super.select(viewer, parentElement, element);
                    // if ("page".equals(extension)
                    // || "library".equals(extension)
                    // || "application".equals(extension)
                    // || "class".equals(extension))
                    // return false;
                }

            }
            if (element instanceof IContainer || element instanceof IJavaElement)
            {
                return hasUnfilteredChild(viewer, element);
            }

            return super.select(viewer, parentElement, element);
        }
    }

    abstract class BusyRunnable implements Runnable
    {

        protected Object runnableResult;

        public final void run()
        {
            runnableResult = doRun();
        }

        public abstract Object doRun();

        public Object getResult()
        {
            return runnableResult;
        }

    }

    private static Object[] EMPTY = new Object[] {};

    class ContentProvider implements ITreeContentProvider
    {

        AbstractRootLocation[] roots;
        
        protected Object [] elementsCache;

        protected Map childrenCache = new HashMap();

        protected Map parentCache = new HashMap();

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
            roots = (AbstractRootLocation[]) newInput;           
            clear();

        }

        public Object[] getChildren(Object parentElement)
        {
            if (parentElement instanceof IStorage)
                return EMPTY;

            return (Object[]) childrenCache.get(parentElement);

        }

        public Object getParent(Object element)
        {

            return parentCache.get(element);
        }

        public boolean hasChildren(Object element)
        {
            return getChildren(element).length > 0;
        }

        public Object[] getElements(Object inputElement)
        {
            if (elementsCache != null)
                return elementsCache;
            
            AbstractRootLocation[] roots = ((AbstractRootLocation[]) inputElement);

            if (roots == null || roots.length == 0)
                return new Object[] {};

            ArrayList allRoots = new ArrayList();

            if (fClasspathRoot != null)
            {
                BusyRunnable runnable = new BusyRunnable()
                {
                    public Object doRun()
                    {
                        return findClasspathElements(fClasspathRoot);
                    }
                };

                BusyIndicatorSpindle.showWhile(null, runnable);

                allRoots.addAll(Arrays.asList((Object[]) runnable.getResult()));
            }

            if (fContextRoot != null)
            {
                allRoots.addAll(Arrays.asList((Object[]) findWorkspaceElements(fContextRoot)));
            }

            elementsCache = allRoots.toArray();
            return elementsCache;
        }

        public void dispose()
        {
            elementsCache = null;
            childrenCache = null;
            parentCache = null;
        }

        public void clear()
        {
            elementsCache = null;
            childrenCache.clear();
            parentCache.clear();
        }

        protected Object[] findClasspathElements(ClasspathRootLocation root)
        {

            ArrayList resultElements = new ArrayList();
            Map tempPackageMap = new HashMap();
            // iterate over the package fragment roots
            // a root is included iff it contains at least one package with at least
            // one non java resource.
            try
            {
                extractPackagesAndResources(root, tempPackageMap);
                if (!tempPackageMap.isEmpty())
                {
                    for (Iterator iter = tempPackageMap.entrySet().iterator(); iter.hasNext();)
                    {
                        Map.Entry entry = (Map.Entry) iter.next();
                        PackageHolder holder = (PackageHolder) entry.getValue();
                        Object[] nonjava = holder.getNonJavaResources();

                        if (nonjava == null)
                            continue;

                        IPackageFragment fragment = holder.fragment;
                        resultElements.add(fragment);
                        childrenCache.put(fragment, nonjava);

                        for (int i = 0; i < nonjava.length; i++)
                            parentCache.put(nonjava[i], fragment);
                    }
                }
            }
            catch (JavaModelException e)
            {
                UIPlugin.log(e);
            }

            return resultElements.toArray();
        }

        private void extractPackagesAndResources(ClasspathRootLocation rootLocation, Map packageMap)
                throws JavaModelException
        {

            IJavaProject jproject = rootLocation.getJavaProject();
            IPackageFragmentRoot[] roots = jproject.getAllPackageFragmentRoots();

            for (int i = 0; i < roots.length; i++)
            {
                IJavaElement[] childElements = roots[i].getChildren();

                if (childElements == null || childElements.length == 0)
                    continue;

                for (int j = 0; j < childElements.length; j++)
                {
                    if (!(childElements[j] instanceof IPackageFragment))
                        continue;

                    IPackageFragment fragment = (IPackageFragment) childElements[j];

                    if (fragment.isDefaultPackage())
                        continue;                  

                    String fragmentName = fragment.getElementName();
                    ChooseResourceProposal.Filter f = fProposal.getPackageExclusionFilter();

                    if (f.matches(fragmentName))
                        continue;
                    // if (fragmentName.startsWith("com.sun")
                    // || fragmentName.startsWith("java.")
                    // || fragmentName.startsWith("sun.")
                    // || fragmentName.startsWith("javax.")
                    // || fragmentName.startsWith("META-INF")
                    // || fragmentName.startsWith("CVS"))
                    // continue;
                    PackageHolder holder = (PackageHolder) packageMap.get(fragmentName);

                    if (holder == null)
                    {
                        holder = new PackageHolder(fragment);
                        packageMap.put(fragmentName, holder);
                    }

                    Object[] children = null;
                    
                    try
                    {
                       children =  ClasspathRootLocation.getNonJavaResources(fragment);
                    }
                    catch (CoreException e)
                    {
                        //Do nothing!
                    }

                    if (children == null || children.length == 0)
                        continue;

                    for (int k = 0; k < children.length; k++)
                        holder.addNonJavaResource(children[k]);                                    
                }
            }

        }

        private Object[] findWorkspaceElements(ContextRootLocation root)
        {
            ArrayList elements = new ArrayList();
            IContainer container = root.getContainer();
            visitContainer(elements, container);

            return elements.toArray();

        }

        private void visitContainer(List elements, IContainer container)
        {
            ChooseResourceProposal.Filter fileExclusionFilter = fProposal.getFileExclusionFilter();
            ChooseResourceProposal.Filter containerExclusionFilter = fProposal
                    .getContainerExclusionFilter();
            ChooseResourceProposal.Filter extensionExclusionFilter = fProposal
                    .getExtensionExclusionFilter();
            ChooseResourceProposal.Filter extensionInclusionFilter = fProposal
                    .getExtensionlnclusionFilter();
            try
            {
                ArrayList nonContainerChildren = new ArrayList();

                Object[] members = container.members();
                if (members == null || members.length == 0)
                    return;

                for (int i = 0; i < members.length; i++)
                {
                    if (members[i] instanceof IContainer)
                    {

                        IContainer memberContainer = (IContainer) members[i];
                        String memberName = memberContainer.getName();

                        if (containerExclusionFilter.matches(memberName))
                            continue;
                        // if ("CVS".equals(memberName))
                        // continue;

                        if ("classes".equals(memberName) && "WEB-INF".equals(container.getName()))
                            continue;

                        visitContainer(elements, memberContainer);
                    }
                    else
                    {

                        IResource resource = (IResource) members[i];
                        if (fileExclusionFilter.matches(resource.getName()))
                            continue;
                        // if ("package.html".equals(resource.getName()))
                        // continue;
                        String extension = resource.getFileExtension();
                        if (extension != null)
                        {
                            if (extensionExclusionFilter.matches(extension))
                                continue;

                            if (!extensionInclusionFilter.matches(extension))
                                continue;
                            // && ("page".equals(extension)
                            // || "library".equals(extension)
                            // || "application".equals(extension)
                            // || "class".equals(extension))
                            // || "jar".equals(extension)) continue;
                        }
                        nonContainerChildren.add(members[i]);
                    }
                }

                if (!nonContainerChildren.isEmpty())
                {
                    elements.add(container);
                    Object[] children = nonContainerChildren.toArray();
                    childrenCache.put(container, children);
                    for (int i = 0; i < children.length; i++)
                    {
                        parentCache.put(children[i], container);
                    }
                }
            }
            catch (CoreException e)
            {
                UIPlugin.log(e);
            }
        }

        class PackageHolder
        {
            IPackageFragment fragment;

            List names;

            List nonjava;

            ChooseResourceProposal.Filter fileExclusionFilter = fProposal.getFileExclusionFilter();

            ChooseResourceProposal.Filter containerExclusionFilter = fProposal
                    .getContainerExclusionFilter();

            ChooseResourceProposal.Filter extensionExclusionFilter = fProposal
                    .getExtensionExclusionFilter();

            ChooseResourceProposal.Filter extensionInclusionFilter = fProposal
                    .getExtensionlnclusionFilter();

            PackageHolder(IPackageFragment fragment)
            {
                this.fragment = fragment;
            }

            void addNonJavaResource(Object object)
            {
                IStorage storage = (IStorage) object;
                String name = storage.getName();
                if (fileExclusionFilter.matches(name))
                    return;
                // if ("package.html".equals(name))
                // return;
                String extension = storage.getFullPath().getFileExtension();
                if (extension != null)
                {
                    if (extensionExclusionFilter.matches(extension))
                        return;

                    if (!extensionInclusionFilter.matches(extension))
                        return;
                    // && ("page".equals(extension)
                    // || "library".equals(extension)
                    // || "application".equals(extension)
                    // || "class".equals(extension))
                    // || "jar".equals(extension)) continue;
                }
                if (names == null || !names.contains(name))
                {
                    if (names == null)
                    {
                        names = new ArrayList();
                        nonjava = new ArrayList();
                    }
                    names.add(name);
                    nonjava.add(object);
                }
            }

            Object[] getNonJavaResources()
            {
                if (nonjava == null)
                    return null;
                return nonjava.toArray();
            }
        }

    }

    class LabelProvider implements ILabelProvider
    {

        JavaElementLabelProvider javaVerboseLabels = new JavaElementLabelProvider(
                JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_ROOT);

        JavaElementLabelProvider javaBriefLabels = new JavaElementLabelProvider(
                JavaElementLabelProvider.SHOW_DEFAULT);

        ILabelProvider workbenchLabels = WorkbenchLabelProvider
                .getDecoratingWorkbenchLabelProvider();

        public Image getImage(Object element)
        {
            if (element instanceof IJavaElement)
                return javaVerboseLabels.getImage(element);
            if (element instanceof IStorage)
                return Images.getSharedImage("file_obj.gif");
            return workbenchLabels.getImage(element);
        }

        public String getText(Object element)
        {
            if (element instanceof IJavaElement)
                return getPackageLabel((IPackageFragment) element);
            if (element instanceof IContainer)
                return getContainerLabel((IContainer) element);
            if (element instanceof IStorage)
                return ((IStorage) element).getName();
            return workbenchLabels.getText(element);
        }

        private String getPackageLabel(IPackageFragment fragment)
        {

            IProject current = fClasspathRoot.getProject();
            try
            {

                IProject fproject = fragment.getJavaProject().getProject();
                boolean isSource = fragment.getKind() == IPackageFragmentRoot.K_SOURCE;
                boolean sameProject = current.equals(fproject);

                if (isSource && sameProject)
                {
                    return javaBriefLabels.getText(fragment);
                }
            }
            catch (JavaModelException e)
            {
                UIPlugin.log(e);
            }

            return javaVerboseLabels.getText(fragment);
        }

        private String getContainerLabel(IContainer container)
        {
            IContainer root = fContextRoot.getContainer();

            if (container == root)
                return "/";

            IPath rootpath = root.getFullPath();
            IPath folderpath = container.getFullPath();
            folderpath = folderpath.removeFirstSegments(rootpath.segmentCount());
            return folderpath.makeAbsolute().toString();
        }

        public void addListener(ILabelProviderListener listener)
        { //
        }

        public void dispose()
        {
            javaVerboseLabels.dispose();
            workbenchLabels.dispose();
        }

        public boolean isLabelProperty(Object element, String property)
        {
            if (element instanceof IJavaElement)
                return javaVerboseLabels.isLabelProperty(element, property);
            return workbenchLabels.isLabelProperty(element, property);
        }

        public void removeListener(ILabelProviderListener listener)
        { //
        }

    }

    private SpecEditor fEditor;

    private ChooseResourceProposal fProposal;

    private AbstractRootLocation[] fRootLocations;

    private ContextRootLocation fContextRoot;

    private ClasspathRootLocation fClasspathRoot;

    public ResourceChooserInformationControl(Shell parent, int shellStyle, int treeStyle,
            SpecEditor editor)
    {
        super(parent, shellStyle, treeStyle);
        setContentProvider(new ContentProvider());
        setLabelProvider(new LabelProvider());
        setSorter(new Sorter());
        fEditor = editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.spec.TreeInformationControl#doGotoSelectedElement(java.lang.Object)
     */
    protected boolean doHandleSelectedElement(Object selected)
    {
        if (selected instanceof IStorage)
        {
            Object parent = fContentProvider.getParent(selected);
            String selectedName = ((IStorage) selected).getName();
            if (parent instanceof IContainer)
            {
                String selectedPath = fLabelProvider.getText(parent) + "/" + selectedName;
                if (fProposal.getAllowRelativePaths())
                    selectedPath = getRelativePath(selectedPath);

                fProposal.setChosenAsset(selectedPath);
                fProposal
                        .apply(fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()));
            }
            else
            {
                String selectedPath = "/"
                        + ((IPackageFragment) parent).getElementName().replace('.', '/') + "/"
                        + selectedName;

                if (fProposal.getAllowRelativePaths() && fRootLocations.length != 2)
                    selectedPath = getRelativePath(selectedPath);

                fProposal.setChosenAsset(selectedPath);
                fProposal
                        .apply(fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()));

            }

            return true;
        }
        return false;
    }

    private String getRelativePath(String path)
    {
        BaseSpecLocatable spec = (BaseSpecLocatable) fEditor.getSpecification();

        if (spec == null)
            return path;

        IPath specPath = new Path(spec.getSpecificationLocation().getPath());
        Path newPath = new Path(path);

        if (specPath.isPrefixOf(newPath))
            return newPath.removeFirstSegments(specPath.segmentCount()).toString();

        return path;
    }

    protected void doSetInput(Object information)
    {
        fProposal = (ChooseResourceProposal) fEditor.getInformationControlInput();
        fRootLocations = fProposal.getRootLocations();
        if (fRootLocations != null)
        {
            setupLocations();
            fTreeViewer.setInput(fRootLocations);
        }
        else
        {
            UIPlugin.log("argh");
        }
    }

    private void setupLocations()
    {
        for (int i = 0; i < fRootLocations.length; i++)
        {
            if (fRootLocations[i].isOnClasspath())
                fClasspathRoot = (ClasspathRootLocation) fRootLocations[i];
            else
                fContextRoot = (ContextRootLocation) fRootLocations[i];
        }
    }

    public void dispose()
    {
        super.dispose();
    }

    protected NamePatternFilter createFilter()
    {
        return new UserFilter();
    }

}