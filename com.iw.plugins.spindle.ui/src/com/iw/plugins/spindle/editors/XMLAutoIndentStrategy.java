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

package com.iw.plugins.spindle.editors;

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;

/**
 *  Auto indent strategy sensitive to XML tags
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class XMLAutoIndentStrategy extends DefaultAutoIndentStrategy
{
    private static final String FORMATTER_USE_TABS_TO_INDENT = PreferenceConstants.FORMATTER_USE_TABS_TO_INDENT;
    private static final String EDITOR_DISPLAY_TAB_WIDTH = PreferenceConstants.EDITOR_DISPLAY_TAB_WIDTH;

    private XMLDocumentPartitioner fPartitioner =
        new XMLDocumentPartitioner(XMLDocumentPartitioner.SCANNER, XMLDocumentPartitioner.TYPES);

    private TypedPosition[] fTypedPositions;
    private IPreferenceStore fPreferences;
    private int fTabDisplayWidth;

    public XMLAutoIndentStrategy(IPreferenceStore store)
    {
        super();
        fPreferences = store;
        fTabDisplayWidth = fPreferences.getInt(EDITOR_DISPLAY_TAB_WIDTH);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
     */
    public void customizeDocumentCommand(IDocument document, DocumentCommand command)
    {
        if (command.length == 0 && command.text != null && endsWithDelimiter(document, command.text))
        {
            int docLength = document.getLength();
            if (command.offset == -1 || docLength == 0)
                return;

            int offset = (command.offset == docLength ? command.offset - 1 : command.offset);

            try
            {
                connect(document);
                TypedPositionWalker walker = new TypedPositionWalker(fTypedPositions, offset);

                XMLNode artifact = (XMLNode) walker.previous();
                if (artifact == null)
                {
                    super.customizeDocumentCommand(document, command);

                } else
                {
                    String type = artifact.getType();
                    boolean inside =
                        offset <= artifact.getOffset() && offset < artifact.getOffset() + artifact.getLength();

                    if (inside && type != XMLDocumentPartitioner.TEXT)
                    {
                        doIndent(command, getIndent(document, artifact.getOffset()), 1);

                    } else if (type == XMLDocumentPartitioner.TEXT)
                    {
                        do
                        {
                            artifact = (XMLNode) walker.previous();
                        } while (artifact != null && artifact.getType() == XMLDocumentPartitioner.TEXT);
                    }

                    if (artifact == null)
                    {
                        super.customizeDocumentCommand(document, command);
                    } else
                    {
                        if (type == XMLDocumentPartitioner.TAG)
                            doIndent(command, getIndent(document, artifact.getOffset()), 1);
                        else
                            doIndent(command, getIndent(document, artifact.getOffset()), 0);
                    }
                }
            } catch (Exception e)
            {
                UIPlugin.log(e);
                super.customizeDocumentCommand(document, command);
            } finally
            {
                disconnect();
            }
        }
    }

    private void doIndent(DocumentCommand command, int initialIndent, int additionalIndent)
    {
        boolean useTabs = fPreferences.getBoolean(FORMATTER_USE_TABS_TO_INDENT);

        StringBuffer buf = new StringBuffer(command.text);

        writeColumns(initialIndent, buf, useTabs);
        writeIndent(additionalIndent, buf, useTabs);

        command.text = buf.toString();
    }

    /**
     * Returns whether or not the text ends with one of the given search strings.
     */
    private boolean endsWithDelimiter(IDocument d, String txt)
    {
        String[] delimiters = d.getLegalLineDelimiters();
        for (int i = 0; i < delimiters.length; i++)
        {
            if (txt.endsWith(delimiters[i]))
                return true;
        }
        return false;
    }

    private void connect(IDocument d) throws BadLocationException, BadPositionCategoryException
    {
        fPartitioner.connect(d);
        Position[] pos = d.getPositions(fPartitioner.getPositionCategory());
        Arrays.sort(pos, XMLNode.COMPARATOR);
        fTypedPositions = new TypedPosition[pos.length];
        System.arraycopy(pos, 0, fTypedPositions, 0, pos.length);
        XMLNode.createTree(d, -1);
    }

    private void disconnect()
    {
        try
        {
            fPartitioner.disconnect();
        } catch (Exception e)
        {
            UIPlugin.log(e);
        }
    }

    /**
      * Returns the indentation of the line of the given offset.
      *
      *@param document the document
      * @param offset the offset
      * @return the indentation of the line of the offset
      */
    private int getIndent(IDocument document, int offset)
    {

        try
        {
            int start = document.getLineOfOffset(offset);
            start = document.getLineOffset(start);

            int count = 0;
            for (int i = start; i < document.getLength(); ++i)
            {
                char c = document.getChar(i);
                if ('\t' == c)
                    count += fTabDisplayWidth;
                else if (' ' == c)
                    count++;
                else
                    break;
            }
            return count;
        } catch (BadLocationException x)
        {
            return 0;
        }
    }

    private int writeColumns(int columnCount, StringBuffer buffer, boolean useTabsToIndent)
    {
        if (useTabsToIndent)
        {

            int tabs = columnCount / fTabDisplayWidth;
            int spaces = columnCount % fTabDisplayWidth;

            for (int i = 0; i < tabs; i++)
                buffer.append('\t');
            for (int i = 0; i < spaces; i++)
                buffer.append(' ');

            return tabs + spaces;
        } else
        {
            for (int i = 0; i < columnCount; i++)
                buffer.append(' ');

            return columnCount;
        }
    }

    /**
     * write a given number of whitespace columns, using tabs or spaces depending on preference
     * @param indentCount the number of indents to write
     * @param buffer the buffer to write to
     * @return the number of characters inserted into the buffer
     */
    private int writeIndent(int indentCount, StringBuffer buffer, boolean useTabsToIndent)
    {
        if (useTabsToIndent)
        {
            for (int i = 0; i < indentCount; i++)
                buffer.append('\t');

            return indentCount;
        } else
        {
            int length = indentCount * fTabDisplayWidth;
            for (int i = 0; i < length; i++)
                buffer.append(' ');

            return length;
        }
    }
}
