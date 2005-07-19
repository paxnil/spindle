package com.iw.plugins.spindle.core.resources.eclipse;

import java.util.Arrays;

import junit.framework.Test;

import org.easymock.MockControl;
import org.easymock.internal.Range;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Path;

import core.test.AbstractTestCase;
import core.test.SuiteOfTestCases;

public class ContextResourceTests extends AbstractTestCase
{
    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ContextResourceTests.class);
    }

    public ContextResourceTests(String name)
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
    static String[][] data = new String[][]
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

            boolean containerIsNull = testRoot.internalGetContainer(location.getPath(), root) == null;
            assertTrue(Arrays.asList(testData).toString(), !(expectSuccess == containerIsNull));

            mocker.verifyControls();
            assertTrue(logger.isEmpty());

            mocker.resetControls();
        }
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

}
