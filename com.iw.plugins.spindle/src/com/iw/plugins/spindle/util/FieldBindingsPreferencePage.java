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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */package com.iw.plugins.spindle.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.iw.plugins.spindle.TapestryPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.ISearchPattern;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @version 	1.0
 * @author
 */
public class FieldBindingsPreferencePage implements IWorkbenchPreferencePage {

  public static IJavaElement[] unfold(String folded) {
    if (folded != null && !"".equals(folded.trim())) {
      ISearchPattern searchPattern = null;
      StringTokenizer tok = new StringTokenizer(folded, ",");
      while (tok.hasMoreTokens()) {
        String fragment = tok.nextToken();

        if (searchPattern == null) {

          searchPattern =
            SearchEngine.createSearchPattern(
              fragment,
              IJavaSearchConstants.PACKAGE,
              IJavaSearchConstants.DECLARATIONS,
              false);
        } else {

          searchPattern =
            SearchEngine.createOrSearchPattern(
              searchPattern,
              SearchEngine.createSearchPattern(
                fragment,
                IJavaSearchConstants.PACKAGE,
                IJavaSearchConstants.DECLARATIONS,
                false));
        }
      }
      try {
        UnfoldSearchCollector collector = new UnfoldSearchCollector();
        long start = new Date().getTime();
        new SearchEngine().search(
          TapestryPlugin.getDefault().getWorkspace(),
          searchPattern,
          SearchEngine.createWorkspaceScope(),
          collector);
        System.out.println("unfold took: "+(new Date().getTime() - start));
        return collector.getFoundElements();
      } catch (JavaModelException jmex) {
        jmex.printStackTrace();
      }
    }
    return new IJavaElement[0];
  }

  private static void unfoldStar(String fragment, ArrayList elements) {

  }

  public static String fold(List unfolded) {
    Iterator iter = unfolded.iterator();
    StringBuffer buffer = new StringBuffer();
    while (iter.hasNext()) {
      buffer.append(iter.next());
      if (iter.hasNext()) {
        buffer.append(',');
      }
    }
    return buffer.toString();
  }

  /**
   * Constructor for FieldBindingsPreferencePage.
   */
  public FieldBindingsPreferencePage() {
    super();
  }

  /*
   * @see IWorkbenchPreferencePage#init(IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

  /*
   * @see IPreferencePage#computeSize()
   */
  public Point computeSize() {
    return null;
  }

  /*
   * @see IPreferencePage#isValid()
   */
  public boolean isValid() {
    return false;
  }

  /*
   * @see IPreferencePage#okToLeave()
   */
  public boolean okToLeave() {
    return false;
  }

  /*
   * @see IPreferencePage#performCancel()
   */
  public boolean performCancel() {
    return false;
  }

  /*
   * @see IPreferencePage#performOk()
   */
  public boolean performOk() {
    return false;
  }

  /*
   * @see IPreferencePage#setContainer(IPreferencePageContainer)
   */
  public void setContainer(IPreferencePageContainer preferencePageContainer) {
  }

  /*
   * @see IPreferencePage#setSize(Point)
   */
  public void setSize(Point size) {
  }

  /*
   * @see IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
  }

  /*
   * @see IDialogPage#dispose()
   */
  public void dispose() {
  }

  /*
   * @see IDialogPage#getControl()
   */
  public Control getControl() {
    return null;
  }

  /*
   * @see IDialogPage#getDescription()
   */
  public String getDescription() {
    return null;
  }

  /*
   * @see IDialogPage#getErrorMessage()
   */
  public String getErrorMessage() {
    return null;
  }

  /*
   * @see IDialogPage#getImage()
   */
  public Image getImage() {
    return null;
  }

  /*
   * @see IDialogPage#getMessage()
   */
  public String getMessage() {
    return null;
  }

  /*
   * @see IDialogPage#getTitle()
   */
  public String getTitle() {
    return null;
  }

  /*
   * @see IDialogPage#performHelp()
   */
  public void performHelp() {
  }

  /*
   * @see IDialogPage#setDescription(String)
   */
  public void setDescription(String description) {
  }

  /*
   * @see IDialogPage#setImageDescriptor(ImageDescriptor)
   */
  public void setImageDescriptor(ImageDescriptor image) {
  }

  /*
   * @see IDialogPage#setTitle(String)
   */
  public void setTitle(String title) {
  }

  /*
   * @see IDialogPage#setVisible(boolean)
   */
  public void setVisible(boolean visible) {
  }

  public static class UnfoldSearchCollector implements IJavaSearchResultCollector {

    ArrayList collected;

    /**
     * Constructor for UnfoldSearchCollector.
     */
    public UnfoldSearchCollector() {
      super();
    }

    public IJavaElement[] getFoundElements() {
      return (IJavaElement[]) collected.toArray(new IJavaElement[collected.size()]);
    }

    /*
     * @see IJavaSearchResultCollector#aboutToStart()
     */
    public void aboutToStart() {
      collected = new ArrayList();
    }

    /*
     * @see IJavaSearchResultCollector#accept(IResource, int, int, IJavaElement, int)
     */
    public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy)
      throws CoreException {
      collected.add(enclosingElement);
    }

    /*
     * @see IJavaSearchResultCollector#done()
     */
    public void done() {
    }

    /*
     * @see IJavaSearchResultCollector#getProgressMonitor()
     */
    public IProgressMonitor getProgressMonitor() {
      return null;
    }

  }

}