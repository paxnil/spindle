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

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.ElementSourceLocationInfo;
import com.iw.plugins.spindle.core.parser.ISourceLocationResolver;
import com.iw.plugins.spindle.core.parser.xml.TapestryParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.event.ElementXMLEventInfo;

public class TapestryDOMParser extends DOMParser
{

    ISourceLocationResolver resolver;

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

    public void setSourceResolver(ISourceLocationResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(QName, Augmentations)
     */
    public void endElement(QName element, Augmentations augs) throws XNIException
    {
        ElementXMLEventInfo eventInfo = (ElementXMLEventInfo) augs.getItem(TapestryParserConfiguration.AUGMENTATIONS);
        if (eventInfo != null && fDocument != null)
        {
            if (resolver != null)
            {
                ElementSourceLocationInfo resolvedInfo = new ElementSourceLocationInfo(eventInfo, resolver);
                fDocumentImpl.setUserData(fCurrentNode, TapestryCore.PLUGIN_ID, resolvedInfo, null);
            } else
            {
                fDocumentImpl.setUserData(fCurrentNode, TapestryCore.PLUGIN_ID, eventInfo, null);
            }
        }

        super.endElement(element, augs);
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(QName, XMLAttributes, Augmentations)
     */
//    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException
//    {
//        ElementXMLEventInfo eventInfo = (ElementXMLEventInfo) augs.getItem(TapestryParserConfiguration.AUGMENTATIONS);
//        if (eventInfo != null)
//        {
//            if (resolver != null)
//            {
//                ElementSourceLocationInfo resolvedInfo = new ElementSourceLocationInfo(eventInfo, resolver);
//                fDocumentImpl.setUserData(fCurrentNode, TapestryCore.PLUGIN_ID, resolvedInfo, null);
//            } else
//            {
//                fDocumentImpl.setUserData(fCurrentNode, TapestryCore.PLUGIN_ID, eventInfo, null);
//            }
//        }
//        super.emptyElement(element, attributes, augs);
//    }

}
