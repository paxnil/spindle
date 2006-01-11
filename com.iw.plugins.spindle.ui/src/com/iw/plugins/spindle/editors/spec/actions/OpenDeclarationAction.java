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

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
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
import com.iw.plugins.spindle.core.util.SpindleMultiStatus;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.actions.BaseAction;
import com.iw.plugins.spindle.editors.spec.SpecEditor;
import com.iw.plugins.spindle.editors.spec.assist.SpecTapestryAccess;

/**
 * Open an interesting thing, if possible.
 * 
 * @author glongman@gmail.com
 */
public class OpenDeclarationAction extends BaseSpecAction
{
    private Handler fHandler;

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

    protected ChooseLocationPopup getChooseLocationPopup(Object[] locations)
    {
        return null;
    }

    protected void postReveal(Object revealed, IEditorPart editor)
    {
        if (fHandler == null)
            return;
        fHandler.postReveal(revealed, editor);
    }

    protected IStatus doGetStatus(SpindleStatus status)
    {
        fHandler = null;
        
        status = (SpindleStatus) super.doGetStatus(status);
        if (status == null || !status.isOK())
            return status;

        try
        {
            XMLNode artifact = XMLNode.getArtifactAt(fDocument, getDocumentOffset());
            
            String type = artifact.getType();
            if (type == ITypeConstants.TEXT || type == ITypeConstants.COMMENT
                    || type == ITypeConstants.PI || type == ITypeConstants.DECL)
            {
                status.setError("no applicable data  found at the cursor postion");
                return status;
            }

            if (type == ITypeConstants.ENDTAG)
                artifact = artifact.getCorrespondingNode();

            if (artifact == null)
            {
                status.setError("no applicable data found at the cursor postion");
                return status;
            }

            String name = artifact.getName();

            if (name == null)
            {
                status
                        .setError("no applicable data found at the cursor postion (missing element name)");
                return status;
            }

            name = name.toLowerCase();

            fHandler = (Handler) fHandlers.get(name);

            if (fHandler == null)
            {
                status
                        .setError("This file is not well formed, not valid, or can not be seen by the Tapestry builder");
                return status;
            }

            status = fHandler.handle(artifact, status);
            Object interesting = status.isOK() ? fHandler.getInterestingObject() : null;
            fInterestingObjects = interesting == null ? new Object[] {} : new Object[]
            { interesting };
            return status;

        }
        catch (CoreException e)
        {
            SpindleMultiStatus result = new SpindleMultiStatus(IStatus.ERROR, "Operation Aborted");
            result.addStatus(e.getStatus());
            status = result;
        }
        return status;
    }

    /**
     * @param artifact
     */

    /**
     * @param artifact
     */

    /**
     * @param artifact
     * @return TODO
     */

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
            attribute = artifact.getAttributeAt(documentOffset);

        if (attribute == null)
            return (XMLNode) attrs.get(name);

        String attrName = attribute.getName();

        if (attrName == null || !attrName.equalsIgnoreCase(name))
            return (XMLNode) attrs.get(name.toLowerCase());

        return attribute;
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
        fHandlers.put("parameter", new TypeHandler()
        {
            protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                    throws CoreException
            {
                if (fDTD.getPublicId() == SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID)
                    return handleTypeLookup(artifact, "java-type", false, status);
                else
                    return handleTypeLookup(artifact, "type", false, status);
            }
        });
        Handler bindingHandler = new BindingHandler();
        fHandlers.put("binding", bindingHandler);
        fHandlers.put("static-binding", bindingHandler);
        fHandlers.put("inherited-binding", bindingHandler);
        fHandlers.put("message-binding", bindingHandler);
        fHandlers.put("string-binding", bindingHandler);
        fHandlers.put("field-binding", bindingHandler);
        fHandlers.put("component", new Handler(true)
        {
            protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                    throws CoreException
            {

                return handleComponentLookup(artifact, status);
            }

            private SpindleStatus handleComponentLookup(XMLNode artifact, SpindleStatus status)
                    throws IllegalArgumentException
            {

                SpecTapestryAccess access = null;

                try
                {
                    access = new SpecTapestryAccess(getSpindleEditor());
                }
                catch (IllegalArgumentException e)
                {
                    UIPlugin.log(e);
                }

                if (access == null)
                    return status;

                // first try and resolve the component...
                XMLNode attribute = getAttribute(artifact, getDocumentOffset(), "type");
                if (attribute == null)
                {
                    status.setError("could not find the 'type' attribute");
                    return status;
                }

                String typeName = attribute.getAttributeValue();

                if (typeName == null)
                {
                    status.setError("could not find a valid the 'type' attribute value");
                    return status;
                }

                PluginComponentSpecification spec = (PluginComponentSpecification) access
                        .resolveComponentType(typeName);
                if (spec == null)
                {
                    status.setError("could not resolve '" + typeName + "'");
                    return status;
                }

                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec
                        .getSpecificationLocation();
                if (location == null)
                    return status;

                interestingObject = location.getStorage();

                return status;

                // foundResult(location.getStorage(), null, null);

            }
        });
        fHandlers.put("private-asset", new Handler(true)
        {

            protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                    throws CoreException
            {
                return handlePrivateAsset(artifact);
            }

            private SpindleStatus handlePrivateAsset(XMLNode artifact) throws CoreException
            {
                SpindleStatus status = new SpindleStatus();

                XMLNode attribute = getAttribute(artifact, getDocumentOffset(), "resource-path");
                if (attribute == null)
                {
                    status.setError("could not location the 'resource-path' attribute");
                    return status;
                }

                String path = attribute.getAttributeValue();
                if (path == null)
                {
                    status.setError("could not locate the 'resource-path' attribute value");
                    return status;
                }

                // here we are doing a classpath lookup,
                // need to get access to the ClasspathRoot
                IStorage storage = getEditorStorage();
                if (storage == null)
                    return status;

                ITapestryProject project = (ITapestryProject) storage
                        .getAdapter(ITapestryProject.class);
                if (project == null)
                    return status;

                ClasspathRootLocation root = project.getClasspathRoot();
                if (root == null)
                    return status;

                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) root
                        .getRelativeLocation(path);
                IStorage s = location.getStorage();

                if (s == null)
                    return status;

                interestingObject = s;

                return status;

            }
        });
        fHandlers.put("context-asset", new Handler(true)
        {
            protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                    throws CoreException
            {
                return handleContextAsset(artifact, status);
            }

            private SpindleStatus handleContextAsset(XMLNode artifact, SpindleStatus status)
            {

                XMLNode attribute = getAttribute(artifact, getDocumentOffset(), "path");
                if (attribute == null)
                {
                    status.setError("could not location the 'path' attribute");
                    return status;
                }

                String path = attribute.getAttributeValue();

                if (path == null)
                {
                    status.setError("could not location the 'path' attribute");
                    return status;
                }

                // here we are doing a context lookup,
                // need to get access to the ContextRoot
                IStorage storage = getEditorStorage();
                if (storage == null)
                    return status;

                ITapestryProject project = (ITapestryProject) storage
                        .getAdapter(ITapestryProject.class);
                if (project == null)
                    return status;

                ContextRootLocation contextRoot = project.getWebContextLocation();
                if (contextRoot == null)
                    return status;

                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) contextRoot
                        .getRelativeLocation(path);
                IStorage s = location.getStorage();
                if (s == null)
                    return status;

                interestingObject = s;

                return status;
            }
        });
        fHandlers.put("component-alias", new RelativeLookupHandler("specification-path"));
        fHandlers.put("component-type", new RelativeLookupHandler("specification-path"));
        fHandlers.put("page", new RelativeLookupHandler("specification-path"));
        fHandlers.put("library", new Handler(true)
        {
            protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                    throws IllegalArgumentException, CoreException
            {
                return handleLibraryLookup(artifact, status);
            }

            private SpindleStatus handleLibraryLookup(XMLNode artifact, SpindleStatus status)
                    throws CoreException
            {

                XMLNode attribute = getAttribute(artifact, getDocumentOffset(), "specification-path");
                if (attribute == null)
                {
                    status.setError("could not location the 'specification-path' attribute");
                    return status;
                }

                String path = attribute.getAttributeValue();
                if (path == null)
                {
                    status.setError("could not location the 'specification-path' attribute value");
                    return status;

                }

                // here we are doing a classpath lookup,
                // need to get access to the ClasspathRoot
                IStorage storage = getEditorStorage();
                if (storage == null)
                    return status;

                ITapestryProject project = (ITapestryProject) storage
                        .getAdapter(ITapestryProject.class);
                if (project == null)
                    return status;

                ClasspathRootLocation root = project.getClasspathRoot();
                if (root == null)
                    return status;

                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) root
                        .getRelativeLocation(path);
                IStorage s = location.getStorage();
                if (s == null)
                    return status;

                interestingObject = s;
                // foundResult(s, null, null);

                return status;
            }
        });
    }

    class BindingHandler extends Handler
    {
        private IParameterSpecification parameterSpec;

        private String parameterName;

        BindingHandler()
        {
            super(true);
        }

        protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status) throws CoreException
        {
            parameterName = null;
            parameterSpec = null;

            XMLNode parent = artifact.getParent();
            String parentName = parent.getName();
            if (parentName == null)
            {
                status.setError("could not locate a valid parent tag  (expected <component>)");
                return status;
            }
            parentName = parentName.toLowerCase();
            if (!parentName.equals("component"))
            {
                status.setError("could not locate a valid parent tag  (expected <component>)");
                return status;
            }

            return handleComponentBinding(parent, artifact, status);
        }

        public void postReveal(Object object, IEditorPart editor)
        {
            if (object != null && object == interestingObject && editor != null
                    && (editor instanceof AbstractTextEditor))
            {
                if (parameterSpec != null && parameterName != null)
                    revealParameter((AbstractTextEditor) editor, parameterName);
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

        private SpindleStatus handleComponentBinding(XMLNode parent, XMLNode binding,
                SpindleStatus status) throws IllegalArgumentException
        {

            SpecTapestryAccess access = null;

            try
            {
                access = new SpecTapestryAccess(getSpindleEditor());
            }
            catch (IllegalArgumentException e)
            {
                UIPlugin.log(e);
            }

            if (access == null)
                return status;

            XMLNode typeAttribute = getAttribute(parent, getDocumentOffset(), "type");
            if (typeAttribute == null)
            {
                status.setError("could not locate the component type in the parent element");
                return status;
            }

            String resolveType = typeAttribute.getAttributeValue();

            if (resolveType == null)
            {
                status.setError("could not locate the component type in the parent element");
                return status;
            }

            PluginComponentSpecification spec = (PluginComponentSpecification) access
                    .resolveComponentType(resolveType);
            if (spec == null)
            {
                status.setError("could not resolve the component type '" + resolveType
                        + "' in the parent element");
                return status;
            }

            Map bindingAttrs = binding.getAttributesMap();
            XMLNode nameAttribute = (XMLNode) bindingAttrs.get("name");
            if (nameAttribute == null)
            {
                status.setError("binding name is missing");
                return status;
            }

            parameterName = nameAttribute.getAttributeValue();
            if (parameterName == null)
            {
                status.setError("binding name is missing");
                return status;
            }

            parameterSpec = spec.getParameter(parameterName);
            if (parameterSpec == null)
                return status;

            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec
                    .getSpecificationLocation();

            if (location == null)
                return status;

            interestingObject = location.getStorage();

            return status;

        }
    }

    abstract class Handler
    {
        Object interestingObject;

        boolean needsNamespace;

        Handler(boolean needsNamespace)
        {
            this.needsNamespace = needsNamespace;
        }

        public final SpindleStatus handle(XMLNode node, SpindleStatus status) throws CoreException
        {
            interestingObject = null;

            if (needsNamespace && getSpindleEditor().getNamespace(false) == null)
            {
                status
                        .setError("This file is not well formed, the project has not been built, or can not be seen by the Tapestry builder");
                return status;
            }

            return doHandle(node, status);
        }

        protected abstract SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                throws CoreException;

        public void postReveal(Object object, IEditorPart editor)
        {
            // default impl does nothing
        }

        public Object getInterestingObject()
        {
            return interestingObject;
        }

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

        public TypeHandler()
        {
            this(null, false);
        }

        public TypeHandler(String attrName)
        {
            this(attrName, false);
        }

        public TypeHandler(String attrName, boolean useComponentResolver)
        {
            super(false, attrName);
            this.useComponentResolver = useComponentResolver;
        }

        protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status)
                throws IllegalArgumentException, CoreException
        {
            return handleTypeLookup(artifact, attrName, useComponentResolver, status);
        }

        protected SpindleStatus handleTypeLookup(XMLNode artifact, String attrName,
                boolean useComponentResolver, SpindleStatus status)
        {
            XMLNode attribute = getAttribute(artifact, getDocumentOffset(), attrName);
            if (attribute == null)
            {
                status.setError("could not find the '" + attrName + "' attribute");
                return status;
            }

            String typeName = attribute.getAttributeValue();

            if (typeName == null)
            {
                status.setError("could not find the '" + attrName + "' attribute's value.");
                return status;
            }
            
            if (BaseAction.PRIMITIVE_TYPES.contains(typeName.trim()))
                return status;

            IType type = resolveType(typeName);

            if (type == null)
            {
                status.setError("could not resolve the type '" + typeName + "'.");
                return status;
            }

            if (useComponentResolver)
            {
                ComponentTypeResourceResolvers resolver = new ComponentTypeResourceResolvers();

                if (resolver.canResolve(type))
                {
                    Editor spindleEditor = (Editor) getSpindleEditor();
                    IStatus resolveStatus = resolver.doResolve(
                            spindleEditor.getLocation(),
                            (IComponentSpecification) spindleEditor.getSpecification());
                    if (!resolveStatus.isOK())
                    {
                        status.setError(resolveStatus.getMessage());
                        return status;
                    }
                    interestingObject = resolver.getStorage();
                }
                else
                {
                    interestingObject = type;
                }
            }
            else
            {
                interestingObject = type;
            }
            return status;
        }
    }

    class RelativeLookupHandler extends AttributeHandler
    {
        public RelativeLookupHandler(String attrName)
        {
            super(true, attrName);
        }

        protected SpindleStatus doHandle(XMLNode artifact, SpindleStatus status) throws CoreException
        {
            return handleRelativeLookup(artifact, attrName, status);
        }

        private SpindleStatus handleRelativeLookup(XMLNode artifact, String attrName,
                SpindleStatus status)
        {
            XMLNode attribute = (XMLNode) artifact.getAttributesMap().get(attrName);
            if (attribute == null)
                return status;

            String name = attribute.getName();

            if (name == null)
                return status;

            if (!attrName.equals(name.toLowerCase()))
                return status;

            String path = attribute.getAttributeValue();
            if (path == null)
                return status;

            // here we are doing a relative lookup
            // need to get the location object for the Spec we are editing
            // That means it can have no error markers (parsed without error in the
            // last
            // build)
            SpecEditor editorPart = (SpecEditor) getSpindleEditor();
            BaseSpecLocatable spec = (BaseSpecLocatable) editorPart.getSpecification();

            if (spec == null)
                return status;

            IResourceWorkspaceLocation rootLocation = (IResourceWorkspaceLocation) spec
                    .getSpecificationLocation();

            if (rootLocation == null)
                return status;

            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) rootLocation
                    .getRelativeLocation(path);
            IStorage s = location.getStorage();
            if (s == null)
                return status;
            interestingObject = s;

            return status;
        }
    }

}