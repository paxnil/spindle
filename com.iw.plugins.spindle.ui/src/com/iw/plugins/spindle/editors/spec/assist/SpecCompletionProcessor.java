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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.wutka.dtd.DTD;

/**
 *  Base class for context assist processors for Tapestry specss
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class SpecCompletionProcessor extends ContentAssistProcessor
{

    protected static List findRawNewTagProposals(DTD dtd, DocumentArtifact artifact, int documentOffset)
    {
        DocumentArtifact parent = artifact.getParent();
        DocumentArtifact previousSibling = null;

        if (parent != null && !parent.getType().equals("/"))
        {
            boolean lookupPreviousSib = artifact.getCorrespondingNode() != parent;
            String parentName = parent.getName();
            if (parentName != null)
            {
                String parentAllowedContent = SpecAssistHelper.getAllowedElements(dtd, parentName);
                if (parentAllowedContent != null)
                {
                    String sibName = null;
                    if (lookupPreviousSib)
                        previousSibling = artifact.getPreviousSiblingTag(parentAllowedContent);
                    if (previousSibling != null)
                        sibName = previousSibling.getName();
                    return getRawNewTagProposals(dtd, parentName, sibName);
                }
            }
        }
        return Collections.EMPTY_LIST;

    }

    protected static List getRawNewTagProposals(DTD dtd, String parentName, String sibName)
    {
        List allowedChildren = SpecAssistHelper.getAllowedChildren(dtd, parentName, sibName, false);
        List result = new ArrayList();
        for (Iterator iter = allowedChildren.iterator(); iter.hasNext();)
        {
            String tagName = (String) iter.next();
            result.addAll(SpecAssistHelper.getNewElementCompletionProposals(dtd, tagName));

        }
        return result;
    }

    protected String fDeclaredRootElementName;
    protected String fPublicId;
    protected DTD fDTD;
    protected DocumentArtifact fArtifact;

    public SpecCompletionProcessor(Editor editor)
    {
        super(editor);
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        fDeclaredRootElementName = null;
        fPublicId = null;
        fDTD = null;
        fArtifact = null;

        try
        {
            IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
            fAssistParititioner.connect(document);
            if (document.getLength() == 0 || document.get().trim().length() == 0)
            {
                return computeEmptyDocumentProposal(viewer, documentOffset);
            }
            Point p = viewer.getSelectedRange();
            if (p.y > 0)
                return NoProposals;

            try
            {
                DocumentArtifact root = DocumentArtifact.createTree(document, -1);
                fPublicId = root.fPublicId;
                fDeclaredRootElementName = root.fRootNodeId;
                fDTD = DOMValidator.getDTD(fPublicId);

            } catch (BadLocationException e)
            {
                // do nothing
            }

            if (fDTD == null || fDeclaredRootElementName == null)
                return NoProposals;

            return doComputeCompletionProposals(viewer, documentOffset);

        } finally
        {
            fAssistParititioner.disconnect();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeInformation(ITextViewer viewer, int documentOffset)
    {
        try
        {
            IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
            fAssistParititioner.connect(document);

            try
            {
                DocumentArtifact root = DocumentArtifact.createTree(document, -1);
                fPublicId = root.fPublicId;
                fDeclaredRootElementName = root.fRootNodeId;
                fDTD = DOMValidator.getDTD(fPublicId);

            } catch (BadLocationException e)
            {
                // do nothing
            }

            if (fDTD == null || fDeclaredRootElementName == null)
                return NoInformation;

            return doComputeContextInformation(viewer, documentOffset);

        } finally
        {
            fAssistParititioner.disconnect();
        }
    }

    private ICompletionProposal[] computeEmptyDocumentProposal(ITextViewer viewer, int documentOffset)
    {
        IEditorInput input = fEditor.getEditorInput();
        IStorage storage = (IStorage) input.getAdapter(IStorage.class);
        String extension = storage.getFullPath().getFileExtension();
        if (extension == null || extension.length() == 0)
        {
            return NoProposals;
        }
        String replacement = getSkeletonSpecification(extension);
        return new ICompletionProposal[] {
             new CompletionProposal(
                replacement,
                0,
                viewer.getDocument().getLength(),
                new Point(0, 0),
                UIPlugin.getDefault().getStorageLabelProvider().getImage(storage),
                "insert default skeletion XML",
                null,
                null)};
    }

    private String getSkeletonSpecification(String extension)
    {
        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);
        if ("jwc".equals(extension))
        {
            XMLUtil.writeComponentSpecification(pwriter, UIPlugin.DEFAULT_COMPONENT_SPEC, 0);
            return swriter.toString();
        } else if ("page".equals(extension))
        {
            XMLUtil.writeComponentSpecification(pwriter, UIPlugin.DEFAULT_PAGE_SPEC, 0);
            return swriter.toString();
        } else if ("application".equals(extension))
        {
            XMLUtil.writeApplicationSpecification(pwriter, UIPlugin.DEFAULT_APPLICATION_SPEC, 0);
            return swriter.toString();
        } else if ("library".equals(extension))
        {
            XMLUtil.writeLibrarySpecification(pwriter, UIPlugin.DEFAULT_LIBRARY_SPEC, 0);
            return swriter.toString();
        }
        return "";
    }

}
