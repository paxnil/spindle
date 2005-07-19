package all.tests;

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

import junit.framework.Test;
import all.tests.core.CoreFrameworkTestSuite;
import all.tests.eclipse.InsideEclipseTestSuite;
import all.tests.parser.OldTests;

/**
 * RUNS ALL THE TESTS! that can be done inside of an Eclipse runtime workbench
 * 
 * @author glongman@gmail.com
 */
public class EclipseTestSuite extends Tests
{
    public EclipseTestSuite(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return Tests.createSuite(EclipseTestSuite.class, getAllTestClasses());
    }

    static Class[] getAllTestClasses()
    {
        return new Class[]
            { CoreFrameworkTestSuite.class, InsideEclipseTestSuite.class, OldTests.class };
    }
}
