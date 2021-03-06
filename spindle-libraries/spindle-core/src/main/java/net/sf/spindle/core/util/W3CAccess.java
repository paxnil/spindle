package net.sf.spindle.core.util;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.spindle.core.TapestryCore;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Static Helper methods for accessing data in DOM nodes Includes transparent support for some PULL
 * parser ideosyncracies. i.e. see getAttribute() src if you care.
 * 
 * @author glongman@gmail.com
 */
public class W3CAccess
{

    public static Set<String> getAttributeNames(Node node)
    {
        Assert.isNotNull(node);
        if (!node.hasAttributes())
            return Collections.emptySet();

        HashSet<String> result = new HashSet<String>();
        NamedNodeMap attrMap = node.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++)
            result.add(attrMap.item(i).getNodeName());

        return result;
    }

    public static String getAttribute(Node node, String attributeName)
    {
        String result = null;
        NamedNodeMap map = node.getAttributes();

        if (map != null)
        {
            Node attributeNode = map.getNamedItem(attributeName);

            if (attributeNode != null)
                result = attributeNode.getNodeValue();
        }
        return result;
    }

    public static boolean doesAttributeExist(Node node, String attributeName)
    {
        NamedNodeMap map = node.getAttributes();

        if (map != null)
        {
            Node attributeNode = map.getNamedItem(attributeName);

            return attributeNode != null;
        }
        return false;
    }

    public static boolean getBooleanAttribute(Node node, String attributeName)
    {
        String attributeValue = getAttribute(node, attributeName);

        return "yes".equals(attributeValue);
    }

    public static boolean getBooleanAttribute(Node node, String attributeName, boolean defaultValue)
    {
        String attributeValue = getAttribute(node, attributeName);
        if (TapestryCore.isNull(attributeValue))
            return defaultValue;
        return "yes".equals(attributeValue);
    }

    public static String getValue(Node node)
    {
        StringBuffer buffer = new StringBuffer();
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            short type = child.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE)
                buffer.append(child.getNodeValue());
        }

        String result = buffer.toString().trim();
        if (result == null || "".equals(result))
            return null;

        return result;
    }

    public static boolean isComment(Node node)
    {
        return node.getNodeType() == Node.COMMENT_NODE;
    }

    public static boolean isElement(Node node)
    {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    public static boolean isElement(Node node, String elementName)
    {
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
            return false;

        return node.getNodeName().equals(elementName);

    }

    // public static ISourceLocationInfo getSourceLocationInfo(Node node)
    // {
    // ISourceLocationInfo result = null;
    // // if (node instanceof PullParserNode)
    // // {
    // // PullParserNode ppnode = (PullParserNode) node;
    // // result = ppnode.getSourceLocationInfo();
    // //
    // // }
    // // else
    // // {
    //       
    // try
    // {
    // DocumentImpl document = (DocumentImpl) node.getOwnerDocument();
    // result = (ISourceLocationInfo) document.getUserData(node,
    // TapestryCore.IDENTIFIER);
    // }
    // catch (RuntimeException e)
    // {
    //               
    // e.printStackTrace();
    // }
    // // }
    // return result;
    // }

    public static boolean isTextNode(Node child)
    {
        short type = child.getNodeType();
        return type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE;
    }

    /**
     * @param document
     * @return
     */
    public static String getPublicId(Document document)
    {
        DocumentType type = document.getDoctype();
        if (type == null)
            return null;
        return type.getPublicId();
    }

    public static String getDeclaredRootElement(Document document)
    {
        DocumentType type = document.getDoctype();
        if (type == null)
            return null;
        return type.getName();
    }

}