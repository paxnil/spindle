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

package com.iw.plugins.spindle.editors.util;

import java.util.Comparator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  Base class for Completion Proposals
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class CompletionProposal implements ICompletionProposal
{
    public static final String DEFAULT_ATTR_VALUE = "";

    public static final Comparator PROPOSAL_COMPARATOR = new Comparator()
    {
        String s1;
        String s2;
        int z1;
        int z2;
        public int compare(Object o1, Object o2)
        {

            ICompletionProposal p1 = (ICompletionProposal) o1;
            ICompletionProposal p2 = (ICompletionProposal) o2;

            s1 = p1.getDisplayString();
            s2 = p2.getDisplayString();

            z1 = z2 = 0;

            if (p1 instanceof CompletionProposal)
                z1 = ((CompletionProposal) p1).fYOrder;

            if (p2 instanceof CompletionProposal)
                z2 = ((CompletionProposal) p2).fYOrder;

            return (z1 == z2) ? s1.compareTo(s2) : (z1 < z2 ? -1 : 1);
        }
    };

    public static CompletionProposal getAttributeProposal(
        String attributeName,
        boolean addLeadingSpace,
        int replacementOffset)
    {
        return getAttributeProposal(
            attributeName,
            attributeName,
            DEFAULT_ATTR_VALUE,
            null,
            addLeadingSpace,
            replacementOffset);
    }

    public static CompletionProposal getAttributeProposal(
        String attributeName,
        String displayName,
        String defaultValue,
        String extraInfo,
        boolean addLeadingSpace,
        int replacementOffset)
    {
        String replacementString = (addLeadingSpace ? " " : "") + attributeName + "=\"" + defaultValue + "\"";
        Point replacementPoint =
            new Point((addLeadingSpace ? 1 : 0) + attributeName.length() + 2, defaultValue.length());

        return new CompletionProposal(
            replacementString,
            replacementOffset,
            0,
            replacementPoint,
            Images.getSharedImage("bullet.gif"),
            displayName,
            null,
            extraInfo);
    }

    /** The string to be displayed in the completion proposal popup */
    private String fDisplayString;
    /** The replacement string */
    private String fReplacementString;
    /** The replacement offset */
    private int fReplacementOffset;
    /** The replacement length */
    private int fReplacementLength;
    /** The cursor position after this proposal has been applied */
    private Point fSelectionPoint;
    /** The image to be displayed in the completion proposal popup */
    private Image fImage;
    /** The context information of this proposal */
    private IContextInformation fContextInformation;
    /** The additional info of this proposal */
    private String fAdditionalProposalInfo;

    protected int fYOrder = 0;

    /**
     * Creates a new completion proposal based on the provided information.  The replacement string is
     * considered being the display string too. All remaining fields are set to <code>null</code>.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
     */
    public CompletionProposal(
        String replacementString,
        int replacementOffset,
        int replacementLength,
        Point selectionPoint)
    {
        this(replacementString, replacementOffset, replacementLength, selectionPoint, null, null, null, null);
    }

    /**
     * Creates a new completion proposal. All fields are initialized based on the provided information.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param selectionPoint the selection point following the insert relative to replacementOffset
     * @param image the image to display for this proposal
     * @param displayString the string to be displayed for the proposal
     * @param contentInformation the context information associated with this proposal
     * @param additionalProposalInfo the additional information associated with this proposal
     */
    public CompletionProposal(
        String replacementString,
        int replacementOffset,
        int replacementLength,
        Point selectionPoint,
        Image image,
        String displayString,
        IContextInformation contextInformation,
        String additionalProposalInfo)
    {
        Assert.isNotNull(replacementString);
        Assert.isTrue(replacementOffset >= 0);
        Assert.isTrue(replacementLength >= 0);
        Assert.isNotNull(selectionPoint);
        Assert.isTrue(selectionPoint.x >= 0 && selectionPoint.y >= 0);

        fReplacementString = replacementString;
        fReplacementOffset = replacementOffset;
        fReplacementLength = replacementLength;
        fSelectionPoint = selectionPoint;
        fImage = image;
        fDisplayString = displayString;
        fContextInformation = contextInformation;
        fAdditionalProposalInfo = additionalProposalInfo;
    }

    public void setYOrder(int order)
    {
        fYOrder = order;
    }

    /*
     * @see ICompletionProposal#apply(IDocument)
     */
    public void apply(IDocument document)
    {
        try
        {
            document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
        } catch (BadLocationException x)
        {
            // ignore
        }
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    public Point getSelection(IDocument document)
    {
        fSelectionPoint.x += fReplacementOffset;
        return fSelectionPoint;
    }

    /*
     * @see ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation()
    {
        return fContextInformation;
    }

    /*
     * @see ICompletionProposal#getImage()
     */
    public Image getImage()
    {
        return fImage;
    }

    public void setImage(Image image)
    {
        fImage = image;
    }

    /*
     * @see ICompletionProposal#getDisplayString()
     */
    public String getDisplayString()
    {
        if (fDisplayString != null)
            return fDisplayString;
        return fReplacementString;
    }

    /*
     * @see ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo()
    {
        return fAdditionalProposalInfo;
    }

    static public class NullProposal implements ICompletionProposal
    {
        private String fMessage;
        private String fExtraInfo = null;
        private int fOffset;

        public NullProposal(String message, String xtraInfo, int documentOffset)
        {
            fMessage = message;
            fExtraInfo = xtraInfo;
            fOffset = documentOffset;

        }

        public NullProposal(String message, int documentOffset)
        {
            this(message, null, documentOffset);
        }
        /* (non-Javadoc)
        * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
        */
        public void apply(IDocument document)
        {}

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
         */
        public String getAdditionalProposalInfo()
        {
            return fExtraInfo;
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
            return fMessage;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
         */
        public Image getImage()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
         */
        public Point getSelection(IDocument document)
        {
            return new Point(fOffset, 0);
        }

    }

}