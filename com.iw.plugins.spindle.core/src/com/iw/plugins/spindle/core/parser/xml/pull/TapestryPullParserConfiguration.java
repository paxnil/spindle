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

package com.iw.plugins.spindle.core.parser.xml.pull;

import java.io.IOException;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;

import com.iw.plugins.spindle.core.parser.ParserRuntimeException;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParserConfiguration;

/**
 * A configuration used by TapestryPullParser
 * 
 * @author glongman@intelligentworks.com
  */
public class TapestryPullParserConfiguration extends TapestryDOMParserConfiguration
{
  private boolean fStopParsing = false;

  public TapestryPullParserConfiguration()
  {
    super();
  }

  public boolean parse() throws ParserRuntimeException, IOException
  {
    try
    {
      return parse(false);
    } catch (XMLParseException e)
    {
      throw new ParserRuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.parser.XMLPullParserConfiguration#parse(boolean)
   */
  public boolean parse(boolean complete) throws XNIException, IOException
  {
    boolean more = super.parse(complete);
    if (!more || fStopParsing)
    {
      fStopParsing = false;
      return more;
    }
    return parse(complete);

  }

  public void stopParsing()
  {
    if (TapestryPullParser.Debug)
      System.err.println("stopParsing called!");
    fStopParsing = true;
  }

}