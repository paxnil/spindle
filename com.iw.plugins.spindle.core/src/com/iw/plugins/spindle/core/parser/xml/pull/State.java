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

package com.iw.plugins.spindle.core.parser.xml.pull;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Holds, immutably, the information gathered from the parser when its in a particular state
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
/*package*/
class State implements Node
{

    private StateType type;
    private boolean complete;

    /*package*/
    State()
    {}

    public StateType getType()
    {
        return type;
    }

    public void populate(StateType type, TapestryPullParser parser)
    {
        this.type = type;
    }

    public void complete(TapestryPullParser parser)
    {
//        if (type == StateType.START_TAG || type == StateType.TEXT)
//        {
//            complete = true;
//        } else
//        {
//            throw new IllegalStateException("can't complete!");
//        }
    }

    public boolean isComplete()
    {
        return complete;
    }
    /* **************************************** 
     *    org.w3c.dom.Node Stuff      
     * ****************************************/

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
     */
    public Node appendChild(Node arg0) throws DOMException
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    public Node cloneNode(boolean arg0)
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getAttributes()
     */
    public NamedNodeMap getAttributes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getChildNodes()
     */
    public NodeList getChildNodes()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getFirstChild()
     */
    public Node getFirstChild()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getLastChild()
     */
    public Node getLastChild()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNextSibling()
     */
    public Node getNextSibling()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeName()
     */
    public String getNodeName()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeValue()
     */
    public String getNodeValue() throws DOMException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getOwnerDocument()
     */
    public Document getOwnerDocument()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getParentNode()
     */
    public Node getParentNode()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getPreviousSibling()
     */
    public Node getPreviousSibling()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#hasAttributes()
     */
    public boolean hasAttributes()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#hasChildNodes()
     */
    public boolean hasChildNodes()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public Node insertBefore(Node arg0, Node arg1) throws DOMException
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
     */
    public boolean isSupported(String arg0, String arg1)
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#normalize()
     */
    public void normalize()
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
     */
    public Node removeChild(Node arg0) throws DOMException
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public Node replaceChild(Node arg0, Node arg1) throws DOMException
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
     */
    public void setNodeValue(String arg0) throws DOMException
    {
        throw new IllegalStateException("not supported");
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#setPrefix(java.lang.String)
     */
    public void setPrefix(String arg0) throws DOMException
    {
        throw new IllegalStateException("not supported");
    }

    /* **************************************** 
     *   END OF org.w3c.dom.Node Stuff      
     * ****************************************/

}
