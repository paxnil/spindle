/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package tests.Scanners.mr;

import java.net.URL;
import java.util.Locale;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.Location;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Just tests the scanBinding method in ComponentScanner
 * 
 *  //TODO must be made complete. Also the scanner can't validate whether the parameter exists yet
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class TestComponentScanBindingMR extends BaseComponentScannerTest
{

    public TestComponentScanBindingMR()
    {
        super();
    }

    /**
     * @param arg0
     */
    public TestComponentScanBindingMR(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    protected IContainedComponent createContainedComponent()
    {
        IContainedComponent result = factory.createContainedComponent();
        // don't really care that all the methods return null
        Location location = new Location(new IResourceLocation()
        {
            public URL getResourceURL()
            {
                return null;
            }

            public String getName()
            {
                return null;
            }

            public IResourceLocation getLocalization(Locale locale)
            {
                return null;
            }

            public IResourceLocation getRelativeLocation(String name)
            {
                return null;
            }

            public String getPath()
            {
                return null;
            }
            
            public Locale getLocale() {
                return null;
            }
        });
        result.setLocation(location);
        return result;
    }

    public void testDynamicBinding() throws Exception
    {
        String content = "<binding name='poo' expression='fun.name'/>";
        doTestDynamicBinding(getXMLDocument(XMLUtil.DTD_1_3, "binding", content));
        doTestDynamicBinding(getXMLDocument(XMLUtil.DTD_3_0, "binding", content));
    }

    private void doTestDynamicBinding(String content) throws Exception
    {
        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);

        IContainedComponent component = createContainedComponent();
        scanBinding(component, node);
        basicCheckProblems(scanner.getProblems(), 0);
        IBindingSpecification result = component.getBinding("poo");
        m_assertNotNull(result);
        m_assertEquals(result.getValue(), "fun.name");
        m_assertSame(result.getType(), BindingType.DYNAMIC);
        m_assertSame(result.getLocation().getResourceLocation(), component.getLocation().getResourceLocation());
    }

    public void testDynamicBindingWithInvalidExpression() throws Exception
    {
        String content = "<binding name='poo' expression='listeners.{?#this instanceof antlr.TokenListener'/>";
        doTestDynamicBindingWithInvalidExpression(getXMLDocument(XMLUtil.DTD_1_3, "binding", content));
        doTestDynamicBindingWithInvalidExpression(getXMLDocument(XMLUtil.DTD_3_0, "binding", content));
    }

    private void doTestDynamicBindingWithInvalidExpression(String content) throws Exception
    {
        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);

        IContainedComponent component = createContainedComponent();
        scanBinding(component, node);
        basicCheckProblems(scanner.getProblems(), 1);
        IBindingSpecification result = component.getBinding("poo");
        m_assertNotNull(result);
        m_assertEquals(result.getValue(), "listeners.{?#this instanceof antlr.TokenListener");
        m_assertSame(result.getType(), BindingType.DYNAMIC);
    }

    public void testDummyName() throws Exception
    {
        String content = "<static-binding name=''>\nStuff\n</static-binding>";
        doTestDummyName(getXMLDocument(XMLUtil.DTD_1_3, "static-binding", content));
        doTestDummyName(getXMLDocument(XMLUtil.DTD_3_0, "static-binding", content));
    }

    private void doTestDummyName(String content) throws Exception
    {
//        scanner.resetForTestingOnly();
//        Node node = parseToRootNode(content, 0);
//
//        IContainedComponent component = createContainedComponent();
//        scanBinding(component, node);
//
//        // on this pass a null name does not cause a problem
//        // but it may as future validations come online
//        basicCheckProblems(scanner.getProblems(), 0);
//
//        List names = (List) component.getBindingNames();
//        m_assertTrue(names.size() == 1);
//
//        String shouldBeDummy = (String) names.get(0);
//        m_assertTrue(shouldBeDummy.startsWith(validator.getDummyStringPrefix()));
//        System.err.println(shouldBeDummy);
//
//        IBindingSpecification result = component.getBinding(shouldBeDummy);
//        m_assertNotNull(result);
//        m_assertEquals(result.getValue(), "Stuff");
//        m_assertSame(result.getType(), BindingType.STATIC);
    }

    protected void scanBinding(IContainedComponent component, Node child) throws Exception
    {
        try
        {
            if (scanner.isElement(child, "binding"))
            {
                scanner.scanBinding(component, child, BindingType.DYNAMIC, "expression");
                return;
            }

            // Field binding is in 1.3 DTD, but removed from 1.4

            if (scanner.isElement(child, "field-binding"))
            {

                scanner.scanBinding(component, child, BindingType.FIELD, "field-name");
                return;
            }

            if (scanner.isElement(child, "inherited-binding"))
            {
                scanner.scanBinding(component, child, BindingType.INHERITED, "parameter-name");
                return;
            }

            if (scanner.isElement(child, "static-binding"))
            {
                scanner.scanBinding(component, child, BindingType.STATIC, "value");
                return;
            }

            // <string-binding> added in release 2.0.4

            if (scanner.isElement(child, "string-binding"))
            {
                scanner.scanBinding(component, child, BindingType.STRING, "key");
                return;
            }
        } catch (ScannerException e)
        {
            m_fail("unexpected ScannerException");
        }

    }

}
