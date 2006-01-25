package com.iw.plugins.spindle.xerces.parser.xml;
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

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import core.DTDRegistry;



/**
 * Entity resolver that pulls DTDs out of the plugin classpath
 * 
 * @author glongman@gmail.com
 * 
 */
public class TapestryEntityResolver implements XMLEntityResolver
{

  static public XMLInputSource doResolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException,
      IOException
  {
    return getInputSource(resourceIdentifier);
  }

  private static XMLInputSource getInputSource(
      XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException
  {
    InputStream stream = DTDRegistry.getDTDInputStream(resourceIdentifier.getPublicId());
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