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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.UITapestryAccess;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.iw.plugins.spindle.editors.util.DocumentArtifactPartitioner;

/**
 *  Content assist inside of attribute values
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class AttributeCompletionProcessor extends SpecCompletionProcessor
{
    private String fTagName;

    private String fAttributeName;

    private Point fValueLocation;

    private String fAttributeValue;

    private String fMatchString;

    private boolean fIsAttributeTerminated;

    public AttributeCompletionProcessor(Editor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        DocumentArtifact tag = DocumentArtifact.getArtifactAt(viewer.getDocument(), documentOffset);
        fTagName = tag.getName();
        String type = tag.getType();
        if (fTagName == null
            || (type != DocumentArtifactPartitioner.TAG && type != DocumentArtifactPartitioner.EMPTYTAG))
            return NoProposals;

        DocumentArtifact attribute = tag.getAttributeAt(documentOffset);
        fAttributeName = attribute.getName();

        if (fAttributeName == null)
            return NoProposals;

        int state = attribute.getStateAt(documentOffset);

        if (state == DocumentArtifact.TAG)
            return NoProposals;

        fValueLocation = null;
        fAttributeValue = null;
        fMatchString = "";
        try
        {
            IDocument document = viewer.getDocument();
            ITypedRegion region = document.getPartition(documentOffset);
            fValueLocation = new Point(region.getOffset() + 1, region.getLength() - 1);
            int lastCharOffset = fValueLocation.x + fValueLocation.y - 1;
            char last = viewer.getDocument().getChar(lastCharOffset);
            fIsAttributeTerminated = last == '\'' || last == '"';
            if (fIsAttributeTerminated)
            {
                fValueLocation.y -= 1;
            }
            fAttributeValue = document.get(fValueLocation.x, fValueLocation.y);
            char[] chars = fAttributeValue.toCharArray();
            int i = 0;
            for (; i < chars.length; i++)
            {
                if (!Character.isWhitespace(chars[i]))
                    break;
            }
            if (i > 0)
            {
                fValueLocation.x += i;
                fValueLocation.y -= i;
                fAttributeValue = document.get(fValueLocation.x, fValueLocation.y);
            }

            if (documentOffset > fValueLocation.x)
                fMatchString = fAttributeValue.substring(0, documentOffset - fValueLocation.x).toLowerCase();

        } catch (BadLocationException e)
        {
            return NoProposals;
        }

        List dtdAllowed = computeDTDAllowedProposals(0);

        String special = null;

        if (dtdAllowed.isEmpty())
            special = SpecAssistHelper.getTapestryDefaultValue(fDTD, fTagName, fAttributeName);

        List proposals = new ArrayList(dtdAllowed);

        proposals.addAll(computeTagAttrSpecificProposals());

        if (proposals.isEmpty())
        {
            if (special != null)
                proposals.add(getProposal(special));
        } 

        Collections.sort(proposals, CompletionProposal.PROPOSAL_COMPARATOR);

        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    /**
     * @return
     */
    private List computeTagAttrSpecificProposals()
    {
        if ("component".equals(fTagName) && "type".equals(fAttributeName))
            return computeComponentTypeProposals();

        return Collections.EMPTY_LIST;
    }

    /**
     * Compute proposals for converting a Dynamic to a Message or a Static binding
     */
    private List computeDTDAllowedProposals(int documentOffset)
    {
        List allowedValues = SpecAssistHelper.getAllowedAttributeValues(fDTD, fTagName, fAttributeName);
        String defaultValue = SpecAssistHelper.getDefaultAttributeValue(fDTD, fTagName, fAttributeName);

        if (allowedValues == null || allowedValues.isEmpty())
            return Collections.EMPTY_LIST;

        List result = new ArrayList();

        for (Iterator iter = allowedValues.iterator(); iter.hasNext();)
        {
            String value = (String) iter.next();
            if (fMatchString.length() > 0 && !value.startsWith(fMatchString))
                continue;

            CompletionProposal proposal = getProposal(value);
            if (value.equals(defaultValue))
                proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
            proposal.setYOrder(value.equals(fAttributeValue) ? 100 : 99);
            result.add(proposal);
        }
        return result;
    }

    private CompletionProposal getProposal(String value)
    {
        return getProposal(value, null, null);
    }

    private CompletionProposal getProposal(String value, String displayName, String extraInfo)
    {
        return new CompletionProposal(
            value,
            fValueLocation.x,
            fIsAttributeTerminated ? fAttributeValue.length() : fMatchString.length(),
            new Point(value.length(), 0),
            Images.getSharedImage("bullet.gif"),
            displayName,
            null,
            extraInfo);
    }

    private List computeComponentTypeProposals()
    {
        SpecAssistHelper helper = null;
        try
        {
            helper = new SpecAssistHelper(fEditor);
            IStorage storage = (IStorage) fEditor.getEditorInput().getAdapter(IStorage.class);
            IProject project = TapestryCore.getDefault().getProjectFor(storage);
            helper.setFrameworkNamespace(
                (ICoreNamespace) TapestryArtifactManager.getTapestryArtifactManager().getFrameworkNamespace(project));
        } catch (IllegalArgumentException e1)
        {
            return Collections.EMPTY_LIST;
        }

        List proposals = new ArrayList();

        UITapestryAccess.Result[] foundTopLevel = helper.getComponents();
        for (int i = 0; i < foundTopLevel.length; i++)
        {
            boolean matches =
                fMatchString == null ? true : foundTopLevel[i].name.toLowerCase().startsWith(fMatchString);
            if (matches)
            {
                CompletionProposal proposal =
                    getProposal(foundTopLevel[i].name, foundTopLevel[i].displayName, foundTopLevel[i].description);
                proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
                proposals.add(proposal);
            }
        }
        UITapestryAccess.Result[] foundChild = helper.getAllChildNamespaceComponents();
        for (int i = 0; i < foundChild.length; i++)
        {
            boolean matches = fMatchString == null ? true : foundChild[i].name.toLowerCase().startsWith(fMatchString);
            if (matches)
            {
                CompletionProposal proposal =
                    getProposal(foundChild[i].name, foundChild[i].displayName, foundChild[i].description);
                proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
                proposals.add(proposal);
            }
        }
        return proposals;
    }

}
