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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.util;

import java.io.PrintWriter;

public class Indenter
{

    public static String Indent = "    ";

    public static void printlnIndented(PrintWriter writer, int indent, String value)
    {
        writeIndent(writer, indent);
        writer.println(value);
    }

    public static void printIndented(PrintWriter writer, int indent, String value)
    {
        writeIndent(writer, indent);
        writer.print(value);
    }

    private static void writeIndent(PrintWriter writer, int indent)
    {
        if (indent <= 0)
        {
            return;
        }
        for (int i = 0; i < indent; i++)
        {
            writer.print(Indent);
        }
    }

}