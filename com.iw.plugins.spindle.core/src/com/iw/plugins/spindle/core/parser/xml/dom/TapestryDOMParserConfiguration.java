package com.iw.plugins.spindle.core.parser.xml.dom;
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

import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.validation.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;

import com.iw.plugins.spindle.core.parser.xml.StandardParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.TapestryEntityResolver;
import com.iw.plugins.spindle.core.parser.xml.TapestryXMLDTDValidator;
import com.iw.plugins.spindle.core.parser.xml.XMLDTDScannerImpl;
import com.iw.plugins.spindle.core.parser.xml.XMLDocumentScannerImpl;
import com.iw.plugins.spindle.core.parser.xml.XMLEntityManager;

public class TapestryDOMParserConfiguration extends StandardParserConfiguration
{
    /** custom Xerces Feature identifier*/
    public static final String AUGMENTATIONS = "http://intelligentworks.com/xml/features/augmentations-location";

    public static final XMLGrammarPoolImpl GRAMMAR_POOL = new XMLGrammarPoolImpl();
    /**
     * Constructor for MyConfiguration.
     */
    public TapestryDOMParserConfiguration()
    {
        super();
        addRecognizedFeatures(new String[] { AUGMENTATIONS });

    }

    public TapestryDOMParserConfiguration(XMLGrammarPool grammarPool)
    {
        super(null, grammarPool);
        addRecognizedFeatures(new String[] { AUGMENTATIONS });
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

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.xml.StandardParserConfiguration#createDTDValidator()
     */
    protected XMLDTDValidator createDTDValidator()
    {
        return new TapestryXMLDTDValidator();
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
