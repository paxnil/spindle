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

/**
 *  Initial basic pull parser test
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TestBasicPullParser extends PullParserBase
{

    /**
     * Constructor for SimplePullParser.
     * @param arg0
     */
    public TestBasicPullParser(String arg0)
    {
        super(arg0);
    }

    public void testInstantiatePullParser()
    {
        pullParseConfiguration.setDocumentHandler(pullParser);
        pullParseConfiguration.setErrorHandler(pullParser);
        pullParseConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
        pullParseConfiguration.setFeature("http://xml.org/sax/features/validation", true);
        pullParseConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);
        assertTrue("incorrect configuration: document handler", pullParseConfiguration.getDocumentHandler() == pullParser);
        assertTrue("incorrect configuration: error handler", pullParseConfiguration.getErrorHandler() == pullParser);
        assertTrue(
            "incorrect configuration: continue-after-fatal-error",
            !pullParseConfiguration.getFeature("http://apache.org/xml/features/continue-after-fatal-error"));
        assertTrue(
            "incorrect configuration: validation",
            pullParseConfiguration.getFeature("http://xml.org/sax/features/validation"));
        assertTrue(
            "incorrect configuration: augmentations",
            pullParseConfiguration.getFeature("http://intelligentworks.com/xml/features/augmentations-location"));

    }

}
