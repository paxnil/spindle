package com.iw.plugins.spindle.parser.xml;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLParserConfiguration;


public class TapestryDOMParser extends DOMParser
{

    /**
     * Constructor for MyDOMParser.
     */
    public TapestryDOMParser()
    {
        super();
    }

    /**
     * Constructor for MyDOMParser.
     * @param config
     */
    public TapestryDOMParser(XMLParserConfiguration config)
    {
        super(config);
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(XMLLocator, String, Augmentations)
     */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException
    {
    	System.out.println("===>Start doc");
        super.startDocument(locator, encoding, augs);
    }

}
