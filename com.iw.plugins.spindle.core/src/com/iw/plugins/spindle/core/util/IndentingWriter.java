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
package com.iw.plugins.spindle.core.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class IndentingWriter extends PrintWriter
{
    public static final String DEFAULT_DELIMITER = "\n";

    public static IndentingWriter getDefaultIndentingWriter(Writer out)
    {
        return getDefaultIndentingWriter(out, 0);
    }

    public static IndentingWriter getDefaultIndentingWriter(Writer out, int initialIndent)
    {
        return new IndentingWriter(out, true, 4, initialIndent, DEFAULT_DELIMITER);
    }

    private boolean fUseTabsToIndent = false;
    private String fLineDelimiter;
    private int fIndentWidth;
    private int fInitialIndent;

    /**
     * @param out
     */
    public IndentingWriter(
        Writer out,
        boolean useTabsToIndent,
        int indentWidth,
        int initialIndent,
        String lineDelimiter)
    {
        super(out);
        init(useTabsToIndent, indentWidth, initialIndent, lineDelimiter);
    }

    /**
     * @param out
     * @param autoFlush
     */
    public IndentingWriter(
        Writer out,
        boolean autoFlush,
        boolean useTabsToIndent,
        int indentWidth,
        int initialIndent,
        String lineDelimiter)
    {
        super(out, autoFlush);
        init(useTabsToIndent, indentWidth, initialIndent, lineDelimiter);
    }

    /**
     * @param out
     */
    public IndentingWriter(
        OutputStream out,
        boolean useTabsToIndent,
        int indentWidth,
        int initialIndent,
        String lineDelimiter)
    {
        super(out);
        init(useTabsToIndent, indentWidth, initialIndent, lineDelimiter);
    }

    /**
     * @param out
     * @param autoFlush
     */
    public IndentingWriter(
        OutputStream out,
        boolean autoFlush,
        boolean useTabsToIndent,
        int indentWidth,
        int initialIndent,
        String lineDelimiter)
    {
        super(out, autoFlush);
        init(useTabsToIndent, indentWidth, initialIndent, lineDelimiter);
    }

    private void init(boolean useTabsToIndent, int indentWidth, int initialIndent, String lineDelimiter)
    {
        fUseTabsToIndent = useTabsToIndent;
        fIndentWidth = indentWidth;
        fInitialIndent = initialIndent;
        fLineDelimiter = lineDelimiter;
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#println()
     */
    public void println()
    {
        if (fLineDelimiter == null)
            super.println();
        else
            print(fLineDelimiter);
    }

    public void printlnIndented( int indent, String value)
    {
        writeIndent(indent);
        println(value);
    }

    public void printIndented(int indent, String value)
    {
        writeIndent(indent);
        print(value);
    }

    private void writeIndent(int indent)
    {
        if (fInitialIndent > 0)
            writeColumns();

        writeIndent0(indent);

    }

    private void writeColumns()
    {
        if (fUseTabsToIndent)
        {
            int tabs = fInitialIndent / fIndentWidth;
            int spaces = fInitialIndent % fIndentWidth;

            for (int i = 0; i < tabs; i++)
                print('\t');
            for (int i = 0; i < spaces; i++)
                print(' ');

        } else
        {
            for (int i = 0; i < fInitialIndent; i++)
                print(' ');
        }
    }

    private void writeIndent0(int indentCount)
    {
        if (fUseTabsToIndent)
        {
            for (int i = 0; i < indentCount; i++)
                print('\t');
        } else
        {
            int length = indentCount * fIndentWidth;
            for (int i = 0; i < length; i++)
                print(' ');
        }
    }

}