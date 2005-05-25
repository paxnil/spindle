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

package com.iw.plugins.spindle.core.parser;

import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.parser.XMLParseException;

/**
 * Runtime exception thrown by PullParser when a fatal error occurs
 * 
 * @author glongman@gmail.com
  */
public class ParserRuntimeException extends XMLParseException
{

  /**
   * @param locator
   * @param message
   */
  public ParserRuntimeException(XMLParseException exception)
  {
    super(new XMLLocatorWrapper(exception), exception.getMessage());
  }

  static class XMLLocatorWrapper implements XMLLocator
  {

    private XMLParseException fWrapped;

    public XMLLocatorWrapper(XMLParseException exception)
    {
      this.fWrapped = exception;
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xerces.xni.XMLLocator#getColumnNumber()
     */
    public int getColumnNumber()
    {
      return fWrapped.getColumnNumber();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xerces.xni.XMLLocator#getLineNumber()
     */
    public int getLineNumber()
    {
      return fWrapped.getLineNumber();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xerces.xni.XMLResourceIdentifier#getBaseSystemId()
     */
    public String getBaseSystemId()
    {
      return fWrapped.getBaseSystemId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xerces.xni.XMLResourceIdentifier#getExpandedSystemId()
     */
    public String getExpandedSystemId()
    {
      return fWrapped.getExpandedSystemId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xerces.xni.XMLResourceIdentifier#getLiteralSystemId()
     */
    public String getLiteralSystemId()
    {
      return fWrapped.getLiteralSystemId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xerces.xni.XMLResourceIdentifier#getPublicId()
     */
    public String getPublicId()
    {
      return fWrapped.getPublicId();
    }

  }

}