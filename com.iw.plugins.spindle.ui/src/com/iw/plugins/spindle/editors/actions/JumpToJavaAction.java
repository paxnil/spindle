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

package com.iw.plugins.spindle.editors.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IStorageEditorInput;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.extensions.ComponentTypeResourceResolvers;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.spec.SpecEditor;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@gmail.com
 */
public class JumpToJavaAction extends BaseJumpAction implements IElementChangedListener
{

    IType fType;

    public JumpToJavaAction()
    {
        super();
        JavaCore.addElementChangedListener(this);
    }

    public JumpToJavaAction(IType type)
    {
        this();
        fType = type;
    }

    public void dispose()
    {
        super.dispose();
        JavaCore.removeElementChangedListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
     */
    protected void doRun()
    {
        Object typeObject = findType();
        if (typeObject == null)
            return;
        if (typeObject instanceof IType)
            reveal((IType) typeObject);
        else if (typeObject instanceof IStorage)
            reveal((IStorage) typeObject);
    }

    // might be an IType, an IStorage, or null.
    protected Object findType()
    {
        Object result;
        if (fEditor instanceof TemplateEditor)
        {
            result = getTypeFromTemplate();
        }
        else
        {
            result = getTypeFromSpec();
        }
        return result;
    }

    private boolean typeResolved(String typeName)
    {
        if (fType == null || typeName == null)
            return false;
        return fType.getFullyQualifiedName().equals(typeName.trim());
    }

    private Object getTypeFromTemplate()
    {
        Editor editorPart = (Editor) getSpindleEditor();
        IComponentSpecification componentSpec = (IComponentSpecification) editorPart
                .getSpecification();
        if (componentSpec != null)
        {
            String typeName = componentSpec.getComponentClassName();
            if (BaseAction.PRIMITIVE_TYPES.contains(typeName.trim()))
                return null;
            IType javaType = findType(typeName);
            if (javaType != null)
            {
                ComponentTypeResourceResolvers resolver = new ComponentTypeResourceResolvers();
                if (!resolver.canResolve(javaType))
                    return javaType;

                if (!resolver.doResolve(
                        (IResourceWorkspaceLocation) componentSpec.getSpecificationLocation(),
                        componentSpec).isOK())
                    return null;

                return resolver.getStorage();
            }
        }
        return null;
    }

    private IType findType(String typeName)
    {
        IType javaType = null;
        if (typeResolved(typeName))
        {
            javaType = fType;
        }
        else
        {
            javaType = resolveType(typeName);
            fType = javaType;
        }
        return javaType;
    }

    private Object getTypeFromSpec()
    {

        XMLNode root = getRootNode();
        if (root == null)
            return null;

        List children = root.getChildren();
        for (Iterator iter = children.iterator(); iter.hasNext();)
        {
            XMLNode child = (XMLNode) iter.next();
            String type = child.getType();
            if (type == ITypeConstants.TAG || type == ITypeConstants.EMPTYTAG)
            {
                String name = child.getName();
                if (name == null)
                    return null;
                name = name.toLowerCase();
                Map attrMap;
                if (name.equals("component-specification") || name.equals("page-specification"))
                {
                    attrMap = child.getAttributesMap();
                    XMLNode attribute = (XMLNode) attrMap.get("class");
                    if (attribute != null)
                    {
                        String attrValue = attribute.getAttributeValue();
                        if (attrValue == null)
                            return null;

                        if (BaseAction.PRIMITIVE_TYPES.contains(attrValue.trim()))
                            return null;

                        IType javaType = findType(attrValue);
                        if (javaType != null)
                        {
                            try
                            {
                                ComponentTypeResourceResolvers resolver = new ComponentTypeResourceResolvers();
                                if (!resolver.canResolve(javaType))
                                    return javaType;

                                IStorage storage = ((IStorageEditorInput) fEditor.getEditorInput())
                                        .getStorage(); // potentially expensive!!

                                ITapestryProject tproject = (ITapestryProject) javaType
                                        .getJavaProject().getAdapter(ITapestryProject.class);

                                if (tproject == null)
                                    return javaType;

                                IResourceWorkspaceLocation specLocation;
                                if (storage instanceof IFile)
                                {
                                    specLocation = tproject.getWebContextLocation()
                                            .getRelativeLocation((IFile) storage);
                                }
                                else
                                {
                                    specLocation = tproject.getClasspathRoot().getRelativeLocation(
                                            storage);
                                }

                                SpecEditor editorPart = (SpecEditor) getSpindleEditor();
                                IComponentSpecification componentSpec = (IComponentSpecification) editorPart
                                        .getSpecification();

                                if (!resolver.doResolve(specLocation, componentSpec).isOK())
                                    return javaType;

                                return resolver.getStorage();
                            }
                            catch (CoreException e)
                            {
                                UIPlugin.log(e);
                            }
                        }
                    }
                }
                else if (name.equals("application"))
                {
                    attrMap = child.getAttributesMap();
                    XMLNode attribute = (XMLNode) attrMap.get("engine-class");
                    if (attribute != null)
                    {
                        String attrValue = attribute.getAttributeValue();
                        if (attrValue != null)
                            return resolveType(attrValue);
                    }
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.editors.actions.BaseEditorAction#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void editorContextMenuAboutToShow(IMenuManager menu)
    {
        Object typeObject = findType();
        if (typeObject != null)
        {
            if (typeObject instanceof IType)
                menu.add(new MenuOpenTypeAction((IType) typeObject));
            else if (typeObject instanceof IStorage)
                menu.add(new MenuOpenTypeAction((IStorage) typeObject));
        }
    }

    class MenuOpenTypeAction extends Action
    {
        Object typeObject;

        public MenuOpenTypeAction(IType type)
        {
            Assert.isNotNull(type);
            this.typeObject = type;
            setImageDescriptor(getImageDescriptorFor(BaseJumpAction.LABEL_PROVIDER.getImage(type)));
            setText(type.getFullyQualifiedName());
        }

        public MenuOpenTypeAction(IStorage storage)
        {
            Assert.isNotNull(storage);
            this.typeObject = storage;
            setImageDescriptor(getImageDescriptorFor(BaseJumpAction.LABEL_PROVIDER
                    .getImage(storage)));
            setText(storage.getFullPath().toString());
        }

        public void run()
        {
            if (typeObject instanceof IType)
                reveal((IType) typeObject);
            else if (typeObject instanceof IStorage)
                reveal((IStorage) typeObject);
        }
    }

    public void elementChanged(ElementChangedEvent event)
    {
        if (fType == null || event.getType() != ElementChangedEvent.POST_CHANGE)
            return;
        
        if (!fType.exists())
            fType = null;
    
    }

}