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

package com.iw.plugins.spindle.editors.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;


public class XMLNodeLabelProvider extends LabelProvider 
{
    public String getText(Object obj)
    {
        if (obj instanceof XMLNode)
        {
            XMLNode artifact = (XMLNode) obj;
            String type = artifact.getType();

            if (type == ITypeConstants.TAG
                || type == ITypeConstants.EMPTYTAG
                || type == ITypeConstants.DECL)
            {
                String name = artifact.getName();
                return name == null ? "" : name;
            }

            if (type == ITypeConstants.ATTR)
            {
                String name = artifact.getName();
                String attrvalue = artifact.getAttributeValue();
                return (name == null ? "" : name)
                    + " = "
                    + StringUtils.abbreviate(attrvalue == null ? "" : attrvalue, 50);
            }

            if (type == ITypeConstants.COMMENT)
                return "COMMENT" + StringUtils.abbreviate(artifact.getContent().trim(), 50);

            if (type == ITypeConstants.TEXT)
                return StringUtils.abbreviate(artifact.getContent().trim(), 50);

            if (type == ITypeConstants.PI)
                return StringUtils.abbreviate(artifact.getContent().trim(), 50);
        }

        return obj.toString();
    }
    public Image getImage(Object obj)
    {
        if (obj instanceof XMLNode)
        {
            XMLNode artifact = (XMLNode) obj;
            String type = artifact.getType();
            if (type == ITypeConstants.DECL)
            {

                if (artifact.getParent().getType().equals("/"))
                    return Images.getSharedImage("decl16.gif");

                return Images.getSharedImage("cdata16.gif");

            }

            if (type == ITypeConstants.TAG)
                return Images.getSharedImage("tag16.gif");

            if (type == ITypeConstants.EMPTYTAG)
                return Images.getSharedImage("empty16.gif");

            if (type == ITypeConstants.ATTR)
                return Images.getSharedImage("bullet.gif");

            if (type == ITypeConstants.COMMENT)
                return Images.getSharedImage("comment16.gif");

            if (type == ITypeConstants.TEXT)
                return Images.getSharedImage("text16.gif");

            if (type == ITypeConstants.PI)
                return Images.getSharedImage("pi16.gif");
        }
        return null;
    }

}