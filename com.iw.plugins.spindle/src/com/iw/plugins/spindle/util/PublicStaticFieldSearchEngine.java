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
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.TapestryPlugin;

/**
 * 
 * Search for types containing public static fields
 * 
 * @version 	1.0
 * @author	    GWL
 */
public class PublicStaticFieldSearchEngine {

  /**
   * Searches for all types with public static fields in the given scope.
   * Valid styles are IJavaElementSearchConstants.CONSIDER_BINARIES and
   * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
   */
  public IType[] searchPublicStaticMethods(IProgressMonitor pm, IJavaSearchScope scope, int style)
    throws JavaModelException {
    List typesFound = new ArrayList(200);

    IJavaSearchResultCollector collector = new FieldCollector(typesFound, style, pm);
    new SearchEngine().search(
      TapestryPlugin.getDefault().getWorkspace(),
      "*",
      IJavaSearchConstants.FIELD,
      IJavaSearchConstants.DECLARATIONS,
      scope,
      collector);

    return (IType[]) typesFound.toArray(new IType[typesFound.size()]);
  }

  /**
   * Searches for all types with public static fields in the given scope.
   * Valid styles are IJavaElementSearchConstants.CONSIDER_BINARIES and
   * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
   */
  public IType[] searchPublicStaticMethods(IRunnableContext context, final IJavaSearchScope scope, final int style)
    throws InvocationTargetException, InterruptedException {
    int allFlags =
      IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS | IJavaElementSearchConstants.CONSIDER_BINARIES;
    Assert.isTrue((style | allFlags) == allFlags);

    final IType[][] res = new IType[1][];

    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor pm) throws InvocationTargetException {
        try {
           long start = new Date().getTime();
       
        
          res[0] = searchPublicStaticMethods(pm, scope, style);
          System.out.println("ps search took: "+(new Date().getTime() - start));
        } catch (JavaModelException e) {
          throw new InvocationTargetException(e);
        }
      }
    };
    context.run(true, true, runnable);

    return res[0];
  }

  private static class FieldCollector implements IJavaSearchResultCollector {
    private List result;
    private int style;
    private IProgressMonitor monitor;

    public FieldCollector(List result, int style, IProgressMonitor progressMonitor) {
      Assert.isNotNull(result);
      this.result = result;
      this.style = style;
      monitor = progressMonitor;
    }

    private boolean considerExternalJars() {
      return (style & IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS) != 0;
    }

    private boolean considerBinaries() {
      return (style & IJavaElementSearchConstants.CONSIDER_BINARIES) != 0;
    }

    public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy) {      
      if (enclosingElement instanceof IField) { // defensive code
        try {
          IField current = (IField) enclosingElement;
          if (isPublicStatic(current)) {
            
            if (!considerExternalJars()) {
              IPackageFragmentRoot root = Utils.getPackageFragmentRoot(current);
              if (root == null || root.isArchive()) {
                return;
              }
            }
            if (!considerBinaries() && current.isBinary()) {
              return;
            }
            IType declaring = current.getDeclaringType();
            if (!result.contains(declaring)) {
              result.add(declaring);
            }
          }
        } catch (JavaModelException e) {
          JavaPlugin.log(e.getStatus());
        }
      }
    }

    private boolean isPublicStatic(IField aField) throws JavaModelException {
      
      if (aField != null) {
        int flags = aField.getFlags();
       
        return Flags.isStatic(flags) && Flags.isPublic(flags);
      }
      return false;
    }

    public IProgressMonitor getProgressMonitor() {
      return monitor;
    }

    public void aboutToStart() {
    }

    public void done() {
    }
  }

}