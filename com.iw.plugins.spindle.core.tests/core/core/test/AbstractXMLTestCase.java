package core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.hivemind.Resource;
import org.apache.tapestry.parse.SpecificationParser;
import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iw.plugins.spindle.core.parser.dom.IDOMModel;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.source.SourceLocation;
import com.iw.plugins.spindle.core.util.XMLPublicIDUtil;

public abstract class AbstractXMLTestCase extends AbstractTestCase
{
    public static class EResolver implements EntityResolver
    {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                IOException
        {
            InputSource result = null;
            int version = XMLPublicIDUtil.getDTDVersion(publicId);
            switch (version)
            {
                case XMLPublicIDUtil.DTD_3_0:
                    result = new InputSource(SpecificationParser.class
                            .getResourceAsStream("Tapestry_3_0.dtd"));
                    break;

                case XMLPublicIDUtil.DTD_4_0:
                    result = new InputSource(SpecificationParser.class
                            .getResourceAsStream("Tapestry_4_0.dtd"));
                    break;

                default:
                    break;

            }
            return result;
        }
    }

    private static ArgumentsMatcher SRC_LOC_INFO_MATCHER = new ArgumentsMatcher() {

        public boolean matches(Object[] expected, Object[] actual)
        {            
            return actual.length == 1 && actual[0] != null;
        }

        public String toString(Object[] arg0)
        {            
            return "SRC_LOC_INFO_MATCHER";
        }
        
    };
   
    public static final ISourceLocationInfo DUMMY_ISOURCE_LOCATION_INFO = new ISourceLocationInfo()
    {

        public boolean hasAttributes()
        {
            return false;
        }

        public boolean isEmptyTag()
        {
            return false;
        }

        public ISourceLocation getSourceLocation()
        {

            return SourceLocation.FILE_LOCATION;
        }

        public ISourceLocation getContentSourceLocation()
        {
            return SourceLocation.FILE_LOCATION;
        }

        public ISourceLocation getTagNameLocation()
        {
            return SourceLocation.FILE_LOCATION;
        }

        public ISourceLocation getStartTagSourceLocation()
        {
            return SourceLocation.FILE_LOCATION;
        }

        public ISourceLocation getEndTagSourceLocation()
        {
            return SourceLocation.FILE_LOCATION;
        }

        public ISourceLocation getAttributeSourceLocation(String rawname)
        {
            return SourceLocation.FILE_LOCATION;
        }

        public Set getAttributeNames()
        {
            return Collections.EMPTY_SET;
        }

        public void setResource(Resource location)
        {
            // Do nothing

        }

        public int getOffset()
        {

            return 0;
        }

        public int getLength()
        {

            return 0;
        }

        public Resource getResource()
        {

            return null;
        }

        public int getLineNumber()
        {

            return 0;
        }

        public int getColumnNumber()
        {
            return 0;
        }

    };

    
    public AbstractXMLTestCase(String name)
    {
        super(name);
    }

    protected Document getDocument(InputStream in) throws Exception 
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EResolver());        
        return builder.parse(in);
    }

    protected IDOMModel createMockIDOMModel(Document document)
    {
        IDOMModel model = null;
        MockControl control = mockContainer.newControl(IDOMModel.class);
        model = (IDOMModel) control.getMock();
    
        control.expectAndReturn(model.getDocument(), document, MockControl.ONE);
        control.expectAndReturn(
                model.getSourceLocationInfo(null),
                AbstractXMLTestCase.DUMMY_ISOURCE_LOCATION_INFO,
                MockControl.ZERO_OR_MORE);
        control.setMatcher(SRC_LOC_INFO_MATCHER); 
        return model;
    }

}
