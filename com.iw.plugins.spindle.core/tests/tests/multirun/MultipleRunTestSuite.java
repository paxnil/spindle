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

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.iw.plugins.spindle.core.util.Assert;

/**
 *  Special TestSuite that will run all the tests found in it multiple times.
 *  Will only allow tests of type MultipleRunTestCase (and its subclasses) to
 *  be added to it.
 *   
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MultipleRunTestSuite extends TestSuite
{
    private String currentIdentifier;

    private String[] runIdentifiers;

    public MultipleRunTestSuite() {
        super();
    }
    
    public MultipleRunTestSuite(final Class theClass, String[] runIdentifiers)
    {
        super(theClass);
        Assert.isNotNull(runIdentifiers);
        Assert.isTrue(runIdentifiers.length > 0);
        this.runIdentifiers = runIdentifiers;
    }

    public void addTest(MultipleRunTestCase test)
    {
        super.addTest(test);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestSuite#addTest(junit.framework.Test)
     */
    public void addTest(Test test)
    {
        addTest((MultipleRunTestCase)test);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestSuite#addTestSuite(java.lang.Class)
     */
    public void addTestSuite(Class testClass)
    {
        throw new IllegalStateException("not allowed here");
    }

    public void run(TestResult result)
    {
        for (int i = 0; i < runIdentifiers.length; i++)
        {
            currentIdentifier = runIdentifiers[i];
            super.run(result);
        }

    }

    public void runTest(Test test, TestResult result)
    {
        ((MultipleRunTestCase) test).run(result, currentIdentifier);
    }

}
