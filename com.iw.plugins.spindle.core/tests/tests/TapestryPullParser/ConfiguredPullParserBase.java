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

package tests.TapestryPullParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.xerces.impl.validation.XMLGrammarPoolImpl;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.w3c.dom.Node;

/**
 *  Base class for test requiring a preconfigured parser!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ConfiguredPullParserBase extends PullParserBase
{

    /**
     * @param arg0
     */
    public ConfiguredPullParserBase(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        pullParseConfiguration.setDocumentHandler(pullParser);
        pullParseConfiguration.setErrorHandler(pullParser);
        pullParseConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        pullParseConfiguration.setFeature("http://xml.org/sax/features/validation", true);
        pullParseConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);
        pullParseConfiguration.setProperty(
            "http://apache.org/xml/properties/internal/grammar-pool",
            new XMLGrammarPoolImpl());
    }

    protected void parseAll(final String content) throws XMLConfigurationException, IOException, XMLParseException
    {
        parseAll(new StringReader(content));
    }

    protected void parseAll(InputStream content) throws XMLConfigurationException, IOException, XMLParseException
    {
        parseAll(new BufferedReader(new InputStreamReader(content)));
    }

    protected void parseAll(Reader reader) throws XMLConfigurationException, IOException, XMLParseException
    {
        pullParseConfiguration.setInputSource(new XMLInputSource(null, "", null, reader, null));
        System.err.println("Starting!\n");
        pullParseConfiguration.parse();
        Node node = pullParser.getRootNode();
        boolean stop = false;
        node = node.getFirstChild();
        while (node != null) {
            node = node.getNextSibling();
            if (node == null) {
                break;
            }
        }
           
    }

}
