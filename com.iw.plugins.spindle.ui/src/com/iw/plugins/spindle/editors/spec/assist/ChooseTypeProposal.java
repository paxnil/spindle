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

package com.iw.plugins.spindle.editors.spec.assist;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * Proposal that gets it contents from the user!
 * 
 * @author glongman@intelligentworks.com
 * 
 */
public class ChooseTypeProposal implements ICompletionProposal
{
  protected IJavaProject fJavaProject;
  String chosenType;
  String fHierarchyRoot;
  boolean fIncludeInterfaces;
  int fDocumentOffset;
  int fReplacementOffset;
  int fReplacementLength;

  public ChooseTypeProposal(IJavaProject project, boolean includeInterfaces,
      int documentOffset, int replacementOffset, int replacementLength)
  {
    this(
        project,
        null,
        includeInterfaces,
        documentOffset,
        replacementLength,
        replacementLength);
  }

  public ChooseTypeProposal(IJavaProject project, String hierarchyRoot,
      boolean includeInterfaces, int documentOffset, int replacementOffset,
      int replacementLength)
  {
    Assert.isNotNull(project);
    fJavaProject = project;
    fHierarchyRoot = hierarchyRoot;
    fIncludeInterfaces = includeInterfaces;
    fDocumentOffset = documentOffset;
    fReplacementOffset = replacementOffset;
    fReplacementLength = replacementLength;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
   */
  public void apply(IDocument document)
  {
    chosenType = chooseType("Java Type Chooser");
    if (chosenType != null)
    {
      if (chosenType.length() == 0)
      {
        chosenType = null;
        return;
      }

      try
      {
        document.replace(fReplacementOffset, fReplacementLength, chosenType);
      } catch (BadLocationException x)
      {
        // ignore
      }

    }

  }

  protected String chooseType(String title)
  {

    Shell shell = UIPlugin.getDefault().getActiveWorkbenchShell();
    try
    {

      if (fJavaProject == null)
        return null;

      IJavaSearchScope scope = createSearchScope(fJavaProject, fHierarchyRoot);

      SelectionDialog dialog = JavaUI.createTypeDialog(shell, PlatformUI
          .getWorkbench()
          .getProgressService(), scope, (fIncludeInterfaces
          ? IJavaElementSearchConstants.CONSIDER_TYPES
          : IJavaElementSearchConstants.CONSIDER_CLASSES), false);

      dialog.setTitle(fIncludeInterfaces ? "Java Type Chooser" : "Java Class Chooser");
      dialog.setMessage("Choose " + (fIncludeInterfaces ? "Type" : "a Class"));

      if (dialog.open() == dialog.OK)
      {
        IType chosen = (IType) dialog.getResult()[0];
        return chosen.getFullyQualifiedName(); //FirstResult();
      }
    } catch (CoreException jmex)
    {
      ErrorDialog.openError(shell, "Spindle error", "unable to continue", jmex
          .getStatus());
    }
    return null;
  }

  protected IJavaSearchScope createSearchScope(IJavaProject jproject, String hierarchyRoot) throws JavaModelException
  {
    IJavaSearchScope result = null;
    IType hrootElement = null;
    try
    {
      if (hierarchyRoot != null)
      {
        hrootElement = resolveTypeName(jproject, hierarchyRoot);
      }
      if (hrootElement != null)
      {
        result = SearchEngine.createHierarchyScope(hrootElement);
      }
    } catch (JavaModelException jmex)
    {
      //ignore
      jmex.printStackTrace();
    }
    if (result == null)
    {
      IJavaElement[] elements = new IJavaElement[]{jproject};
      result = SearchEngine.createJavaSearchScope(elements);
    }
    return result;
  }

  protected IType resolveTypeName(IJavaProject jproject, String typeName) throws JavaModelException
  {
    if (jproject == null)
      return null;
    return jproject.findType(typeName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo()
  {
    //        return "Note due to a known pre-existing bug in eclispe:\n\n [Bug 45193]
    // hierarchy scope search only shows types that exist in jars\n\nThe search
    // can't be limited to Tapestry types";
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
   */
  public IContextInformation getContextInformation()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
   */
  public String getDisplayString()
  {
    return "Choose Type Dialog"
        + (fHierarchyRoot != null ? " implements (" + fHierarchyRoot + ")" : "");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
   */
  public Image getImage()
  {
    return Images.getSharedImage("opentype.gif");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
   */
  public Point getSelection(IDocument document)
  {
    if (chosenType == null)
      return new Point(fDocumentOffset, 0);

    return new Point(fReplacementOffset + chosenType.length(), 0);
  }
}