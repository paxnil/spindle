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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TapestryEntityResolver implements XMLEntityResolver
{

    static private Map entities = new HashMap();

    static public void register(String publicId, String entityPath)
    {

        entities.put(publicId, entityPath);

    }

    /**
     * @see org.apache.xerces.xni.parser.XMLEntityResolver#resolveEntity(XMLResourceIdentifier)
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException
    {
        String publicId = resourceIdentifier.getPublicId();

        String entityPath = null;

        entityPath = (String) entities.get(publicId);

        if (entityPath != null)
        {
            InputStream stream = SpecificationParser.class.getResourceAsStream(entityPath);

            XMLInputSource result =
                new XMLInputSource(
                    resourceIdentifier.getPublicId(),
                    resourceIdentifier.getLiteralSystemId(),
                    resourceIdentifier.getBaseSystemId(),
                    stream,
                    (String) null);

            return result;

        }

        return null;

    }
}
