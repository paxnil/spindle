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

package tests.Parser.mr;

import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.scanning.W3CAccess;

/**
 *  Basic Sanity Test for Node Traveral.
 *  
 *  This is a Multirun Test. One run each for DOM and PULL parser!
 * 
 *  Trivial for DOM, but important to make sure that PULL works properly!
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class NodeTraversalMRTest extends MRBaseParserTest
{

    /**
     * Constructor for BasicParserTest.
     * @param name
     */
    public NodeTraversalMRTest(String name)
    {
        super(name);
    }

//    public void testTraverseAll()
//    {
//        try
//        {
//            String content = getXMLSourceAsString("/testdata/NodeTraversalData.xml");
//            Node rootNode = parseToRootNode(content, 0);
//            basicCheckProblems(parser.getProblems(), 0);
//            m_assertEquals(10, visitAllChildren(rootNode));
//        } catch (RuntimeException e)
//        {
//            m_fail("RuntimeException caught");
//        }
//    }

    protected int visitAllChildren(Node rootNode)
    {
        if (rootNode == null)
        {
            return 0;
        }
        Node child = rootNode.getFirstChild();
        if (child == null)
        {
            return 1;
        }
        int count = 0;
        for (; child != null; child = child.getNextSibling())
        {            
                count += visitAllChildren(child);            
        }
        return 1+count ;
    }

    public void testTraverseTopLevelSiblings()
    {
        try
        {
            String content = getXMLSourceAsString("/testdata/NodeTraversalData.xml");
            Node rootNode = parseToRootNode(content, 0);
            basicCheckProblems(parser.getProblems(), 0);
            m_assertTrue(W3CAccess.isElement(rootNode, "animals"));

            Node node = rootNode.getFirstChild();
            m_assertNotNull(node);
            m_assertTrue(W3CAccess.isElement(node, "moose"));

            node = node.getNextSibling();
            m_assertNotNull(node);
            m_assertTrue(W3CAccess.isElement(node, "moose"));

            node = node.getNextSibling();
            m_assertNotNull(node);
            m_assertTrue(W3CAccess.isElement(node, "canine"));

            node = node.getNextSibling();
            m_assertNotNull(node);
            m_assertTrue(W3CAccess.isElement(node, "feline"));

            node = node.getNextSibling();
            m_assertNotNull(node);
            m_assertTrue(W3CAccess.isElement(node, "rodent"));

            node = node.getNextSibling();
            m_assertNull(node);
        } catch (RuntimeException e)
        {
            m_fail("RuntimeException caught");
        }
    }

    /** test getting attributes from elements in both before an after other elements have been accessed */
    public void testAttributes()
    {
        try
        {
            String content = getXMLSourceAsString("/testdata/NodeTraversalData.xml");
            Node rootNode = parseToRootNode(content, 0);
            basicCheckProblems(parser.getProblems(), 0);
            m_assertNotNull(rootNode);

            Node moose1 = rootNode.getFirstChild();
            assertNull(W3CAccess.getAttribute(moose1, "one"));
            assertNull(W3CAccess.getAttribute(moose1, "two"));
            assertNull(W3CAccess.getAttribute(moose1, "three"));

            Node moose2 = moose1.getNextSibling();
            assertEquals("AAAA", W3CAccess.getAttribute(moose2, "one"));
            assertEquals("BBBB", W3CAccess.getAttribute(moose2, "two"));
            assertEquals("CCCC", W3CAccess.getAttribute(moose2, "three"));

            assertNull(W3CAccess.getAttribute(moose1, "one"));
            assertNull(W3CAccess.getAttribute(moose1, "two"));
            assertNull(W3CAccess.getAttribute(moose1, "three"));

            Node other = moose2.getNextSibling();

            assertNull(W3CAccess.getAttribute(moose1, "one"));
            assertNull(W3CAccess.getAttribute(moose1, "two"));
            assertNull(W3CAccess.getAttribute(moose1, "three"));

            assertEquals("AAAA", W3CAccess.getAttribute(moose2, "one"));
            assertEquals("BBBB", W3CAccess.getAttribute(moose2, "two"));
            assertEquals("CCCC", W3CAccess.getAttribute(moose2, "three"));

        } catch (RuntimeException e)
        {
            e.printStackTrace();
            m_fail("RuntimeException caught");
        }
    }

    /** 
     * this one is tricky in the PULL case, we allow that
     * once the value has been pulled, it can't be pulled again (for PULL!)
     */
    public void testGetValue()
    {
        String content = getXMLSourceAsString("/testdata/NodeTraversalData.xml");
        Node rootNode = parseToRootNode(content, 0);
        basicCheckProblems(parser.getProblems(), 0);
        m_assertNotNull(rootNode);

        Node moose1 = rootNode.getFirstChild();
        Node moose2 = moose1.getNextSibling();
        try
        {
            assertEquals("Bullwinkle", W3CAccess.getValue(moose1));
            assertNull(W3CAccess.getValue(moose2));
        } catch (RuntimeException e)
        {
            // Should never happen in either case
            e.printStackTrace();
            m_fail("Runtime exception caught");
        }

    }
    

   
}
