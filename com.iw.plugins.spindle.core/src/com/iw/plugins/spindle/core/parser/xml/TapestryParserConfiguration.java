package com.iw.plugins.spindle.core.parser.xml;
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


import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLErrorHandler;


public class TapestryParserConfiguration extends StandardParserConfiguration 
{
	
	

    /**
     * Constructor for MyConfiguration.
     */
    public TapestryParserConfiguration(XMLErrorHandler errorHandler)
    {
        super();
        if (errorHandler != null) {
        	setProperty(StandardParserConfiguration.ERROR_HANDLER, errorHandler);
        }
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
