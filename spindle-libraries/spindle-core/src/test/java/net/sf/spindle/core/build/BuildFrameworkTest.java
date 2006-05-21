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
import java.io.File;
import java.util.Collections;

import junit.framework.Test;
import net.sf.spindle.core.AbstractTestCase;
import net.sf.spindle.core.ICoreListeners;
import net.sf.spindle.core.IPreferenceSource;
import net.sf.spindle.core.SuiteOfTestCases;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.resources.PathUtils;

import org.easymock.MockControl;

public class BuildFrameworkTest extends AbstractTestCase
{

    private static String TAPESTRY_JAR_DIR = "jars/build-test-jars";
    private static String PROJECT_RESOURCES = "resources/";

    private static File[] EMPTY_FILE_LIST = new File[] {};

    private ProblemPersisterFixture problemPersistManager;

    private MockControl buildNotifierMock;

    private IBuildNotifier buildNotifier;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(BuildFrameworkTest.class);
    }

    public BuildFrameworkTest(String name)
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

        problemPersistManager = new ProblemPersisterFixture();
        setUpBuildNotifier();

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
        
        //This will return defaults
        IPreferenceSource preferenceSource =  new IPreferenceSource() {

            public double getDouble(String name)
            {               
                return 0;
            }

            public float getFloat(String name)
            {                
                return 0;
            }

            public int getInt(String name)
            {               
                return 0;
            }

            public long getLong(String name)
            {                
                return 0;
            }

            public String getString(String name)
            {                
                return null;
            }
            
        };

        new TapestryCore(logger, listeners, preferenceSource);
    }

    private File getContextDirectory(String name)
    {        
        return getFile(PROJECT_RESOURCES+name);
    }

    protected File getFile(String relativePath)
    {
        PathUtils jarPath = new PathUtils(System.getProperty("basedir")).append("testData").append(
                relativePath);
        File file = new File(jarPath.toOSString());
        assertTrue(file.exists());
        return file;
    }

    // we are not testing the notifier here, so mock it so things don't break
    // cuz of the notifier!
    private void setUpBuildNotifier()
    {
        buildNotifierMock = mockContainer.newControl(IBuildNotifier.class, false);
        buildNotifierMock.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
        buildNotifier = (IBuildNotifier) buildNotifierMock.getMock();

        // begin must be called and only called once
        buildNotifier.begin();
        buildNotifierMock.setVoidCallable(1);

        // the following are called often

        buildNotifier.checkCancel();
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        buildNotifier.aboutToProcess(null);
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        buildNotifier.processed(null);
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        buildNotifier.setProcessingProgressPer(1.0f);
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        buildNotifier.subTask("anything");
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        buildNotifier.updateProgress(1.0f);
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);

        buildNotifier.updateProgressDelta(1.0f);
        buildNotifierMock.setVoidCallable(MockControl.ZERO_OR_MORE);
        buildNotifierMock.setMatcher(MockControl.ALWAYS_MATCHER);

        // done() must be called and only once.
        buildNotifier.done();
        buildNotifierMock.setVoidCallable(1);
    }

    public void testNotWorthBuilding()
    {
        setUpTapestryCore(1);
        ProjectFixture tproject = new ProjectFixture();
        InfrastructureFixture inf = new InfrastructureFixture(tproject, problemPersistManager,
                buildNotifier)
        {

            @Override
            protected boolean isWorthBuilding() throws BuilderException, BrokenWebXMLException
            {
                throw new BuilderException("not worth it!");
            }

        };

        try
        {
            mockContainer.replayControls();
            inf.build(false, Collections.emptyMap());
        }
        finally
        {
            mockContainer.verifyControls();
        }

        assertEquals(1, problemPersistManager.getAllProblemCount());
        assertTrue(problemPersistManager.hasBrokenBuildProblems(tproject));
        assertTrue(logger.isEmpty());
        assertNull(inf.getLastState());
    }

    public void testBuildFramework()
    {
        setUpTapestryCore(1);
        File jars = getFile(TAPESTRY_JAR_DIR);
        File[] context = new File [] {getContextDirectory("build-context1")};
        ProjectFixture tproject = new ProjectFixture(jars, EMPTY_FILE_LIST,
                context, new PermissiveTypeFinder());
        InfrastructureFixture inf = new InfrastructureFixture(tproject, problemPersistManager,
                buildNotifier);

        try
        {
            mockContainer.replayControls();
            inf.build(false, Collections.emptyMap());
        }
        finally
        {
            mockContainer.verifyControls();
        }

        assertEquals(0, problemPersistManager.getAllProblemCount());
        assertFalse(problemPersistManager.hasBrokenBuildProblems(tproject));
        if (!logger.isEmpty()) {
            logger.dump();
            fail("expected no log entries");
        }
        if (problemPersistManager.getAllProblemCount() > 0) {
            problemPersistManager.dump();
            fail("problems were encountered where none should have been encountered!");
        }
        //assertNotNull(inf.getLastState()); TODO fix state management
    }
}
