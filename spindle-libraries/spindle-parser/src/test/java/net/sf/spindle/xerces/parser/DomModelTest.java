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

package net.sf.spindle.xerces.parser;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import net.sf.spindle.core.AbstractTestCase;
import net.sf.spindle.core.SuiteOfTestCases;
import net.sf.spindle.core.TestLogger.LoggingEvent;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.parser.IDOMModelSource;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.hivemind.Resource;
import org.easymock.MockControl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author glongman@gmail.com
 */
public class DomModelTest extends AbstractTestCase
{
    private IDOMModelSource modelSource;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(DomModelTest.class);
    }

    public DomModelTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        modelSource = new XercesDOMModelSource();
        setUpTapestryCore();
    }

    public void setUpSuite() throws Exception
    {
        // do nothing
    }

    @Override
    protected void tearDown() throws Exception
    {
        destroyTapestryCore();
        logger.clear();
    }

    public void testNPE()
    {
        ICoreResource resource = null;

        IDOMModel model = modelSource.parseDocument(resource, false, this);

        assertNull(model.getDocument());
        assertEquals(0, model.getProblems().length);
        assertFalse(model.hasFatalProblems());
        List<LoggingEvent> events = getInterceptedLogEvents();
        assertEquals(1, events.size());
        assertEquals("java.lang.NullPointerException", events.get(0).getException().getClass()
                .getName());
    }
    
    public void testMalformed() {
        String completeContent = "<foo/><foo/>";
        ICoreResource resource = getResource(completeContent);

        mockContainer.replayControls();

        IDOMModel model = modelSource.parseDocument(resource, false, this);
        assertNotNull(model);
        try
        {
            Document document = model.getDocument();

            assertNull(document);
            assertTrue(model.getProblems().length == 1);

        }
        finally
        {
            model.release();
        }

        assertEquals(0, logger.size());
        
        mockContainer.verifyControls();
    }

    public void testWellFormed1()
    {
        doTestStringBasic(new String[]
        { "<private-asset name='poo' path='moo'/>" });
    }

    public void testWellFormed2()
    {
        doTestStringBasic(new String[]
        { "<dog></dog>" });
    }

    public void testWellFormed3()
    {
        doTestStringBasic(new String[]
        { "<dog><foo fix='no'/></dog>" });
    }
    
    public void testWellFormed4()
    {
        doTestStringBasic(new String[]
        { "<dog><foo fix='no'>\n\n   \n <private-asset name='poo' path='moo'/> sfdkasjldkjsadlksjd \n\n </foo></dog>" });
    }
    
    public void testWellFormed5()
    {
        doTestStringBasic(new String[]
        { "<dog><foo fix='no'>\n\n  <!-- booo \n   \n   \n--> \n <private-asset name='poo' path='moo'/> sfdkasjldkjsadlksjd \n\n </foo></dog>" });
    }

    private void doTestStringBasic(String[] content)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < content.length; i++)
            builder.append(content[i]);

        String completeContent = builder.toString();
        ICoreResource resource = getResource(completeContent);

        mockContainer.replayControls();

        IDOMModel model = modelSource.parseDocument(resource, false, this);
        assertNotNull(model);
        try
        {
            Document document = model.getDocument();

            assertNotNull(document);
            assertTrue(model.getProblems().length == 0);

            Node root = document.getDocumentElement();

            visitAndAssert(model, root, resource, completeContent);

        }
        finally
        {
            model.release();
        }

        assertEquals(0, logger.size());

        mockContainer.verifyControls();
    }

    private void visitAndAssert(IDOMModel model, Node node, Resource resource,
            String completeContent)
    {
        basicNodeSourceLocationInfoAssertions(model, node, resource, completeContent);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            visitAndAssert(model, child, resource, completeContent);
        }
    }

    private void basicNodeSourceLocationInfoAssertions(IDOMModel model, Node node,
            Resource resource, String content)
    {
        ISourceLocation srcloc = null;
        ISourceLocationInfo locationInfo = model.getSourceLocationInfo(node);

        assertNotNull(locationInfo);

        Resource srclocResource = locationInfo.getResource();
        assertEquals(resource, srclocResource);

        NamedNodeMap attrs = node.getAttributes();
        int attrlen = attrs.getLength();

        if (attrlen > 0)
        {
            assertTrue(locationInfo.hasAttributes());
            Set attributeNames = locationInfo.getAttributeNames();
            assertEquals(attrlen, attributeNames.size());

            for (int i = 0; i < attrlen; i++)
            {
                Attr attribute = (Attr) attrs.item(i);
                String name = attribute.getName();
                assertTrue(attributeNames.contains(name));
                srcloc = locationInfo.getAttributeSourceLocation(name);
                assertNotNull(srcloc);
                assertEquals(attribute.getValue(), checkAndGetNodeContent(content, srcloc));
            }
        }
        srcloc = locationInfo.getStartTagSourceLocation();
        assertNotNull(srcloc);

        ISourceLocation endTagLoc = locationInfo.getEndTagSourceLocation();
        assertNotNull(endTagLoc);

        if (locationInfo.isEmptyTag())
            assertEquals(srcloc, endTagLoc);

        srcloc = locationInfo.getEndTagSourceLocation();
        assertNotNull(srcloc);

        srcloc = locationInfo.getSourceLocation();
        assertNotNull(srcloc);

        srcloc = locationInfo.getContentSourceLocation();
        if (!locationInfo.isEmptyTag())
            assertNotNull(srcloc);
        else
            assertNull(srcloc);

        srcloc = locationInfo.getTagNameLocation();
        assertNotNull(srcloc);
        assertEquals(node.getNodeName(), checkAndGetNodeContent(content, srcloc));
    }

    private String checkAndGetNodeContent(String content, ISourceLocation location)
    {
        int start = location.getCharStart();
        int stop = location.getCharEnd();
        assertTrue(start <= stop);
        return content.substring(start, stop);
    }

    private ICoreResource getResource(final String content)
    {
        MockControl control = mockContainer.newControl(ICoreResource.class);
        ICoreResource result = (ICoreResource) control.getMock();
        control.expectAndReturn(
                result.getContents(),
                new ByteArrayInputStream(content.getBytes()),
                MockControl.ONE);
        return result;
    }
}
