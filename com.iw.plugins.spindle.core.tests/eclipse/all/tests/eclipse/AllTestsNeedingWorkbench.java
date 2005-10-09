package all.tests.eclipse;

import junit.framework.Test;
import all.tests.Tests;
import core.build.ResourceClashTests;
import core.test.eclipse.ContextTests;
import core.test.eclipse.DirectLinkTests;
import core.test.eclipse.ProjectAdapterFactoryTests;

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

public class AllTestsNeedingWorkbench extends Tests
{

    public AllTestsNeedingWorkbench(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return Tests.createSuite(AllTestsNeedingWorkbench.class, getAllTestClasses());
    }

    static Class[] getAllTestClasses()
    {
        return new Class[] {DirectLinkTests.class, ResourceClashTests.class, ContextTests.class, ProjectAdapterFactoryTests.class};
    }
}
