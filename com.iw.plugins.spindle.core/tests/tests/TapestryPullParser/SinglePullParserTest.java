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

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLParseException;

/**
 *  Simplest pull parser test
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SinglePullParserTest extends ConfiguredPullParserBase
{
    final String PROLOG_1 =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE dog [\n"
            + "<!ELEMENT dog (#PCDATA)>\n"
            + "<!ATTLIST dog test CDATA #IMPLIED>\n"
            + "]>\n";
    /**
    * Constructor for SimplePullParser.
    * @param arg0
    */
    public SinglePullParserTest(String arg0)
    {
        super(arg0);

    }

    public void testSimpleValidationSuccess() throws Exception
    {
        final String VALID = PROLOG_1 + "<dog test='poo'>Hello, world!</dog>\n";
        parseAll(VALID);

    }

    public void testValidParsingUsingTapestryEntityResolver() throws IOException
    {
        InputStream in = getClass().getResourceAsStream("/testdata/basicTapestryComponent.jwc");
        assertNotNull(in);
        try
        {
            parseAll(in);
        } catch (XMLConfigurationException e)
        {
            fail("XMLConfigurationException: " + e.getMessage());
        } catch (XMLParseException e)
        {
            fail("XMLParseException " + e.getMessage());
        } catch (IOException e)
        {
            fail("IOException " + e.getMessage());
        } finally
        {
            in.close();
        }
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SinglePullParserTest.class);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {

        super.setUp();

    }

}
