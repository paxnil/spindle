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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JumpToJavaAction extends BaseJumpAction
{

    public JumpToJavaAction()
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
     */
    protected void doRun()
    {
        IType javaType = findType();
        if (javaType == null)
            return;
        reveal(javaType);
    }

    protected IType findType()
    {
        IType javaType;
        if (fEditor instanceof TemplateEditor)
        {
            javaType = getTypeFromTemplate();
        } else
        {
            javaType = getTypeFromSpec();
        }
        return javaType;
    }

    private IType getTypeFromTemplate()
    {
        IComponentSpecification componentSpec = (IComponentSpecification) fEditor.getSpecification();
        if (componentSpec != null)
        {
            String typeName = componentSpec.getComponentClassName();
            return resolveType(typeName);

        }
        return null;
    }

    private IType getTypeFromSpec()
    {
        try
        {
            attachPartitioner();
            XMLNode root = XMLNode.createTree(getDocument(), -1);
            List children = root.getChildren();
            for (Iterator iter = children.iterator(); iter.hasNext();)
            {
                XMLNode child = (XMLNode) iter.next();
                String type = child.getType();
                if (type == XMLDocumentPartitioner.TAG || type == XMLDocumentPartitioner.EMPTYTAG)
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
                            if (attrValue != null)
                                return resolveType(attrValue);
                        }

                    } else if (name.equals("application"))
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
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
        } finally
        {
            detachPartitioner();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.actions.BaseEditorAction#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public  void editorContextMenuAboutToShow(IMenuManager menu)
    {
        IType javaType = findType();
        if (javaType != null)
            menu.add(new MenuOpenTypeAction(javaType));
    }

    class MenuOpenTypeAction extends Action
    {
        IType type;

        public MenuOpenTypeAction(IType type)
        {
            Assert.isNotNull(type);
            this.type = type;
            setImageDescriptor(getImageDescriptorFor(BaseJumpAction.LABEL_PROVIDER.getImage(type)));
            setText(type.getFullyQualifiedName());
        }

        public void run()
        {
            reveal(type);
        }
    }

}