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

package com.iw.plugins.spindle.core.scanning;

import java.util.Map;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.parser.xml.pull.PullParserNode;

/**
 *  Static Helper methods for accessing data in DOM nodes
 * 
 *  Includes transparent support for some PULL parser ideosyncracies. i.e. see getAttribute() src if you care.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class NodeAccess
{

    public static String getAttribute(Node node, String attributeName)
    {
        String result = null;
        if (node instanceof PullParserNode)
        {
            PullParserNode ppnode = (PullParserNode) node;
            Map attrs = ppnode.getKludgeAttributes();
            if (attrs != null)
                result = (String) attrs.get(attributeName);

        } else
        {
            NamedNodeMap map = node.getAttributes();

            if (map != null)
            {
                Node attributeNode = map.getNamedItem(attributeName);

                if (attributeNode != null)
                    result = attributeNode.getNodeValue();
            }
        }
        return result;
    }

    public static boolean getBooleanAttribute(Node node, String attributeName)
    {
        String attributeValue = getAttribute(node, attributeName);

        return attributeValue.equals("yes");
    }

    public static String getValue(Node node)
    {
        StringBuffer buffer = new StringBuffer();
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child.getNodeType() == Node.TEXT_NODE)
                buffer.append(child.getNodeValue());
        }

        String result = buffer.toString().trim();
        if (result == null || "".equals(result))
            return null;

        return result;
    }

    public static boolean isElement(Node node, String elementName)
    {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            return false;

        return node.getNodeName().equals(elementName);

    }

    public static ISourceLocationInfo getSourceLocationInfo(Node node)
    {
        ISourceLocationInfo result = null;
        if (node instanceof PullParserNode)
        {
            PullParserNode ppnode = (PullParserNode) node;
            result = ppnode.getSourceLocationInfo();

        } else
        {
            DocumentImpl document = (DocumentImpl) node.getOwnerDocument();
            result = (ISourceLocationInfo) document.getUserData(node, TapestryCore.PLUGIN_ID);
        }
        return result;
    }

}