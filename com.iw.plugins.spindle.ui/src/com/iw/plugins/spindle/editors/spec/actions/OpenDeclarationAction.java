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

package com.iw.plugins.spindle.editors.spec.actions;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.spec.assist.SpecTapestryAccess;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.iw.plugins.spindle.editors.util.DocumentArtifactPartitioner;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 *  Open an interesting thing, if possible.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class OpenDeclarationAction extends BaseSpecAction
{
    public static final String ACTION_ID = UIPlugin.PLUGIN_ID + ".spec.openDeclaration";

    public OpenDeclarationAction()
    {
        super();
        //      TODO I10N
        setText("Open Declaration");
        setId(ACTION_ID);
    }

    protected void doRun()
    {
        DocumentArtifact artifact = DocumentArtifact.getArtifactAt(fDocument, fDocumentOffset);
        String type = artifact.getType();
        if (type == DocumentArtifactPartitioner.TEXT
            || type == DocumentArtifactPartitioner.COMMENT
            || type == DocumentArtifactPartitioner.PI
            || type == DocumentArtifactPartitioner.DECL)
        {
            return;
        }
        if (type == DocumentArtifactPartitioner.ENDTAG)
            artifact = artifact.getCorrespondingNode();

        if (artifact == null)
            return;

        String name = artifact.getName();

        if (name == null)
            return;

        name = name.toLowerCase();

        if (name.equals("binding")
            || name.equals("static-binding")
            || name.equals("inherited-binding")
            || name.equals("message-binding")
            || name.equals("string-binding")
            || name.equals("field-binding"))
        {
            DocumentArtifact parent = artifact.getParent();
            String parentName = parent.getName();
            if (parentName == null)
                return;
            parentName = parentName.toLowerCase();
            if (!parentName.equals("component"))
                return;

            handleComponentBinding(parent, artifact, name);
            return;
        }

        if ("component".equals(name))
            handleComponentLookup(artifact);

        else if ("application".equals(name))
            handleTypeLookup(artifact, "engine-class");

        else if ("bean".equals(name))
            handleTypeLookup(artifact, "class");

        else if ("component-specification".equals(name))
            handleTypeLookup(artifact, "class");

        else if ("page-specification".equals(name))
            handleTypeLookup(artifact, "class");

        else if ("extension".equals(name))
            handleTypeLookup(artifact, "class");

        else if ("service".equals(name))
            handleTypeLookup(artifact, "class");
    }

    /**
     * @param artifact
     */
    private void handleComponentLookup(DocumentArtifact artifact)
    {

        SpecTapestryAccess access = null;
        try
        {
            access = new SpecTapestryAccess(fEditor);
        } catch (IllegalArgumentException e)
        {
            // do nothing
        }

        if (access == null)
            return;

        // first try and resolve the component...
        DocumentArtifact attribute = artifact.getAttributeAt(fDocumentOffset);
        if (attribute == null)
            return;

        String name = attribute.getName();

        if (name == null)
            return;

        String typeName = null;
        if ("type".equals(name.toLowerCase()))
            typeName = attribute.getAttributeValue();

        if (typeName == null)
            return;

        PluginComponentSpecification spec = (PluginComponentSpecification) access.resolveComponentType(typeName);
        if (spec == null)
            return;

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec.getSpecificationLocation();
        if (location == null || !location.exists())
            return;

        foundResult(location.getStorage(), null);

    }

    private void handleComponentBinding(DocumentArtifact parent, DocumentArtifact binding, String attrName)
    {
        // TODO Auto-generated method stub

    }

    private void handleTypeLookup(DocumentArtifact artifact, String attrName)
    {
        DocumentArtifact attribute = artifact.getAttributeAt(fDocumentOffset);
        if (attribute == null)
            return;

        String name = attribute.getName();

        if (name == null)
            return;

        String typeName = null;
        if (attrName.equals(name.toLowerCase()))
            typeName = attribute.getAttributeValue();

        if (typeName == null)
            return;

        IJavaProject jproject = TapestryCore.getDefault().getJavaProjectFor(fEditor.getStorage());
        if (jproject == null)
            return;

        IType type = null;

        try
        {
            type = jproject.findType(typeName);
        } catch (JavaModelException e)
        {
            //do nothing
        }

        if (type == null)
            return;

        foundResult(type, null);

    }

    protected void foundResult(Object result, Object moreInfo)
    {
        if (result instanceof IType)
        {
            try
            {
                JavaUI.openInEditor((IType) result);
            } catch (PartInitException e)
            {
                UIPlugin.log(e);
            } catch (JavaModelException e)
            {
                UIPlugin.log(e);
            }
        } else if (result instanceof IStorage)
        {
            UIPlugin.openTapestryEditor((IStorage) result);
            IEditorPart editor = UIUtils.getEditorFor((IStorage) result);
            if (editor != null && (editor instanceof Editor) || moreInfo != null)
            {
                // TODO implement
                //((Editor)editor).openTo(moreInfo);
            }

        }
    }
}
