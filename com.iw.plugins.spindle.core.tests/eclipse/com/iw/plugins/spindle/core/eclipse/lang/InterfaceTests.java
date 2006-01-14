package com.iw.plugins.spindle.core.eclipse.lang;

import junit.framework.Test;
import core.test.SuiteOfTestCases;
import core.types.IJavaType;

public class InterfaceTests extends AbstractTypeTest
{
    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(InterfaceTests.class);
    }

    public InterfaceTests(String name)
    {
        super(name);
    }

    private EclipseJavaType Object;

    private EclipseJavaType One;

    private EclipseJavaType Two;

    private EclipseJavaType Three;

    private EclipseJavaType BaseClass;

    private EclipseJavaType Subclass;

    public void setUpSuite() throws Exception
    {
        // TODO Auto-generated method stub
        super.setUpSuite();
        Object = findType("java.lang.Object");
        One = findType("tests.declaredIface.One");
        Two = findType("tests.declaredIface.Two");
        Three = findType("tests.declaredIface.Three");
        BaseClass = findType("tests.declaredIface.BaseClass");
        Subclass = findType("tests.declaredIface.Subclass");
    }

    public void testInterface() throws Exception
    {
        assertNotNull(One);

        assertTrue(One.isInterface());

        assertInfo(One, false);

        IJavaType type = One.getSuperClass();
        IJavaType[] ifaces = One.getInterfaces();

        assertInfo(One, true);

        assertNull(type);
        assertEquals(0, ifaces.length);
    }

    public void testIntefaceHierarchy() throws Exception
    {
        TypeElement[] all = new TypeElement[]
        { One, Two, Three };
        TypeElement[] oneTwo = new TypeElement[]
        { One, Two };

        assertInfos(all, false);

        // accessing Two should force info for One also, but not Three
        IJavaType type = Two.getSuperClass();
        IJavaType[] ifaces = Two.getInterfaces();

        assertInfos(oneTwo, true);
        assertInfo(Three, false);

        assertNull(type); // interfaces have no superclass!
        assertTrue(ifaces.length == 1);
        assertTrue(ifaces[0].equals(One));
        assertHasChild(One, Two);

        assertTrue(One.getChildren().length == 1);
        assertTrue(Two.getChildren().length == 0);

        // accessing Three should force info for Three, but One and two are already loaded.
        type = Three.getSuperClass();
        ifaces = Three.getInterfaces();

        assertInfos(all, true);

        assertNull(type); // interfaces have no superclass!
        assertTrue(ifaces.length == 1);
        assertTrue(ifaces[0].equals(One));

        assertHasChild(One, Two);
        assertHasChild(One, Three);

        assertTrue(One.getChildren().length == 2);
        assertTrue(Two.getChildren().length == 0);
        assertTrue(Three.getChildren().length == 0);
    }

    public void testCloseInterface() throws Exception
    {
        TypeElement[] all = new TypeElement[]
        { One, Two, Three };

        assertInfos(all, false);

        // force all info creation
        Two.getSuperClass();
        Three.getSuperClass();

        assertInfos(all, true);

        One.close();

        assertInfos(all, false);
    }

    public void testCloseInterface2() throws Exception
    {
        TypeElement[] all = new TypeElement[]
        { One, Two, Three };

        assertInfos(all, false);

        IJavaType type;
        // force all info creation
        type = Two.getSuperClass();

        TypeElement[] oneTwo = new TypeElement[]
        { One, Two };

        assertInfos(oneTwo, true);
        assertInfo(Three, false);

        One.close();

        assertInfos(all, false);
    }

    public void testBaseClass() throws Exception
    {
        TypeElement[] all = new TypeElement[]
        { Object, One, BaseClass };

        assertInfos(all, false);

        IJavaType superclass = BaseClass.getSuperClass();
        IJavaType[] ifaces = BaseClass.getInterfaces();

        assertTrue(superclass.equals(Object));
        assertTrue(ifaces.length == 1);
        assertTrue(ifaces[0].equals(One));

        assertInfos(all, true);

        assertHasChild(Object, BaseClass);
        assertHasChild(One, BaseClass);
        assertHasChild(Object, One, false);
    }

    public void testCloseInterfaceWithImplementor() throws Exception
    {
        TypeElement[] all = new TypeElement[]
        { Object, One, BaseClass };

        assertInfos(all, false);

        IJavaType superclass = BaseClass.getSuperClass();
        IJavaType[] ifaces = BaseClass.getInterfaces();

        assertTrue(superclass.equals(Object));
        assertTrue(ifaces.length == 1);
        assertTrue(ifaces[0].equals(One));

        assertInfos(all, true);

        One.close();

        assertInfo(Object, true);
        assertInfo(One, false);
        assertInfo(BaseClass, false);

        assertHasChild(Object, BaseClass, false);
    }

    public void testSubclass() throws Exception
    {
        TypeElement[] all =
        { Object, One, Two, Three, BaseClass, Subclass };

        assertInfos(all, false);

        IJavaType superclass = Subclass.getSuperClass();
        IJavaType[] ifaces = Subclass.getInterfaces();
        
        assertInfos(all, true);

    }
}
