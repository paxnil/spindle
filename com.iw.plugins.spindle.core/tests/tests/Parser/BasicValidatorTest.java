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

package tests.Parser;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.xerces.dom.DocumentImpl;

import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.util.Files;

/**
 *  Basic Sanity Test for Parser
 *  
 *  This is not a Multirun Test. Only works with DOM parser!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BasicValidatorTest extends TestCase
{

    private Parser parser;
    private DocumentImpl xmlDocument;
    /**
     * Constructor for BasicParserTest.
     * @param name
     */
    public BasicValidatorTest(String name)
    {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        parser = new Parser();
        parser.setDoValidation(false);
    }

    private void parse(String content)
    {
        try
        {
            parser.parse(content);
            printProblems(parser.getProblems());
            assertTrue("document is not well formed!", !parser.getHasFatalErrors());
            xmlDocument = parser.getParsedDocument();
            assertNotNull(xmlDocument);
            System.err.println();
        } catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    private void printProblems(IProblem[] problems)
    {
        for (int i = 0; i < problems.length; i++)
        {
            System.err.println(getClass().getName() + ":" + getName() + " " + problems[i]);
        }
    }

    private String getContent(String file)
    {
        try
        {
            return Files.readFileToString(getClass().getResourceAsStream(file), null);
        } catch (IOException e)
        {
            fail("could not read file: " + file + ", message:" + e.getMessage());
        }
        return null;
    }

    public void testValidComponent()
    {
        parse(getContent("/testdata/validComponent.jwc"));
        DOMValidator validator = new DOMValidator();
        validator.validate(xmlDocument);
        IProblem[] problems = validator.getProblems();
        printProblems(problems);
        assertTrue("validation should have been successful", problems.length == 0);
    }

    public void testValidPage()
    {
        parse(getContent("/testdata/validPage.page"));
        DOMValidator validator = new DOMValidator();
        validator.validate(xmlDocument);
        IProblem[] problems = validator.getProblems();
        printProblems(problems);
        assertTrue("validation should have been successful", problems.length == 0);
    }

    public void testInvalidCompnent()
    {
        parse(getContent("/testdata/invalidComponent.jwc"));
        DOMValidator validator = new DOMValidator();
        validator.validate(xmlDocument);
        IProblem[] problems = validator.getProblems();
        printProblems(problems);
        assertTrue("validation should have failed", problems.length == 1);
    }

}
