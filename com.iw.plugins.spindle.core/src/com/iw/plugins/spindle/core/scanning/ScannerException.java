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

package com.iw.plugins.spindle.core.scanning;

import com.iw.plugins.spindle.core.source.ISourceLocation;

/**
 *  Exception type thrown by Processors
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ScannerException extends Exception
{

    ISourceLocation location;
    
    public ScannerException()
    {
        super();
    }

    /**
     * @param arg0
     */
    public ScannerException(String arg0)
    {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public ScannerException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public ScannerException(Throwable arg0)
    {
        super(arg0);
    }
    
    public ScannerException(String message, ISourceLocation location) {
        super(message);
        this.location = location;
    }
    
    public ISourceLocation getLocation() {
        return this.location;
    }

}
