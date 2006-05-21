package net.sf.spindle.core.parser.validator;

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
import java.io.InputStream;

import net.sf.spindle.core.AbstractXMLTestCase;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.source.IProblem;

import org.w3c.dom.Document;

/**
 * Basic Sanity Test for Parser This is not a Multirun Test. Only works with DOM parser!
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class BasicValidatorTest extends AbstractXMLTestCase
{

    // private Parser parser;

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
        // parser = new Parser();
        // parser.setDoValidation(false);
    }

    public void testValidComponent() throws Exception
    {
        validateAndAssert("validComponent.jwc", new TestAsserter()
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
        System.out.println("file: " + file);
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
        validateAndAssert("validPage.page", new TestAsserter()
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
        validateAndAssert("invalidComponent.jwc", new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                IProblem[] problems = (IProblem[]) result;
                assertTrue("validation should have failed", problems.length == 1);
            }
        });
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
