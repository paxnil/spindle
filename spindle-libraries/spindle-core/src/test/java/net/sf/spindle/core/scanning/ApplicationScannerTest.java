package net.sf.spindle.core.scanning;

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
import junit.framework.Test;
import net.sf.spindle.core.AbstractXMLTestCase;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.SuiteOfTestCases;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.types.IJavaTypeFinder;

import org.apache.hivemind.Resource;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.w3c.dom.Document;

// use JAXP DOM parser
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

    public void testUnsupportedPageClassPackageMeta() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedPageClassMeta.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_PAGE_CLASS_META);
            }
        });

    }

    public void testUnsupportedPageClassPackageMeta30() throws Exception, ScannerException
    {

        readScanAssertApplication("AppScan_UnsupportedPageClassMeta30.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_PAGE_CLASS_META);
            }
        });
    }

    public void testUnsupportedComponentClassPackageMeta() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedComponentClassMeta.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_COMPONENT_CLASS_META);
            }
        });

    }

    public void testUnsupportedComponentClassPackageMeta30() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedComponentClassMeta30.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_COMPONENT_CLASS_META);
            }
        });
    }

    public void testUnsupportedPageNames() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedPageName.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_PAGE_NAME);
            }
        });
    }

    public void testUnsupportedPageNames30() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedPageName30.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_PAGE_NAME);
            }
        });
    }
    
    public void testUnsupportedComponentNames() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedComponentName.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_COMPONENT_NAME);
            }
        });
    }

    public void testUnsupportedComponentNames30() throws Exception, ScannerException
    {
        readScanAssertApplication("AppScan_UnsupportedComponentName30.xml", new TestAsserter()
        {
            public void makeAssertions(Object result)
            {
                assertNotNull(result);
                assertTrue(logger.isEmpty());

                assertSingleProblemCode(IProblem.SPINDLE_UNSUPPORTED_COMPONENT_NAME);
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

        //none of the tests in here care about valid resource locations
        SpecificationValidator validator = new SpecificationValidator(mockFinder, mockProject) {

            @Override
            public boolean validateResource(Resource location, String relativePath, String errorKey, ISourceLocation source) throws ScannerException
            {
                // TODO Auto-generated method stub
                return true;
            }

            @Override
            public boolean validateResourceLocation(Resource location, String relativePath, String errorKey, ISourceLocation source, boolean accountForI18N) throws ScannerException
            {
               
                return true;
            }
            
        };

        IApplicationSpecification result = (IApplicationSpecification) scanner.scan(
                model,
                validator);

        asserter.makeAssertions(result);

        mockContainer.verifyControls();
    }

}
