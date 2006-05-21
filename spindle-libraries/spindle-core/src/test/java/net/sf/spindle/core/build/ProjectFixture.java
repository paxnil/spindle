package net.sf.spindle.core.build;

import java.io.File;
import java.io.FileFilter;

import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.resources.ClasspathRoot;
import net.sf.spindle.core.resources.ContextRoot;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.types.IJavaTypeFinder;

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
public class ProjectFixture implements ITapestryProject
{
    private File jarDirectory;

    private File[] sourceClasspathRootDirs;

    private File[] contextRoots;

    // done this way so I can mock type lookup!
    private IJavaTypeFinder typeFinder;

    private boolean validatingWebXML = false;

    private IResourceRoot webContext;

    private IResourceRoot classpathRoot;

    public ProjectFixture()
    {
        this(null, null, null, new NullTypeFinder());
    }

    public ProjectFixture(File jarDirectory, File[] sourceClasspathRootDirs, File[] contextRoots,
            IJavaTypeFinder typeFinder)
    {
        super();
        this.jarDirectory = jarDirectory;
        this.sourceClasspathRootDirs = sourceClasspathRootDirs;
        this.contextRoots = contextRoots;
        this.typeFinder = typeFinder;
    }

    public IResourceRoot getClasspathRoot()
    {
        if (classpathRoot == null)
            classpathRoot = createClasspathRoot();
        return classpathRoot;
    }

    private IResourceRoot createClasspathRoot()
    {
        ClasspathRoot root = new ClasspathRoot();
        // add jars
        if (jarDirectory != null)
        {
            File[] jars = jarDirectory.listFiles(new FileFilter()
            {

                public boolean accept(File file)
                {
                    return !file.isDirectory() && file.getName().endsWith(".jar");
                }
            });
            for (File file : jars)
            {
                root.addJar(file);
            }
        }
        // add src folders
        if (sourceClasspathRootDirs != null)
        {
            for (File srcFolder : sourceClasspathRootDirs)
            {
                root.addFolder(srcFolder);
            }
        }
        return root;
    }

    public IResourceRoot getWebContextLocation()
    {
        if (webContext == null)
            webContext = createWebContext();
        return webContext;
    }

    private IResourceRoot createWebContext()
    {
        ContextRoot result = null;
        if (contextRoots != null)
        {
            // ctx root should remain null if there are no root folders!
            result = new ContextRoot();
            for (File contextRootFolder : contextRoots)
            {
                result.addFolder(contextRootFolder);
            }
        }
        return result;
    }

    public boolean isValidatingWebXML()
    {
        return validatingWebXML;
    }

    public void setValidatingWebXML(boolean validating)
    {
        validatingWebXML = validating;
    }

    public IJavaType findType(String fullyQualifiedName)
    {
        return typeFinder.findType(fullyQualifiedName);
    }

    public boolean isCachingJavaTypes()
    {
        return typeFinder.isCachingJavaTypes();
    }

    static class NullTypeFinder implements IJavaTypeFinder
    {

        public IJavaType findType(String fullyQualifiedName)
        {
            return new NullType(fullyQualifiedName);
        }

        public boolean isCachingJavaTypes()
        {
            return false;
        }
    }

    static class NullType implements IJavaType
    {
        String fqn;

        public NullType(String fqn)
        {
            super();
            this.fqn = fqn;
        }

        public boolean exists()
        {
            return false;
        }

        public String getFullyQualifiedName()
        {
            return fqn;
        }

        public Object getUnderlier()
        {
            return null;
        }

        public boolean isAnnotation()
        {
            return false;
        }

        public boolean isBinary()
        {
            return false;
        }

        public boolean isInterface()
        {
            return false;
        }

        public boolean isSuperTypeOf(IJavaType candidate)
        {
            return false;
        }
    }

}
