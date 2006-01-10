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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.template.actions;

import java.util.Map;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IParameterSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.editors.template.assist.TemplateTapestryAccess;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * Open an interesting thing, if possible.
 * 
 * @author glongman@gmail.com
 */
public class OpenDeclarationAction extends BaseTemplateAction
{
    public static final String ACTION_ID = UIPlugin.PLUGIN_ID
            + ".editor.commands.navigate.openDeclaration";

    private TemplateTapestryAccess fAccess;

    private XMLNode fJwcidAttribute;

    public OpenDeclarationAction()
    {
        super();
        setText(UIPlugin.getString(ACTION_ID));
        setId(ACTION_ID);
    }

    protected IStatus getStatus()
    {
        SpindleStatus status = (SpindleStatus) super.getStatus();
        if (status == null || !status.isOK())
            return status;

        INamespace namespace = getSpindleEditor().getNamespace();
        if (namespace == null)
        {
            status.setError("This file can not be seen by the Tapestry builder");
            return status;
        }

        XMLNode artifact = XMLNode.getArtifactAt(fDocument, getDocumentOffset());
        if (artifact == null)
            return status;
        String type = artifact.getType();
        if (type == ITypeConstants.TEXT || type == ITypeConstants.COMMENT
                || type == ITypeConstants.PI || type == ITypeConstants.DECL
                || type == ITypeConstants.ENDTAG)
        {
            return status;
        }

        XMLNode attrAtOffset = artifact.getAttributeAt(getDocumentOffset());
        if (attrAtOffset == null)
            return status;

        Map attrs = artifact.getAttributesMap();

        XMLNode jwcidAttribute = (XMLNode) attrs.get(TemplateParser.JWCID_ATTRIBUTE_NAME);

        if (jwcidAttribute == null)
            return status;

        TemplateTapestryAccess access = getTemplateAccess();
        if (access == null)
            return status;

        IComponentSpecification componentSpec = resolveComponentSpec(jwcidAttribute
                .getAttributeValue(), access);

        if (componentSpec == null)
            return status;

        if (attrAtOffset.equals(jwcidAttribute))
        {
            status = handleComponentLookup(componentSpec, status, access);
        }
        else
        {
            status = handleBinding(componentSpec, attrAtOffset.getName(), status);
        }

        return status;
    }

    protected ChooseLocationPopup getChooseLocationPopup(Object[] locations)
    {       
        return null;
    }
    
    

    protected void reveal(Object[] objects)
    {
        if (objects.length != 3)
            return;
        foundResult(objects[0], (String) objects[1], objects[2]);
    }

    protected void postReveal(Object revealed, IEditorPart editor)
    {
       //do nothing
    }


    private SpindleStatus handleComponentLookup(IComponentSpecification componentSpec,
            SpindleStatus status, TemplateTapestryAccess templateTapestryAccess)
    {
        IResourceWorkspaceLocation location = null;
        IContainedComponent contained = templateTapestryAccess.getContainedComponent();
        if (contained != null)
        {
            String simpleId = templateTapestryAccess.getSimpleId();
            location = (IResourceWorkspaceLocation) templateTapestryAccess.getBaseSpecification()
                    .getSpecificationLocation();

            if (location == null)
                return status;

            IStorage storage = location.getStorage();

            if (storage == null)
                return status;

            fInterestingObjects = new Object[]
            { storage, simpleId, contained };
        }
        else
        {
            location = (IResourceWorkspaceLocation) componentSpec.getSpecificationLocation();
            if (location == null)
                return status;

            IStorage storage = location.getStorage();

            if (storage == null)
                return status;

            fInterestingObjects = new Object[]
            { storage, null, null };
        }

        return status;
    }

    /**
     * @param componentSpec
     * @param status
     *            TODO
     * @param string
     * @return TODO
     */
    private SpindleStatus handleBinding(IComponentSpecification componentSpec,
            String parameterName, SpindleStatus status)
    {
        IParameterSpecification parameterSpec = (IParameterSpecification) componentSpec
                .getParameter(parameterName);
        if (parameterSpec != null)
        {
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) componentSpec
                    .getSpecificationLocation();
            if (location == null)
                return status;

            IStorage storage = location.getStorage();
            if (storage == null)
                return status;

            fInterestingObjects = new Object[]
            { storage, parameterName, parameterSpec };
            // foundResult(location.getStorage(), parameterName, parameterSpec);
        }

        return status;

    }

    private TemplateTapestryAccess getTemplateAccess()
    {
        try
        {
            return new TemplateTapestryAccess(getSpindleEditor());
        }
        catch (IllegalArgumentException e)
        {
            // do nothing
        }
        return null;
    }

    private IComponentSpecification resolveComponentSpec(String jwcid,
            TemplateTapestryAccess templateTapestryAccess)
    {
        if (jwcid == null || templateTapestryAccess == null)
            return null;

        templateTapestryAccess.setJwcid(jwcid);
        return templateTapestryAccess.getResolvedComponent();
    }

    protected void foundResult(Object result, String key, Object moreInfo)
    {
        if (result instanceof IType)
        {
            try
            {
                JavaUI.openInEditor((IType) result);
            }
            catch (PartInitException e)
            {
                UIPlugin.log(e);
            }
            catch (JavaModelException e)
            {
                UIPlugin.log(e);
            }
        }
        else if (result instanceof IStorage)
        {
            UIPlugin.openTapestryEditor((IStorage) result);
            IEditorPart editor = UIUtils.getEditorFor((IStorage) result);
            if (editor != null && (editor instanceof AbstractTextEditor) || moreInfo != null)
            {
                if (moreInfo instanceof IParameterSpecification && key != null)
                {
                    reveal((AbstractTextEditor) editor, "parameter", "name", key);
                }
                else if (moreInfo instanceof IContainedComponent && key != null)
                {
                    reveal((AbstractTextEditor) editor, "component", "id", key);
                }

            }
        }
    }

    private void reveal(AbstractTextEditor editor, String elementName, String attrName,
            String attrValue)
    {
        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

        try
        {
            XMLNode reveal = null;
            Position[] pos = null;
            pos = document.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
            for (int i = 0; i < pos.length; i++)
            {
                XMLNode artifact = (XMLNode) pos[i];
                if (artifact.getType() == ITypeConstants.ENDTAG)
                    continue;
                String name = artifact.getName();
                if (name == null)
                    continue;

                if (!elementName.equals(name.toLowerCase()))
                    continue;

                Map attributesMap = artifact.getAttributesMap();
                XMLNode attribute = (XMLNode) attributesMap.get(attrName);
                if (attribute == null)
                    continue;

                String value = attribute.getAttributeValue();
                if (value != null && value.equals(attrValue))
                {
                    reveal = artifact;
                    break;
                }
            }
            if (reveal != null)
                editor.setHighlightRange(reveal.getOffset(), reveal.getLength(), true);

        }
        catch (Exception e)
        {
            UIPlugin.log(e);
        }
    }

}