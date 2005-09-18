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

package tests.Parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;

import com.iw.plugins.spindle.core.parser.dom.IDOMModel;
import com.iw.plugins.spindle.core.parser.dom.validator.DOMValidator;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.util.Files;
import com.iw.plugins.spindle.xerces.parser.Parser;

import core.test.AbstractXMLTestCase;

/**
 * Basic Sanity Test for Parser This is not a Multirun Test. Only works with DOM parser!
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class BasicValidatorTest extends AbstractXMLTestCase
{

    private Parser parser;

    /**
     * Constructor for BasicParserTest.
     * 
     * @param name
     */
    public BasicValidatorTest(String name)
    {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        parser = new Parser();
        parser.setDoValidation(false);
    }

    public void testValidComponent() throws Exception
    {
        validateAndAssert("/testdata/validComponent.jwc", new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                IProblem[] problems = (IProblem[]) result;
                assertTrue("validation should have been successful", problems.length == 0);
            }
        });
    }

    private void validateAndAssert(String file, TestAsserter asserter) throws Exception
    {
        System.out.println("file: "+file);
        Document document = getDocument(getContent(file));
        assertNotNull(document);
        assertTrue(document.getDocumentElement() != null);

        IDOMModel model = createMockIDOMModel(document);

        mockContainer.replayControls();

        DOMValidator validator = new DOMValidator();
        validator.validate(model);
        IProblem[] problems = validator.getProblems();
        printProblems(problems);
        asserter.makeAssertions(problems);

        mockContainer.verifyControls();
    }

    public void testValidPage() throws Exception
    {
        validateAndAssert("/testdata/validPage.page", new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                IProblem[] problems = (IProblem[]) result;
                assertTrue("validation should have been successful", problems.length == 0);
            }
        });

    }

    public void testInvalidCompnent() throws Exception
    {
        validateAndAssert("/testdata/invalidComponent.jwc", new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                IProblem[] problems = (IProblem[]) result;
                assertTrue("validation should have failed", problems.length == 1);
            }
        });
    }

    protected Document getDocument(InputStream content)
    {
        try
        {
            parser.parse(content, null);
            printProblems(parser.getProblems());
            assertTrue("document is not well formed!", !parser.getHasFatalErrors());
            Document xmlDocument = parser.getParsedDocument();
            assertNotNull(xmlDocument);
            System.err.println();
            return xmlDocument;
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
        return null;
    }

    private void printProblems(IProblem[] problems)
    {
        for (int i = 0; i < problems.length; i++)
        {
            System.err.println(getClass().getName() + ":" + getName() + " " + problems[i]);
        }
    }

    private InputStream getContent(String file)
    {
        InputStream result = getClass().getResourceAsStream(file);
        if (result == null)
            fail("could not read file: " + file);

        return result;
    }

}
