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

package tests.Parser.mr;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.w3c.dom.Node;

import tests.multirun.MultipleRunTestCase;

import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.parser.ParserRuntimeException;
import com.iw.plugins.spindle.core.util.Files;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Base class for running Parser Tests multiple run
 * 
 *  Expects run idenfiers "DOM" and "PULL";
 * 
 *  Will create (in setup()) an appropriately configured Parser instance
 *  Make sure that you override setup() in subclasses to call super.setup()!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MRBaseParserTest extends MultipleRunTestCase
{

    protected Parser parser;

    public static final String DOM = "DOM";

    public static final String PULL = "PULL";

    /**
     * 
     */
    public MRBaseParserTest()
    {
        super();
    }

    /**
     * @param arg0
     */
    public MRBaseParserTest(String arg0)
    {
        super(arg0);
    }

    protected boolean isDOMRun()
    {
        return DOM.equals(runIdentifier);
    }

    protected boolean isPULLRun()
    {
        return PULL.equals(runIdentifier);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        boolean useDOM = MRBaseParserTest.DOM.equals(runIdentifier);
        boolean usePull = MRBaseParserTest.PULL.equals(runIdentifier);
        if (useDOM)
        {
            parser = new Parser(false);
        } else if (usePull)
        {
            parser = new Parser(true);
        } else
        {
            throw new IllegalStateException("unrecognized run id = " + runIdentifier);
        }
    }

    // use when a test wants to simply scan all the document
    // Needed for Pull, appropriate for both PULL & DOM

    protected Node parseAll(String content) throws IOException
    {
        Node result = parser.parse(content);
        // recall that dom parses everything.
        // to get the same results in this test using pull,
        // we need to traverse all the nodes!
        if (runIdentifier == PULL)
        {
            result = pullAll(result);
        }
        return result;
    }

    protected Node pullAll(Node node)
    {
        if (node == null)
        {
            return node;
        }
        Node result = null;
        try
        {
            Node nextNode = node.getFirstChild();
            if (nextNode == null)
            {
                result = node;
            } else
            {

                result = nextNode;
                while (nextNode != null)
                {
                    nextNode = nextNode.getNextSibling();
                    result = nextNode;
                }
            }

        } catch (ParserRuntimeException e)
        {
            result = null;
        }
        return result;
    }

    protected String getDTDPreamble(int DTDVersion, String rootNode)
    {
        String publicId = XMLUtil.getPublicId(DTDVersion);
        StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        buffer.append("<!DOCTYPE ");
        buffer.append(rootNode);
        buffer.append(" \n");
        buffer.append("PUBLIC \"");
        buffer.append(publicId);
        buffer.append("\"\n");
        buffer.append(" \"http://ignored\">\n");
        return buffer.toString();
    }

    protected String getXMLDocument(int DTDVersion, String rootNode, String content)
    {
        return getDTDPreamble(DTDVersion, rootNode) + content;
    }

    protected void basicCheckProblems(IProblem[] problems, int expectedCount)
    {
        PrintStream stream = problems.length == expectedCount ? System.out : System.err;
        printProblems(problems, stream);
        m_assertEquals(expectedCount, problems.length);
    }
    protected void printProblems(IProblem[] problems, PrintStream stream)
    {
        for (int i = 0; i < problems.length; i++)
        {
            stream.println(runIdentifier + ":" + getClass().getName() + ":" + getName() + " " + problems[i]);
        }
    }

    protected Node parseToRootNode(String content, int expectedParserProblems)
    {
        Node node = null;
        try
        {
            node = parser.parse(content);

            IProblem[] parserProblems = parser.getProblems();
            basicCheckProblems(parserProblems, expectedParserProblems);
            m_assertNotNull(node);
        } catch (IOException e)
        {
            m_fail("failed to parse, IOException: " + e.getMessage());
        }
        return node;
    }

    /**
     * 
     * Load a string from a resource on the classpath
     * 
     * <p>
     * example path: /testdata/basicTapestryComponent.jwc 
     * 
     * @param path the path to pull the data from. should be an absolute path only
     * @return the String unless the load fails, in which case a junit assertion will fail
     */
    protected String getXMLSourceAsString(String path)
    {
        try
        {
            InputStream in = getClass().getResourceAsStream(path);
            m_assertNotNull("failed to load: " + path, in);
            return Files.readFileToString(in, null);
        } catch (IOException e)
        {
            m_fail("IOException:" + e.getMessage());
        }
        // unreachable!
        return null;
    }

}
