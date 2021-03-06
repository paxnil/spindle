package net.sf.spindle.xerces.parser.xml.dom;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.source.ISourceLocationResolver;
import net.sf.spindle.xerces.parser.XercesDOMElementSourceLocationInfo;
import net.sf.spindle.xerces.parser.xml.event.ElementXMLEventInfo;

import org.apache.hivemind.Resource;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

public class TapestryDOMParser extends DOMParser
{

    ISourceLocationResolver fResolver;
    Resource fResource;

    /**
     * Constructor for MyDOMParser.
     * 
     * @param config
     */
    public TapestryDOMParser(XMLParserConfiguration config)
    {
        super(config);
    }

    public void setSourceResolver(ISourceLocationResolver resolver)
    {
        fResolver = resolver;
    }

    /**
     * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(QName, Augmentations)
     */
    public void endElement(QName element, Augmentations augs) throws XNIException
    {
        ElementXMLEventInfo eventInfo = (ElementXMLEventInfo) augs
                .getItem(TapestryDOMParserConfiguration.AUGMENTATIONS);
        if (eventInfo != null && fDocument != null)
        {
            if (fResolver != null)
            {
                XercesDOMElementSourceLocationInfo resolvedInfo = new XercesDOMElementSourceLocationInfo(
                        element.rawname, eventInfo, fResolver);
                resolvedInfo.setResource(fResource);
                fDocumentImpl
                        .setUserData(fCurrentNode, TapestryCore.IDENTIFIER, resolvedInfo, null);
            }
            else
            {
                fDocumentImpl.setUserData(fCurrentNode, TapestryCore.IDENTIFIER, eventInfo, null);
            }
        }

        super.endElement(element, augs);
    }

    public Resource getResource()
    {
        return fResource;
    }

    public void setResource(Resource resource)
    {
        fResource = resource;
    }
}