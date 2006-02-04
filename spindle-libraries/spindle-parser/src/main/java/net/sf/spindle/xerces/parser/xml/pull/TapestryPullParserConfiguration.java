package net.sf.spindle.xerces.parser.xml.pull;
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
import java.io.IOException;

import net.sf.spindle.xerces.parser.xml.dom.TapestryDOMParserConfiguration;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;


/**
 * A configuration used by TapestryPullParser
 * 
 * @author glongman@gmail.com
 * @deprecated PULL parser is OUT!
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