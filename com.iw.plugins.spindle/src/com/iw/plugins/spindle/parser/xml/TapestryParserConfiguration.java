package com.iw.plugins.spindle.parser.xml;


import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;


public class TapestryParserConfiguration extends StandardParserConfiguration
{

    /**
     * Constructor for MyConfiguration.
     */
    public TapestryParserConfiguration()
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
