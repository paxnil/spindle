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

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
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
    private int fDocumentOffset;

    private String fTagName;

    private String fAttributeName;

    private Point fValueLocation;

    private String fAttributeValue;

    private String fMatchString;

    private boolean fIsAttributeTerminated;

    private DocumentArtifact fTag;

    private SpecTapestryAccess fAssistHelper;

    public AttributeCompletionProcessor(Editor editor)
    {
        super(editor);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.assist.ContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        try
        {
            fDocumentOffset = documentOffset;
            fTag = DocumentArtifact.getArtifactAt(viewer.getDocument(), fDocumentOffset);
            fTagName = fTag.getName();
            String type = fTag.getType();
            if (fTagName == null
                || (type != DocumentArtifactPartitioner.TAG && type != DocumentArtifactPartitioner.EMPTYTAG))
                return NoProposals;

            DocumentArtifact attribute = fTag.getAttributeAt(fDocumentOffset);
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
                ITypedRegion region = document.getPartition(fDocumentOffset);
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

            List dtdAllowed = computeDTDAllowedProposals();

            String special = null;

            if (dtdAllowed.isEmpty())
                special = SpecTapestryAccess.getTapestryDefaultValue(fDTD, fTagName, fAttributeName);

            List proposals = new ArrayList(dtdAllowed);

            proposals.addAll(computeTagAttrSpecificProposals());

            if (proposals.isEmpty())
            {
                if (special != null)
                    proposals.add(getProposal(special));
            }

            Collections.sort(proposals, CompletionProposal.PROPOSAL_COMPARATOR);

            return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
        } catch (RuntimeException e)
        {
            UIPlugin.log(e);
            throw e;
        } finally
        {
            fAssistHelper = null;
        }
    }

    /* (non-Javadoc)
        * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#doComputeContextInformation(org.eclipse.jface.text.ITextViewer, int)
        */
    public IContextInformation[] doComputeContextInformation(ITextViewer viewer, int documentOffset)
    {
        try
        {
            fDocumentOffset = documentOffset;
            SpecTapestryAccess helper;
            try
            {
                helper = new SpecTapestryAccess(fEditor);
            } catch (IllegalArgumentException e)
            {
                return NoInformation;
            }
            fTag = DocumentArtifact.getArtifactAt(viewer.getDocument(), fDocumentOffset);
            fTagName = fTag.getName();

            if (fTagName == null)
                return NoInformation;

            DocumentArtifact attribute = fTag.getAttributeAt(fDocumentOffset);

            if (attribute != null)
            {
                fAttributeName = attribute.getName();
                String value = attribute.getAttributeValue();

                if (value != null)
                {
                    fAssistHelper = null;
                    try
                    {
                        fAssistHelper = new SpecTapestryAccess(fEditor);
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
        } catch (RuntimeException e)
        {
            UIPlugin.log(e);
            throw e;
        } finally
        {
            fAssistHelper = null;
        }
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

        UITapestryAccess.Result info = fAssistHelper.createComponentInformationResult(value, value, resolved);

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
            fAssistHelper = new SpecTapestryAccess(fEditor);
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

        if ("application".equals(fTagName) && "engine-class".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("bean".equals(fTagName) && "class".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("component-specification".equals(fTagName) && "class".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("page-specification".equals(fTagName) && "class".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("extension".equals(fTagName) && "class".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("service".equals(fTagName) && "class".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("property-specification".equals(fTagName) && "type".equals(fAttributeName) && fIsAttributeTerminated)
            return chooseJavaTypeNameProposals();

        if ("parameter".equals(fTagName) && fIsAttributeTerminated)
        {
            if (fDTD.getPublicId().equals(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID)
                && "java-type".equals(fAttributeName))
            {
                return chooseJavaTypeNameProposals();
            } else if ("type".equals(fAttributeName))
            {
                return chooseJavaTypeNameProposals();
            }
        }

        return Collections.EMPTY_LIST;
    }

    private PluginComponentSpecification findParentSpecification()
    {
        Object spec = fEditor.getSpecification();
        if (spec == null || !(spec instanceof PluginComponentSpecification))
            return null;
        PluginComponentSpecification fileComponent = (PluginComponentSpecification) spec;

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
            SpecTapestryAccess.findParameters(containedComponent, fMatchString, existingParameterNames);

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
    private List computeDTDAllowedProposals()
    {
        List allowedValues = SpecTapestryAccess.getAllowedAttributeValues(fDTD, fTagName, fAttributeName);
        String defaultValue = SpecTapestryAccess.getDefaultAttributeValue(fDTD, fTagName, fAttributeName);

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

    /**
     * @return
     */
    private List chooseJavaTypeNameProposals()
    {
        IJavaProject jproject = TapestryCore.getDefault().getJavaProjectFor(fEditor.getStorage());
        if (jproject == null)
            return Collections.EMPTY_LIST;

        List result = new ArrayList();
        result.add(new ChooseTypeProposal(jproject, fDocumentOffset, fValueLocation.x, fAttributeValue.length()));
        return result;
    }

    class ChooseTypeProposal implements ICompletionProposal
    {

        protected IJavaProject jproject;
        String chosenType;
        int documentOffset;
        int replacementOffset;
        int replacementLength;

        public ChooseTypeProposal(
            IJavaProject project,
            int documentOffset,
            int replacementOffset,
            int replacementLength)
        {
            Assert.isNotNull(project);
            jproject = project;
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
                {
                    return null;
                }
                IJavaSearchScope scope = createSearchScope(jproject);

                SelectionDialog dialog =
                    JavaUI.createTypeDialog(
                        shell,
                        new ProgressMonitorDialog(shell),
                        scope,
                        IJavaElementSearchConstants.CONSIDER_CLASSES,
                        false);

                dialog.setTitle("Java Type Chooser");
                dialog.setMessage("Choose Type");

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

}
