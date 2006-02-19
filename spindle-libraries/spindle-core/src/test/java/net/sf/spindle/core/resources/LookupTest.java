package net.sf.spindle.core.resources;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import net.sf.spindle.core.SuiteOfTestCases;

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
public class LookupTest extends AbstractTestRoot
{

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(LookupTest.class);
    }

    public LookupTest(String name)
    {
        super(name);
    }

    public void testLookupZeroJars() throws Exception
    {

        doTestLookupJars(LookupDepth.ZERO, 24);
    }

    public void testLookupInifinteJars() throws Exception
    {

        doTestLookupJars(LookupDepth.INFINITE, 29);
    }

    private void doTestLookupJars(LookupDepth depth, int expectedHits)
    {
        ClasspathRoot root = getTestClasspathRoot();

        // add the same jar 4 times
        for (int i = 0; i < 4; i++)
        {
            addJar(JARS_TAPESTRY_TEST_JAR, root);
        }

        // get a resource on package org.apache.tapestry.form
        ICoreResource resource = (ICoreResource) root
                .getRelativeResource("/org/apache/tapestry/form/");

        assertTrue(resource.isBinaryResource());
        assertTrue(resource.isClasspathResource());
        assertTrue(resource.exists());

        List<ICoreResource> found = doLookup(resource, depth);

        assertEquals(expectedHits, found.size());
    }

    public void testLookupZeroFolders() throws Exception
    {
        doTestLookupFolders(LookupDepth.ZERO, 1);
    }

    public void testLookupInfinteFolders() throws Exception
    {
        doTestLookupFolders(LookupDepth.INFINITE, 2);
    }

    private void doTestLookupFolders(LookupDepth depth, int expectedSize)
    {
        ClasspathRoot root = getTestClasspathRoot();

        // add the same folder 4 times
        for (int i = 0; i < 4; i++)
        {
            addSourceFolder(RESOURCES_ROOT1, root);
        }

        // get a resource on package org.apache.tapestry.form
        ICoreResource resource = (ICoreResource) root.getRelativeResource("/");

        assertTrue(!resource.isBinaryResource());
        assertTrue(resource.isClasspathResource());
        assertTrue(resource.exists());

        List<ICoreResource> found = doLookup(resource, depth);

        assertEquals(expectedSize, found.size());
    }

    public void testLookupZeroJarThenFolder() throws Exception
    {
        doTestLookupJarThenFolder(LookupDepth.ZERO, 5);
    }
    
    public void testLookupInfiniteJarThenFolder() throws Exception
    {
        doTestLookupJarThenFolder(LookupDepth.INFINITE, 150 /* tap jar - I counted - really! */);
    }

    private void doTestLookupJarThenFolder(LookupDepth depth, int expectedCount)
    {
        ClasspathRoot root = getTestClasspathRoot();

        addJar(JARS_TAPESTRY_TEST_JAR, root);
        addSourceFolder(RESOURCES_ROOT1, root);

        // get a resource on package org.apache.tapestry.form
        ICoreResource resource = (ICoreResource) root
                .getRelativeResource("/org/apache/tapestry/Framework.library");

        assertTrue(resource.isBinaryResource());
        assertTrue(resource.isClasspathResource());
        assertTrue(resource.exists());

        
        List<ICoreResource> found = doLookup(resource, depth);

        
        assertEquals(expectedCount, found.size());

        for (ICoreResource res : found)
        {
            assertTrue(res.isBinaryResource());
            assertTrue(res.isClasspathResource());
            assertTrue(res.exists());
        }
    }

    public void testLookupZeroFolderThenJar() throws Exception
    {
        ClasspathRoot root = getTestClasspathRoot();

        addSourceFolder(RESOURCES_ROOT1, root);
        addJar(JARS_TAPESTRY_TEST_JAR, root);

        // get a resource on package org.apache.tapestry.form
        ICoreResource resource = (ICoreResource) root
                .getRelativeResource("/org/apache/tapestry/Framework.library");

        assertTrue(!resource.isBinaryResource());
        assertTrue(resource.isClasspathResource());
        assertTrue(resource.exists());

        List<ICoreResource> found = doLookup(resource, LookupDepth.ZERO);

        assertEquals(5, found.size());

        for (ICoreResource res : found)
        {
            assertTrue(res.isClasspathResource());
            assertTrue(res.exists());
            if ("/org/apache/tapestry/Framework.library".equals(res.getPath()))
            {
                assertFalse(res.isBinaryResource());
            }
            else
            {
                assertTrue(res.isBinaryResource());
            }
        }
    }    

    private List<ICoreResource> doLookup(ICoreResource resource, LookupDepth depth)
    {
        final ArrayList<ICoreResource> found = new ArrayList<ICoreResource>();

        IResourceAcceptor acceptor = new IResourceAcceptor()
        {

            public boolean accept(ICoreResource location)
            {
                found.add(location);
                return true;
            }

            public ICoreResource[] getResults()
            {
                // NEVER CALLED
                fail("should never get here");
                return null;
            }
        };

        resource.lookup(acceptor, depth);
        if (!resource.isFolder())
            assertTrue(found.contains(resource));
        return found;
    }
}
