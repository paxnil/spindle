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

package tests.TapestryDOMParser;

import junit.framework.TestCase;

import org.apache.xerces.xni.parser.XMLParserConfiguration;

import com.iw.plugins.spindle.core.parser.xml.TapestryParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParser;

/**
 *  Base for PullParser tests
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DOMParserBase extends TestCase
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
        parserConfiguration = new TapestryParserConfiguration(TapestryParserConfiguration.GRAMMAR_POOL);
        domParser = new TapestryDOMParser(parserConfiguration);
    }

}
