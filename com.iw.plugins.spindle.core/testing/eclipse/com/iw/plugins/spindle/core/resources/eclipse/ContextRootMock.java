package com.iw.plugins.spindle.core.resources.eclipse;

import java.util.Arrays;

import junit.framework.Test;

import org.apache.hivemind.Resource;
import org.easymock.MockControl;
import org.easymock.internal.Range;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import core.test.AbstractTestCase;
import core.test.SuiteOfTestCases;

public class ContextRootMock extends AbstractTestCase
{
    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ContextRootMock.class);
    }

    public ContextRootMock(String name)
    {
        super(name);
    }

    /**
     * Data for each test is an array consisting of:
     * <ul>
     * <li>path for ContextRoot's IContainer, "a/b/c"</li>
     * <li>path for a IResource, "/a/b/c/d/Home.html"</li>
     * <li>path of the IResource, relative to the ContextRoot</li>
     * <li>flag indicating if ContextRoot.internalGetContainer() should succeed for fail, "yes" or
     * "no"</li>
     * </ul>
     */
    String[][] data = new String[][]
        { new String[]
            { "a/b/c", "/a/b/c/d/Home.html", "/d/Home.html", "yes" }, new String[]
            { "/a/b/c", "/a/b/z/d/Home.html", "../z/d/Home.html", "no" }, new String[]
            { "/a/b/c", "/a/b/c/Home.html", "/Home.html", "yes" }, new String[]
            { "/a", "/Home.html", "../Home.html", "no" } };

    public void testFindRelativePath()
    {
        for (int i = 0; i < data.length; i++)
        {
            MockContainer mocker = new MockContainer(mockContainer);
            String[] testData = data[i];

            IContainer rootContainer = newContainerMock(mocker, testData[0], 1);

            IResource resource = newResourceMock(mocker, testData[1], 1);

            mocker.replayControls();

            ContextRoot testRoot = new ContextRoot(rootContainer);

            String relativePath = testRoot.findRelativePath(resource);

            assertEquals(Arrays.asList(testData).toString(), testData[2], relativePath);
            assertTrue(logger.isEmpty());

            mocker.verifyControls();

            mocker.resetControls();
        }
    }

    /*
     * Class under test for IContainer getContainer(ContextResource)
     */
    public void testGetContainerContextResource()
    {
        for (int i = 0; i < data.length; i++)
        {
            MockContainer mocker = new MockContainer(mockContainer);
            String[] testData = data[i];

            boolean expectSuccess = "yes".equalsIgnoreCase(testData[3]);

            IContainer rootContainer = newContainerMock(mocker, testData[0], 2);

            IResource resource = newResourceMock(mocker, testData[1], 1);

            MockControl wkspMock = mocker.newControl(IWorkspaceRoot.class);
            IWorkspaceRoot root = (IWorkspaceRoot) wkspMock.getMock();
            if (expectSuccess)
            {
                // if we expect success - then a container will be asked for
                // otherwise any call on root would be an error!
                root.getFolder(new Path("bogus")); // whatever - we don't care
                wkspMock.setDefaultReturnValue(mockContainer.newMock(IFolder.class));
            }

            mocker.replayControls();

            ContextRoot testRoot = new ContextRoot(rootContainer);

            ContextResource location = new ContextResource(testRoot, resource);

            assertEquals(testData[2], location.getPath());

            // must call internal method as this test can run outside of Eclipse runtime workbench
            boolean containerIsNull = testRoot.internalGetContainer(location.getPath(), root) == null;
            assertTrue(Arrays.asList(testData).toString(), !(expectSuccess == containerIsNull));

            mocker.verifyControls();
            assertTrue(logger.isEmpty());

            mocker.resetControls();
        }
    }

    public void testGetRelativeResource()
    {
        IContainer rootContainer = (IContainer) mockContainer.newMock(IContainer.class);

        mockContainer.replayControls();

        ContextRoot root = new ContextRoot(rootContainer);

        ContextResource r1 = new ContextResource(root, "/foo/bar/baz.html");
        Resource r2 = r1.getRelativeResource("baz.gif");

        assertEquals("/foo/bar/baz.gif", r2.getPath());

        mockContainer.verifyControls();
    }

    public void testGetRelativeResource1()
    {
        IContainer rootContainer = (IContainer) mockContainer.newMock(IContainer.class);

        mockContainer.replayControls();

        ContextRoot root = new ContextRoot(rootContainer);

        ContextResource r1 = new ContextResource(root, "/foo/bar/baz.html");
        Resource r2 = r1.getRelativeResource("/foo/bar/baz.html");

        assertSame(r1, r2);

        mockContainer.verifyControls();
    }

    public void testGetRelativeFolder()
    {
        IContainer rootContainer = newContainerMock(
                mockContainer,
                "/root",
                MockControl.ZERO_OR_MORE);

        IWorkspaceRoot wkspRoot = newWorkspaceMock(mockContainer, new String[][]
            { new String[]
                { "/root/foo/bar/WEB-INF/", "exists" } });

        mockContainer.replayControls();

        ContextRoot root = new ContextRoot(rootContainer);

        ContextResource r1 = new ContextResource(root, "/foo/bar/");
        Resource r2 = r1.getRelativeResource("WEB-INF/");

        assertEquals("/foo/bar/WEB-INF/", r2.getPath());

        // must call internal method as this test can run outside of Eclipse runtime workbench
        IContainer c1 = root.internalGetContainer(r2.getPath(), wkspRoot);

        assertTrue(c1.exists());

        mockContainer.verifyControls();
    }

    public void testHashCode()
    {
        IContainer rootContainer = (IContainer) mockContainer.newMock(IContainer.class);
        mockContainer.replayControls();

        ContextRoot root = new ContextRoot(rootContainer);

        ContextResource baseResource = new ContextResource(root, "a/happy/man");

        Resource relative = baseResource.getRelativeResource("lady");
        
        assertFalse(baseResource.hashCode() == relative.hashCode());
        
        Resource resource1 = new ContextResource(root, "");
        relative = resource1.getRelativeResource("a/happy/man");
        
        assertTrue(baseResource.hashCode() == relative.hashCode());
        
        
//        relative = baseResource.getRelativeResource("../man");
//        
//        assertTrue(baseResource.hashCode() == relative.hashCode());

        mockContainer.verifyControls();
    }

    private IContainer newContainerMock(MockContainer container, String path, int count)
    {
        MockControl control = container.newControl(IContainer.class);
        IContainer result = (IContainer) control.getMock();

        control.expectAndReturn(result.getFullPath(), new Path(path), count);
        return result;
    }

    private IContainer newContainerMock(MockContainer container, String path, Range range)
    {
        MockControl control = container.newControl(IContainer.class);
        IContainer result = (IContainer) control.getMock();

        control.expectAndReturn(result.getFullPath(), new Path(path), range);
        return result;
    }

    private IResource newResourceMock(MockContainer container, String path, int count)
    {
        MockControl control = container.newControl(IResource.class);
        IResource resource = (IResource) control.getMock();

        control.expectAndReturn(resource.getFullPath(), new Path(path), count);
        return resource;
    }

    private IResource newResourceMock(MockContainer container, String path, Range range)
    {
        MockControl control = container.newControl(IResource.class);
        IResource resource = (IResource) control.getMock();

        control.expectAndReturn(resource.getFullPath(), new Path(path), range);
        return resource;
    }

    private IFolder newFolderMock(MockContainer container, String path, boolean exists)
    {
        MockControl control = container.newControl(IFolder.class);
        IFolder folder = (IFolder) control.getMock();

        control.expectAndReturn(folder.getFullPath(), new Path(path), MockControl.ZERO_OR_MORE);
        control.expectAndReturn(folder.exists(), exists, MockControl.ZERO_OR_MORE);
        return folder;
    }

    String[][] testData =
        { new String[]
            { "/a/b/c", "exists" }, new String[]
            { "/a/b/z", "nonexistant" } };

    public void testMockWorkspace()
    {
        IWorkspaceRoot root = newWorkspaceMock(mockContainer, testData);

        mockContainer.replayControls();

        IFolder folder = root.getFolder(new Path("/a/b/c"));
        assertTrue(folder.exists());

        folder = root.getFolder(new Path("/a/b/z"));
        assertTrue(!folder.exists());

        try
        {
            folder = root.getFolder(new Path("missing"));
            unreachable();
        }
        catch (Error e)
        {
            assertExceptionSubstring(e, "Unexpected method call getFolder(missing)");

        }

        mockContainer.verifyControls();

    }

    private IWorkspaceRoot newWorkspaceMock(MockContainer container, String[][] folderData)
    {
        MockControl control = container.newControl(IWorkspaceRoot.class);
        IWorkspaceRoot root = (IWorkspaceRoot) control.getMock();

        for (int i = 0; i < folderData.length; i++)
        {
            String[] datum = folderData[i];
            IPath path = new Path(datum[0]);
            boolean exists = "exists".equals(datum[1]);

            IFolder folder = newFolderMock(container, datum[0], exists);

            control.expectAndReturn(
                    root.getFolder(new Path(datum[0])),
                    folder,
                    MockControl.ZERO_OR_MORE);
        }

        return root;
    }

}
