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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package tests.Scanners.mr;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.Location;
import org.apache.tapestry.spec.IBeanSpecification;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Just tests the scanSetProperty method in ComponentScanner
 * 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TestComponentScanSetPropertyMR extends BaseComponentScannerTest
{

    public TestComponentScanSetPropertyMR()
    {
        super();
    }

    /**
     * @param arg0
     */
    public TestComponentScanSetPropertyMR(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    protected IBeanSpecification createBeanSpecification()
    {
        IBeanSpecification result = factory.createBeanSpecification();
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

    public void testSetProperty() throws Exception
    {
        String content = "<set-property name='foo' expression='fun.name'/>";
        doTestSetProperty(getXMLDocument(XMLUtil.DTD_1_3, "set-property", content));
        doTestSetProperty(getXMLDocument(XMLUtil.DTD_3_0, "set-property", content));
    }

    private void doTestSetProperty(String content) throws Exception
    {
        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);

        IBeanSpecification bean = createBeanSpecification();
        scanner.scanSetProperty(bean, node);
        basicCheckProblems(scanner.getProblems(), 0);
        List initializers = bean.getInitializers();
        m_assertFalse(initializers.isEmpty());
        PluginExpressionBeanInitializer result = (PluginExpressionBeanInitializer)initializers.get(0);
        m_assertNotNull(result);
        m_assertEquals("foo", result.getPropertyName());
        m_assertEquals("fun.name",result.getValue());
        m_assertSame(bean.getLocation().getResourceLocation(),result.getLocation().getResourceLocation());
    }

    public void testSetPropertyWithInvalidExpression() throws Exception
    {

        String content = "<set-property name='foo' expression='listeners.{?#this instanceof antlr.TokenListener'/>";
        doTestSetPropertyWithInvalidExpression(getXMLDocument(XMLUtil.DTD_1_3, "set-property", content));
        doTestSetPropertyWithInvalidExpression(getXMLDocument(XMLUtil.DTD_3_0, "set-property", content));
    }

    private void doTestSetPropertyWithInvalidExpression(String content) throws Exception
    {
        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);

        IBeanSpecification bean = createBeanSpecification();
        scanner.scanSetProperty(bean, node);
        basicCheckProblems(scanner.getProblems(), 1);
    }

    public void testDummyName() throws Exception
    {
        String content = "<set-property name='' expression='fun.name'/>";
        doTestDummyName(getXMLDocument(XMLUtil.DTD_1_3, "set-property", content));
        doTestDummyName(getXMLDocument(XMLUtil.DTD_3_0, "set-property", content));
    }

    private void doTestDummyName(String content) throws Exception
    {

        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);

        IBeanSpecification bean = createBeanSpecification();
        scanner.scanSetProperty(bean, node);
        basicCheckProblems(scanner.getProblems(), 1);

    }

}
