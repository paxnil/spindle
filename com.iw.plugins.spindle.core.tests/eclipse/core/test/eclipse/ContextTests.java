package core.test.eclipse;

import java.io.InputStream;
import java.util.ArrayList;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.IResourceAcceptor;
import com.iw.plugins.spindle.core.resources.eclipse.ContextResource;
import com.iw.plugins.spindle.core.resources.eclipse.ContextRoot;

import core.test.SuiteOfTestCases;

public class ContextTests extends AbstractEclipseTestCase
{
    IProject project;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ContextTests.class);
    }

    public ContextTests(String name)
    {
        super(name);
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        project = setUpProject("ContextResourceTests");
    }

    public void testProject()
    {
        assertTrue(project != null && project.exists());
        assertTrue(logger.isEmpty());
    }

    public void testFindRelativeContainerExists()
    {
        ContextRoot root = createContextRoot("one/context");

        ContextResource relative = (ContextResource) root.getRelativeResource("WEB-INF/");

        assertTrue(relative.getContainer().exists());
        assertNull(relative.getStorage());
        assertNull(relative.getContents());

        relative = (ContextResource) root.getRelativeResource("WEB-INF");

        assertFalse(relative.exists());
        assertTrue(relative.getContainer().exists());
        assertNull(relative.getStorage());
        assertNull(relative.getContents());

        relative = (ContextResource) root.getRelativeResource("../..");

        assertFalse(relative.exists());
        assertNull(relative.getContainer());
        assertNull(relative.getStorage());
        assertNull(relative.getContents());

        assertTrue(logger.isEmpty());
    }

    public void testFindRelativeResourceExists() throws Exception
    {
        ContextRoot root = createContextRoot("one/context");

        ContextResource relative = (ContextResource) root.getRelativeResource("WEB-INF/web.xml");

        assertTrue(relative.exists());
        InputStream stream = relative.getContents();
        assertNotNull(stream);

        String contents = read(stream, null);

        assertEquals("web.xml empty", contents);

        assertTrue(logger.isEmpty());
    }

    class TestAcceptor implements IResourceAcceptor
    {
        ArrayList collect = new ArrayList();

        public boolean accept(ICoreResource location)
        {
            collect.add(location);
            return true;
        }

        public ICoreResource[] getResults()
        {
            return (ICoreResource[]) collect.toArray(new ICoreResource[collect.size()]);
        }
    }

    public void testRootLookup()
    {
        ContextRoot root = createContextRoot("one/context");
        IResourceAcceptor requestor = new TestAcceptor();
        root.lookup(requestor);

        ICoreResource[] result = requestor.getResults();
        assertTrue(result.length == 1);
        assertEquals("B.txt", result[0].getName());

        assertTrue(logger.isEmpty());
    }
    
    public void testGetContainer() {
        ContextRoot root = createContextRoot("one/context");
        ContextResource resource = (ContextResource) root.getRelativeResource("WEB-INF/");
        
        IContainer container = resource.getContainer();
        assertNotNull(container);
        assertEquals("/ContextResourceTests/one/context/WEB-INF", container.getFullPath().toString());
    }

    public void testRelativeFolderLookup()
    {
        ContextRoot root = createContextRoot("one/context");
        ContextResource resource = (ContextResource) root.getRelativeResource("WEB-INF/");

        IResourceAcceptor requestor = new TestAcceptor();
        resource.lookup(requestor);

        ICoreResource[] result = requestor.getResults();
        assertTrue(result.length == 1);
        assertEquals("web.xml", result[0].getName());
    }

    public void tearDownSuite() throws Exception
    {
        super.tearDownSuite();
        deleteProject("ContextResourceTests");
    }

    private ContextRoot createContextRoot(String path)
    {
        IFolder folder = project.getFolder(new Path(path));
        if (folder == null || !folder.exists())
            throw new AssertionFailedError((project.getFullPath().append(path).toString())
                    + "does not exist");
        return new ContextRoot(folder);
    }

}
