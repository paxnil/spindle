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

package com.iw.plugins.spindle.core.parser.template;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.parse.OpenToken;

/**
 * Token representing the open tag for a component. Extends the superclass by
 * recording source location info provided by the parser.
 * 
 * @see org.apache.tapestry.parse.OpenToken
 * @author glongman@intelligentworks.com
 * @version $Id$
 *  
 */

public class CoreOpenToken extends OpenToken
{

  private TagEventInfo fEventInfo;

  /**
   * Creates a new token with the given tag, id and type, and source location
   * info from the parser
   */

  public CoreOpenToken(String tag, String id, String componentType, ILocation location,
      TagEventInfo eventInfo)
  {
    super(tag, id, componentType, location);

    fEventInfo = eventInfo;
  }

  public TagEventInfo getEventInfo()
  {
    return fEventInfo;
  }

}