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

package com.iw.plugins.spindle.ui.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.resources.eclipse.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.search.ISearch;
import com.iw.plugins.spindle.core.resources.search.eclipse.AbstractEclipseSearchAcceptor;
import com.iw.plugins.spindle.ui.widgets.TwoListSearchWidget;

/**
 * A subclass of TwoListSearchWidget used for picking a library in the source
 * path of a Java Project
 * 
 * TODO REMOVE
 * 
 * @author glongman@gmail.com
 * 
 * Copyright 2002, Geoffrey Longman. All Rights Reserved.
 */
public class SearchForLibraryWidget extends TwoListSearchWidget
{

  static private final Object[] empty = new Object[0];

  private SearchAcceptor fAcceptor;
  private ISearch fSearcher;
  private String fResultString;

  public SearchForLibraryWidget(IJavaProject project)
  {
    super();
    configure(project);
    fAcceptor = new SearchAcceptor(project);
    NameProvider nameProvider = new NameProvider();
    setUpperListLabelProvider(nameProvider);
    setUpperListContentProvider(nameProvider);
    setLowerListLabel(UIPlugin.getString("property-page-library-dialog-package"));
    PackageProvider packageProvider = new PackageProvider();
    setLowerListLabelProvider(packageProvider);
    setLowerListContentProvider(packageProvider);
  }

  public int getAcceptFlags()
  {
    return AbstractEclipseSearchAcceptor.ACCEPT_LIBRARIES;
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

    String elementName = fragment.getElementName();
    if (elementName == null || elementName.trim().length() == 0)
    {
      fResultString = "/" + name;
    } else
    {
      fResultString = "/" + elementName.replace('.', '/') + "/" + name;
    }
    return new StructuredSelection(fResultString);
  }

  public void refresh()
  {
    if (fSearcher == null)
    {
      return;

    } else
    {
      super.refresh();
    }
  }

  public void configure(IJavaProject project)
  {
    fSearcher = null;
    try
    {
      ClasspathRootLocation rootLocation = new ClasspathRootLocation(project);
      fSearcher = rootLocation.getSearch();

    } catch (CoreException e)
    {
      UIPlugin.log_it(e);
    }
  }

  public void dispose()
  {
    super.dispose();
    fSearcher = null;
  }

  public String getResult()
  {
    return fResultString;
  }

  class NameProvider extends LabelProvider implements IStructuredContentProvider
  {

    Image libraryImage = Images.getSharedImage("library16.gif");

    public Object[] getElements(Object inputElement)
    {
      String searchFilter = (String) inputElement;
      if (searchFilter == null || searchFilter.trim().length() == 0)
      {
        return empty;
      }
      fAcceptor.reset();
      fAcceptor.setMatchString(searchFilter.trim());
      fSearcher.search(fAcceptor);
      return fAcceptor.getNames();
    }

    public void dispose()
    {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    public Image getImage(Object element)
    {
      return libraryImage;
    }
    public String getText(Object element)
    {
      return super.getText(element);
    }
  }

  class PackageProvider extends JavaElementLabelProvider
      implements
        IStructuredContentProvider
  {
    public Object[] getElements(Object inputElement)
    {
      String selectedName = (String) inputElement;
      if (selectedName == null)
      {
        return empty;
      }
      return fAcceptor.getPackagesFor(selectedName);

    }
    public void dispose()
    {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }
  }

  class SearchAcceptor extends AbstractEclipseSearchAcceptor
  {
    String matchString;

    Map results;
    Map packageLookup;
    IJavaProject jproject;

    public SearchAcceptor(IJavaProject jproject)
    {
      super(getAcceptFlags());
      this.jproject = jproject;
      reset();
    }

    public void reset()
    {
      results = new HashMap();
      packageLookup = new HashMap();
    }

    public Map getResults()
    {
      return results;
    }

    public void setMatchString(String value)
    {
      matchString = value;
    }

    private boolean match(String name)
    {
      if ("*".equals(matchString))
      {
        return true;
      }
      return name.startsWith(matchString);
    }

    public Object[] getNames()
    {
      if (results == null)
      {
        return empty;
      }
      return new TreeSet(results.keySet()).toArray();
    }

    public Object[] getPackagesFor(String name)
    {
      if (results == null)
      {
        return empty;
      }
      Set packages = (Set) results.get(name);
      if (packages == null)
      {
        return empty;
      }
      return packages.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.resources.search.AbstractTapestrySearchAcceptor#acceptTapestry(java.lang.Object,
     *      org.eclipse.core.resources.IStorage)
     */
    public boolean acceptTapestry(Object parent, IStorage storage)
    {
      IPackageFragment fragment = (IPackageFragment) parent;

      IPackageFragmentRoot root = (IPackageFragmentRoot) fragment.getParent();
      try
      {
        if (root.getKind() == root.K_BINARY
            || !fragment.getJavaProject().equals(jproject))
        {
          // continue the search
          return true;
        }
      } catch (JavaModelException e)
      {
        UIPlugin.log_it(e);
        return true;
      }
      String name = storage.getName();
      String matchName = storage
          .getFullPath()
          .removeFileExtension()
          .lastSegment()
          .toString();
      if (!match(matchName))
      {
        return true;
      }
      packageLookup.put(name, fragment);
      Set packages = (Set) results.get(name);
      if (packages == null)
      {
        packages = new HashSet();
        packages.add(fragment);
        results.put(name, packages);

      } else if (!packages.contains(fragment))
      {

        packages.add(fragment);
      }
      return true;
    }
  }

}