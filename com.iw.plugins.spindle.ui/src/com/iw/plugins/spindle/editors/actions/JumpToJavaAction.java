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
import org.eclipse.jface.text.BadLocationException;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.iw.plugins.spindle.editors.util.DocumentArtifactPartitioner;

/**
 * Jump from spec/template editors to associated java files
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JumpToJavaAction extends BaseJumpAction
{
    /**
     * 
     */
    public JumpToJavaAction()
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.actions.BaseJumpAction#doRun()
     */
    protected void doRun()
    {
        if (fEditor instanceof TemplateEditor)
        {
            IComponentSpecification componentSpec = (IComponentSpecification) fEditor.getSpecification();
            if (componentSpec != null)
            {
                String typeName = componentSpec.getComponentClassName();
                IType type = resolveType(typeName);
                if (type == null)
                    return;
                reveal(type);
            }
        } else
        {
            attachPartitioner();
            try
            {
                DocumentArtifact root = DocumentArtifact.createTree(getDocument(), -1);
                List children = root.getChildren();
                for (Iterator iter = children.iterator(); iter.hasNext();)
                {
                    DocumentArtifact child = (DocumentArtifact) iter.next();
                    String type = child.getType();
                    if (type == DocumentArtifactPartitioner.TAG || type == DocumentArtifactPartitioner.EMPTYTAG)
                    {
                        String name = child.getName();
                        if (name == null)
                            return;
                        name = name.toLowerCase();
                        Map attrMap;
                        if (name.equals("component-specification") || name.equals("page-specification"))
                        {
                            attrMap = child.getAttributesMap();
                            DocumentArtifact attribute = (DocumentArtifact) attrMap.get("class");
                            if (attribute == null)
                                return;
                            String attrValue = attribute.getAttributeValue();
                            if (attrValue == null)
                                return;

                            IType resolvedType = resolveType(attrValue);

                            if (resolvedType == null)
                                return;

                            reveal(resolvedType);

                        } else if (name.equals("application"))
                        {
                            attrMap = child.getAttributesMap();
                            DocumentArtifact attribute = (DocumentArtifact) attrMap.get("engine-class");
                            if (attribute == null)
                                return;
                            String attrValue = attribute.getAttributeValue();
                            if (attrValue == null)
                                return;

                            IType resolvedType = resolveType(attrValue);

                            if (resolvedType == null)
                                return;

                            reveal(resolvedType);
                        }
                        return;
                    }
                }
            } catch (BadLocationException e)
            {
                UIPlugin.log(e);
            }
        }
    }

}
