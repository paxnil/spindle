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

package tests.mixedDTD;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.source.IProblem;

/**
 *  Tests for xml files that use internal and external DTD stuff simultaneously
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MixedDTDTest extends TestCase
{
    private Parser fParser;
    /**
     * Constructor for MixedDTDTest.
     * @param arg0
     */
    public MixedDTDTest(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        fParser = new Parser(false);
        fParser.setDoValidation(true);

        super.setUp();
    }

    public void test() throws Exception
    {
        // parse a non mixed one first to intialize the
        // EntityResolver
        InputStream in = getClass().getResourceAsStream("/testdata/basicTapestryComponent.jwc");
        assertNotNull(in);

        try
        {
            fParser.parse(in);
            basicCheckProblems(fParser.getProblems(), 0);
        } catch (IOException e1)
        {
            fail("IOException " + e1.getMessage());
        } finally
        {
            in.close();
            in = null;
        }
        in = getClass().getResourceAsStream("/testdata/mixedDTD.jwc");
        assertNotNull(in);

        try
        {
            fParser.parse(in);
            basicCheckProblems(fParser.getProblems(), 0);
        } catch (IOException e1)
        {
            fail("IOException " + e1.getMessage());
        } finally
        {
            in.close();
        }
    }
    protected void basicCheckProblems(IProblem[] problems, int expectedCount)
    {
        PrintStream stream = problems.length == expectedCount ? System.out : System.err;
        printProblems(problems, stream);
        assertEquals(expectedCount, problems.length);
    }
    protected void printProblems(IProblem[] problems, PrintStream stream)
    {
        for (int i = 0; i < problems.length; i++)
        {
            stream.println(problems[i]);
        }
    }

}
