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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
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
 * Entity resolver that pulls DTDs out of the plugin classpath
 * 
 * @author glongman@gmail.com
 * 
 */
public class TapestryEntityResolver implements XMLEntityResolver
{

  static
  {
    TapestryEntities = new HashMap();
    registerTapestryDTD(
        SpecificationParser.TAPESTRY_DTD_4_0_PUBLIC_ID,
        "Tapestry_4_0.dtd");
    registerTapestryDTD(
        SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID,
        "Tapestry_3_0.dtd");
    ServletEntities = new HashMap();
    registerServletDTD(TapestryCore.SERVLET_2_2_PUBLIC_ID, "web-app_2_2.dtd");
    registerServletDTD(TapestryCore.SERVLET_2_3_PUBLIC_ID, "web-app_2_3.dtd");
  }

  static private Map TapestryEntities;
  static private Map ServletEntities;

  static public void registerTapestryDTD(String publicId, String entityPath)
  {
    TapestryEntities.put(publicId, entityPath);
  }

  static public void registerServletDTD(String publicId, String entityPath)
  {
    ServletEntities.put(publicId, entityPath);
  }

  static public XMLInputSource doResolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException,
      IOException
  {
    XMLInputSource result = getTapestryInputSource(resourceIdentifier);

    if (result == null)
      result = getServletInputSource(resourceIdentifier);

    return result;
  }

  static private InputStream getTapestryDTDInputStream(String publicId)
  {
    String entityPath = (String) TapestryEntities.get(publicId);
    if (entityPath != null)
      return SpecificationParser.class.getResourceAsStream(entityPath);
    return null;
  }

  static private InputStream getServletDTDInputStream(String publicId)
  {
    String entityPath = (String) ServletEntities.get(publicId);
    if (entityPath != null)
      return TapestryCore.class.getResourceAsStream(entityPath);
    return null;
  }

  private static XMLInputSource getTapestryInputSource(
      XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException
  {
    InputStream stream = getTapestryDTDInputStream(resourceIdentifier.getPublicId());
    if (stream == null)
      return null;
    return getInputSource(resourceIdentifier, stream);
  }

  private static XMLInputSource getServletInputSource(
      XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException
  {
    InputStream stream = getServletDTDInputStream(resourceIdentifier.getPublicId());
    if (stream == null)
      return null;
    return getInputSource(resourceIdentifier, stream);
  }

  private static XMLInputSource getInputSource(
      XMLResourceIdentifier resourceIdentifier,
      InputStream stream)
  {
    return new XMLInputSource(
        resourceIdentifier.getPublicId(),
        resourceIdentifier.getLiteralSystemId(),
        resourceIdentifier.getBaseSystemId(),
        stream,
        (String) null);
  }

  public TapestryEntityResolver()
  {
    super();
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLEntityResolver#resolveEntity(XMLResourceIdentifier)
   */
  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException,
      IOException
  {
    return doResolveEntity(resourceIdentifier);
  }

}