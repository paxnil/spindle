package core.namespace;

import junit.framework.Test;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;
import org.easymock.MockControl;

import core.util.Assert.AssertionFailedException;

import core.test.AbstractTestCase;
import core.test.SuiteOfTestCases;

public class ComponentSpecificationResolverTests extends AbstractTestCase
{

    INamespace frameworkNamespace;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ComponentSpecificationResolverTests.class);
    }

    public ComponentSpecificationResolverTests(String name)
    {
        super(name);
    }

    public void setUp()
    {
        frameworkNamespace = createNamespace(new String[]
            { "FRAMEWORK1", "FRAMEWORK2" }, INamespace.FRAMEWORK_NAMESPACE);
    }

    public void testConstructor() throws Exception
    {
        mockContainer.replayControls();
        try
        {
            new ComponentSpecificationResolver(null, null);
            unreachable();
        }
        catch (NullPointerException e)
        {
            // expected
        }
        catch (RuntimeException e)
        {
            unreachable();
        }
        mockContainer.verifyControls();
    }

    public void testConstructor1()
    {

        mockContainer.replayControls();
        try
        {
            new ComponentSpecificationResolver(frameworkNamespace, null);
            unreachable();
        }
        catch (NullPointerException e)
        {
            // expected
        }
        catch (RuntimeException e)
        {
            unreachable();
        }
        mockContainer.verifyControls();
    }

    public void testConstructor2()
    {
        INamespace testNamespace = (INamespace) mockContainer.newMock(INamespace.class);

        mockContainer.replayControls();

        new ComponentSpecificationResolver(frameworkNamespace, testNamespace);

        mockContainer.verifyControls();
    }

    public void testConstructor3()
    {
        MockControl control = mockContainer.newControl(INamespace.class);
        INamespace testNamespace = (INamespace) control.getMock();
        control.expectAndReturn(testNamespace.getId(), "application", MockControl.ZERO_OR_MORE);

        mockContainer.replayControls();

        try
        {
            new ComponentSpecificationResolver(testNamespace, frameworkNamespace);
            unreachable();
        }
        catch (AssertionFailedException e)
        {
            // expected
        }
        catch (RuntimeException e)
        {
            unreachable();
        }

        mockContainer.verifyControls();
    }

    public void testConstructor4() throws Exception
    {
        // to bootstrap the framework this must pass

        mockContainer.replayControls();

        new ComponentSpecificationResolver(null, frameworkNamespace);

        mockContainer.verifyControls();
    }

    public void testResolveInFramework()
    {
        INamespace dummy = (INamespace) mockContainer.newMock(INamespace.class);
        IComponentSpecification spec = null;

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(
                frameworkNamespace, dummy);
        spec = resolver.resolveInFramework("Missing");
        assertNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolveInFramework1()
    {
        INamespace dummy = (INamespace) mockContainer.newMock(INamespace.class);
        IComponentSpecification spec = null;

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(
                frameworkNamespace, dummy);

        spec = resolver.resolveInFramework("FRAMEWORK1");
        assertNotNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolve()
    {
        INamespace testFramework = createNoAcccessNamespace(INamespace.FRAMEWORK_NAMESPACE);
        INamespace namespace = createNamespace(new String[]
            { "COMP1" }, "any");

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(testFramework,
                namespace);

        // don't care about above result. looking for a mock exception
        // if the framework is consulted

        IComponentSpecification spec = resolver.resolve("COMP1");
        
        assertNotNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolveOverride()
    {
        INamespace testFramework = createNoAcccessNamespace(INamespace.FRAMEWORK_NAMESPACE);
        INamespace namespace = createNamespace(new String[]
            { "FRAMEWORK1" }, "any");

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(testFramework,
                namespace);

        IComponentSpecification spec = resolver.resolve("FRAMEWORK1");
        assertNotNull(spec);

        // could fail (mock exception) if the spec did not come from the "any" ns.

        mockContainer.verifyControls();

    }

    public void testResolveChildNoChild()
    {
        MockControl namespaceControl = mockContainer.newControl(INamespace.class);
        INamespace namespace = (INamespace) namespaceControl.getMock();
        namespaceControl.expectAndReturn(namespace.getId(), "any", MockControl.ZERO_OR_MORE);
        namespace.getChildNamespace("anything");
        namespaceControl.setMatcher(MockControl.ALWAYS_MATCHER);
        namespaceControl.setReturnValue(null);

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(
                frameworkNamespace, namespace);

        IComponentSpecification spec = resolver.resolve("missing:NS1");
        assertNull(spec);

        // could fail (mock exception) if the spec did not come from the "any" ns.

        mockContainer.verifyControls();
    }

    public void testResolveChildMissing()
    {

        // the child - has no components
        INamespace childNs = (INamespace) createEmptyNamespace("child1");
        // the namespace that will contain the child
        MockControl namespaceControl = mockContainer.newControl(INamespace.class);
        INamespace namespace = (INamespace) namespaceControl.getMock();
        // set its id
        namespaceControl.expectAndReturn(namespace.getId(), "any", MockControl.ZERO_OR_MORE);
        // "add" the child ns to it
        namespaceControl.expectAndReturn(namespace.getChildNamespace("child1"), childNs, 1);

        // ensure that no access is made to the framework
        INamespace framework = createNoAcccessNamespace(INamespace.FRAMEWORK_NAMESPACE);

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(framework,
                namespace);

        IComponentSpecification spec = resolver.resolve("child1:missing");
        assertNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolveChild()
    {

        // the child - has no components
        INamespace childNs = (INamespace) createNamespace(new String[]
            { "COMP1" }, "child1");
        // the namespace that will contain the child
        MockControl namespaceControl = mockContainer.newControl(INamespace.class);
        INamespace namespace = (INamespace) namespaceControl.getMock();
        // set its id
        namespaceControl.expectAndReturn(namespace.getId(), "any", MockControl.ZERO_OR_MORE);
        // "add" the child ns to it
        namespaceControl.expectAndReturn(namespace.getChildNamespace("child1"), childNs, 1);

        // ensure that no access is made to the framework
        INamespace framework = createNoAcccessNamespace(INamespace.FRAMEWORK_NAMESPACE);

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(framework,
                namespace);

        IComponentSpecification spec = resolver.resolve("child1:COMP1");
        assertNotNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolveStandard()
    {
        INamespace applicationNS = createNamespace(new String[]
            { "Comp1" }, (String) null);

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(
                frameworkNamespace, applicationNS);

        IComponentSpecification spec = resolver.resolve("Comp1");
        assertNotNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolveNullLibId()
    {
        INamespace applicationNS = createNamespace(new String[]
            { "Comp1" }, (String) null);

        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(
                frameworkNamespace, applicationNS);

        IComponentSpecification spec = resolver.resolve(null, "Comp1");
        assertNotNull(spec);

        mockContainer.verifyControls();
    }

    public void testResolveSpecial() throws Exception
    {
        // to bootstrap the framework namespace; we have to know
        // this works
        mockContainer.replayControls();

        ComponentSpecificationResolver resolver = new ComponentSpecificationResolver(null,
                frameworkNamespace);

        IComponentSpecification spec = resolver.resolve("FRAMEWORK1");
        assertNotNull(spec);

    }

    // should puke if any calls are made to find anything other than the id
    private INamespace createNoAcccessNamespace(String namespaceId)
    {
        MockControl nsControl = mockContainer.newControl(INamespace.class);
        INamespace result = (INamespace) nsControl.getMock();
        nsControl.expectAndReturn(result.getId(), namespaceId, MockControl.ZERO_OR_MORE);
        return result;
    }

    // should puke if any calls are made to find anything other than the id and if it contains
    // a component - which it does not.
    private INamespace createEmptyNamespace(String namespaceId)
    {
        MockControl nsControl = mockContainer.newControl(INamespace.class);
        INamespace result = (INamespace) nsControl.getMock();
        nsControl.expectAndReturn(result.getId(), namespaceId, MockControl.ZERO_OR_MORE);
        result.containsComponentType("anything");
        nsControl.setMatcher(MockControl.ALWAYS_MATCHER);
        nsControl.setReturnValue(false);
        return result;
    }

    private INamespace createNamespace(String[] componentIds, String namespaceId)
    {
        MockControl nsControl = mockContainer.newControl(INamespace.class);
        INamespace result = (INamespace) nsControl.getMock();
        nsControl.expectAndReturn(result.getId(), namespaceId, MockControl.ZERO_OR_MORE);
        for (int i = 0; i < componentIds.length; i++)
        {
            nsControl.expectAndReturn(
                    result.containsComponentType(componentIds[i]),
                    true,
                    MockControl.ZERO_OR_MORE);
        }
        nsControl.expectAndReturn(
                result.containsComponentType("Missing"),
                false,
                MockControl.ZERO_OR_MORE);

        for (int i = 0; i < componentIds.length; i++)
        {
            IComponentSpecification component = (IComponentSpecification) mockContainer
                    .newMock(IComponentSpecification.class);
            nsControl.expectAndReturn(
                    result.getComponentSpecification(componentIds[i]),
                    component,
                    MockControl.ZERO_OR_MORE);
        }
        nsControl.expectAndReturn(
                result.getComponentSpecification("Missing"),
                null,
                MockControl.ZERO_OR_MORE);
        return result;
    }

}
