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

package core.parser.template;

import java.util.Map;

import org.apache.hivemind.Location;
import org.apache.tapestry.parse.LocalizationToken;

/**
 * Represents localized text from the template.
 * 
 * @see TokenType#LOCALIZATION
 * @author Howard Lewis Ship
 * @since 3.0
 */

public class CoreLocalizationToken extends LocalizationToken
{

    @SuppressWarnings("unused")
	private TagEventInfo fEventInfo;

    /**
     * Creates a new token.
     * 
     * @param tag
     *            the tag of the element from the template
     * @param key
     *            the localization key specified
     * @param raw
     *            if true, then the localized value contains markup that should not be escaped
     * @param attribute
     *            any additional attributes (beyond those used to define key and raw) that were
     *            specified. This value is retained, not copied.
     * @param location
     *            location of the tag which defines this token
     */
    public CoreLocalizationToken(String tag, String key, boolean raw, Map attributes,
            Location location, TagEventInfo eventInfo)
    {
        super(tag, key, raw, attributes, location);

        fEventInfo = eventInfo;
    }

}