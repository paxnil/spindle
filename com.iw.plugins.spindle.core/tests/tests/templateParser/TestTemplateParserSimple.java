package tests.templateParser;

import java.net.URL;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.parse.ITemplateParserDelegate;

import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;
import com.iw.plugins.spindle.core.util.Files;

/**
 *  To Be Removed. Some simple tests used to figure out
 *  how the TemplateParser works.
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class TestTemplateParserSimple extends TestCase
{

    static private class MockResourceLocation implements IResourceLocation
    {

        private String name;
        private String path;

        public MockResourceLocation(String name, String path)
        {
            this.name = name;
            this.path = path;
        }
        /* (non-Javadoc)
         * @see org.apache.tapestry.IResourceLocation#getLocalization(java.util.Locale)
         */
        public IResourceLocation getLocalization(Locale arg0)
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.IResourceLocation#getName()
         */
        public String getName()
        {
            return name;
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.IResourceLocation#getPath()
         */
        public String getPath()
        {
            return path;
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.IResourceLocation#getRelativeLocation(java.lang.String)
         */
        public IResourceLocation getRelativeLocation(String arg0)
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.IResourceLocation#getResourceURL()
         */
        public URL getResourceURL()
        {
            return null;
        }
        /* (non-Javadoc)
         * @see org.apache.tapestry.IResourceLocation#getLocale()
         */
        public Locale getLocale()
        {
            // TODO Auto-generated method stub
            return null;
        }

    }
    /**
     * Constructor for TestTemplateParser.
     * @param name
     */
    public TestTemplateParserSimple(String name)
    {
        super(name);
    }

    public void testSpindleParser() throws Exception
    {
        ITemplateParserDelegate delegate = new ITemplateParserDelegate()
        {
            public boolean getKnownComponent(String componentId)
            {
                return true;
            }

            public boolean getAllowBody(String componentId, ILocation location)
            {
                return true;
            }

            public boolean getAllowBody(String libraryId, String type, ILocation location)
            {
                return true;
            }
        };
        String input = Files.readFileToString(getClass().getResourceAsStream("/testdata/Dates.html"), null);
        CoreTemplateParser parser = new CoreTemplateParser();
        parser.parse(input.toCharArray(), delegate, new MockResourceLocation("Dates.html", "/tests/testData/"));
    }

    public void testTapestryParser() throws Exception
    {
        ITemplateParserDelegate delegate = new ITemplateParserDelegate()
        {
            public boolean getKnownComponent(String componentId)
            {
                return true;
            }

            public boolean getAllowBody(String componentId, ILocation location)
            {
                return true;
            }

            public boolean getAllowBody(String libraryId, String type, ILocation location)
            {
                return true;
            }
        };
        String input = Files.readFileToString(getClass().getResourceAsStream("/testdata/Dates.html"), null);
        org.apache.tapestry.parse.TemplateParser parser = new org.apache.tapestry.parse.TemplateParser();
        parser.parse(input.toCharArray(), delegate, new MockResourceLocation("Dates.html", "/tests/testData/"));
    }

}
