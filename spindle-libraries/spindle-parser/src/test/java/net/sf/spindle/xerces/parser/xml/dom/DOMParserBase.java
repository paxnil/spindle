package net.sf.spindle.xerces.parser.xml.dom;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Base for PullParser tests
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public abstract class DOMParserBase extends TestCase implements XMLErrorHandler
{

    protected XMLParserConfiguration parserConfiguration;
    protected TapestryDOMParser domParser;

    public DOMParserBase(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        parserConfiguration = new TapestryDOMParserConfiguration(new TapestryDOMParserConfiguration.GrammarPoolImpl());
        domParser = new TapestryDOMParser(parserConfiguration);
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

    public void error(String domain, String key, XMLParseException exception) throws XNIException
    {
        // do nothing
    }

    public void fatalError(String domain, String key, XMLParseException exception) throws XNIException
    {
        // do nothing
    }

    public void warning(String domain, String key, XMLParseException exception) throws XNIException
    {
        // do nothing
    }

}
