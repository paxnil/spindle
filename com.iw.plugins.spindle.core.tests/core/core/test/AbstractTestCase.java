// Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package core.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import core.ICoreListeners;
import core.IJavaType;
import core.IJavaTypeFinder;
import core.IPreferenceSource;
import core.ITapestryProject;
import core.TapestryCore;
import core.resources.IResourceRoot;
import core.source.IProblem;
import core.source.IProblemCollector;
import core.source.ISourceLocation;
import core.test.TestLogger.LoggingEvent;

/**
 * Borrowed from Hivemind (HivemindTestCase) and modified.
 * 
 * @author hls, gwl
 */
public abstract class AbstractTestCase extends SuiteOfTestCases implements IProblemCollector
{

    // /CLOVER:OFF

    protected TestLogger logger = new TestLogger();

    protected final TestProblemCollector problems = new TestProblemCollector();

    protected MockContainer mockContainer = new MockContainer();

    interface MockControlFactory
    {
        public MockControl newControl(Class mockClass);
    }

    protected static class MockContainer
    {

        /** List of {@link org.easymock.MockControl}. */

        private List controls = new ArrayList();

        private MockContainer parent;

        public MockContainer()
        {
            super();
        }

        public MockContainer(MockContainer parent)
        {
            this();
            this.parent = parent;
        }

        /**
         * Creates a <em>managed</em> control via
         * {@link MockControl#createStrictControl(java.lang.Class)}. The created control is
         * remembered, and will be invoked by {@link #replayControls()},{@link #verifyControls()},
         * etc..
         * <p>
         * The class to mock may be either an interface or a class. The EasyMock class extension
         * (easymockclassextension-1.1.jar) and CGLIB (cglib-full-2.01.jar) must be present in the
         * latter case (new since 1.1).
         */
        public MockControl newControl(Class mockClass)
        {
            MockControlFactory factory = mockClass.isInterface() ? interfaceMockControlFactory
                    : classMockControlFactory;

            MockControl result = factory.newControl(mockClass);

            addControl(result);

            return result;
        }

        /**
         * Adds the control to the list of managed controls used by {@link #replayControls()}and
         * {@link #verifyControls()}.
         */
        public void addControl(MockControl control)
        {
            controls.add(control);
        }

        /**
         * Convienience for invoking {@link #newControl(Class)}and then invoking
         * {@link MockControl#getMock()}on the result.
         */
        public Object newMock(Class mockClass)
        {
            return newControl(mockClass).getMock();
        }

        /**
         * Invokes {@link MockControl#replay()}on all controls created by
         * {@link #newControl(Class)}.
         */
        public void replayControls()
        {
            Iterator i = controls.iterator();
            while (i.hasNext())
            {
                MockControl c = (MockControl) i.next();
                c.replay();
            }
            if (parent != null)
                parent.replayControls();
        }

        /**
         * Invokes {@link org.easymock.MockControl#verify()}and {@link MockControl#reset()}on all
         * controls created by {@link #newControl(Class)}.
         */

        public void verifyControls()
        {
            Iterator i = controls.iterator();
            while (i.hasNext())
            {
                MockControl c = (MockControl) i.next();
                c.verify();
                c.reset();
            }
            if (parent != null)
                parent.verifyControls();
        }

        /**
         * Invokes {@link org.easymock.MockControl#reset()}on all controls.
         */

        public void resetControls()
        {
            Iterator i = controls.iterator();
            while (i.hasNext())
            {
                MockControl c = (MockControl) i.next();
                c.reset();
            }
            if (parent != null)
                parent.resetControls();
        }
    }

    private static class InterfaceMockControlFactory implements MockControlFactory
    {
        public MockControl newControl(Class mockClass)
        {
            return MockControl.createStrictControl(mockClass);
        }
    }

    private static class ClassMockControlFactory implements MockControlFactory
    {
        public MockControl newControl(Class mockClass)
        {
            return MockClassControl.createStrictControl(mockClass);
        }
    }

    static class PlaceholderClassMockControlFactory implements MockControlFactory
    {
        public MockControl newControl(Class mockClass)
        {
            throw new RuntimeException(
                    "Unable to instantiate EasyMock control for "
                            + mockClass
                            + "; ensure that easymockclassextension-1.1.jar and cglib-full-2.0.1.jar are on the classpath.");
        }
    }

    protected interface TestAsserter
        {
            void makeAssertions(Object result);
        }

    private static final MockControlFactory interfaceMockControlFactory = new InterfaceMockControlFactory();

    private static MockControlFactory classMockControlFactory;

    static
    {
        try
        {
            classMockControlFactory = new ClassMockControlFactory();
        }
        catch (NoClassDefFoundError ex)
        {
            classMockControlFactory = new PlaceholderClassMockControlFactory();
        }
    }

    public AbstractTestCase(String name)
    {
        super(name);
    }

    /**
     * setup TapestryCore with a TestLogger and dumb mocks for listeners and preferences called by
     * {@link #setUpSuite()}. NOTE: This is for use outside of Eclipse only, subclasses that run
     * inside of Eclipse should override. infact
     */
    protected void setUpTapestryCore()
    {
        ICoreListeners listeners = (ICoreListeners) mockContainer.newMock(ICoreListeners.class);
        IPreferenceSource preferenceSource = (IPreferenceSource) mockContainer
                .newMock(IPreferenceSource.class);

        new TapestryCore(logger, listeners, preferenceSource);
    }

    /**
     * Converts the actual list to an array and invokes
     * {@link #assertListsEqual(Object[], Object[])}.
     */
    protected static void assertListsEqual(Object[] expected, List actual)
    {
        assertListsEqual(expected, actual.toArray());
    }

    /**
     * Asserts that the two arrays are equal; same length and all elements equal. Checks the
     * elements first, then the length.
     */
    protected static void assertListsEqual(Object[] expected, Object[] actual)
    {
        assertNotNull(actual);

        int min = Math.min(expected.length, actual.length);

        for (int i = 0; i < min; i++)
            assertEquals("list[" + i + "]", expected[i], actual[i]);

        assertEquals("list length", expected.length, actual.length);
    }

    /**
     * Called when code should not be reachable (because a test is expected to throw an exception);
     * throws AssertionFailedError always.
     */
    protected static void unreachable()
    {
        throw new AssertionFailedError("This code should be unreachable.");
    }

    /**
     * Gets the list of events most recently intercepted. This resets the appender, clearing the
     * list of stored events.
     */

    protected List getInterceptedLogEvents()
    {
        return logger.getEvents();
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();       
        setUpTapestryCore();
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        logger.clear();
        problems.clear();
    }

    /**
     * Checks that the provided problem has the expected code
     */
    protected void assertProblemCode(IProblem problem, int expected)
    {
        int code = problem.getCode();
        if (expected != code)
            throw new AssertionFailedError("expected problem code " + expected + "but got " + code
                    + " : " + problem.toString());
    }
        
    /**
     * Checks that there is only one problem and the code matches
     */
    protected void assertSingleProblemCode(int expectedProblemCode)
    {
        IProblem [] found = getProblems();
        assertEquals("expecting exactly one problem!", 1, found.length);
        assertProblemCode(found[0], expectedProblemCode);      
    }

    /**
     * Checks that the provided substring exists in the exceptions message.
     */
    protected void assertExceptionSubstring(Throwable ex, String substring)
    {
        String message = ex.getMessage();
        assertNotNull(message);

        int pos = message.indexOf(substring);

        if (pos < 0)
            throw new AssertionFailedError("Exception message (" + message + ") does not contain ["
                    + substring + "]");
    }

    /**
     * Checks to see if a specific event matches the name and message.
     * 
     * @param message
     *            exact message to search for
     * @param events
     *            the list of events {@link #getInterceptedLogEvents()}
     * @param index
     *            the index to check at
     */
    private void assertLoggedMessage(String message, List events, int index)
    {
        LoggingEvent e = (LoggingEvent) events.get(index);

        assertEquals("Message", message, e.getMessage());
    }

    /**
     * Checks the messages for all logged events for exact match against the supplied list.
     */
    protected void assertLoggedMessages(String[] messages)
    {
        List events = getInterceptedLogEvents();

        for (int i = 0; i < messages.length; i++)
        {
            assertLoggedMessage(messages[i], events, i);
        }
    }

    /**
     * Asserts that some capture log event matches the given message exactly.
     */
    protected void assertLoggedMessage(String message)
    {
        assertLoggedMessage(message, getInterceptedLogEvents());
    }

    /**
     * Asserts that some capture log event matches the given message exactly.
     * 
     * @param message
     *            to search for; success is finding a logged message contain the parameter as a
     *            substring
     * @param events
     *            from {@link #getInterceptedLogEvents()}
     */
    protected void assertLoggedMessage(String message, List events)
    {
        int count = events.size();

        for (int i = 0; i < count; i++)
        {
            LoggingEvent e = (LoggingEvent) events.get(i);

            String eventMessage = String.valueOf(e.getMessage());

            if (eventMessage.indexOf(message) >= 0)
                return;
        }

        throw new AssertionFailedError("Could not find logged message: " + message);
    }

    public void addProblem(int severity, ISourceLocation location, String message,
            boolean isTemporary, int code)
    {
        problems.addProblem(severity, location, message, isTemporary, code);
    }

    public void addProblem(IProblem problem)
    {
        problems.addProblem(problem);
    }

    public void beginCollecting()
    {
        problems.beginCollecting();
    }

    public void endCollecting()
    {
        problems.endCollecting();
    }

    public IProblem[] getProblems()
    {
        return problems.getProblems();
    }

    protected IJavaTypeFinder createTypeFinder(MockContainer container, String[] knownTypes, String[] unknownTypes, boolean isCaching)
    {
        MockControl control = container.newControl(IJavaTypeFinder.class);
        IJavaTypeFinder finder = (IJavaTypeFinder) control.getMock();
    
        control.expectAndReturn(finder.isCachingJavaTypes(), isCaching, MockControl.ZERO_OR_MORE);
        for (int i = 0; i < knownTypes.length; i++)
        {
            control.expectAndReturn(finder.findType(knownTypes[i]), createJavaType(
                    container,
                    knownTypes[i],
                    true));
        }
        for (int i = 0; i < unknownTypes.length; i++)
        {
            control.expectAndReturn(finder.findType(unknownTypes[i]), createJavaType(
                    container,
                    unknownTypes[i],
                    false));
        }
    
        return finder;
    
    }

    protected IJavaType createJavaType(MockContainer container, String fullyQualifiedName, boolean exists)
    {
        MockControl control = container.newControl(IJavaType.class);
        IJavaType type = (IJavaType) control.getMock();
    
        control.expectAndReturn(
                type.getFullyQualifiedName(),
                fullyQualifiedName,
                MockControl.ZERO_OR_MORE);
        control.expectAndReturn(type.exists(), exists, MockControl.ZERO_OR_MORE);
        return type;
    }

    protected ITapestryProject createBasicTapestryProject(MockContainer container)
    {
        MockControl control = container.newControl(ITapestryProject.class);
        ITapestryProject project = (ITapestryProject) control.getMock();
    
        control.expectAndReturn(project.getWebContextLocation(), container
                .newMock(IResourceRoot.class), MockControl.ZERO_OR_MORE);
    
        control.expectAndReturn(
                project.getClasspathRoot(),
                container.newMock(IResourceRoot.class),
                MockControl.ZERO_OR_MORE);
    
        return project;
    
    }

}