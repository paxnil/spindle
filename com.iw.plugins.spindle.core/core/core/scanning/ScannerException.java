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

package core.scanning;

import core.source.ISourceLocation;

/**
 * Exception type thrown by Processors
 * 
 * @author glongman@gmail.com
 */
public class ScannerException extends Exception
{

    ISourceLocation fLocation;

    int fCode = -1;

    boolean fTemporary = false;

    /**
     * @param arg0
     */
    public ScannerException(String message, boolean temporary, int code)
    {
        super(message);
        fCode = code;
        fTemporary = temporary;
    }

    /**
     * @param arg0
     * @param arg1
     */
    public ScannerException(String message, Throwable exception, boolean temporary, int code)
    {
        super(message, exception);
        fCode = code;
        fTemporary = temporary;
    }

    public ScannerException(String message, ISourceLocation location, boolean temporary, int code)
    {
        super(message);
        this.fLocation = location;
        fCode = code;
        fTemporary = temporary;
    }

    public int getCode()
    {
        return fCode;
    }

    public ISourceLocation getLocation()
    {
        return this.fLocation;
    }

    public boolean isTemporary()
    {
        return fTemporary;
    }

}