package com.iw.plugins.spindle.parser.xml;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MyDOMParser extends DOMParser
{

    /**
     * Constructor for MyDOMParser.
     */
    public MyDOMParser()
    {
        super();
    }

    /**
     * Constructor for MyDOMParser.
     * @param config
     */
    public MyDOMParser(XMLParserConfiguration config)
    {
        super(config);
    }

    /**
     * Constructor for MyDOMParser.
     * @param symbolTable
     */
    public MyDOMParser(SymbolTable symbolTable)
    {
        super(symbolTable);
    }

    /**
     * Constructor for MyDOMParser.
     * @param symbolTable
     * @param grammarPool
     */
    public MyDOMParser(SymbolTable symbolTable, XMLGrammarPool grammarPool)
    {
        super(symbolTable, grammarPool);
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
