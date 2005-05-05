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

package com.iw.plugins.spindle.editors.spec.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.extensions.ComponentTypeResourceResolvers;
import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.editors.spec.assist.SpecTapestryAccess;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * Open an interesting thing, if possible.
 * 
 * @author glongman@gmail.com
 */
public class OpenDeclarationAction extends BaseSpecAction
{

    public static final String ACTION_ID = UIPlugin.PLUGIN_ID
            + ".editor.commands.navigate.openDeclaration";

    private Map fHandlers;

    public OpenDeclarationAction()
    {
        super();
        setText(UIPlugin.getString(ACTION_ID));
        setId(ACTION_ID);
        init();
    }

    protected void doRun()
    {
        try
        {
            XMLNode artifact = XMLNode.getArtifactAt(fDocument, fDocumentOffset);
            String type = artifact.getType();
            if (type == ITypeConstants.TEXT || type == ITypeConstants.COMMENT
                    || type == ITypeConstants.PI || type == ITypeConstants.DECL)
                throw new IllegalArgumentException(
                        "no applicable data  found at the cursor postion");

            if (type == ITypeConstants.ENDTAG)
                artifact = artifact.getCorrespondingNode();

            if (artifact == null)
                throw new IllegalArgumentException(
                        "no applicable data  found at the cursor postion");

            String name = artifact.getName();

            if (name == null)
                throw new IllegalArgumentException(
                        "no applicable data  found at the cursor postion (missing element name)");

            name = name.toLowerCase();

            Handler handler = (Handler) fHandlers.get(name);

            if (handler != null)
                handler.handle(artifact);

            else
                throw new IllegalArgumentException(
                        "This file is not well formed or can not be seen by the Tapestry builder");

        }
        catch (IllegalArgumentException e)
        {
            canNotContinue(e.getMessage());
        }
        catch (CoreException e)
        {
            UIPlugin.log(e);
            ErrorDialog.openError(
                    UIPlugin.getDefault().getActiveWorkbenchShell(),
                    "Operation Aborted",
                    null,
                    e.getStatus());
        }
    }

    private void handleLibraryLookup(XMLNode artifact) throws IllegalArgumentException,
            CoreException
    {

        XMLNode attribute = getAttribute(artifact, fDocumentOffset, "specification-path");
        if (attribute == null)
            throw new IllegalArgumentException(
                    "could not location the 'specification-path' attribute");

        String path = attribute.getAttributeValue();
        if (path == null)
            throw new IllegalArgumentException(
                    "could not location the 'specification-path' attribute value");

        //here we are doing a classpath lookup,
        //need to get access to the ClasspathRoot
        IStorage storage = fEditor.getStorage();
        if (storage != null)
        {
            ITapestryProject project = (ITapestryProject) storage
                    .getAdapter(ITapestryProject.class);
            if (project == null)
                return;

            ClasspathRootLocation root = project.getClasspathRoot();
            if (root == null)
                return;

            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) root
                    .getRelativeResource(path);
            IStorage s = location.getStorage();
            if (s != null)
                foundResult(s, null, null);

        }
    }

    /**
     * @param artifact
     */
    private void handlePrivateAsset(XMLNode artifact) throws CoreException
    {

        XMLNode attribute = getAttribute(artifact, fDocumentOffset, "resource-path");
        if (attribute == null)
            throw new IllegalArgumentException("could not location the 'resource-path' attribute");

        String path = attribute.getAttributeValue();
        if (path == null)
            throw new IllegalArgumentException(
                    "could not location the 'resource-path' attribute value");

        //here we are doing a classpath lookup,
        //need to get access to the ClasspathRoot
        IStorage storage = fEditor.getStorage();
        if (storage != null)
        {
            ITapestryProject project = (ITapestryProject) storage
                    .getAdapter(ITapestryProject.class);
            if (project == null)
                return;

            ClasspathRootLocation root = project.getClasspathRoot();
            if (root == null)
                return;

            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) root
                    .getRelativeResource(path);
            IStorage s = location.getStorage();
            if (s != null)
                foundResult(s, null, null);
        }
    }

    /**
     * @param artifact
     */
    private void handleContextAsset(XMLNode artifact)
    {

        XMLNode attribute = getAttribute(artifact, fDocumentOffset, "path");
        if (attribute == null)
            throw new IllegalArgumentException("could not location the 'path' attribute");

        String path = attribute.getAttributeValue();

        if (path == null)
            throw new IllegalArgumentException("could not location the 'path' attribute value");

        //here we are doing a context lookup,
        //need to get access to the ContextRoot
        IStorage storage = fEditor.getStorage();
        if (storage != null)
        {
            ITapestryProject project = (ITapestryProject) storage
                    .getAdapter(ITapestryProject.class);
            if (project == null)
                return;

            ContextRootLocation contextRoot = project.getWebContextLocation();
            if (contextRoot == null)
                return;

            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) contextRoot
                    .getRelativeResource(path);
            IStorage s = location.getStorage();
            if (s != null)
                foundResult(s, null, null);
        }
    }

    private void handleRelativeLookup(XMLNode artifact, String attrName)
    {
        XMLNode attribute = (XMLNode) artifact.getAttributesMap().get(attrName);
        if (attribute == null)
            return;

        String name = attribute.getName();

        if (name == null)
            return;

        if (!attrName.equals(name.toLowerCase()))
            return;

        String path = attribute.getAttributeValue();
        if (path == null)
            return;

        //here we are doing a relative lookup
        //need to get the location object for the Spec we are editing
        //That means it can have no error markers (parsed without error in the last
        // build)
        BaseSpecLocatable spec = (BaseSpecLocatable) fEditor.getSpecification();
        if (spec != null)
        {
            IResourceWorkspaceLocation rootLocation = (IResourceWorkspaceLocation) spec
                    .getSpecificationLocation();
            if (rootLocation == null)
                return;

            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) rootLocation
                    .getRelativeResource(path);
            IStorage s = location.getStorage();
            if (s != null)
                foundResult(s, null, null);
        }
    }

    /**
     * @param artifact
     */
    private void handleComponentLookup(XMLNode artifact) throws IllegalArgumentException
    {

        SpecTapestryAccess access = new SpecTapestryAccess(fEditor);

        // first try and resolve the component...
        XMLNode attribute = getAttribute(artifact, fDocumentOffset, "type");
        if (attribute == null)
            throw new IllegalArgumentException("could not find the 'type' attribute");

        String typeName = attribute.getAttributeValue();

        if (typeName == null)
            throw new IllegalArgumentException("could not find a valid the 'type' attribute value");

        PluginComponentSpecification spec = (PluginComponentSpecification) access
                .resolveComponentType(typeName);
        if (spec == null)
            throw new IllegalArgumentException("could not resolve '" + typeName + "'");

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec
                .getSpecificationLocation();
        if (location == null || location.getStorage() == null)
            return;

        foundResult(location.getStorage(), null, null);

    }

    protected XMLNode getAttribute(XMLNode artifact, String name)
    {
        return getAttribute(artifact, -1, name);
    }

    protected XMLNode getAttribute(XMLNode artifact, int documentOffset, String name)
    {
        if (artifact == null)
            return null;

        Map attrs = artifact.getAttributesMap();
        if (attrs == null || attrs.isEmpty())
            return null;

        XMLNode attribute = null;
        if (documentOffset >= 0)
            attribute = artifact.getAttributeAt(fDocumentOffset);

        if (attribute == null)
            return (XMLNode) attrs.get(name);

        String attrName = attribute.getName();

        if (attrName == null || !attrName.equalsIgnoreCase(name))
            return (XMLNode) attrs.get(name.toLowerCase());

        return attribute;
    }

    protected void canNotContinue(String message)
    {
        MessageDialog.openError(
                UIPlugin.getDefault().getActiveWorkbenchShell(),
                "Problem Encountered",
                message);
    }

    private void handleComponentBinding(XMLNode parent, XMLNode binding)
            throws IllegalArgumentException
    {

        SpecTapestryAccess access = new SpecTapestryAccess(fEditor);

        XMLNode typeAttribute = getAttribute(parent, fDocumentOffset, "type");
        if (typeAttribute == null)
            throw new IllegalArgumentException(
                    "could not locate the component type in the parent element");

        String resolveType = typeAttribute.getAttributeValue();

        if (resolveType == null)
            throw new IllegalArgumentException(
                    "could not locate the component type in the parent element");

        PluginComponentSpecification spec = (PluginComponentSpecification) access
                .resolveComponentType(resolveType);
        if (spec == null)
            throw new IllegalArgumentException("could not resolve the component type '"
                    + resolveType + "' in the parent element");

        Map bindingAttrs = binding.getAttributesMap();
        XMLNode nameAttribute = (XMLNode) bindingAttrs.get("name");
        if (nameAttribute == null)
            throw new IllegalArgumentException("binding name is missing");

        String parameterName = nameAttribute.getAttributeValue();
        if (parameterName == null)
            throw new IllegalArgumentException("binding name is missing");

        IParameterSpecification parameterSpec = spec.getParameter(parameterName);

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec
                .getSpecificationLocation();
        if (location == null || location.getStorage() == null)
            return;

        foundResult(location.getStorage(), parameterName, parameterSpec);
    }

    private void handleTypeLookup(XMLNode artifact, String attrName)
    {
        handleTypeLookup(artifact, attrName, false);
    }

    private void handleTypeLookup(XMLNode artifact, String attrName, boolean useComponentResolver)
    {
        XMLNode attribute = getAttribute(artifact, fDocumentOffset, attrName);
        if (attribute == null)
            throw new IllegalArgumentException("could not find the '" + attrName + "' attribute");

        String typeName = attribute.getAttributeValue();

        if (typeName == null)
            return;

        IType type = resolveType(typeName);

        if (type == null)
            throw new IllegalArgumentException("could resolve the type '" + typeName + "'");

        if (useComponentResolver)
        {
            ComponentTypeResourceResolvers resolver = new ComponentTypeResourceResolvers();

            if (resolver.canResolve(type))
            {
                IStatus resolveStatus = resolver.doResolve(
                        fEditor.getLocation(),
                        (IComponentSpecification) fEditor.getSpecification());
                if (!resolveStatus.isOK())
                    throw new IllegalArgumentException(resolveStatus.getMessage());
                foundResult(resolver.getStorage(), null, null);
            }
            else
            {
                foundResult(type, null, null);
            }
        }
        else
        {
            foundResult(type, null, null);
        }

    }

    protected void foundResult(Object result, String key, Object moreInfo)
    {
        if (result instanceof IType)
        {
            reveal((IType) result);
        }
        else if (result instanceof IStorage)
        {
            reveal((IStorage) result);
            IEditorPart editor = UIUtils.getEditorFor((IStorage) result);
            if (editor != null && (editor instanceof AbstractTextEditor) || moreInfo != null)
            {
                if (moreInfo instanceof IParameterSpecification && key != null)
                {
                    revealParameter((AbstractTextEditor) editor, key);
                }
            }
        }
    }

    private void revealParameter(AbstractTextEditor editor, String parameterName)
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

                if (!"parameter".equals(name.toLowerCase()))
                    continue;

                XMLNode attribute = getAttribute(artifact, "name");
                if (attribute == null)
                    continue;

                String value = attribute.getAttributeValue();
                if (value != null && value.equals(parameterName))
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

    protected void init()
    {
        fHandlers = new HashMap();
        fHandlers.put("application", new TypeHandler("engine-class"));
        fHandlers.put("bean", new TypeHandler("class"));
        fHandlers.put("component-specification", new TypeHandler("class", true));
        fHandlers.put("page-specification", new TypeHandler("class", true));
        fHandlers.put("extension", new TypeHandler("class"));
        fHandlers.put("service", new TypeHandler("class"));
        fHandlers.put("property-specification", new TypeHandler("type"));
        fHandlers.put("parameter", new Handler(false)
        {
            protected void doHandle(XMLNode artifact) throws IllegalArgumentException,
                    CoreException
            {
                handleTypeLookup(artifact, "type");
            }
        });
        Handler bindingHandler = new Handler(true)
        {
            protected void doHandle(XMLNode artifact) throws IllegalArgumentException,
                    CoreException
            {
                XMLNode parent = artifact.getParent();
                String parentName = parent.getName();
                if (parentName == null)
                    throw new IllegalArgumentException(
                            "could not locate a valid parent  tag  (expected <component>)");
                parentName = parentName.toLowerCase();
                if (!parentName.equals("component"))
                    throw new IllegalArgumentException(
                            "could not locate a valid parent  tag (expected <component>)");
                handleComponentBinding(parent, artifact);
            }
        };
        fHandlers.put("binding", bindingHandler);
        fHandlers.put("static-binding", bindingHandler);
        fHandlers.put("inherited-binding", bindingHandler);
        fHandlers.put("message-binding", bindingHandler);
        fHandlers.put("string-binding", bindingHandler);
        fHandlers.put("field-binding", bindingHandler);
        fHandlers.put("component", new Handler(true)
        {
            protected void doHandle(XMLNode artifact) throws IllegalArgumentException,
                    CoreException
            {
                handleComponentLookup(artifact);
            }
        });
        fHandlers.put("private-asset", new Handler(true)
        {
            protected void doHandle(XMLNode artifact) throws IllegalArgumentException,
                    CoreException
            {
                handlePrivateAsset(artifact);
            }
        });
        fHandlers.put("context-asset", new Handler(true)
        {
            protected void doHandle(XMLNode artifact) throws IllegalArgumentException,
                    CoreException
            {
                handleContextAsset(artifact);
            }
        });
        fHandlers.put("component-alias", new RelativeLookupHandler("specification-path"));
        fHandlers.put("component-type", new RelativeLookupHandler("specification-path"));
        fHandlers.put("page", new RelativeLookupHandler("specification-path"));
        fHandlers.put("library", new Handler(true)
        {
            protected void doHandle(XMLNode artifact) throws IllegalArgumentException,
                    CoreException
            {
                handleLibraryLookup(artifact);
            }
        });
    }

    abstract class Handler
    {
        boolean needsNamespace;

        Handler(boolean needsNamespace)
        {
            this.needsNamespace = needsNamespace;
        }

        public final void handle(XMLNode node) throws IllegalArgumentException, CoreException
        {
            if (needsNamespace && fEditor.getNamespace() == null)
                throw new IllegalArgumentException(
                        "This file is not well formed or can not be seen by the Tapestry builder");

            doHandle(node);
        }

        protected abstract void doHandle(XMLNode artifact) throws IllegalArgumentException,
                CoreException;
    }

    abstract class AttributeHandler extends Handler
    {
        protected String attrName;

        public AttributeHandler(boolean needsNamespace, String attrName)
        {
            super(needsNamespace);
            this.attrName = attrName;
        }
    }

    class TypeHandler extends AttributeHandler
    {

        private boolean useComponentResolver;

        public TypeHandler(String attrName)
        {
            this(attrName, false);
        }

        public TypeHandler(String attrName, boolean useComponentResolver)
        {
            super(false, attrName);
            this.useComponentResolver = useComponentResolver;
        }

        protected void doHandle(XMLNode artifact) throws IllegalArgumentException, CoreException
        {
            handleTypeLookup(artifact, attrName, useComponentResolver);
        }
    }

    class RelativeLookupHandler extends AttributeHandler
    {
        public RelativeLookupHandler(String attrName)
        {
            super(true, attrName);
        }

        protected void doHandle(XMLNode artifact) throws IllegalArgumentException, CoreException
        {
            handleRelativeLookup(artifact, attrName);
        }
    }

}