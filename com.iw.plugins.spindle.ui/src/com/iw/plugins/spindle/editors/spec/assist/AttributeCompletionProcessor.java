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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
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

    private DocumentArtifact fTag;

    private SpecAssistHelper fAssistHelper;

    public AttributeCompletionProcessor(Editor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        fTag = DocumentArtifact.getArtifactAt(viewer.getDocument(), documentOffset);
        fTagName = fTag.getName();
        String type = fTag.getType();
        if (fTagName == null
            || (type != DocumentArtifactPartitioner.TAG && type != DocumentArtifactPartitioner.EMPTYTAG))
            return NoProposals;

        DocumentArtifact attribute = fTag.getAttributeAt(documentOffset);
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

    /* (non-Javadoc)
        * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeContextInformation(org.eclipse.jface.text.ITextViewer, int)
        */
    public IContextInformation[] doComputeContextInformation(ITextViewer viewer, int documentOffset)
    {
        SpecAssistHelper helper;
        try
        {
            helper = new SpecAssistHelper(fEditor);
        } catch (IllegalArgumentException e)
        {
            return NoInformation;
        }
        fTag = DocumentArtifact.getArtifactAt(viewer.getDocument(), documentOffset);
        fTagName = fTag.getName();

        if (fTagName == null)
            return NoInformation;

        DocumentArtifact attribute = fTag.getAttributeAt(documentOffset);

        if (attribute != null)
        {
            fAttributeName = attribute.getName();
            String value = attribute.getAttributeValue();

            if (value != null)
            {
                fAssistHelper = null;
                try
                {
                    fAssistHelper = new SpecAssistHelper(fEditor);
                } catch (IllegalArgumentException e1)
                {
                    return NoInformation;
                }

                if ("component".equals(fTagName) && "type".equals(fAttributeName))
                    return computeComponentTypeInformation(value);

                if ("binding".equals(fTagName) && "name".equals(fAttributeName))
                    return computeBindingNameInformation(value);

                if ("static-binding".equals(fTagName) && "name".equals(fAttributeName))
                    return computeBindingNameInformation(value);

                if ("message-binding".equals(fTagName) && "name".equals(fAttributeName))
                    return computeBindingNameInformation(value);

                if ("inherited-binding".equals(fTagName) && "name".equals(fAttributeName))
                    return computeBindingNameInformation(value);

                if ("listener-binding".equals(fTagName) && "name".equals(fAttributeName))
                    return computeBindingNameInformation(value);
            }
        }
        return NoInformation;
    }

    /**
     * @param value
     * @return
     */
    private IContextInformation[] computeComponentTypeInformation(String value)
    {
        IComponentSpecification resolved = fAssistHelper.resolveComponentType(value);
        
        if (resolved == null)
            return NoInformation;
            
        
        UITapestryAccess.Result info =
            fAssistHelper.createComponentInformationResult(value, value, resolved);
            
        return new IContextInformation[] { new ContextInformation(value, info.description)};    
    }

    /**
     * @param value
     * @return
     */
    private IContextInformation[] computeBindingNameInformation(String value)
    {
        PluginComponentSpecification containedComponent = findParentSpecification();
        // are we editing a component/page and was it picked up by the last build?

        if (containedComponent == null)
            return NoInformation;

        IParameterSpecification parameter = containedComponent.getParameter(value);

        if (parameter == null)
            return NoInformation;

        UITapestryAccess.Result info =
            fAssistHelper.createParameterResult(value, parameter, containedComponent.getPublicId());

        return new IContextInformation[] { new ContextInformation(value, info.description)};
    }

    /**
     * @return
     */
    private List computeTagAttrSpecificProposals()
    {
        fAssistHelper = null;
        try
        {
            fAssistHelper = new SpecAssistHelper(fEditor);
        } catch (IllegalArgumentException e1)
        {
            return Collections.EMPTY_LIST;
        }
        if ("component".equals(fTagName) && "type".equals(fAttributeName))
            return computeComponentTypeProposals();

        if ("binding".equals(fTagName) && "name".equals(fAttributeName))
            return computeBindingNameProposals();

        if ("static-binding".equals(fTagName) && "name".equals(fAttributeName))
            return computeBindingNameProposals();

        if ("message-binding".equals(fTagName) && "name".equals(fAttributeName))
            return computeBindingNameProposals();

        if ("inherited-binding".equals(fTagName) && "name".equals(fAttributeName))
            return computeBindingNameProposals();

        if ("listener-binding".equals(fTagName) && "name".equals(fAttributeName))
            return computeBindingNameProposals();

        return Collections.EMPTY_LIST;
    }

    private PluginComponentSpecification findParentSpecification()
    {
        PluginComponentSpecification fileComponent = (PluginComponentSpecification) fEditor.getComponent();
        if (fileComponent == null)
            return null;

        // need to determine:
        // 1. the component type
        // 2. the parameter names already in use
        DocumentArtifact parent = fTag.getParent();
        String parentName;

        // locate the spec for the contained component
        if (parent != null)
        {
            parentName = parent.getName();
            if (parentName != null && parentName.equals("component"))
            {
                Map attrs = parent.getAttributesMap();
                DocumentArtifact typeAttribute = (DocumentArtifact) attrs.get("type");
                if (typeAttribute != null)
                {
                    String typeValue = typeAttribute.getAttributeValue();
                    if (typeValue != null && typeValue.length() > 0)
                        return (PluginComponentSpecification) fAssistHelper.resolveComponentType(typeValue);
                }
            }
        }
        return null;
    }

    /**
     * compute proposals for 'name' attributes of the various binding tags.
     */
    private List computeBindingNameProposals()
    {
        PluginComponentSpecification containedComponent = findParentSpecification();
        // are we editing a component/page and was it picked up by the last build?

        if (containedComponent == null)
            return Collections.EMPTY_LIST;

        // get a list of parameters already chosen
        List children = fTag.getParent().getChildren();
        Set existingParameterNames = new HashSet();
        for (Iterator iter = children.iterator(); iter.hasNext();)
        {
            DocumentArtifact child = (DocumentArtifact) iter.next();
            String childType = child.getType();
            if ((childType != DocumentArtifactPartitioner.TAG && childType != DocumentArtifactPartitioner.EMPTYTAG)
                || child.equals(fTag))
                continue;

            String childName = child.getName();
            if (childName == null || childName.length() == 0)
                continue;

            if (!("binding".equals(childName)
                || "static-binding".equals(childName)
                || "inherited-binding".equals(childName)
                || "message-binding".equals(childName)
                || "listener-binding".equals(childName)))
            {
                continue;
            }
            Map childParms = child.getAttributesMap();
            DocumentArtifact nameAttr = (DocumentArtifact) childParms.get("name");
            if (nameAttr == null)
                continue;

            existingParameterNames.add(nameAttr.getAttributeValue().toLowerCase());
        }

        UITapestryAccess.Result[] parms =
            SpecAssistHelper.findParameters(containedComponent, fMatchString, existingParameterNames);

        List proposals = new ArrayList();
        for (int i = 0; i < parms.length; i++)
        {
            CompletionProposal proposal = getProposal(parms[i].name, parms[i].displayName, parms[i].description);
            if (parms[i].required)
                proposal.setImage(Images.getSharedImage("bullet_pink.gif"));
            proposals.add(proposal);
        }

        return proposals;
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

        List proposals = new ArrayList();

        UITapestryAccess.Result[] foundTopLevel = fAssistHelper.getComponents();
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
        UITapestryAccess.Result[] foundChild = fAssistHelper.getAllChildNamespaceComponents();
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
