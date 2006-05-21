package net.sf.spindle.core.build;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import junit.framework.Test;
import net.sf.spindle.core.AbstractTestCase;
import net.sf.spindle.core.ICoreListeners;
import net.sf.spindle.core.IPreferenceSource;
import net.sf.spindle.core.IProblemPeristManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.SuiteOfTestCases;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.SourceLocation;

import org.apache.hivemind.Resource;
import org.easymock.MockControl;

public class InfrastructureIntializeTest extends AbstractTestCase
{

    private MockControl problemPersistManagerMock;

    private IProblemPeristManager problemPersistManager;

    private MockControl buildNotifierMock;

    private IBuildNotifier buildNotifier;

    private ITapestryProject tapestryProject;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(InfrastructureIntializeTest.class);
    }

    public InfrastructureIntializeTest(String name)
    {
        super(name);
    }

    @Override
    public void setUpSuite() throws Exception
    {
        // override we not use setUpTapestryCore() defined in superclass;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        problemPersistManagerMock = mockContainer.newControl(IProblemPeristManager.class);
        problemPersistManager = (IProblemPeristManager) problemPersistManagerMock.getMock();

        tapestryProject = createBasicTapestryProject(mockContainer);

        buildNotifierMock = mockContainer.newControl(IBuildNotifier.class);
        buildNotifier = (IBuildNotifier) buildNotifierMock.getMock();

    }

    @Override
    protected void tearDown() throws Exception
    {
        destroyTapestryCore();
    }

    private void setUpTapestryCore(int buildNotifications)
    {
        MockControl control = mockContainer.newControl(ICoreListeners.class);
        ICoreListeners listeners = (ICoreListeners) control.getMock();

        if (buildNotifications >= 1)
        {
            listeners.buildOccurred();
            control.setVoidCallable(buildNotifications);
        }

        IPreferenceSource preferenceSource = (IPreferenceSource) mockContainer
                .newMock(IPreferenceSource.class);

        new TapestryCore(logger, listeners, preferenceSource);
    }

    private void setMinimumBuildNotifierCalls()
    {

        // begin must be called and only called once
        buildNotifier.begin();
        buildNotifierMock.setVoidCallable(1);

        // checkCancel is called often
        buildNotifier.checkCancel();
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        // done() must be called and only once.
        buildNotifier.done();

    }

    public void testAssertProjectNotNull()
    {
        setUpTapestryCore(0);

        AbstractBuildInfrastructure inf = new BaseTestInfrastructure(null, problemPersistManager,
                buildNotifier);
        boolean screwed = false;
        mockContainer.replayControls();
        try
        {
            inf.build(false, Collections.emptyMap());
            screwed = true;
        }
        catch (NullPointerException e)
        {
            assertTrue(e.getMessage().indexOf("tapestry project must not be null") >= 0);
        } finally {
            mockContainer.verifyControls();
        }

        assertFalse(screwed);
    }

    public void testAssertProblemPersisterNotNull()
    {
        setUpTapestryCore(0);

        AbstractBuildInfrastructure inf = new BaseTestInfrastructure(tapestryProject, null,
                buildNotifier);
        boolean screwed = false;
        mockContainer.replayControls();
        try
        {
            inf.build(false, Collections.emptyMap());
            screwed = true;
        }
        catch (NullPointerException e)
        {
            assertTrue(e.getMessage().indexOf("problem persister must not be null") >= 0);
        }
        finally
        {
            mockContainer.verifyControls();
        }

        assertFalse(screwed);
    }

    public void testAssertNotifierNotNull()
    {
        setUpTapestryCore(0);

        AbstractBuildInfrastructure inf = new BaseTestInfrastructure(tapestryProject,
                problemPersistManager, null);
        boolean screwed = false;
        mockContainer.replayControls();
        try
        {
            inf.build(false, Collections.emptyMap());
            screwed = true;
        }
        catch (NullPointerException e)
        {
            assertTrue(e.getMessage().indexOf("notifier must not be null") >= 0);
        }
        finally
        {
            mockContainer.verifyControls();
        }

        assertFalse(screwed);
    }

    public void testInitializeFailed()
    {

        setUpTapestryCore(1);

        problemPersistManager.removeAllProblems(tapestryProject);

        problemPersistManagerMock.setVoidCallable(1);

        problemPersistManager.recordProblem(tapestryProject, new DefaultProblem(
                IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, "failed!",
                SourceLocation.FOLDER_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));

        problemPersistManagerMock.setVoidCallable(1);

        BaseTestInfrastructure inf = new BaseTestInfrastructure(tapestryProject,
                problemPersistManager, buildNotifier)
        {

            @Override
            protected void initialize() throws BuilderException
            {
                throw new BuilderException("failed!");

            }           

            @Override
            protected void clearLastState()
            {
                // we expect this.
            }
        };

        setMinimumBuildNotifierCalls();

        mockContainer.replayControls();

        try
        {

            inf.build(false, Collections.emptyMap());
        }
        finally
        {
            mockContainer.verifyControls();
        }
    }

    public void testIsWorthBuldingFailedBuilderException()
    {

        setUpTapestryCore(1);

        problemPersistManager.removeAllProblems(tapestryProject);

        problemPersistManagerMock.setVoidCallable(1);

        problemPersistManager.recordProblem(tapestryProject, new DefaultProblem(
                IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, "failed!",
                SourceLocation.FOLDER_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));

        problemPersistManagerMock.setVoidCallable(1);

        BaseTestInfrastructure inf = new BaseTestInfrastructure(tapestryProject,
                problemPersistManager, buildNotifier)
        {

            @Override
            protected void initialize() throws BuilderException
            {
                // do nothing to simulate no problems here.
            }

            @Override
            protected boolean isWorthBuilding() throws BuilderException
            {
                throw new BuilderException("failed!");
            }   

            @Override
            protected void clearLastState()
            {
                // we expect this.
            }
        };

        setMinimumBuildNotifierCalls();

        mockContainer.replayControls();
        try
        {
            inf.build(false, Collections.emptyMap());
        }
        finally
        {
            mockContainer.verifyControls();
        }
    }

    public void testIsWorthBuldingFailedBrokenWebXMLException()
    {

        setUpTapestryCore(1);        

        problemPersistManager.recordProblem(tapestryProject, new DefaultProblem(
                IProblem.TAPESTRY_BUILDBROKEN_MARKER, IProblem.ERROR, "failed!",
                SourceLocation.FOLDER_LOCATION, false, IProblem.NOT_QUICK_FIXABLE));

        problemPersistManagerMock.setVoidCallable(1);

        BaseTestInfrastructure inf = new BaseTestInfrastructure(tapestryProject,
                problemPersistManager, buildNotifier)
        {

            @Override
            protected void initialize() throws BuilderException
            {
                // do nothing to simulate no problems here.
            }

            @Override
            protected boolean isWorthBuilding() throws BuilderException
            {
                throw new BrokenWebXMLException("failed!");
            }

            @Override
            protected void clearLastState()
            {
                // we expect this.
            }
        };

        setMinimumBuildNotifierCalls();

        mockContainer.replayControls();
        try
        {
            inf.build(false, Collections.emptyMap());
        }
        finally
        {
            mockContainer.verifyControls();
        }
    }

    class BaseTestInfrastructure extends AbstractBuildInfrastructure
    {

        public BaseTestInfrastructure()
        {
            super();
        }

        public BaseTestInfrastructure(ITapestryProject tapestryProject,
                IProblemPeristManager problemPersistManager, IBuildNotifier buildNotifier)
        {
            super();
            this.tapestryProject = tapestryProject;
            this.problemPersister = problemPersistManager;
            this.notifier = buildNotifier;
        }

        @Override
        protected void clearLastState()
        {
            fail("never should be called!");

        }

        @Override
        public Object copyClasspathMemento(Object memento)
        {
            fail("never should be called!");
            return null;
        }

        @Override
        protected AbstractBuild createIncrementalBuild()
        {
            fail("never should be called!");
            return null;
        }

        @Override
        public WebXMLScanner createWebXMLScanner(FullBuild build)
        {
            fail("never should be called!");
            return null;
        }

        @Override
        public void findAllTapestrySourceFiles(Set<String> knownTemplateExtensions,
                ArrayList<Resource> found)
        {
            fail("never should be called!");
        }

        @Override
        public Object getClasspathMemento()
        {
            fail("never should be called!");
            return null;
        }

        @Override
        public State getLastState()
        {
            fail("never should be called!");
            return null;
        }

        @Override
        protected void initialize() throws BuilderException
        {
            fail("never should be called!");
        }

        @Override
        protected boolean isWorthBuilding() throws BuilderException
        {
            fail("never should be called!");
            return false;
        }

        @Override
        public void persistState(State state)
        {
            fail("never should be called!");

        }

        @Override
        public boolean projectSupportsAnnotations()
        {
            fail("never should be called!");
            return false;
        }

    }
}
