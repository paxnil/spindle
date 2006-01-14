package com.iw.plugins.spindle.core.eclipse.lang;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import core.test.eclipse.AbstractEclipsePluginTestCase;
import core.types.TypeModelException;

public class AbstractTypeTest extends AbstractEclipsePluginTestCase
{

    protected static String PROJECT_NAME = "TypeTests";

    protected IProject project;

    private ClassLoader loader;

    public AbstractTypeTest(String name)
    {
        super(name);
    }

    public void setUpSuite() throws Exception
    {
        setWorkspaceAutobuild(false);
        super.setUpSuite();
        deleteProject(PROJECT_NAME);
        project = setUpProject(PROJECT_NAME);
    }

    protected EclipseJavaType findType(String fqn) throws Exception
    {
        IJavaProject jproject = JavaCore.create(project);

        assertNotNull(jproject);

        IType javaType = jproject.findType(fqn);

        if (javaType == null)
            return null;

        return new EclipseJavaType(javaType);
    }

    protected void assertInfos(TypeElement[] elements, boolean exists)
    {
        for (int i = 0; i < elements.length; i++)
        {
            Object info = TypeModelManager.getTypeModelManager().getInfo(elements[i]);
            if (exists)
                assertNotNull(elements[i].getElementName(), info);
            else
                assertNull(elements[i].getElementName(), info);
        }
    }

    protected void assertInfo(TypeElement element, boolean exists)
    {
        assertInfos(new TypeElement[]
        { element }, exists);
    }

    protected void assertHasChild(TypeElement parent, TypeElement child) throws TypeModelException
    {
        assertHasChild(parent, child, true);
    }

    protected void assertHasChild(TypeElement parent, TypeElement child, boolean expectedResult)
            throws TypeModelException
    {
        boolean includesChild = parent.getInfo().includesChild(child);
        if (expectedResult)
            assertTrue(includesChild);
        else
            assertFalse(includesChild);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        TypeModelManager.clearTypeCache();
    }

    protected ClassLoader getClassLoader() throws Exception
    {
        if (loader == null)
        {
            if (!isWorkspaceAutobuild())
                incrementalBuild(project);

            assertNotNull(project);

            IPath path = project.getLocation();
            path = path.append("/bin");
            File bin = new File(path.toOSString());
            assertTrue(bin.exists() && bin.isDirectory());

            URL[] urls = new URL[]
            { bin.toURL() };
            loader = new URLClassLoader(urls, this.getClass().getClassLoader());
        }
        return loader;
    }

    protected ClassLoader recycleClassLoader() throws Exception
    {
        if (loader != null)
            loader = null;
        return getClassLoader();
    }    
}
