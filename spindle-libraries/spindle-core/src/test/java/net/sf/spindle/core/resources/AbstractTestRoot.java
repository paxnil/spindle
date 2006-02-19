package net.sf.spindle.core.resources;

import java.io.File;

import net.sf.spindle.core.AbstractTestCase;

import junit.framework.TestCase;

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
public abstract class AbstractTestRoot extends AbstractTestCase
{

    protected static final String RESOURCES_ROOT2 = "resources/root2";

    protected static final String RESOURCES_ROOT1 = "resources/root1";

    protected static final String JARS_TAPESTRY_TEST_JAR = "jars/tapestryTest.jar";

    protected static final String JARS_FOO_JAR = "jars/foo.jar";

    protected static final String[] EMPTY = new String[] {};

    public AbstractTestRoot(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }

    protected File getFile(String relativePath)
    {
        PathUtils jarPath = new PathUtils(System.getProperty("basedir")).append("testData").append(
                relativePath);
        File file = new File(jarPath.toOSString());
        assertTrue(file.exists());
        return file;
    }

    protected ContextRoot getTestContextRoot() throws Exception
    {
        return new ContextRoot();
    }

    protected ContextRoot getTestContextRoot(String[] folders) throws Exception
    {
        ContextRoot root = getTestContextRoot();

        addSourceFolders(folders, root);

        return root;
    }

    protected ClasspathRoot getTestClasspathRoot() 
    {
        return new ClasspathRoot();
    }

    protected ClasspathRoot getTestClasspathRoot(String[] jars, String[] folders, boolean flipOrder)
            throws Exception
    {
        ClasspathRoot root = getTestClasspathRoot();
        if (flipOrder)
        {
            addSourceFolders(folders, root);
            addJars(jars, root);
        }
        else
        {
            addJars(jars, root);
            addSourceFolders(folders, root);
        }
        return root;
    }

    protected void addSourceFolders(String[] folders, ParentRoot root)
    {
        for (int i = 0; i < folders.length; i++)
        {
            addSourceFolder(folders[i], root);
        }
    }

    protected void addSourceFolder(String folder, ParentRoot root)
    {
        root.addFolder(getFile(folder));
    }

    protected void addJars(String[] jars, ClasspathRoot root)
    {
        for (int i = 0; i < jars.length; i++)
        {
            String jar = jars[i];
            addJar(jar, root);
        }
    }

    protected void addJar(String jar, ClasspathRoot root)
    {
        root.addJar(getFile(jar));
    }

}
