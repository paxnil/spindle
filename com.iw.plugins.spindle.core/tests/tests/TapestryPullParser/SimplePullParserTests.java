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

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.validation.XMLGrammarPoolImpl;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;

import com.iw.plugins.spindle.core.parser.xml.TapestryEntityResolver;

/**
 *  Simplest pull parser tests
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SimplePullParserTests extends ConfiguredPullParserBase
{

    final String PROLOG_1 =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<!DOCTYPE dog [\n"
            + "<!ELEMENT dog (#PCDATA)>\n"
            + "<!ATTLIST dog test CDATA #IMPLIED>\n"
            + "]>\n";

    protected XMLEntityResolver testResolver;

    protected final String GRAMMAR_POOL = Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    protected final String TAPESTRY_1_3_PUBLIC_ID = SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID;
    /**
     * Constructor for SimplePullParser.
     * @param arg0
     */
    public SimplePullParserTests(String arg0)
    {
        super(arg0);

    }

    public void testSimpleValidationSuccess() throws Exception
    {
        final String VALID = PROLOG_1 + "<dog test='poo'>Hello, world!<cat>stupid</cat></dog>\n";
        parseAll(VALID);

    }

    public void testSimpleValidationFailure() throws Exception
    {
        final String INVALID = PROLOG_1 + "<cat test='poo'>Hello, world!</cat>\n";
        try
        {
            parseAll(INVALID);
        } catch (XMLConfigurationException e)
        {
            fail("XMLConfigurationException: " + e.getMessage());
        } catch (XMLParseException e)
        {
            fail("XMLParseException " + e.getMessage());
        } catch (IOException e)
        {
            fail("IOException " + e.getMessage());
        }
    }

    public void testSimpleMalformedFailure() throws Exception
    {
        final String INVALID = PROLOG_1 + "<dog test='poo'>Hello, world!\n";
        try
        {
            parseAll(INVALID);
            fail("parse was supposed to fail");
        } catch (XMLConfigurationException e)
        {
            fail("XMLConfigurationException " + e.getMessage());
        } catch (XMLParseException e)
        {
            assertEquals(e.getMessage().trim(), "XML document structures must start and end within the same entity.");
        } catch (IOException e)
        {
            fail("IOException " + e.getMessage());
        }
    }

    public void testTapestryEntityResolver() throws Exception
    {
        TapestryEntityResolver.register(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID, "Tapestry_1_3.dtd");
        final TapestryEntityResolver RESOLVER = new TapestryEntityResolver();
        // lets wrap the resolver so that the default fallback behaviour of xerces is short circuited
        XMLEntityResolver testResolver = new XMLEntityResolver()
        {
            public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException
            {
                XMLInputSource result = RESOLVER.resolveEntity(resourceIdentifier);
                if (result == null)
                {
                    throw new XNIException("resolve failed");
                }
                return result;
            }
        };
        XMLResourceIdentifierImpl id = new XMLResourceIdentifierImpl("cat", null, null, null);
        try
        {
            testResolver.resolveEntity(id);
            fail("resolver fail test: didn't fail!");
        } catch (XNIException e)
        {
            //this is expected
        } catch (IOException e)
        {
            fail("resolver fail test: IOException " + e.getMessage());
        }
        id.setPublicId(TAPESTRY_1_3_PUBLIC_ID);
        try
        {
            testResolver.resolveEntity(id);
        } catch (XNIException e1)
        {
            fail("resolver pass test: XNIException " + e1.getMessage());
        } catch (IOException e1)
        {
            fail("resolver pass test: IOException " + e1.getMessage());
        }
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

    public void testGrammarPool() throws IOException
    {
        XMLGrammarPoolImpl pool = (XMLGrammarPoolImpl) pullParseConfiguration.getProperty(GRAMMAR_POOL);
        assertNotNull("no pool configured!", pool);
        assertNull("pool is not empty!", pool.getGrammar(TAPESTRY_1_3_PUBLIC_ID));

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

        assertNull("pool shouldn't cache DTD until the parser is reset!", pool.getGrammar(TAPESTRY_1_3_PUBLIC_ID));

        // setInputSource in the parser forces a reset, which triggers the caching
        Reader reader =
            new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/testdata/basicTapestryComponent.jwc")));
        pullParseConfiguration.setInputSource(new XMLInputSource(null, "", null, reader, null));
        assertTrue("parser didn't stop!", pullParseConfiguration.parse(false));
        assertNotNull("pool didn't cache the DTD", pool.getGrammar(TAPESTRY_1_3_PUBLIC_ID));

    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SimplePullParserTests.class);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {

        super.setUp();

    }

}
