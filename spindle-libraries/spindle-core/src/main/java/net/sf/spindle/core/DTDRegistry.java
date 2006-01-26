package net.sf.spindle.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.parse.SpecificationParser;

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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
/**
 * Registry for the DTDs (and hopefully someday schema) used by Spindle
 * 
 * @author glongman@gmail.com
 * 
 */
public class DTDRegistry
{
    public static final String SERVLET_2_2_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";

    public static final String SERVLET_2_3_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";

    public static final String SERVLET_2_4_SCHEMA = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";
    
    static
    {
      TAPESTRY_DTD_ENTITIES = new HashMap<String, String>();
      registerTapestryDTD(
          SpecificationParser.TAPESTRY_DTD_4_0_PUBLIC_ID,
          "Tapestry_4_0.dtd");
      registerTapestryDTD(
          SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID,
          "Tapestry_3_0.dtd");
      SERVLET_DTD_ENTITIES = new HashMap<String, String>();
      registerServletDTD(SERVLET_2_2_PUBLIC_ID, "web-app_2_2.dtd");
      registerServletDTD(SERVLET_2_3_PUBLIC_ID, "web-app_2_3.dtd");
    }

    public static Map<String, String> TAPESTRY_DTD_ENTITIES;
    public static Map<String, String> SERVLET_DTD_ENTITIES;

    static public void registerTapestryDTD(String publicId, String entityPath)
    {
      TAPESTRY_DTD_ENTITIES.put(publicId, entityPath);
    }

    static public void registerServletDTD(String publicId, String entityPath)
    {
      SERVLET_DTD_ENTITIES.put(publicId, entityPath);
    }
    
    static public InputStream getDTDInputStream(String publicId) {
        InputStream result = getTapestryDTDInputStream(publicId);
        if (result != null)
            return result;
        return getServletDTDInputStream(publicId);            
    }
    
    static private InputStream getTapestryDTDInputStream(String publicId)
    {
      String entityPath = (String) TAPESTRY_DTD_ENTITIES.get(publicId);
      if (entityPath != null)
        return SpecificationParser.class.getResourceAsStream(entityPath);
      return null;
    }

    static private InputStream getServletDTDInputStream(String publicId)
    {
      String entityPath = (String) SERVLET_DTD_ENTITIES.get(publicId);
      if (entityPath != null)
        return TapestryCore.class.getResourceAsStream(entityPath);
      return null;
    }
    
    private DTDRegistry()
    {
        super();        
    }
}
