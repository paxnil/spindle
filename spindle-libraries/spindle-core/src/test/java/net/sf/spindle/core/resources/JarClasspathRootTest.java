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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import junit.framework.TestCase;
import net.sf.spindle.core.util.Files;

import org.apache.hivemind.Resource;

public class JarClasspathRootTest extends TestCase
{

    IResourceRoot root;

    @Override
    protected void setUp() throws Exception
    {
        root = getTapestryTestRoot();
    }
    
    private File getFile(String relativePath) {
        PathUtils jarPath = new PathUtils(System.getProperty("basedir")).append("testData").append(
                relativePath);
        File file = new File(jarPath.toOSString());
        assertTrue(file.exists());
        assertFalse(file.isDirectory());
        return file;
    }

    private IResourceRoot getTapestryTestRoot() throws IOException, URISyntaxException
    {
        ClasspathRoot root = new ClasspathRoot();
        root.addJar(getFile("tapestryTest.jar"));
        return root;
    }

    public void test() throws IOException
    {
        Resource framework = root.getRelativeResource("org/apache/tapestry/Framework.library");

        URL url = framework.getResourceURL();

        assertNotNull(url);

        InputStream input = url.openStream();

        String all = Files.readFileToString(input, null);

        // TODO remove
        System.out.println(all);
    }

    public void testLookup()
    {
        ICoreResource framework = (ICoreResource) root
                .getRelativeResource("org/apache/tapestry/Framework.library");
        assertTrue(framework.exists());

        IResourceAcceptor acceptor = new IResourceAcceptor()
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
        };
        framework.lookup(acceptor);

        assertEquals(5, acceptor.getResults().length);
    }

}
