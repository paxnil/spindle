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

package com.iw.plugins.spindle.core.parser;

import org.eclipse.core.resources.IMarker;


/**
 *  Default impl of IProblem
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DefaultProblem implements IProblem
{

    private int charEnd;

    private int charStart;

    private int lineNumber;

    private String message;

    private int severity;

    private String type;

    /**
     * 
     */
    public DefaultProblem(String type, int severity, String message, int lineNumber, int charStart, int charEnd)
    {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.lineNumber = lineNumber;
        this.charStart = charStart;
        this.charEnd = charEnd;
    }
    
    public String toString() {
        String name = getClass().getName();
        int index = name.lastIndexOf(".");
        if (index > 0) {
            name = name.substring(index + 1);
        }
        StringBuffer buffer = new StringBuffer(name);
        buffer.append("[");
        switch (getSeverity())
        {
            case IMarker.SEVERITY_ERROR :
                buffer.append("ERROR");
                break;
            case IMarker.SEVERITY_WARNING:
                buffer.append("WARNING");
                break;
            case IMarker.SEVERITY_INFO:
                buffer.append("INFO");
                break;

            default :
                buffer.append("NOT SET");
                break;
        }
        buffer.append(", ");
        buffer.append("L=");buffer.append(getLineNumber());
        buffer.append(", ");
        buffer.append("CS=");buffer.append(getCharStart());
        buffer.append(", ");
        buffer.append("CE=");buffer.append(getCharEnd());
        buffer.append(", ");
        buffer.append(getMessage());
        buffer.append("]");
        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblem#getCharEnd()
     */
    public int getCharEnd()
    {
        return charEnd;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblem#getCharStart()
     */
    public int getCharStart()
    {
        return charStart;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblem#getLineNumber()
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblem#getMessage()
     */
    public String getMessage()
    {
        return message;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblem#getSeverity()
     */
    public int getSeverity()
    {
        return severity;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblem#getType()
     */
    public String getType()
    {
        return type;
    }

}
