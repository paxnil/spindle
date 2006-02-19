package net.sf.spindle.core.resources;

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
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import junit.framework.Test;
import net.sf.spindle.core.SuiteOfTestCases;

public class JarClasspathRootTest extends AbstractTestRoot
{

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(JarClasspathRootTest.class);
    }
    
    public JarClasspathRootTest(String name)
    {
        super(name);
    }

    public void testBase() throws Exception
    {
        IResourceRoot root = getTestClasspathRoot(new String[]
        { JARS_TAPESTRY_TEST_JAR }, EMPTY, false);

        ICoreResource framework = (ICoreResource) root
                .getRelativeResource("org/apache/tapestry/Framework.library");

        assertTrue(framework.isClasspathResource() && framework.isBinaryResource());

        URL url = framework.getResourceURL();

        assertNotNull(url);

        InputStream input = url.openStream();

        assertNotNull(input);

    }

    public void testLookupOnejar() throws Exception
    {
        IResourceRoot root = getTestClasspathRoot(new String[]
        { JARS_TAPESTRY_TEST_JAR }, EMPTY, false);

        ICoreResource framework = (ICoreResource) root
                .getRelativeResource("org/apache/tapestry/Framework.library");
        assertTrue(framework.exists());

        assertTrue(framework.isClasspathResource() && framework.isBinaryResource());

        IResourceAcceptor acceptor = new Acceptor();

        framework.lookup(acceptor, LookupDepth.ZERO);

        assertEquals(5, acceptor.getResults().length);
    }

    public void testFindLibrary() throws Exception
    {

        IResourceRoot root = getTestClasspathRoot(new String[]
        { JARS_TAPESTRY_TEST_JAR }, new String[]
        { RESOURCES_ROOT1 }, false);

        ICoreResource framework = (ICoreResource) root
                .getRelativeResource("org/apache/tapestry/Framework.library");

        assertTrue(framework.exists());

        assertTrue(framework.isClasspathResource() && framework.isBinaryResource());

        assertTrue(framework.getUnderlier() instanceof URL);

        File jar = getFile(JARS_TAPESTRY_TEST_JAR);

        ((ParentRoot) root).removeChildRoot(jar);

        assertTrue(framework.isClasspathResource() && !framework.isBinaryResource());

        assertTrue(framework.exists());

        assertTrue(framework.getUnderlier() instanceof File);
    }

    public void testFindLibrary2() throws Exception
    {

        IResourceRoot root = getTestClasspathRoot(new String[]
        { JARS_TAPESTRY_TEST_JAR }, new String[]
        { RESOURCES_ROOT1 }, false);

        ICoreResource framework = (ICoreResource) root
                .getRelativeResource("org/apache/tapestry/Framework.library");

        assertTrue(framework.exists());

        assertTrue(framework.isClasspathResource() && framework.isBinaryResource());

        Object underlier = framework.getUnderlier();
        assertTrue(underlier instanceof URL);

        File folder = getFile(RESOURCES_ROOT1);

        ((ParentRoot) root).removeChildRoot(folder);

        assertTrue(framework.isClasspathResource() && framework.isBinaryResource());

        assertTrue(framework.exists());

        Object secondUnderlier = framework.getUnderlier();

        assertTrue(secondUnderlier instanceof URL);

        assertEquals(underlier, secondUnderlier);
    }

    public void testDefaultPackage() throws Exception
    {
        IResourceRoot root = getTestClasspathRoot(new String[]
        { JARS_FOO_JAR }, new String[]
        { RESOURCES_ROOT1 }, false);

        ICoreResource foo = (ICoreResource) root.getRelativeResource("foo.html");

        assertTrue(foo.exists());

        assertTrue(foo.isClasspathResource() && foo.isBinaryResource());

        assertTrue(foo.getUnderlier() instanceof URL);

        File jar = getFile(JARS_FOO_JAR);

        ((ParentRoot) root).removeChildRoot(jar);

        assertTrue(foo.exists());

        assertTrue(foo.isClasspathResource() && !foo.isBinaryResource());

        assertTrue(foo.getUnderlier() instanceof File);
    }

    public void testGetLocalization() throws Exception
    {
        IResourceRoot root = getTestClasspathRoot(new String[]
        { JARS_FOO_JAR }, new String[]
        { RESOURCES_ROOT1 }, false);

        ICoreResource foo = (ICoreResource) root.getRelativeResource("foo.html");

        assertTrue(foo.exists());

        assertTrue(foo.isClasspathResource() && foo.isBinaryResource());

        assertTrue(foo.getUnderlier() instanceof URL);

        ICoreResource localized = (ICoreResource) foo.getLocalization(Locale.CANADA);

        assertNotNull(localized);

        assertTrue(localized.exists());

        assertEquals("foo_en_CA.html", localized.getName());

        assertTrue(localized.isClasspathResource() && localized.isBinaryResource());
    }
    
    public void testGetLocalization2() throws Exception
    {
        ClasspathRoot root = getTestClasspathRoot();
        
        addSourceFolder(RESOURCES_ROOT2, root);
        addJar(JARS_FOO_JAR, root);

        ICoreResource foo = (ICoreResource) root.getRelativeResource("foo.html");

        assertTrue(foo.exists());

        assertTrue(foo.isClasspathResource() && !foo.isBinaryResource());

        assertTrue(foo.getUnderlier() instanceof File);

        ICoreResource localized = (ICoreResource) foo.getLocalization(Locale.CANADA);

        assertNotNull(localized);

        assertTrue(localized.exists());

        assertEquals("foo_en_CA.html", localized.getName());

        assertTrue(localized.isClasspathResource() && !localized.isBinaryResource());
        
        assertTrue(localized.getUnderlier() instanceof File);
    }
    
    public void testGetLocalization3() throws Exception
    {
        ClasspathRoot root = getTestClasspathRoot();
        
        addSourceFolder(RESOURCES_ROOT1, root);
        addJar(JARS_FOO_JAR, root);

        ICoreResource foo = (ICoreResource) root.getRelativeResource("foo.html");

        assertTrue(foo.exists());

        assertTrue(foo.isClasspathResource() && !foo.isBinaryResource());

        assertTrue(foo.getUnderlier() instanceof File);

        ICoreResource localized = (ICoreResource) foo.getLocalization(Locale.CANADA);

        assertNotNull(localized);

        assertTrue(localized.exists());

        assertEquals("foo_en_CA.html", localized.getName());

        assertTrue(localized.isClasspathResource() && localized.isBinaryResource());
        
        assertTrue(localized.getUnderlier() instanceof URL);
    }

    class Acceptor implements IResourceAcceptor
    {
        private ArrayList<ICoreResource> results = new ArrayList<ICoreResource>();

        public boolean accept(ICoreResource location)
        {
            results.add(location);
            return true;
        }

        public ICoreResource[] getResults()
        {
            return results.toArray(new ICoreResource[] {});
        }
    }
}
