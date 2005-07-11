package com.iw.plugins.spindle.core.scanning;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.easymock.MockControl;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iw.plugins.spindle.core.IJavaType;
import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.resources.IResourceRoot;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.util.XMLPublicIDUtil;

import core.test.AbstractTestCase;
import core.test.SuiteOfTestCases;

//use JAXP DOM parser
public class ApplicationScannerTest extends AbstractTestCase
{
    // is this needed if we are not validating?
    private static class EResolver implements EntityResolver
    {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                IOException
        {
            InputSource result = null;
            int version = XMLPublicIDUtil.getDTDVersion(publicId);
            switch (version)
            {
                case XMLPublicIDUtil.DTD_3_0:
                    result = new InputSource(SpecificationParser.class
                            .getResourceAsStream("Tapestry_3_0.dtd"));
                    break;

                case XMLPublicIDUtil.DTD_4_0:
                    result = new InputSource(SpecificationParser.class
                            .getResourceAsStream("Tapestry_4_0.dtd"));
                    break;

                default:
                    break;

            }
            return result;
        }
    }

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ApplicationScannerTest.class);
    }

    public ApplicationScannerTest(String name)
    {
        super(name);
    }

    public void test30() throws Exception
    {
        doTest("AppScan1_30.xml");

    }

    public void test40() throws Exception
    {
        doTest("AppScan1_40.xml");

    }

    private void doTest(String xmlFile) throws Exception, ScannerException
    {
        IJavaTypeFinder finder = createTypeFinder(
                mockContainer,
                new String[] {},
                new String[] {},
                true);
        ITapestryProject tapestryProject = createBasicTapestryProject(mockContainer);

        mockContainer.replayControls();

        ApplicationScanner scanner = new ApplicationScanner();
        scanner.setValidating(false);
        scanner.setExternalProblemCollector(this); // for testing

        Document document = getDocument(getClass().getResourceAsStream(xmlFile));

        SpecificationValidator validator = new SpecificationValidator(finder, tapestryProject);

        IApplicationSpecification result = (IApplicationSpecification) scanner.scan(
                document,
                validator);

        assertNotNull(result);

        assertTrue(logger.isEmpty());

        assertTrue(problems.isEmpty());

        mockContainer.verifyControls();
    }

    public void testMissingDTD() throws Exception
    {
        IJavaTypeFinder finder = createTypeFinder(
                mockContainer,
                new String[] {},
                new String[] {},
                true);
        ITapestryProject tapestryProject = createBasicTapestryProject(mockContainer);

        mockContainer.replayControls();

        ApplicationScanner scanner = new ApplicationScanner();
        scanner.setValidating(false);
        scanner.setExternalProblemCollector(this); // for testing

        Document document = getDocument(getClass().getResourceAsStream("AppScan1_MissingDTD.xml"));

        SpecificationValidator validator = new SpecificationValidator(finder, tapestryProject);

        IApplicationSpecification result = (IApplicationSpecification) scanner.scan(
                document,
                validator);

        assertNull(result);

        assertTrue(logger.isEmpty());

        assertSingleProblemCode(IProblem.SPINDLE_MISSING_PUBLIC_ID);

        mockContainer.verifyControls();
    }

    public void testInvalidDTD() throws Exception
    {
        IJavaTypeFinder finder = createTypeFinder(
                mockContainer,
                new String[] {},
                new String[] {},
                true);
        ITapestryProject tapestryProject = createBasicTapestryProject(mockContainer);

        mockContainer.replayControls();

        ApplicationScanner scanner = new ApplicationScanner();
        scanner.setValidating(false);
        scanner.setExternalProblemCollector(this); // for testing

        Document document = getDocument(getClass().getResourceAsStream(
                "AppScan1_UnrecognizedDTD.xml"));

        SpecificationValidator validator = new SpecificationValidator(finder, tapestryProject);

        IApplicationSpecification result = (IApplicationSpecification) scanner.scan(
                document,
                validator);

        assertNull(result);

        assertTrue(logger.isEmpty());

        assertSingleProblemCode(IProblem.SPINDLE_INVALID_PUBLIC_ID);

        mockContainer.verifyControls();
    }

    public void testWrongRootElement30() throws Exception
    {
        doTestWrongRootElement("AppScan1_WrongRootElement_30.xml");
    }

    public void testWrongRootElement40() throws Exception
    {
        doTestWrongRootElement("AppScan1_WrongRootElement_40.xml");
    }

    private void doTestWrongRootElement(String xmlFile) throws Exception, ScannerException
    {
        IJavaTypeFinder finder = createTypeFinder(
                mockContainer,
                new String[] {},
                new String[] {},
                true);
        ITapestryProject tapestryProject = createBasicTapestryProject(mockContainer);

        mockContainer.replayControls();

        ApplicationScanner scanner = new ApplicationScanner();
        scanner.setValidating(false);
        scanner.setExternalProblemCollector(this); // for testing

        Document document = getDocument(getClass().getResourceAsStream(xmlFile));

        SpecificationValidator validator = new SpecificationValidator(finder, tapestryProject);

        IApplicationSpecification result = (IApplicationSpecification) scanner.scan(
                document,
                validator);

        assertNotNull(result);
        assertTrue(logger.isEmpty());

        assertSingleProblemCode(IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_APPLICATION);

        mockContainer.verifyControls();
    }

    private Document getDocument(InputStream in) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EResolver());
        return builder.parse(in);
    }

    protected IJavaTypeFinder createTypeFinder(MockContainer container, String[] knownTypes,
            String[] unknownTypes, boolean isCaching)
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

    protected IJavaType createJavaType(MockContainer container, String fullyQualifiedName,
            boolean exists)
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
