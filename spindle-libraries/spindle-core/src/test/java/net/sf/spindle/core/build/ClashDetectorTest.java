package net.sf.spindle.core.build;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import net.sf.spindle.core.CoreStatus;
import net.sf.spindle.core.SuiteOfTestCases;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.resources.AbstractTestRoot;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.ResourceImpl;
import net.sf.spindle.core.source.IProblem;

import org.easymock.MockControl;

public class ClashDetectorTest extends AbstractTestRoot
{

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ClashDetectorTest.class);
    }

    public ClashDetectorTest(String name)
    {
        super(name);
    }
    
    

    @Override
    public void setUpSuite() throws Exception
    {        
        super.setUpSuite();
        ClashDetector.SWITCHED_ON = true;
    }
    
    

    @Override
    public void tearDownSuite() throws Exception
    {        
        super.tearDownSuite();
        ClashDetector.SWITCHED_ON = false;
    }

    public void testClashesWithNot()
    {
        ICoreResource one = createMockResource("/WEB-INF/app1/app1.application", false);
        ICoreResource two = createMockResource("/WEB-INF/app2/app2.application", false);

        assertFalse(one.clashesWith(two));
        assertFalse(two.clashesWith(one));
    }

    public void testClashesWith()
    {
        ICoreResource cp = createMockResource("/", true);

        assertTrue(cp.clashesWith(cp));

        ICoreResource ctx = createMockResource("/", false);

        assertFalse(cp.clashesWith(ctx));

        assertFalse(ctx.clashesWith(cp));
    }

    public void testClashesWith2()
    {
        ICoreResource one = createMockResource("/WEB-INF/", false);
        ICoreResource two = createMockResource("/WEB-INF/", false);

        assertTrue(one.clashesWith(two));
        assertTrue(two.clashesWith(one));
    }

    public void testClashesWith3()
    {
        ICoreResource one = createMockResource("/WEB-INF/foobar/fun/Boy.jwc", false);
        ICoreResource two = createMockResource("/WEB-INF/foobar/fun/Girl.jwc", false);

        assertTrue(one.clashesWith(two));
        assertTrue(two.clashesWith(one));
    }

    public void testClashesWith3a()
    {
        ICoreResource one = createMockResource("/WEB-INF/foobar/fun", false);
        ICoreResource two = createMockResource("/WEB-INF/foobar/fun", false);

        assertTrue(one.clashesWith(two));
        assertTrue(two.clashesWith(one));
    }

    public void testClashesWith4()
    {
        ICoreResource one = createMockResource("/WEB-INF/Girl.jwc", false);
        ICoreResource two = createMockResource("/GOMER/foobar/fun/Girl.jwc", false);

        assertFalse(one.clashesWith(two));
        assertFalse(two.clashesWith(one));
    }

    public void testClashesWith5()
    {
        ICoreResource one = createMockResource("/", false);
        ICoreResource two = createMockResource("/GOMER/foobar/fun/Girl.jwc", false);

        assertTrue(one.clashesWith(two));
        assertTrue(two.clashesWith(one));
    }

    public void testClashesWith6()
    {
        ICoreResource one = createMockResource("/Girl.jwc", false);
        ICoreResource two = createMockResource("/GOMER/foobar/fun/Girl.jwc", false);

        assertTrue(one.clashesWith(two));
        assertTrue(two.clashesWith(one));
    }

    public void testClashesWith7()
    {
        ICoreResource one = createMockResource("C:/Girl.jwc", false);
        ICoreResource two = createMockResource("/GOMER/foobar/fun/Girl.jwc", false);

        assertFalse(one.clashesWith(two));
        assertFalse(two.clashesWith(one));
    }

    public void testClashesWith8()
    {
        ICoreResource one = createMockResource("/org/apache/tapestry/fun.library", true);
        ICoreResource two = createMockResource("/org/apache/tapestry/foo/foo.libary", true);

        assertTrue(one.clashesWith(two));
        assertTrue(two.clashesWith(one));
    }

    public void testClashesWith9()
    {
        ICoreResource one = createMockResource("/com/foo/mylib/fun.library", true);
        ICoreResource two = createMockResource("/com/foo/doglib/dog.library", true);

        assertFalse(one.clashesWith(two));
        assertFalse(two.clashesWith(one));
    }

    public void testNoNamespaceClash()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo.application" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        ICoreNamespace testSubject = namespaces.get(0);

        mockContainer.replayControls();

        IProblem[] problems = ClashDetector.checkNamspaceClash(
                testSubject,
                namespaces,
                "",
                CoreStatus.ERROR);

        mockContainer.verifyControls();

        assertEquals(0, problems.length);
    }

    public void testNoNamespaceClash2()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo/foo.application", "/bar/bar.libary" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(0, problems.size());

    }

    public void testNoNamespaceClash3()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo/foo.application", "/WEB-INF/bar/bar.libary", "/WEB-INF/cat/cat.libary" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(0, problems.size());
    }

    public void testNamespaceClash()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo/foo.application", "/WEB-INF/foo/foo.application" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(2, problems.size());
    }

    public void testNamespaceClash1()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo.application", "/WEB-INF/bar.application" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, true);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(2, problems.size());
    }

    public void testNamespaceClash3()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo/foo.application", "/WEB-INF/dog.application" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, true);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(2, problems.size());
    }

    public void testNamespaceClash4()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo/foo.application", "/bar.libary", "/dog/dog.libary", };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(4, problems.size());
    }

    public void testNamespaceClash5()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo/foo.application", "/bar.libary" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(2, problems.size());
    }

    public void testNamespaceClash6()
    {
        String[] ns = new String[]
        { "/WEB-INF/foo.application", "/bar.libary" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, false);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(2, problems.size());

    }

    public void testNamespaceClash7()
    {
        String[] ns = new String[]
        { "/org/apache/tapestry/Framework.library", "/org/apache/tapestry/contrib/Contrib.library",
                "/org/apache/tapestry/contrib/inspector/Inspector.library" };
        List<ICoreNamespace> namespaces = getNamespaces(ns, true);

        mockContainer.replayControls();

        ArrayList<IProblem> problems = new ArrayList<IProblem>();
        for (ICoreNamespace namespace : namespaces)
        {
            problems.addAll(Arrays.asList(ClashDetector.checkNamspaceClash(
                    namespace,
                    namespaces,
                    "",
                    CoreStatus.ERROR)));
        }

        mockContainer.verifyControls();

        assertEquals(6, problems.size());

    }

    public List<ICoreNamespace> getNamespaces(String[] paths, boolean isClasspath)
    {
        List<ICoreNamespace> result = new ArrayList<ICoreNamespace>();
        for (int i = 0; i < paths.length; i++)
        {
            result.add(createNamespace(paths[i], isClasspath));
        }
        return result;
    }

    private ICoreNamespace createNamespace(String resourcePath, boolean isClasspathResource)
    {
        MockControl nsControl = mockContainer.newControl(ICoreNamespace.class);
        ICoreNamespace ns = (ICoreNamespace) nsControl.getMock();

        nsControl.expectAndReturn(ns.getSpecificationLocation(), createMockResource(
                resourcePath,
                isClasspathResource), MockControl.ZERO_OR_MORE);
        return ns;
    }

    private ICoreResource createMockResource(String resourcePath, boolean isClasspathResource)
    {
        return new MockResource(resourcePath, isClasspathResource);
    }

    class MockResource extends ResourceImpl
    {

        boolean classpath;

        MockResource(String path, boolean isClasspath)
        {
            super(null, path);
            classpath = isClasspath;
        }

        @Override
        public boolean isClasspathResource()
        {
            return classpath;
        }
        
        public String toString()
        {
            return getPath();
        }

    }
}
