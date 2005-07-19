package com.iw.plugins.spindle.core.scanning;



import junit.framework.Test;

import org.apache.tapestry.spec.IApplicationSpecification;
import org.w3c.dom.Document;

import com.iw.plugins.spindle.core.IJavaTypeFinder;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.parser.dom.IDOMModel;
import com.iw.plugins.spindle.core.source.IProblem;

import core.test.AbstractXMLTestCase;
import core.test.SuiteOfTestCases;

//use JAXP DOM parser
public class ApplicationScannerTest extends AbstractXMLTestCase
{
   
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
        okTest("AppScan1_30.xml");

    }

    public void test40() throws Exception
    {
        okTest("AppScan1_40.xml");

    }

    private void okTest(String xmlFile) throws Exception
    {
        readScanAssertApplication(xmlFile, new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                assertNotNull(result);

                assertTrue(logger.isEmpty());

                assertTrue(problems.isEmpty());

            }
        });
    }

    public void testMissingDTD() throws Exception
    {
        readScanAssertApplication("AppScan1_MissingDTD.xml", new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                assertNull(result);

                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_MISSING_PUBLIC_ID);

            }
        });
    }

    public void testInvalidDTD() throws Exception
    {
        readScanAssertApplication("AppScan1_UnrecognizedDTD.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNull(result);

                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_INVALID_PUBLIC_ID);
            }
        });
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
        readScanAssertApplication(xmlFile, new TestAsserter()
        {

            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_APPLICATION);
            }
        });
    }

    private void readScanAssertApplication(String file, TestAsserter asserter) throws Exception
    {
        assertNotNull(asserter);
        assertNotNull(file);

        IJavaTypeFinder mockFinder = createTypeFinder(
                mockContainer,
                new String[] {},
                new String[] {},
                true);
        ITapestryProject mockProject = createBasicTapestryProject(mockContainer);

        ApplicationScanner scanner = new ApplicationScanner();
        scanner.setValidating(false);
        scanner.setExternalProblemCollector(this); // for testing

        Document document = getDocument(getClass().getResourceAsStream(file));
        IDOMModel model = createMockIDOMModel(document);

        mockContainer.replayControls();

        SpecificationValidator validator = new SpecificationValidator(mockFinder, mockProject);

        IApplicationSpecification result = (IApplicationSpecification) scanner.scan( 
                model,
                validator);

        asserter.makeAssertions(result);

        mockContainer.verifyControls();
    }

}
