package com.iw.plugins.spindle.parser.xml;


import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MyConfiguration extends StandardParserConfiguration
{

    /**
     * Constructor for MyConfiguration.
     */
    public MyConfiguration()
    {
        super();
    }

  
    /**
     * @see org.apache.xerces.parsers.StandardParserConfiguration#createDocumentScanner()
     */
    protected XMLDocumentScanner createDocumentScanner()
    {
        return new XMLDocumentScannerImpl();
    }

    /**
     * @see org.apache.xerces.parsers.StandardParserConfiguration#createEntityManager()
     */
    protected XMLEntityManager createEntityManager()
    {
        return new XMLEntityManager();
    }

    /**
     * @see scanner.StandardParserConfiguration#createDTDScanner()
     */
    protected XMLDTDScanner createDTDScanner()
    {
        return new XMLDTDScannerImpl();
    }

    /**
     * @see org.apache.xerces.parsers.BasicParserConfiguration#reset()
     */
    protected void reset() throws XNIException
    {
        super.reset();
        
        setProperty("http://apache.org/xml/properties/internal/entity-resolver", new TapestryEntityResolver());
    }

}
