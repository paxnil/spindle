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

package tests.TapestryPullParser;

import junit.framework.TestCase;

import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.parser.xml.TapestryParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.pull.TapestryPullParser;

/**
 *  Pull parser implements org.w3c.dom.Node but a lot of methods don't make
 *  sense in a pull environment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class UnsupportedNodeMethods extends TestCase
{

    /**
     * Constructor for UnsupportedNodeMethods.
     * @param arg0
     */
    public UnsupportedNodeMethods(String arg0)
    {
        super(arg0);
    }

    public void testUnsupportedNodeMethods()
    {
        Node testNode = (Node) new TapestryPullParser(new TapestryParserConfiguration());
        try
        {
            testNode.appendChild(null);
            fail();
        } catch (RuntimeException e)
        {}
        try
        {
            testNode.cloneNode(true);
            fail();
        } catch (RuntimeException e1)
        {}
        try
        {
            testNode.cloneNode(false);
            fail();
        } catch (RuntimeException e2)
        {}
        try
        {
            testNode.getLastChild();
            fail();
        } catch (RuntimeException e3)
        {}
        try
        {
            testNode.getOwnerDocument();
            fail();
        } catch (RuntimeException e4)
        {}
        try
        {
            testNode.getPreviousSibling();
            fail();
        } catch (RuntimeException e5)
        {}
        try
        {
            testNode.getParentNode();
            fail();
        } catch (RuntimeException e6)
        {}
        try
        {
            testNode.hasChildNodes();
            fail();
        } catch (RuntimeException e7)
        {}
        try
        {
            testNode.insertBefore(null, null);
            fail();
        } catch (RuntimeException e8)
        {}
        try
        {
            testNode.removeChild(null);
            fail();
        } catch (RuntimeException e9)
        {}
        try
        {
            testNode.replaceChild(null, null);
            fail();
        } catch (RuntimeException e10)
        {}
        try
        {
            testNode.setNodeValue(null);
            fail();
        } catch (RuntimeException e11)
        {}
        try
        {
            testNode.setPrefix(null);
            fail();
        } catch (RuntimeException e12)
        {}

    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UnsupportedNodeMethods.class);
    }

}
