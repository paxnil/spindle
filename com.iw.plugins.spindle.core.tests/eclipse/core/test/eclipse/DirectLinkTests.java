package core.test.eclipse;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.eclipse.TapestryCorePlugin;

import core.test.SuiteOfTestCases;

public class DirectLinkTests extends AbstractEclipsePluginTestCase
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

    public void testProject() throws CoreException
    {
        assertTrue(project != null && project.exists());
        assertTrue(logger.isEmpty());
        assertProjectHasNature(project, TapestryCorePlugin.NATURE_ID);
        assertBuildSpecHasBuilder(project, TapestryCorePlugin.BUILDER_ID);
        assertProjectHasNoTapestryErrorMarkers(project);
    }   
}
