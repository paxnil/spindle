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
 *  glongman@intelligentworks.com,
 *  bgarson@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.parser.xml.pull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.iw.plugins.spindle.core.builder.TapestryBuilder;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  The implementation of org.w3c.dom.Node for the PullParser
 * 
 * 
 *  To create an elementNode...
 * 
 *  PullParserNode.createElementNode(args);
 * 
 *  To create a Text node..
 * 
 *  PullParserNode.createTextNode(args);
 * 
 *  Things that the creator method won't do for you:
 * 
 *  set the first child and the siblings: you have to do this using the
 *  
 *  methods setFirstChild(), setPreviousSibling(), and setNextSibling()
 * 
 *  You also need to call completed() when it appropriate
 * 
 * @author bgarson@intelligentworks.com, glongman@intelligentworks.com
 * @version $Id$
 */
public class PullParserNode implements Node
{

    /**
     *  Create a new Element node
     * 
     * @param parser the parser in use
     * @param parentNode the parent, null if its the root node
     * @param elementName = Qname.rawname
     * @param attributes the XMLAttributes passed to startElement
     * @return
     */
    public static PullParserNode createElementNode(
        TapestryPullParser parser,
        PullParserNode parentNode,
        String elementName,
        XMLAttributes attributes)
    {

        return new PullParserNode(parser, elementName, PullParserNode.ELEMENT_NODE, parentNode, attributes);
    }

    /**
     * 
     * Create a new Text Node
     * 
     * @param parser the parser in use
     * @param parentNode the parent, must not be null
     * @param value The text returned by the characters() method in the parser
     * @return a new Text node
     * 
     * @throws IllegalArgumentException if parent node is null (text nodes can't be the root node)
     */
    public static PullParserNode createTextNode(TapestryPullParser parser, PullParserNode parentNode, String value)
    {
        Assert.isLegal(parentNode != null);
        return new PullParserNode(parser, "#text", PullParserNode.TEXT_NODE, parentNode, value);
    }

    private boolean fComplete;
    private boolean fEmpty;
    private TapestryPullParser fParser;

    /**
     * Name will be:
     * 
     * Node Type       Name
     * ----------------------
     * ELEMENT_NODE    the name of the element (tag name)
     * TEXT_NODE       "#text"
     */
    protected String fNodeName;

    /**
     * One of:
     *  
     *  ELEMENT_NODE
     *  TEXT_NODE
     * 
     * There are many other kinds of Nodes declared in org.w3c.dom.Node
     * But we only care about the above two.
     *  
     */
    protected short fNodeType;

    /**
     * Value will be:
     * 
     * Node Type       Value
     * ----------------------
     * ELEMENT_NODE    null!
     * TEXT_NODE       The content of the text node
     */
    protected String fNodeValue;

    /** parent node **/
    protected PullParserNode fParentNode;

    /** first child **/
    protected PullParserNode fFirstChild;

    /** Previous sibling. */
    protected PullParserNode fPreviousSibling;

    /** Next sibling. */
    protected PullParserNode fNextSibling;

    protected ISourceLocationInfo fSourceInfo;

    protected Map fAttributes;

    /** private constructor - use the static create methods instead **/
    private PullParserNode(TapestryPullParser parser, String nodeName, short nodeType, PullParserNode parentNode)
    {
        Assert.isTrue(nodeType == ELEMENT_NODE || nodeType == TEXT_NODE, "invalid node type");
        this.fParser = parser;
        this.fNodeName = nodeName;
        this.fNodeType = nodeType;
        this.fParentNode = parentNode;
        this.fComplete = false;
    }
    /** private constructor - use the static create methods instead **/
    public PullParserNode(
        TapestryPullParser parser,
        String nodeName,
        short nodeType,
        PullParserNode parentNode,
        XMLAttributes xmlAttributes)
    {
        this(parser, nodeName, nodeType, parentNode);
        createAttributes(xmlAttributes);
    }
    /** private constructor - use the static create methods instead **/
    public PullParserNode(
        TapestryPullParser parser,
        String nodeName,
        short nodeType,
        PullParserNode parentNode,
        String value)
    {
        this(parser, nodeName, nodeType, parentNode);
        this.fNodeValue = value;
    }
    /**
     * @param attributes
     */
    private void createAttributes(XMLAttributes xmlAttributes)
    {
        if (fAttributes != null)
        {
            throw new RuntimeException("PPN already has attributes!");
        }
        int length = xmlAttributes.getLength();
        if (length > 0)
        {
            fAttributes = new HashMap();
            QName qname = new QName();
            for (int i = 0; i < length; i++)
            {
                xmlAttributes.getName(i, qname);
                String value = xmlAttributes.getValue(i);
                fAttributes.put(qname.rawname, value);
            }
        }

    }

    /** 
     * The parser must call this when all processing 
     * for this node is complete.
     *
     */
    protected void completed()
    {
        if (TapestryBuilder.DEBUG)
            System.err.println("PPNode.complete called on: " + getNodeName());
        this.fComplete = true;
    }

    protected void setEmpty()
    {
        this.fEmpty = true;
    }

    /**
     * 
     * Method used to bump the parser into reading the next
     * chunk of information.
     * 
     */
    private void bumpParser()
    {
        fParser.bump();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeName()
     */
    public String getNodeName()
    {
        return fNodeName;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeValue()
     */
    public String getNodeValue() throws DOMException
    {
        return fNodeValue;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
     */
    public void setNodeValue(String arg0) throws DOMException
    {
        throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "read only!");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType()
    {
        return fNodeType;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getParentNode()
     */
    public Node getParentNode()
    {
        return fParentNode;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getChildNodes()
     */
    public NodeList getChildNodes()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getFirstChild()
     */
    public Node getFirstChild()
    {
        if (!fComplete && !fEmpty)
        {
            while (!fComplete)
            {
                fParser.bump();
                if (fFirstChild != null)
                {
                    break;
                }
            }
        }
        return fFirstChild;

    }

    protected void setFirstChild(PullParserNode node)
    {
        this.fFirstChild = node;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getLastChild()
     */
    public Node getLastChild()
    {
        if (!fComplete && !fEmpty)
        {
            while (!fComplete)
            {
                bumpParser();
            }
        }
        Node result = getFirstChild();
        for (Node node = getFirstChild(); node != null; node = node.getNextSibling())
        {
            result = node;
        }
        return result;

    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getPreviousSibling()
     */
    public Node getPreviousSibling()
    {
        return fPreviousSibling;
    }

    protected void setPreviousSibling(PullParserNode node)
    {
        this.fPreviousSibling = node;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNextSibling()
     */
    public Node getNextSibling()
    {
        System.out.println("PPNode.getNextSibling called on: " + getNodeName());

        if (fNextSibling == null)
        {
            if (!fComplete)
            {
                while (!fComplete)
                {
                    bumpParser();
                    if (fNextSibling != null)
                    {
                        break;
                    }
                }
            } else if (fEmpty)
            {
                while (!fParentNode.fComplete)
                {
                    bumpParser();
                    if (fNextSibling != null)
                    {
                        break;
                    }
                }
            }
        }
        return fNextSibling;
    }

    protected void setNextSibling(PullParserNode node)
    {
        this.fNextSibling = node;
    }

    public Map getKludgeAttributes()
    {
        if (fAttributes == null)
        {
            return Collections.EMPTY_MAP;
        }
        return fAttributes;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getAttributes()
     */
    public NamedNodeMap getAttributes()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getOwnerDocument()
     */
    public Document getOwnerDocument()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public Node insertBefore(Node arg0, Node arg1) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public Node replaceChild(Node arg0, Node arg1) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
     */
    public Node removeChild(Node arg0) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
     */
    public Node appendChild(Node child) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#hasChildNodes()
     */
    public boolean hasChildNodes()
    {
        return fFirstChild != null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    public Node cloneNode(boolean arg0)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#normalize()
     */
    public void normalize()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
     */
    public boolean isSupported(String arg0, String arg1)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#setPrefix(java.lang.String)
     */
    public void setPrefix(String arg0) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#hasAttributes()
     */
    public boolean hasAttributes()
    {
        return fAttributes != null;
    }

    /**
     * @return
     */
    public ISourceLocationInfo getSourceLocationInfo()
    {
        return fSourceInfo;
    }

}
