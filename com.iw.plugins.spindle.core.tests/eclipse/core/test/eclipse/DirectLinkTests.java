package core.test.eclipse;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

import core.test.SuiteOfTestCases;

public class DirectLinkTests extends AbstractEclipseTestCase
{
    IProject project;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(DirectLinkTests.class);
    }

    public DirectLinkTests(String name)
    {
        super(name);
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        project = setUpProject("directlink");
    }

    public void testProject()
    {
        assertTrue(project != null && project.exists());
        assertTrue(logger.isEmpty());
    }
    
    public void testHasTapestryNature() throws CoreException {
        assertNotNull(project.getNature(TapestryCorePlugin.NATURE_ID));
    }
}
