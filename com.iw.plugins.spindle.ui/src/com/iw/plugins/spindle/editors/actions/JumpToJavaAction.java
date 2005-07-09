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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IStorageEditorInput;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.extensions.eclipse.EclipseComponentTypeResourceResolvers;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.eclipse.ClasspathResource;
import com.iw.plugins.spindle.core.resources.eclipse.ClasspathRoot;
import com.iw.plugins.spindle.core.resources.eclipse.ContextResource;
import com.iw.plugins.spindle.core.resources.eclipse.ContextRoot;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@gmail.com
 */
public class JumpToJavaAction extends BaseJumpAction
{

    public JumpToJavaAction()
    {
        super();
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

    private Object getTypeFromTemplate()
    {
        IComponentSpecification componentSpec = (IComponentSpecification) fEditor
                .getSpecification();
        if (componentSpec != null)
        {
            String typeName = componentSpec.getComponentClassName();
            IType type = resolveType(typeName);
            if (type != null)
            {
                EclipseComponentTypeResourceResolvers resolver = new EclipseComponentTypeResourceResolvers();
                if (!resolver.canResolve(type))
                    return type;

                if (!resolver.doResolve(
                        (ICoreResource) componentSpec.getSpecificationLocation(),
                        componentSpec).isOK())
                    return null;

                return resolver.getStorage();
            }
        }
        return null;
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

                        IType javaType = resolveType(attrValue);
                        if (javaType != null)
                        {
                            try
                            {
                                IStorage storage = ((IStorageEditorInput) fEditor.getEditorInput())
                                        .getStorage();

                                ITapestryProject tproject = (ITapestryProject) storage
                                        .getAdapter(ITapestryProject.class);

                                ICoreResource specLocation;
                                if (storage instanceof IFile)
                                {
                                    specLocation = new ContextResource(((ContextRoot)tproject.getWebContextLocation()),(IResource) storage);
                                }
                                else
                                {
                                    specLocation = new ClasspathResource(((ClasspathRoot)tproject.getClasspathRoot()), storage);
                                }                                                           

                                IComponentSpecification componentSpec = (IComponentSpecification) fEditor
                                        .getSpecification();

                                EclipseComponentTypeResourceResolvers resolver = new EclipseComponentTypeResourceResolvers();
                                if (!resolver.canResolve(javaType))
                                    return javaType;

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

}