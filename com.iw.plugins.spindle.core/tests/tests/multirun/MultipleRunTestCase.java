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

package tests.multirun;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 *  
 *  A TestCase sublass the can be run more than once.
 * 
 *  Must be run from a MultipleRunTestSuite.
 *  
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MultipleRunTestCase extends TestCase
{

    protected String runIdentifier;
    private long startTime;


    public MultipleRunTestCase()
    {
        super();
    }

    /**
     * @param arg0
     */
    public MultipleRunTestCase(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.Test#run(junit.framework.TestResult)
     */
    public void run(TestResult result)
    {
        throw new IllegalStateException("must be run from within a MutipleRunTestSuite!");
    }

    public void run(TestResult result, String runIdentifier)
    {
        this.runIdentifier = runIdentifier;
        super.run(result);
    }

    private String getM_Message(String originalMessage)
    {
        return runIdentifier + " " + (originalMessage == null ? "" : originalMessage);
    }

    protected void m_assertTrue(String message, boolean condition)
    {
        assertTrue(getM_Message(message), condition);
    }

    protected void m_assertTrue(boolean condition)
    {
        m_assertTrue(null, condition);
    }

    protected void m_assertFalse(String message, boolean condition)
    {
        assertFalse(getM_Message(message), condition);
    }

    protected void m_assertFalse(boolean condition)
    {
        m_assertFalse(null, condition);
    }

    protected void m_assertEquals(String message, Object expected, Object actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(Object expected, Object actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, String expected, String actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(String expected, String actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, double expected, double actual, double delta)
    {
        assertEquals(getM_Message(message), expected, actual, delta);
    }

    protected void m_assertEquals(double expected, double actual, double delta)
    {
        m_assertEquals(null, expected, actual, delta);
    }

    protected void m_assertEquals(String message, float expected, float actual, float delta)
    {
        assertEquals(getM_Message(message), expected, actual, delta);
    }

    protected void m_assertEquals(float expected, float actual, float delta)
    {
        m_assertEquals(null, expected, actual, delta);
    }

    protected void m_assertEquals(String message, long expected, long actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(long expected, long actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, boolean expected, boolean actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(boolean expected, boolean actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, byte expected, byte actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(byte expected, byte actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, char expected, char actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(char expected, char actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, short expected, short actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(short expected, short actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertEquals(String message, int expected, int actual)
    {
        assertEquals(getM_Message(message), expected, actual);
    }

    protected void m_assertEquals(int expected, int actual)
    {
        m_assertEquals(null, expected, actual);
    }

    protected void m_assertNotNull(Object object)
    {
        m_assertNotNull(null, object);
    }

    protected void m_assertNotNull(String message, Object object)
    {
        assertNotNull(getM_Message(message), object);
    }

    protected void m_assertNull(Object object)
    {
        m_assertNull(null, object);
    }

    protected void m_assertNull(String message, Object object)
    {
        assertNull(getM_Message(message), object);
    }

    protected void m_assertSame(String message, Object expected, Object actual)
    {
        assertSame(getM_Message(message), expected, actual);
    }

    protected void m_assertSame(Object expected, Object actual)
    {
        m_assertSame(null, expected, actual);
    }

    protected void m_assertNotSame(String message, Object expected, Object actual)
    {
        assertNotSame(getM_Message(message), expected, actual);
    }

    protected void m_assertNotSame(Object expected, Object actual)
    {
        m_assertNotSame(null, expected, actual);
    }

    protected void m_fail(String message)
    {
        fail(getM_Message(message));
    }

    protected void m_fail()
    {
        m_fail(null);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {        
        super.setUp();
        startTime = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        long finished = System.currentTimeMillis();
        System.out.println(runIdentifier+" "+ getClass().getName()+":"+ getName() + " elapsed = "+(finished - startTime));
        super.tearDown();
    }

}
