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

import com.iw.plugins.spindle.core.TapestryCore;

/**
 *  Entity resolver that pulls DTDs out of the plugin classpath
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TapestryEntityResolver implements XMLEntityResolver
{

    static private Map TapestryEntities = new HashMap();
    static private Map ServletEntities = new HashMap();

    static public void registerTapestryDTD(String publicId, String entityPath)
    {
        TapestryEntities.put(publicId, entityPath);
    }

    static public void registerServletDTD(String publicId, String entityPath)
    {
        ServletEntities.put(publicId, entityPath);
    }

    /**
     * @see org.apache.xerces.xni.parser.XMLEntityResolver#resolveEntity(XMLResourceIdentifier)
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException
    {
        XMLInputSource result = getTapestryInputSource(resourceIdentifier);

        if (result == null)
            result = getServletInputSource(resourceIdentifier);

        return result;
    }

    private XMLInputSource getTapestryInputSource(XMLResourceIdentifier resourceIdentifier)
        throws XNIException, IOException
    {

        String publicId = resourceIdentifier.getPublicId();
        String entityPath = (String) TapestryEntities.get(publicId);

        if (entityPath != null)
        {
            InputStream stream = SpecificationParser.class.getResourceAsStream(entityPath);
            return getInputSource(resourceIdentifier, stream);
        }
        return null;
    }

    private XMLInputSource getServletInputSource(XMLResourceIdentifier resourceIdentifier)
        throws XNIException, IOException
    {
        String publicId = resourceIdentifier.getPublicId();
        String entityPath = (String) ServletEntities.get(publicId);

        if (entityPath != null)
        {
            InputStream stream = TapestryCore.class.getResourceAsStream(entityPath);
            return getInputSource(resourceIdentifier, stream);
        }
        return null;
    }

    private XMLInputSource getInputSource(XMLResourceIdentifier resourceIdentifier, InputStream stream)
    {
        return new XMLInputSource(
            resourceIdentifier.getPublicId(),
            resourceIdentifier.getLiteralSystemId(),
            resourceIdentifier.getBaseSystemId(),
            stream,
            (String) null);
    }
}
