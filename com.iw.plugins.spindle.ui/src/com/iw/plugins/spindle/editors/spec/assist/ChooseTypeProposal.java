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
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  Proposal that gets it contents from the user!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ChooseTypeProposal implements ICompletionProposal
{
    protected IJavaProject jproject;
    String chosenType;
    boolean includeInterfaces;
    int documentOffset;
    int replacementOffset;
    int replacementLength;
 
    public ChooseTypeProposal(
        IJavaProject project,
        boolean includeInterfaces,
        int documentOffset,
        int replacementOffset,
        int replacementLength)
    {
        Assert.isNotNull(project);
        jproject = project;
        this.includeInterfaces = includeInterfaces;
        this.documentOffset = documentOffset;
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
    }

    /* (non-Javadoc)
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
                document.replace(replacementOffset, replacementLength, chosenType);
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

            if (jproject == null)
                return null;

            IJavaSearchScope scope = createSearchScope(jproject);

            SelectionDialog dialog =
                JavaUI.createTypeDialog(
                    shell,
                    new ProgressMonitorDialog(shell),
                    scope,
                    (includeInterfaces
                        ? IJavaElementSearchConstants.CONSIDER_TYPES
                        : IJavaElementSearchConstants.CONSIDER_CLASSES),
                    false);

            dialog.setTitle(includeInterfaces ? "Java Type Chooser" : "Java Class Chooser");
            dialog.setMessage("Choose " + (includeInterfaces ? "Type" : "a Class"));

            if (dialog.open() == dialog.OK)
            {
                IType chosen = (IType) dialog.getResult()[0];
                return chosen.getFullyQualifiedName(); //FirstResult();
            }
        } catch (CoreException jmex)
        {
            ErrorDialog.openError(shell, "Spindle error", "unable to continue", jmex.getStatus());
        }
        return null;
    }

    protected IJavaSearchScope createSearchScope(IJavaElement element) throws JavaModelException
    {
        JavaSearchScope scope = new JavaSearchScope();
        scope.add(element);
        return scope;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo()
    {
        return "Note due to a known pre-existing bug in eclispe:\n\n [Bug 45193] hierarchy scope search only shows types that exist in jars\n\nThe search can't be limited to Tapestry types";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
     */
    public String getDisplayString()
    {
        return "Choose Type Dialog";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage()
    {
        return Images.getSharedImage("opentype.gif");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document)
    {
        if (chosenType == null)
            return new Point(documentOffset, 0);

        return new Point(replacementOffset + chosenType.length(), 0);
    }
}
