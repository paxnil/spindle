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

package com.iw.plugins.spindle.editors.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;

/**
 *  Jump to the next attribute in the document
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JumpToNextAttributeAction extends BaseJumpAction implements IDocumentListener
{

    private boolean fForward;
    protected IRegion[] fRegions;
    private IDocument fStoredDocument;

    /**
     * 
     */
    public JumpToNextAttributeAction(boolean forward)
    {
        super();
        fForward = forward;
    }

    /**
      * @param text
      */
    public JumpToNextAttributeAction(String text, boolean forward)
    {
        super(text);
        fForward = forward;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
     */
    protected void doRun()
    {
        fDocument = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());

        IDocument currentDocument = getDocument();

        if (currentDocument != fStoredDocument)
        {
            if (fStoredDocument != null)
                fStoredDocument.removeDocumentListener(this);

            fStoredDocument = currentDocument;
            fStoredDocument.addDocumentListener(this);

            buildRegionList(fStoredDocument);

        } else if (fRegions == null)
        {
            buildRegionList(fStoredDocument);
        }

        IRegion attributeRegion = null;
        if (fForward)
        {
            attributeRegion = getNextAttributeRegion(fDocumentOffset);
        } else
        {
            attributeRegion = getPreviousAttributeRegion(fDocumentOffset, fStoredDocument.getLength() - 1);
        }

        if (attributeRegion != null)
        {
            fEditor.selectAndReveal(attributeRegion.getOffset(), attributeRegion.getLength());
        }

    }

    protected void buildRegionList(IDocument document)
    {
        attachPartitioner();

        Position[] positions = null;
        try
        {
            positions = document.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
        } catch (BadPositionCategoryException e)
        {
            UIPlugin.log(e);
            return;
        }

        Arrays.sort(positions, XMLNode.COMPARATOR);

        ArrayList regionCollector = new ArrayList();

        List attributes = null;

        for (int i = 0; i < positions.length; i++)
        {
            XMLNode node = (XMLNode) positions[i];
            attributes = node.getAttributes();
            if (attributes.isEmpty())
                continue;

            for (Iterator iter = attributes.iterator(); iter.hasNext();)
            {
                XMLNode attribute = (XMLNode) iter.next();
                regionCollector.add(attribute.getAttributeValueRegion());
            }
        }

        fRegions = (IRegion[]) regionCollector.toArray(new IRegion[regionCollector.size()]);

    }

    /**
     * @param document
     * @return
     */
    private IRegion getNextAttributeRegion(int documentOffset)
    {
        int startIndex = findIndexForOffset(fRegions, documentOffset, true);

        if (startIndex == -1)
            startIndex = findIndexForOffset(fRegions, 0, true);

        if (startIndex >= 0)
            return fRegions[startIndex];

        return null;
    }

    /**
     * @param document
     * @return
     */
    private IRegion getPreviousAttributeRegion(int documentOffset, int lastDocumentOffset)
    {
        int startIndex = findIndexForOffset(fRegions, documentOffset, false);

        if (startIndex == -1)
            startIndex = findIndexForOffset(fRegions, lastDocumentOffset, false);

        if (startIndex >= 0)
            return fRegions[startIndex];

        return null;
    }

    public boolean overlaps(int offset, IRegion region)
    {
        int rOffset = region.getOffset();
        int rLength = region.getLength();
        int rEnd = rOffset + rLength;

        if (rLength > 0)
            return rOffset <= offset && offset <= rEnd;

        return offset == rOffset;
    }

    private int findIndexForOffset(IRegion[] regions, int documentOffset, boolean lookingForward)
    {
        int count = regions.length;
        if (lookingForward)
        {
            for (int i = 0; i < count; i++)
            {
                int offset = regions[i].getOffset();

                if (!overlaps(documentOffset, regions[i]) && offset > documentOffset)
                    return i;
                //            } else if (fDocumentOffset <= end)
                //        {
                //            return i;
                //        }
            }
        } else
        {
            for (int i = count - 1; i >= 0; i -= 1)
            {
                int end = regions[i].getOffset() + regions[i].getLength();

                if (overlaps(documentOffset, regions[i]))
                    continue;
                    
                if (end <= documentOffset)
                    return i;
            }
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event)
    {
        fRegions = null;
    }

}
