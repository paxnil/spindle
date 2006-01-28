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

package net.sf.spindle.xerces.parser;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.util.W3CAccess;
import net.sf.spindle.xerces.parser.Parser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *  Basic Sanity Test for Parser
 *  
 *  This is not a Multirun Test. Only works with DOM parser!
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class BasicParserDOMTest extends TestCase
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
    public BasicParserDOMTest(String name)
    {
        super(name);
    }

    private void printProblems(IProblem[] problems)
    {
        for (int i = 0; i < problems.length; i++)
        {
            System.err.println(getClass().getName() + ":" + getName() + " " + problems[i]);
        }
    }

    public void testVALID()
    {

        Parser parser = new Parser(false);
        parser.setDoValidation(true);

        final String VALID = PROLOG + "<dog test='poo'>Hello, world!</dog>\n";
        Document document = null;
        try
        {
            document = parser.parse(VALID);
        } catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        assertNotNull(document);
        assertTrue(parser.getProblems().length == 0);
        assertNull(W3CAccess.getPublicId(document));
    }

    public void testINVALID()
    {
        Parser parser = new Parser(false);
        parser.setDoValidation(true);

        final String INVALID = PROLOG + "<dog invalid='poo'>Hello, world!</dog>\n";
        Node node = null;
        try
        {
            node = parser.parse(INVALID);
        } catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        assertNotNull(node);
        IProblem[] problems = parser.getProblems();
        assertTrue(problems.length == 1);
        printProblems(problems);

    }

    public void testMalformedProlog()
    {
        Parser parser = new Parser(false);
        parser.setDoValidation(true);
        final String MALFORMED = MALFORMED_PROLOG + "<dog invalid='poo'>Hello, world!</dog>\n";
        Node node = null;
        try
        {
            node = parser.parse(MALFORMED);
        } catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        assertNull(node);
        IProblem[] problems = parser.getProblems();
        assertTrue(problems.length == 1);
        printProblems(problems);

    }

    public void testMalformedContent()
    {
        Parser parser = new Parser(false);
        parser.setDoValidation(true);
        final String MALFORMED = PROLOG + "<dog test='poo'>Hello, world!<dog>\n";
        Node node = null;
        try
        {
            node = parser.parse(MALFORMED);
        } catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        assertNull(node);
        IProblem[] problems = parser.getProblems();
        assertTrue(problems.length == 1);
        printProblems(problems);

    }

    public void testString()
    {
        Parser parser = new Parser(false);
        parser.setDoValidation(false);
        final String content = "<private-asset name='poo' path='moo'/>";
        Document document = null;
        try
        {
            document = parser.parse(content);
        } catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        IProblem[] problems = parser.getProblems();
        printProblems(problems);
        assertNotNull(document);
        assertTrue(problems.length == 0);
        assertNull(W3CAccess.getPublicId(document));

    }

}
