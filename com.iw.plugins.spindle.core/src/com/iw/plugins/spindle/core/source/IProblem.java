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

package com.iw.plugins.spindle.core.source;

import org.eclipse.core.resources.IMarker;

/**
 * Interface describing problems reported by the Parser.
 * 
 * IProblems are convertable into IMarkers
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public interface IProblem
{
    /** 
     * Error severity constant indicating an error state.
     */
    public static final int ERROR = IMarker.SEVERITY_ERROR;

    /** 
     * Info severity constant indicating information only.
     *
     * @see #getAttribute
     */
    public static final int INFO = IMarker.SEVERITY_INFO;

    /** 
     * Warning severity constant indicating a warning.
     *
     * @see #getAttribute
     */
    public static final int WARNING = IMarker.SEVERITY_WARNING;
    /** 
     * An integer value indicating where a problem ends.
     * This attribute is zero-relative and inclusive.
     * 
     * @return a zero-relative integer     
     */
    public int getCharEnd();
    /** 
     * An integer value indicating where a problem starts.
     * This attribute is zero-relative and inclusive.
     * 
     * @return a zero-relative integer     
     */
    public int getCharStart();
    /** 
     * An integer value indicating the line number
     * for a text marker.  This attribute is 1-relative.
     * 
     * @return a 1-relative integer     
     */
    public int getLineNumber();
    /**
     * Returns the message describing the problem.
     *
     * @return a message
     */
    public String getMessage();
    /**
     * Returns the severity. The severities are as follows (in
     * descending order):
     * <ul>
     * <li><code>ERROR</code> - a serious error (most severe)</li>
     * <li><code>WARNING</code> - a warning (less severe)</li>
     * <li><code>INFO</code> - an informational ("fyi") message (least severe)</li>
     * </ul>
     * <p>
     * The int value corresponds to the severities defined in IMarker
     * </p>
     *
     * @return the severity: one of <code>OK</code>,
     *   <code>ERROR</code>, <code>INFO</code>, or <code>WARNING</code>
     */
    public int getSeverity();

    /**
     * A String corresponding to the Eclipse Marker Type
     * Used when converting IProblems into Markers
     */
    public String getType();
    
    /**
     *  Indicates if this problem is temporary (or in other words revalidatable)
     * @return true iff this problem is temporary
     */
    public boolean isTemporary();
}
