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

import org.apache.commons.lang.enum.Enum;

/**
 *  Type safe enum for pull parser state
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class StateType extends Enum
{

    public static final StateType UNDEFINED = new StateType("UNDEFINED");
    public static final StateType START_DOCUMENT = new StateType("START_DOCUMENT");
    public static final StateType END_DOCUMENT = new StateType("END_DOCUMENT");
    public static final StateType START_TAG = new StateType("START_TAG");
    public static final StateType TEXT = new StateType("TEXT");
    public static final StateType CDSECT = new StateType("CDSECT");

    public StateType(String name)
    {
        super(name);
    }

}
