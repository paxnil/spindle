package com.iw.plugins.spindle.core.eclipse.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.eclipse.jdt.core.IMethod;

import junit.framework.Test;

import core.test.SuiteOfTestCases;
import core.types.IJavaType;

public class TypeTests extends AbstractTypeTest
{
    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(TypeTests.class);
    }

    public TypeTests(String name)
    {
        super(name);
    }

    public void testObject() throws Exception
    {

        EclipseJavaType type = findType("java.lang.Object");

        assertNotNull(type);

        assertInfo(type, false);

        // force info creation
        IJavaType superClass = type.getSuperClass();

        assertNull(superClass);

        assertInfo(type, true);
    }

    public void testSuperClass() throws Exception
    {
        EclipseJavaType object = findType("java.lang.Object");
        EclipseJavaType baseClass = findType("tests.subclassing.BaseClass");

        TypeElement[] all = new TypeElement[]
        { object, baseClass };

        assertInfos(all, false);

        // force info creation
        IJavaType superClass = baseClass.getSuperClass();

        assertInfos(all, true);

        assertNotNull(superClass);

        assertEquals(object, superClass);

        assertHasChild(object, baseClass);

        assertTrue(object.getChildren().length == 1);
        assertTrue(baseClass.getChildren().length == 0);
    }

    public void testHierarchy() throws Exception
    {
        EclipseJavaType object = findType("java.lang.Object");
        EclipseJavaType baseClass = findType("tests.subclassing.BaseClass");
        EclipseJavaType subclass = findType("tests.subclassing.Subclass");

        TypeElement[] all = new TypeElement[]
        { object, baseClass, subclass };

        assertInfos(all, false);

        // force info creation
        IJavaType superClass = subclass.getSuperClass();

        assertInfos(all, true);

        assertEquals(baseClass, superClass);

        assertHasChild(object, baseClass);
        assertHasChild(baseClass, subclass);

        assertTrue(object.getChildren().length == 1);
        assertTrue(baseClass.getChildren().length == 1);
        assertTrue(subclass.getChildren().length == 0);
    }

    public void testCloseRoot() throws Exception
    {
        EclipseJavaType object = findType("java.lang.Object");
        EclipseJavaType baseClass = findType("tests.subclassing.BaseClass");
        EclipseJavaType subclass = findType("tests.subclassing.Subclass");

        TypeElement[] all = new TypeElement[]
        { object, baseClass, subclass };

        assertInfos(all, false);

        // force info creation
        IJavaType superClass = subclass.getSuperClass();

        assertInfos(all, true);

        object.close();

        assertInfos(all, false);
    }

    public void testCloseLeaf() throws Exception
    {
        EclipseJavaType object = findType("java.lang.Object");
        EclipseJavaType baseClass = findType("tests.subclassing.BaseClass");
        EclipseJavaType subclass = findType("tests.subclassing.Subclass");

        TypeElement[] parents = new TypeElement[]
        { object, baseClass };

        assertInfos(parents, false);
        assertInfo(subclass, false);

        // force info creation
        IJavaType superClass = subclass.getSuperClass();

        assertInfos(parents, true);
        assertInfo(subclass, true);

        subclass.close();

        assertInfos(parents, true);
        assertInfo(subclass, false);
    }

    public void testConstructors() throws Exception
    {
        EclipseJavaType baseClass = findType("tests.subclassing.BaseClass");

        assertInfo(baseClass, false);

        IMethod[] publicConstructors = baseClass.getConstructors();

        IMethod[] declaredConstructors = baseClass.getDeclaredConstructors();

        ClassLoader loader = getClassLoader();

        Class clazz = loader.loadClass("tests.subclassing.BaseClass");

        assertNotNull(clazz);

        Constructor[] binaryPublic = clazz.getConstructors();

        assertEquals(binaryPublic.length, publicConstructors.length);

        Constructor[] binaryDeclared = clazz.getDeclaredConstructors();

        assertEquals(binaryDeclared.length, declaredConstructors.length);
    }

    public void testDeclaredMethods() throws Exception
    {
        EclipseJavaType baseClass = findType("tests.subclassing.BaseClass");

        assertInfo(baseClass, false);

        IMethod[] declaredMethods = baseClass.getDeclaredMethods();

        ClassLoader loader = getClassLoader();

        Class clazz = loader.loadClass("tests.subclassing.BaseClass");

        assertNotNull(clazz);

        Method[] binaryDeclared = clazz.getDeclaredMethods();

        assertEquals(binaryDeclared.length, declaredMethods.length);
    }

    public void testMethods1() throws Exception
    {
        privateTestMethods("java.lang.Object");
        privateTestMethods("tests.subclassing.BaseClass");
        privateTestMethods("tests.subclassing.Subclass");
    }

    public void testMethods2() throws Exception
    {
        privateTestMethods("java.lang.Object");
        TypeModelManager.clearTypeCache();
        privateTestMethods("tests.subclassing.BaseClass");
        TypeModelManager.clearTypeCache();
        privateTestMethods("tests.subclassing.Subclass");

    }

    private void privateTestMethods(String type) throws Exception
    {
        EclipseJavaType srcType = findType(type);

        IMethod[] methods = srcType.getMethods();

        ClassLoader loader = getClassLoader();

        Class clazz = loader.loadClass(type);

        assertNotNull(clazz);

        Method[] binaryMethods = clazz.getMethods();

        assertEquals(type, binaryMethods.length, methods.length);
    }

}
