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

package com.iw.plugins.spindle.core.parser.xml;

import java.io.IOException;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

/**
 *  A configuration used by TapestryPullParser
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TapestryPullParserConfiguration extends TapestryParserConfiguration
{
    private boolean stopParsing = false;

    public TapestryPullParserConfiguration()
    {
        super();
    }

    public TapestryPullParserConfiguration(XMLGrammarPool grammarPool)
    {
        super(grammarPool);
    }

    public boolean parse() throws XNIException, IOException
    {
        return parse(false);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLPullParserConfiguration#parse(boolean)
     */
    public boolean parse(boolean complete) throws XNIException, IOException
    {
        boolean more = super.parse(complete);
        if (!more || stopParsing)
        {
            stopParsing = false;
            return more;
        }
        return parse(complete);

    }

    public void stopParsing()
    {
        stopParsing = true;
    }

}
