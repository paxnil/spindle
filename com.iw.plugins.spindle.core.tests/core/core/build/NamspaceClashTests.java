package core.build;

import junit.framework.Test;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.eclipse.TapestryProject;

import core.ITapestryProject;
import core.test.SuiteOfTestCases;
import core.test.eclipse.AbstractEclipsePluginTestCase;

public class NamspaceClashTests extends AbstractEclipsePluginTestCase
{
    
    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(NamspaceClashTests.class);
    }

    
    IProject project;

    public NamspaceClashTests(String name)
    {
        super(name);
       
    }
    
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        project = setUpProject("NamespaceClashTests");
    }
    
    public void tearDownSuite() throws Exception
    {
        super.tearDownSuite();
        deleteProject("NamespaceClashTests");
    }
    
    
    public void testFrameworkNoNamespaceClash() throws Exception {
        activateContext("context/frameworkNoClash", false);
        assertProjectHasNoTapestryErrorMarkers(project);
        assertTrue(getInterceptedLogEvents().isEmpty());
    }
    
    public void testFrameworkNoNamespaceClash1() throws Exception {
        activateContext("context/frameworkNoClash1", false);
        assertProjectHasNoTapestryErrorMarkers(project);
        assertTrue(getInterceptedLogEvents().isEmpty());
    }
    
    private void activateContext(String contextPath, boolean validateWebXML) throws Exception {
        
        IFolder context = project.getFolder(new Path(contextPath));
        assertNotNull(context);
        assertTrue(context.exists());
        
        TapestryProject tproject = (TapestryProject) project.getAdapter(ITapestryProject.class);
        assertNotNull(tproject);
        
        tproject.setMetaDataLoaded(true);
        tproject.setWebContextFolder(context);
        tproject.setValidatingWebXML(validateWebXML);
        
        project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
        waitForEclipseBuilder();        
    }


}
