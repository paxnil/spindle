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
/**
 *  Initial basic pull parser test
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class TestBasicDOMParser extends DOMParserBase
{

    /**
     * Constructor for SimplePullParser.
     * @param arg0
     */
    public TestBasicDOMParser(String arg0)
    {
        super(arg0);
    }

    public void testInstantiateDOMParser()
    {
        parserConfiguration.setDocumentHandler(domParser);
        parserConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        parserConfiguration.setFeature("http://xml.org/sax/features/validation", true);
        parserConfiguration.setFeature(TapestryDOMParserConfiguration.AUGMENTATIONS, true);
        assertTrue("incorrect configuration: document handler", parserConfiguration.getDocumentHandler() == domParser);
        assertTrue(
            "incorrect configuration: continue-after-fatal-error",
            !parserConfiguration.getFeature("http://apache.org/xml/features/continue-after-fatal-error"));
        assertTrue(
            "incorrect configuration: validation",
            parserConfiguration.getFeature("http://xml.org/sax/features/validation"));
        assertTrue(
            "incorrect configuration: augmentations",
            parserConfiguration.getFeature(TapestryDOMParserConfiguration.AUGMENTATIONS));

    }

}
