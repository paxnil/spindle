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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ant;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.osgi.framework.Bundle;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * AntScriptGenerator - generates a build.xml and a build.properties file for a
 * given project
 * 
 * @author glongman@gmail.com
 *  
 */
public class AntScriptGenerator
{

  /**
   * $tapestryDistDir$ - absolute path to Tapestry Distro - required
   */
  private static final String TAPESTRY_DIST_DIR = "tapestryDistDir";
  /**
   * $tomcatDir$ - Absolute path to the directory containing Tomcat
   */
  private static final String TOMCAT_DIR = "tomcatDir";
  /**
   * $servletJar$ - the path to the jar containing the servlet API - required if
   * tomcatDir not resolved.
   */
  private static final String SERVLET_JAR = "servletJar";
  /**
   * $deployDir$ - the place to deploy the warfile to...optional
   */
  private static final String DEPLOY_DIR = "deployDir";
  /**
   * $projectName$ - the name of the project
   */
  private static final String PROJECT_NAME = "projectName";
  /**
   * $contextDir$ - the web context folder name (relative to the project root)
   */
  private static final String CONTEXT_DIR = "contextDir";
  /**
   * $projectLibraries$ - pathElement tags for jars in project classpath
   * (excluding tap + servlet.jar)
   */
  private static final String PROJECT_LIBS = "projectLibraries";
  /**
   * $srcDirs$ - <src>tags for src folders in the project for the ant javac
   * classpath
   */
  private static final String SRC_DIRS = "srcDirs";
  /**
   * $warSrcResources$ - pick up non java resources from all the src folders for
   * inclusion in the war file.
   */
  private static final String WAR_SRC_RESOURCES = "warSrcResources";

  private Map fVariableMap = new HashMap();
  private TapestryProject fTapestryProject;
  private List fServletAPIRoots = Collections.EMPTY_LIST;
  private List fSourceFolders = new ArrayList();
  private List fBinaryLibraries = new ArrayList();

  public AntScriptGenerator(TapestryProject project)
  {
    Assert.isNotNull(project);
    fTapestryProject = project;
  }

  public void generate(IProgressMonitor monitor)
  {
    if (monitor.isCanceled())
      return;

    monitor.beginTask(UIPlugin.getString("ant-generate", fTapestryProject
        .getProject()
        .getName()), 3);
    
    resolveProjectClasspath(monitor);

    resolveVariables(monitor);

    if (monitor.isCanceled())
      return;

    monitor.worked(1);

    String buildFileContents = getBuildFileContents();

    String propertiesFileContents = getPropertiesFileContents();

    if (monitor.isCanceled())
      return;

    monitor.worked(1);

    createFile("build.xml", buildFileContents);

    createFile("build.properties", propertiesFileContents);

  }

  /**
   * We need to collect and filter the list of classpath roots, src and binary.
   * We need to separate jars containing javax.servet.* classes and ignore the
   * JRE classpath container setting altogether.
   * 
   * @param monitor a progress monitor
   */
  private void resolveProjectClasspath(IProgressMonitor monitor)
  {
    ArrayList resolvedEntries = new ArrayList();
    IJavaProject jproject = null;
    try
    {
      jproject = fTapestryProject.getJavaProject();
      IClasspathEntry[] entries = jproject.getRawClasspath();
      // we need to ignore JRE entires and collect all of the other entries!
      // don't separate src from binary roots at this time.
      for (int i = 0; i < entries.length; i++)
      {
        switch (entries[i].getEntryKind())
        {
          case IClasspathEntry.CPE_VARIABLE :

            IClasspathEntry resolvedEntry = null;
            try
            {
              resolvedEntry = JavaCore.getResolvedClasspathEntry(entries[i]);
            } catch (org.eclipse.jdt.internal.core.Assert.AssertionFailedException e)
            {
              UIPlugin.log(e);
            }
            if (resolvedEntry != null)
              resolvedEntries.add(resolvedEntry);
            break;

          case IClasspathEntry.CPE_CONTAINER :

            //We don't care about the JRE!
            if ("".equals(entries[i].getPath()))
              break;

            IClasspathContainer container = JavaCore.getClasspathContainer(entries[i]
                .getPath(), jproject);

            if (container == null)
              break;

            IClasspathEntry[] containerEntries = container.getClasspathEntries();
            if (containerEntries == null)
              break;

            // container was bound
            for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++)
            {
              IClasspathEntry cEntry = containerEntries[j];
              //              if (generateMarkerOnError)
              //              {
              //                IJavaModelStatus containerStatus =
              // ClasspathEntry.validateClasspathEntry(
              //                    jproject,
              //                    cEntry,
              //                    false,
              //                    true /* recurse */);
              //                if (!containerStatus.isOK())
              //                  createClasspathProblemMarker(containerStatus);
              //              }
              // if container is exported, then its nested entries must in turn
              // be exported (21749)
              resolvedEntries.add(cEntry);
            }
            break;

          default :
            resolvedEntries.add(entries[i]);
        }
      }
    } catch (CoreException e)
    {
      UIPlugin.log(e);
    }
    if (jproject == null)
      return;

  }
  
  private List resolveProjectClasspath(IJavaProject project) {
    ArrayList resolvedEntries = new ArrayList();
  }

  /**
   * @return
   */
  private String getBuildFileContents()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @return
   */
  private String getPropertiesFileContents()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param fileName
   * @param contents
   */
  private void createFile(String fileName, String contents)
  {

  }

  /**
   * resolve all the variables required to generate the ant files
   */
  private void resolveVariables(IProgressMonitor monitor)
  {
    TapestryCore core = TapestryCore.getDefault();
    Bundle bundle = core.getBundle();

    URL installUrl = bundle.getEntry("/");
    try
    {
      installUrl = Platform.resolve(installUrl);
    } catch (IOException e)
    {
      UIPlugin.log(e);
    }

    IPath installPath = new Path(getFileFromURL(installUrl)).removeTrailingSeparator();

    fVariableMap.put(TAPESTRY_DIST_DIR, installPath.toString());

    //resolve the easy ones first...
    fVariableMap.put(TOMCAT_DIR, "");
    fVariableMap.put(SERVLET_JAR, "");
    fVariableMap.put(DEPLOY_DIR, "");

    fVariableMap.put(PROJECT_NAME, fTapestryProject.getProject().getName());
    fVariableMap.put(CONTEXT_DIR, fTapestryProject
        .getWebContextFolder()
        .getFullPath()
        .removeFirstSegments(1)
        .toString());

    harvestServletJars(monitor);

    harvestBinaryLibrariesAndSourceFolders(monitor);
  }

  /**
   * @param monitor
   */
  private void harvestBinaryLibrariesAndSourceFolders(IProgressMonitor monitor)
  {
    try
    {
      IJavaProject jproject = fTapestryProject.getJavaProject();
      if (jproject == null)
        return;

      IPackageFragmentRoot[] allRoots = jproject.getAllPackageFragmentRoots();
      for (int i = 0; i < allRoots.length; i++)
      {

        int kind = allRoots[i].getKind();
        switch (kind)
        {
          case IPackageFragmentRoot.K_BINARY :
            if (!fServletAPIRoots.contains(allRoots[i]))
              fBinaryLibraries.add(allRoots[i]);
            break;
          case IPackageFragmentRoot.K_SOURCE :
            fSourceFolders.add(allRoots[i]);
            break;

          default :
            break;
        }
      }
    } catch (CoreException e)
    {
      UIPlugin.log(e);
    }
  }

  /**
   *  
   */
  private void harvestServletJars(IProgressMonitor monitor)
  {
    try
    {
      IJavaProject jproject = fTapestryProject.getJavaProject();
      if (jproject == null)
        return;

      // let's locate all occurances of a package in the project classpath that
      // we know should be
      // from the java servlet jar..

      SearchPattern pattern = SearchPattern.createPattern(
          "javax.servlet.http",
          IJavaSearchConstants.PACKAGE,
          IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_EXACT_MATCH);

      IJavaSearchScope scope = SearchEngine
          .createJavaSearchScope(new IJavaElement[]{jproject});

      //we could get lots of hits for various reasons so lets collect the ones
      // we are interested in..
      //we collect the roots (jars) that contain the package of interest.
      final List roots = new ArrayList();

      SearchRequestor requestor = new SearchRequestor()
      {
        public void acceptSearchMatch(SearchMatch match) throws CoreException
        {
          //only keep the roots that are in jar files and that we have not
          // already seen!
          Object element = match.getElement();
          if (element instanceof IPackageFragment)
          {
            IPackageFragment frag = (IPackageFragment) element;
            if (frag.getKind() == IPackageFragmentRoot.K_BINARY)
            {
              IPackageFragmentRoot root = (IPackageFragmentRoot) frag.getParent();
              if (!roots.contains(root))
                roots.add(root);
            }
          }
        }
      };
      //    Search - do the search
      SearchEngine searchEngine = new SearchEngine();
      searchEngine.search(pattern, new SearchParticipant[]{SearchEngine
          .getDefaultSearchParticipant()}, scope, requestor, monitor);

      if (!roots.isEmpty())
        fServletAPIRoots = roots;

    } catch (CoreException e)
    {
      UIPlugin.log(e);
    }
  }
  private String getFileFromURL(URL url)
  {
    String file = url.getFile();
    if (file.startsWith("/"))
      file = file.substring(1);
    return file;
  }

}