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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.Editor;
import com.wutka.dtd.DTD;

/**
 *  Content Assist for Templates
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class ContentAssistProcessor implements IContentAssistProcessor
{
    protected static final ICompletionProposal[] NoProposals = new ICompletionProposal[0];
    protected static final ICompletionProposal[] NoSuggestions =
        new ICompletionProposal[] {
            new ContentAssistProcessor.MessageProposal("no suggestions available"),
            new ContentAssistProcessor.MessageProposal("")};

    protected static final IContextInformation[] NoInformation = new IContextInformation[0];

    protected Editor fEditor;
    protected IPreferenceStore fPreferenceStore = UIPlugin.getDefault().getPreferenceStore();
    protected XMLDocumentPartitioner fAssistParititioner;
    protected boolean fDoingContextInformation = false;
    protected DTD fDTD;

    public ContentAssistProcessor(Editor editor)
    {
        this.fEditor = editor;
        fAssistParititioner = new XMLDocumentPartitioner(XMLDocumentPartitioner.SCANNER, XMLDocumentPartitioner.TYPES);
    }

    protected void connect(IDocument document) throws IllegalStateException
    {
        fAssistParititioner.connect(document);
        try
        {
            XMLNode.createTree(document, -1);
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
            throw new IllegalStateException();
        }
    }

    protected void disconnect()
    {
        try
        {
            fAssistParititioner.disconnect();
        } catch (RuntimeException e)
        {
            UIPlugin.log(e);
        }
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        IDocument document = viewer.getDocument();
        if (document.getLength() == 0 || document.get().trim().length() == 0)
            return computeEmptyDocumentProposal(viewer, documentOffset);

        try
        {
            connect(document);
            Point p = viewer.getSelectedRange();
            if (p.y > 0)
                return NoProposals;

            return doComputeCompletionProposals(viewer, documentOffset);

        } catch (IllegalStateException e)
        {
            return NoProposals;
        } catch (RuntimeException e)
        {
            UIPlugin.log(e);
            throw e;
        } finally
        {
            disconnect();
        }
    }

    protected  ICompletionProposal[] computeEmptyDocumentProposal(ITextViewer viewer, int documentOffset) {
        return NoProposals;
    }

    protected abstract ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset);

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return NoInformation;
    }

    /**
     * @param viewer
     * @param documentOffset
     * @return
     */
    public IContextInformation[] computeInformation(ITextViewer viewer, int documentOffset)
    {
        try
        {
            connect(viewer.getDocument());
            return doComputeContextInformation(viewer, documentOffset);
        } catch (IllegalStateException e)
        {
            return NoInformation;
        } finally
        {
            disconnect();
        }
    }

    // default result, override in subclass
    public IContextInformation[] doComputeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return NoInformation;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage()
    { //TODO I10N
        return "no completions available";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }

    public static class MessageProposal implements ICompletionProposal
    {
        String fLabel = "coming soon!";

        public MessageProposal()
        {}

        public MessageProposal(String label)
        {
            fLabel = label;
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
            return null;
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
            return fLabel;
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
            return null;
        }

    }

}
