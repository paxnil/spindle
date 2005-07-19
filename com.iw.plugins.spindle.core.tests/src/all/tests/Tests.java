package all.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public abstract class Tests extends TestCase
{   
    public Tests(String name)
    {
        super(name);
    }   
    
    protected static Test createSuite(Class suiteClass, Class [] allTestClasses) {
        TestSuite suite = new TestSuite(suiteClass.getName());
        

        for (int i = 0, length = allTestClasses.length; i < length; i++)
        {
            Class clazz = allTestClasses[i];
            Method suiteMethod;
            try
            {
                suiteMethod = clazz.getDeclaredMethod("suite", new Class[0]);
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
                continue;
            }
            Object test;
            try
            {
                test = suiteMethod.invoke(null, new Object[0]);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
                continue;
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
                continue;
            }
            suite.addTest((Test) test);
        }

        return suite;
    }

    public static Test suite()
    {
        throw new Error("override in subclass!");
//        return Tests.createSuite(Test.class, getAllTestClasses());       
    }

}
