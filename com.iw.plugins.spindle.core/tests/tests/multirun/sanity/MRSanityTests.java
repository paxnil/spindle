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

package tests.multirun.sanity;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import tests.multirun.MultipleRunTestSuite;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class MRSanityTests extends TestCase
{

    /**
     * Constructor for MRSanityTests.
     * @param name
     */
    public MRSanityTests(String name)
    {
        super(name);
    }

    public void testNullRunIds()
    {
        try
        {
            MultipleRunTestSuite suite = new MultipleRunTestSuite(MRT1.class, null);
            fail("should thrown NPE");
        } catch (NullPointerException e)
        {}
    }

    public void testEmptyIdentifers()
    {
        try
        {
            MultipleRunTestSuite suite = new MultipleRunTestSuite(MRT1.class, new String[] {});
            fail("should throw runtime exception");
        } catch (RuntimeException e)
        {}
    }

    public void testResults()
    {
        TestResult result = new TestResult();

        TestSuite suite = new MultipleRunTestSuite(MRT1.class, new String[] { "AAA", "BBB" });
        suite.run(result);

        assertTrue(result.failureCount() == 2);

        Enumeration e = result.failures();
        assertTrue(e.nextElement().toString().indexOf("AAA") >= 0);
        assertTrue(e.nextElement().toString().indexOf("BBB") >= 0);
    }

    public void testIllegal()
    {
        TestResult result = new TestResult();

        TestSuite suite = new TestSuite(MRT1.class);
        try
        {
            suite.run(result);
            fail("MRTC's should not run in regular TestSuites!");
        } catch (RuntimeException e)
        {}
    }

}
