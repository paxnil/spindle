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
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Just tests the scanAsset method in in ComponentScanner
 * 
 *  //TODO must be made complete. Also the scanner can't validate whether the asset exists yet
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TestComponentScanAssetMR extends BaseComponentScannerTest
{

    public TestComponentScanAssetMR()
    {
        super();
    }

    /**
     * @param arg0
     */
    public TestComponentScanAssetMR(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        super.setUp();

    }

    protected IComponentSpecification createComponentSpec()
    {
        IComponentSpecification result = factory.createComponentSpecification();
        // don't really care that all the methods return null
        IResourceLocation rlocation = new IResourceLocation()
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
        };
        result.setSpecificationLocation(rlocation);
        return result;
    }

    protected String getXMLAsset(AssetType type, String name, String value)
    {
        StringBuffer buffer = new StringBuffer("<");
        if (type == AssetType.PRIVATE)
        {
            buffer.append("private-asset ");
        } else if (type == AssetType.EXTERNAL)
        {
            buffer.append("context-asset ");

        } else if (type == AssetType.EXTERNAL)
        {
            buffer.append("context-asset ");

        }
        buffer.append("name='");
        buffer.append(name);
        buffer.append("' ");
        if (type == AssetType.PRIVATE)
        {
            buffer.append("resource-path='");
        } else if (type == AssetType.EXTERNAL)
        {
            buffer.append("path='");

        } else if (type == AssetType.EXTERNAL)
        {
            buffer.append("URL='");

        }
        buffer.append(value);
        buffer.append("'");
        buffer.append("/>");
        return buffer.toString();
    }

    public void testPrivateAsset() throws Exception
    {
        String content = getXMLAsset(AssetType.PRIVATE, "css", "/a/b/c/poo.css");
        doTestPrivateAsset(getXMLDocument(XMLUtil.DTD_1_3, "private-asset", content));
        doTestPrivateAsset(getXMLDocument(XMLUtil.DTD_3_0, "private-asset", content));
    }

    private void doTestPrivateAsset(String content) throws Exception
    {
        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);
        IComponentSpecification specification = createComponentSpec();
        scanAsset(specification, node);
        basicCheckProblems(scanner.getProblems(), 0);
        IAssetSpecification result = specification.getAsset("css");
        m_assertNotNull(result);
        m_assertEquals(result.getPath(), "/a/b/c/poo.css");
        m_assertSame(result.getType(), AssetType.PRIVATE);
        m_assertSame(result.getLocation().getResourceLocation(), specification.getSpecificationLocation());
    }

    public void testDummyName() throws Exception
    {

        String content = getXMLAsset(AssetType.PRIVATE, "", "/a/b/c/poo.css");
        doTestDummyName(getXMLDocument(XMLUtil.DTD_1_3, "private-asset", content));
        doTestDummyName(getXMLDocument(XMLUtil.DTD_3_0, "private-asset", content));
    }

    private void doTestDummyName(String content) throws Exception
    {
        scanner.resetForTestingOnly();
        Node node = parseToRootNode(content, 0);
        IComponentSpecification specification = createComponentSpec();
        scanAsset(specification, node);

        basicCheckProblems(scanner.getProblems(), 1);

        List names = specification.getAssetNames();
        m_assertTrue(names.size() == 1);

        String shouldBeDummy = (String) names.get(0);
        m_assertTrue(shouldBeDummy.startsWith(validator.getDummyStringPrefix()));
        System.err.println(shouldBeDummy);

        IAssetSpecification result = specification.getAsset(shouldBeDummy);
        m_assertNotNull(result);
        m_assertEquals(result.getPath(), "/a/b/c/poo.css");
        m_assertSame(result.getType(), AssetType.PRIVATE);
    }

    public void testTemplateName() throws Exception
    {
        String content = getXMLAsset(AssetType.PRIVATE, "$template", "/a/b/c/poo.html");
        doTestTemplateName(getXMLDocument(XMLUtil.DTD_1_3, "private-asset", content));
        doTestTemplateName(getXMLDocument(XMLUtil.DTD_3_0, "private-asset", content));
    }

    private void doTestTemplateName(String content) throws Exception
    {
        Node node = parseToRootNode(content, 0);
        IComponentSpecification specification = createComponentSpec();
        scanAsset(specification, node);

        basicCheckProblems(scanner.getProblems(), 0);

        List names = specification.getAssetNames();
        m_assertTrue(names.size() == 1);
    }

    //    public void testExpressionQuickOnce() throws Exception
    //    {
    //        validator.validateExpression("long.name", IProblem.ERROR);
    //    }
    //
    //    public void testExpressionQuick100() throws Exception
    //    {
    //        for (int i = 0; i < 100; i++)
    //            validator.validateExpression("long.name", IProblem.ERROR);
    //    }
    //
    //    public void testPatternQuickOnce() throws Exception
    //    {
    //        validator.validatePattern(
    //            "a22356768788.999",
    //            SpecificationParser.ASSET_NAME_PATTERN,
    //            "SpecificationParser.invalid-asset-name",
    //            IProblem.ERROR);
    //    }
    //
    //    public void testPatternQuick100() throws Exception
    //    {
    //        for (int i = 0; i < 100; i++)
    //            validator.validatePattern(
    //                "a22356768788.999",
    //                SpecificationParser.ASSET_NAME_PATTERN,
    //                "SpecificationParser.invalid-asset-name",
    //                IProblem.ERROR);
    //    }

    protected void scanAsset(IComponentSpecification specification, Node node) throws Exception
    {
        try
        {
            if (scanner.isElement(node, "external-asset"))
            {
                scanner.scanAsset(specification, node, AssetType.EXTERNAL, "URL");
                return;

            }

            if (scanner.isElement(node, "context-asset"))
            {
                scanner.scanAsset(specification, node, AssetType.CONTEXT, "path");
                return;
            }

            if (scanner.isElement(node, "private-asset"))
            {
                scanner.scanAsset(specification, node, AssetType.PRIVATE, "resource-path");
                return;
            }
        } catch (ScannerException e)
        {

            m_fail("unexpected ScannerException");

        }

    }

}
