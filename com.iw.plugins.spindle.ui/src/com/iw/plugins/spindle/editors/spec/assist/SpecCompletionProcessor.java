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

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.wutka.dtd.DTD;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class SpecCompletionProcessor extends ContentAssistProcessor
{
    public static ICompletionProposal getDefaultInsertCommentProposal(int replacementOffset, int replacementLength)
    {
        return new CompletionProposal(
            "<!--  -->",
            replacementOffset,
            replacementLength,
            new Point(5, 0),
            Images.getSharedImage("bullet.gif"),
            "Insert comment",
            null,
            null);
    }

    protected String fRootElementName;
    protected DTD fDTD;
    protected DocumentArtifact fArtifact;
    /**
     * @param editor
     */
    public SpecCompletionProcessor(AbstractTextEditor editor)
    {
        super(editor);
        // TODO Auto-generated constructor stub
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        fRootElementName = null;
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

            fRootElementName = null;
            try
            {
                DocumentArtifact root = DocumentArtifact.createTree(document, -1);
                String publicId = root.fPublicId;
                fRootElementName = root.fRootNodeId;
                fDTD = DOMValidator.getDTD(publicId);

            } catch (BadLocationException e)
            {
                // do nothing
            }

            if (fDTD == null || fRootElementName == null)
                return NoProposals;

            return doComputeCompletionProposals(viewer, documentOffset);

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
