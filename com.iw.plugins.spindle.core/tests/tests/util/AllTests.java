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

package tests.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class AllTests
{
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AllTests.class);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for tests.util");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(IIdentifiableMapTests.class));
        suite.addTest(new TestSuite(OrderPreservingMapTests.class));
        suite.addTest(new TestSuite(OrderPreservingSetTests.class));
        suite.addTest(new TestSuite(TestPropertyFiringList.class));
        suite.addTest(new TestSuite(TestPropertyFiringMap.class));
        suite.addTest(new TestSuite(TestPropertyFiringSet.class));
        suite.addTest(new TestSuite(PropertyFiringTestInContext.class));
        //$JUnit-END$
        return suite;
    }
}
