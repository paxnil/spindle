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

import org.apache.xerces.xni.parser.XMLErrorHandler;

/**
 *  Base class for test requiring a preconfigured parser!
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public abstract class ValidatingDOMParserBase extends DOMParserBase implements XMLErrorHandler
{

    /**
     * @param arg0
     */
    public ValidatingDOMParserBase(String arg0)
    {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();        
        parserConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        parserConfiguration.setFeature("http://xml.org/sax/features/validation", true);
        parserConfiguration.setFeature(TapestryDOMParserConfiguration.AUGMENTATIONS, true);
        parserConfiguration.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        parserConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        parserConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        
        parserConfiguration.setDocumentHandler(domParser);
        parserConfiguration.setErrorHandler(this);

    }

}
