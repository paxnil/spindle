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

package tests.Parser.mr;

import java.io.IOException;

import org.w3c.dom.Node;

/**
 *  Basic Sanity Test for Parser
 *  
 *  This is a Multirun Test. One run each for DOM and PULL parser!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BasicParserMRTest extends MRBaseParserTest
{

    final String PROLOG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE dog [\n"
            + "<!ELEMENT dog (#PCDATA)>\n"
            + "<!ATTLIST dog test CDATA #IMPLIED>\n"
            + "]>\n";

    final String MALFORMED_PROLOG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE dog [\n"
            + "<!ELEMENT dog (#PCDATA)>\n"
            + "<!BANANA dog test CDATA #IMPLIED>\n"
            + "]>\n";

    /**
     * Constructor for BasicParserTest.
     * @param name
     */
    public BasicParserMRTest(String name)
    {
        super(name);
    }

    public void testVALID()
    {
        final String VALID = PROLOG + "<dog test='poo'>Hello, world!</dog>\n";
        Node node = null;
        try
        {
            node = parseAll(VALID);
        } catch (IOException e)
        {
            m_fail("IOException: " + e.getMessage());
        }
        m_assertNotNull(node);
        m_assertTrue(parser.getProblems().length == 0);
    }

    public void testINVALID()
    {
        final String INVALID = PROLOG + "<dog invalid='poo'>Hello, world!</dog>\n";
        Node node = null;
        try
        {
            node = parseAll(INVALID);
        } catch (IOException e)
        {
            m_fail("IOException: " + e.getMessage());
        }
        m_assertNotNull(node);
        basicCheckProblems(parser.getProblems(), 1);
    }

    public void testMalformedProlog()
    {
        final String MALFORMED = MALFORMED_PROLOG + "<dog invalid='poo'>Hello, world!</dog>\n";
        Node node = null;
        try
        {
            node = parseAll(MALFORMED);
        } catch (IOException e)
        {
            m_fail("IOException: " + e.getMessage());
        }
        m_assertNull(node);
        basicCheckProblems(parser.getProblems(), 1);
    }

    public void testMalformedContent()
    {
        final String MALFORMED = PROLOG + "<dog test='poo'>Hello, world!<dog>\n";
        Node node = null;
        try
        {
            node = parseAll(MALFORMED);

        } catch (IOException e)
        {
            m_fail("IOException: " + e.getMessage());
        }
        m_assertNull(node);
        basicCheckProblems(parser.getProblems(), 1);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(BasicParserMRTest.class);
    }

}
