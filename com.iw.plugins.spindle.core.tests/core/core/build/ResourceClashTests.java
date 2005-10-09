package core.build;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.iw.plugins.spindle.core.resources.eclipse.ClasspathRoot;
import com.iw.plugins.spindle.core.resources.eclipse.ContextRoot;

import core.resources.ICoreResource;
import core.test.SuiteOfTestCases;
import core.test.eclipse.AbstractEclipsePluginTestCase;

public class ResourceClashTests extends AbstractEclipsePluginTestCase
{

    IProject project;
    
    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ResourceClashTests.class);
    }

    public ResourceClashTests(String name)
    {
        super(name);

    }
    
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        project = setUpProject("Clash");
    }

    public void tearDownSuite() throws Exception
    {
        super.tearDownSuite();
        deleteProject("Clash");
    }

    public void testContextClashes() throws Exception
    {
        ContextRoot rootctx = new ContextRoot(project.getFolder(new Path("context/WEB-INF")));
        ICoreResource root = (ICoreResource) rootctx.getRelativeResource("/");
        doTestClashes(root);
    }

    public void testClasspathClashes() throws Exception
    {
        IJavaProject jproject = JavaCore.create(project);
        ICoreResource root = (ICoreResource) new ClasspathRoot(jproject).getRelativeResource("/");
        doTestClashes(root);
    }

    private void doTestClashes(ICoreResource rootLocation)
    {
        ICoreResource test1 = getResource(rootLocation, "/Test1");
        ICoreResource test2 = getResource(rootLocation, "/Test2");
        ICoreResource test3 = getResource(rootLocation, "/a/Test3");
        ICoreResource test4 = getResource(rootLocation, "/a/b/Test4");
        ICoreResource test5 = getResource(rootLocation, "/a/b/d/Test5");
        ICoreResource test6 = getResource(rootLocation, "/a/b/d/e/Test6");
        ICoreResource test7 = getResource(rootLocation, "/a/c/Test7");
        ICoreResource test8 = getResource(rootLocation, "/a/b/d/e/Test8");
        ICoreResource test9 = getResource(rootLocation, "/a/c/f/Test9");
        ICoreResource test10 = getResource(rootLocation, "/z/Test10");

        ICoreResource[][] clashExpected = new ICoreResource[][]
        {
        { test1, test2 },
        { test1, test3 },
        { test1, test4 },
        { test1, test5 },
        { test1, test6 },
        { test1, test7 },
        { test1, test8 },
        { test1, test9 },
        { test1, test10 },
        { test4, test5 },
        { test6, test8 },
        { test7, test9 },
        { test3, test4 },
        { test3, test5 },
        { test3, test6 },
        { test3, test7 },
        { test3, test8 },
        { test3, test9 }};
        
        for (int i = 0; i < clashExpected.length; i++)
        {
            doTestClash(clashExpected[i], true);
        }

        ICoreResource[][] okExpected = new ICoreResource[][]
        {
        { test4, test7 },
        { test8, test9 },
        { test10, test3 },
        { test10, test4 },
        { test10, test5 },
        { test10, test6 },
        { test10, test7 },
        { test10, test8 },
        { test10, test9 } };
        
        for (int i = 0; i < okExpected.length; i++)
        {
            doTestClash(okExpected[i], false);
        }
        
        ICoreResource [][] specialClashExpected = new ICoreResource [][] {
                {test1, getResource(rootLocation, "/")},
                {getResource(rootLocation, "/"), getResource(rootLocation, "/")},
                {test1, getResource(rootLocation, "/a/b/d/e/")}
        };
        
        for (int i = 0; i < specialClashExpected.length; i++)
        {
            doTestClash(specialClashExpected[i], true);
        }        
    }

    private void doTestClash(ICoreResource[] pair, boolean clashExpected)
    {
        boolean[] results = new boolean[2];
        results[0] = pair[0].clashesWith(pair[1]);
        results[1] = pair[1].clashesWith(pair[0]);

        boolean same = results[0] == results[1];
        if (!same || (results[0] != clashExpected))
        {
            StringBuffer report = new StringBuffer();
            String name1 = pair[0].getName();
            String name2 = pair[1].getName();
            report.append(clashExpected ? "CLASH EXPECTED::" : "OK EXPECTED::'");
            report.append(name1);
            report.append(results[0] ? "' clashed with '" : "' ok with '");
            report.append(name2);
            report.append(same ? "' and '" : "' but '");
            report.append(name2);
            report.append(results[1] ? "' clashed with '" : "' ok with '");
            report.append(name1);
            fail(report.toString());
        }
    }

    private ICoreResource getResource(ICoreResource root, String path)
    {
        ICoreResource result = (ICoreResource) root.getRelativeResource(path);
        assertNotNull(result);
        assertTrue(result.exists());
        return result;
    }    
}
