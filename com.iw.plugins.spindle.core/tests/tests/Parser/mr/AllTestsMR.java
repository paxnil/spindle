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

import tests.multirun.MultipleRunTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *  Add MR test suites here.
 * 
 *  Runs all the MR tests in this package
 * 
 *  The goal is to have all the tests run with ALL_RUNS.
 * 
 *  DOM_ONLY is for use while the Pull parser is still broken
 *  
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class AllTestsMR
{
    public static final String[] ALL_RUNS = new String[] { MRBaseParserTest.DOM, MRBaseParserTest.PULL };
    public static final String[] DOM_ONLY = new String[] { MRBaseParserTest.DOM };
    public static final String[] PULL_ONLY = new String[] {MRBaseParserTest.PULL};
    
    public static final String[] CURRENT_RUN = ALL_RUNS;
    

    public static void main(String[] args)
    {}

    public static Test suite()
    {
        TestSuite suite = new TestSuite("MRTest for tests.Parser");
        //$JUnit-BEGIN$
        suite.addTest(new MultipleRunTestSuite(BasicParserMRTest.class, CURRENT_RUN));
        suite.addTest(new MultipleRunTestSuite(NodeTraversalMRTest.class, CURRENT_RUN));
        //$JUnit-END$
        return suite;
    }
}
