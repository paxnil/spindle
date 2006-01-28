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

package net.sf.spindle.xerces.parser.xml.dom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Base class for test requiring a preconfigured parser!
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public abstract class ConfiguredDOMParserBase extends DOMParserBase implements XMLErrorHandler
{

    /**
     * @param arg0
     */
    public ConfiguredDOMParserBase(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        parserConfiguration.setDocumentHandler(domParser);
        parserConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        parserConfiguration.setFeature("http://xml.org/sax/features/validation", true);
        parserConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);
        parserConfiguration.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        parserConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        parserConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        parserConfiguration.setFeature("http://xml.org/sax/features/validation", true);
        parserConfiguration.setDocumentHandler(domParser);
        parserConfiguration.setErrorHandler(this);

    }

    protected void parseAll(final String content) throws SAXException, IOException
    {

        parseAll(new StringReader(content));
    }

    protected void parseAll(InputStream content) throws SAXException, IOException
    {
        parseAll(new BufferedReader(new InputStreamReader(content)));

    }

    protected void parseAll(Reader reader) throws SAXException, IOException
    {
        InputSource source = new InputSource(reader);
        domParser.parse(source);
        assertNotNull("parse was succesful but no document!", domParser.getDocument());

    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void error(String domain, String key, XMLParseException exception) throws XNIException
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void fatalError(String domain, String key, XMLParseException exception) throws XNIException
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void warning(String domain, String key, XMLParseException exception) throws XNIException
    {
        // do nothing
    }

}
