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

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;

/**
 *  Processor for completing comments
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class CommentCompletionProcessor extends ContentAssistProcessor
{

    private static Pattern ENDS_WITH_ANOTHER_COMMENT_PATTERN;
    private static PatternMatcher PATTERN_MATCHER;

    static {
        Perl5Compiler compiler = new Perl5Compiler();

        try
        {
            ENDS_WITH_ANOTHER_COMMENT_PATTERN = compiler.compile(".*<!--.*-->$", Perl5Compiler.SINGLELINE_MASK);
        } catch (MalformedPatternException ex)
        {
            throw new Error(ex);
        }

        PATTERN_MATCHER = new Perl5Matcher();
    }

    /**
     * @param editor
     */
    public CommentCompletionProcessor(AbstractTextEditor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        IDocument document = viewer.getDocument();
        DocumentArtifact comment = DocumentArtifact.getArtifactAt(document, documentOffset);
        String to = comment.getContentTo(documentOffset, false);
        if (to.length() < 4)
        {
            return NoProposals;
        }

        boolean addLeadingSpaces = to.length() == 4;

        String content = comment.getContent().substring(4);
        if (PATTERN_MATCHER.matches(content, ENDS_WITH_ANOTHER_COMMENT_PATTERN))
        {
            return new ICompletionProposal[] {
                 new CompletionProposal(
                    "-->",
                    documentOffset,
                    0,
                    new Point(3, 0),
                    Images.getSharedImage("bullet.gif"),
                    "Close comment here. '-->'",
                    null,
                    null)};
        }
        if (content.endsWith("-->"))
        {
            return NoSuggestions;
        }

        return new ICompletionProposal[] {
             new CompletionProposal(
                addLeadingSpaces ? "  -->" : "-->",
                documentOffset,
                0,
                addLeadingSpaces ? new Point(1, 0) : new Point(3, 0),
                Images.getSharedImage("bullet.gif"),
                "Close comment here. '-->'",
                null,
                null)};

    }

}
