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

package tests.Parser;

import java.io.IOException;

import org.w3c.dom.Node;

import tests.multirun.MultipleRunTestCase;

import com.iw.plugins.spindle.core.parser.*;
import com.iw.plugins.spindle.core.parser.Parser;

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
/*package*/
class MRBaseParserTest extends MultipleRunTestCase
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
        if (DOM.equals(runIdentifier))
        {
            return parser.parse(content);
        }

        // otherwise its pull

        Node rootNode = null;
        try
        {
            rootNode = parser.parse(content);
            if (rootNode != null)
            {
                for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling());
            }
        } catch (ParserRuntimeException e)
        {}
        return rootNode;
    }

}
