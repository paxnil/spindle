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
import com.iw.plugins.spindle.editors.template.assist.TemplateTapestryAccess;
import com.iw.plugins.spindle.ui.util.UIUtils;

import core.resources.ICoreResource;

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

    public OpenDeclarationAction()
    {
        super();
        setText(UIPlugin.getString(ACTION_ID));
        setId(ACTION_ID);
    }

    protected void doRun()
    {

        INamespace namespace = fEditor.getNamespace();
        if (namespace == null)
        {
            MessageDialog.openError(
                    UIPlugin.getDefault().getActiveWorkbenchShell(),
                    "Operation Aborted",
                    "This file can not be seen by the Tapestry builder");
            return;
        }

        XMLNode artifact = XMLNode.getArtifactAt(fDocument, fDocumentOffset);
        if (artifact == null)
            return;
        String type = artifact.getType();
        if (type == ITypeConstants.TEXT || type == ITypeConstants.COMMENT
                || type == ITypeConstants.PI || type == ITypeConstants.DECL
                || type == ITypeConstants.ENDTAG)
        {
            return;
        }

        XMLNode attrAtOffset = artifact.getAttributeAt(fDocumentOffset);
        if (attrAtOffset == null)
            return;

        Map attrs = artifact.getAttributesMap();

        XMLNode jwcidAttribute = (XMLNode) attrs.get(TemplateParser.JWCID_ATTRIBUTE_NAME);

        if (jwcidAttribute == null)
            return;

        IComponentSpecification componentSpec = resolveComponentSpec(jwcidAttribute
                .getAttributeValue());

        if (componentSpec == null)
            return;

        if (attrAtOffset.equals(jwcidAttribute))
        {
            handleComponentLookup(componentSpec);
        }
        else
        {
            handleBinding(componentSpec, attrAtOffset.getName());
        }
    }

    /**
     * @param componentSpec
     */
    private void handleComponentLookup(IComponentSpecification componentSpec)
    {
        ICoreResource location = null;
        IContainedComponent contained = fAccess.getContainedComponent();
        if (contained != null)
        {
            String simpleId = fAccess.getSimpleId();
            location = (ICoreResource) fAccess.getBaseSpecification()
                    .getSpecificationLocation();
            if (location != null && location.getStorage() != null)
                foundResult(location.getStorage(), simpleId, contained);
        }
        else
        {
            location = (ICoreResource) componentSpec.getSpecificationLocation();
            if (location == null || location.getStorage() == null)
                return;

            foundResult(location.getStorage(), null, null);
        }

    }

    /**
     * @param componentSpec
     * @param string
     */
    private void handleBinding(IComponentSpecification componentSpec, String parameterName)
    {
        IParameterSpecification parameterSpec = (IParameterSpecification) componentSpec
                .getParameter(parameterName);
        if (parameterSpec != null)
        {
            ICoreResource location = (ICoreResource) componentSpec
                    .getSpecificationLocation();
            if (location == null || location.getStorage() == null)
                return;

            foundResult(location.getStorage(), parameterName, parameterSpec);
        }

    }

    /**
     * @param string
     * @return
     */
    private IComponentSpecification resolveComponentSpec(String jwcid)
    {
        if (jwcid == null)
            return null;

        try
        {
            fAccess = new TemplateTapestryAccess(fEditor);
            fAccess.setJwcid(jwcid);
            return fAccess.getResolvedComponent();
        }
        catch (IllegalArgumentException e)
        {
            // do nothing
        }
        return null;
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