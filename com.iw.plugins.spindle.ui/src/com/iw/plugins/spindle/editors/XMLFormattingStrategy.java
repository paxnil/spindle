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
 * The Original Code is XMLFormattingStrategy
 *
 * The Initial Developer of the Original Code is
 * Christian Sell <christian.sell@netcologne.de>.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *  christian.sell@netcologne.de
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;

/**
 * 
 * default formatting strategy. It uses the partitioning of the underlying document
 * to determine the different elements for formatting
 * 
 * changed by GL to use a custom document partitioner.
 * 
 * @author cse
 * @version $Id$
 */
public class XMLFormattingStrategy implements XMLContentFormatter.FormattingStrategy
{
    /**
     * line info struct administered by LineWalker
     */
    private static class LineInfo
    {
        int offset, dataOffset, delimiterLength, posIndex1 = -1, posIndex2, delta;
        String data;
        int[] positions;
        boolean empty = false;

        LineInfo(int offset, String data, int delimiterLength)
        {
            this.offset = offset;
            this.dataOffset = offset;
            this.delimiterLength = delimiterLength;
            this.data = data;
        }
        /**
         * remove leading whitespace (modifying the internal state)
         * @return the data string after removing whitespace
         */
        public String trimData()
        {
            int i = 0;
            for (; i < data.length() && data.charAt(i) <= ' '; i++);
            if (i > 0)
            {
                dataOffset += i;
                data = data.substring(i, data.length());
            }
            return data;
        }
        /**
         * @param positions the positions array
         */
        public void recordPositionIndex(int[] positions)
        {
            this.positions = positions;
            int i = 0;
            for (; i < positions.length && positions[i] < offset; i++);
            if (i < positions.length)
            {
                posIndex1 = i;
                int endOffset = dataOffset + data.length() + delimiterLength;
                for (; i < positions.length && positions[i] < endOffset; i++);
                posIndex2 = i;
            }
        }
        /**
         * record the write offset for later processing in {@link #updatePositions()}
         * @param offset the offset at which this line was written into the formatted output
         */
        public void setWriteOffset(int offset)
        {
            if (!empty)
            {
                delta = offset - dataOffset;
            } else
            {
                dataOffset = offset;
            }
        }

        public void markEmpty()
        {
            empty = true;
        }

        public boolean isEmpty()
        {
            return empty;
        }
        /**
         * update the positions within the range of this object such that they are correct 
         * relative to the previously set write offset.
         */
        public void updatePositions()
        {
            if (posIndex1 >= 0)
            {
                for (int i = posIndex1; i < posIndex2; i++)
                {
                    if (!empty)
                    {
                        positions[i] += delta;
                    } else
                    {
                        positions[i] = dataOffset;
                    }
                }
            }
        }
    }

    /**
     * @deprecated use LineWalker
     */
    private static class LineWalkerX
    {
        private List lines = new ArrayList();
        private Iterator iterator;

        public LineWalkerX(IDocument document, IRegion region, String delimiter, int[] positions)
            throws BadLocationException
        {
            int delimiterLength = delimiter.length();
            int textOffset = region.getOffset();
            String text = document.get(textOffset, region.getLength());
            int i1 = 0, count = 0;
            for (int i = 0; i < text.length();)
            {
                int j = 0;
                for (; j < delimiterLength && text.charAt(i) == delimiter.charAt(j); i++, j++);
                if (j == delimiterLength)
                {
                    if (count > 0 || i1 < i - j)
                    {
                        LineInfo info = new LineInfo(textOffset + i1, text.substring(i1, i - j), delimiter.length());
                        info.recordPositionIndex(positions);
                        lines.add(info);
                    }
                    i1 = i;
                    count++;
                } else
                {
                    i++;
                }
            }
            if (i1 < text.length())
            {
                LineInfo info = new LineInfo(textOffset + i1, text.substring(i1, text.length()), 0);
                info.trimData();
                //only the add last segment if it contains some text
                if (info.data.length() > 0)
                {
                    info.recordPositionIndex(positions);
                    lines.add(info);
                }
            }
            iterator = lines.iterator();
        }

        /**
         * @return whether there are any more lines
         */
        public boolean hasMoreLines()
        {
            return iterator.hasNext();
        }

        /**
         * @return the next LineInfo
         * @throws java.util.NoSuchElementException
         */
        public LineInfo nextLine()
        {
            return (LineInfo) iterator.next();
        }

        public void check(LineWalker lw)
        {
            Iterator it = lw.lines.iterator();
            Iterator it2 = lines.iterator();
            if (lw.lines.size() != lines.size())
            {
                System.out.println("ERROR1");
                return;
            }
            while (it.hasNext())
            {
                LineInfo l1 = (LineInfo) it.next();
                LineInfo l2 = (LineInfo) it2.next();

                if (!l1.data.equals(l2.data))
                {
                    System.out.println("ERROR2");
                    return;
                }
                if (l1.offset != l2.offset)
                {
                    System.out.println("ERROR3");
                    return;
                }
                if (l1.dataOffset != l2.dataOffset)
                {
                    System.out.println("ERROR4");
                    return;
                }
                if (l1.posIndex1 != l2.posIndex1)
                {
                    System.out.println("ERROR5");
                    return;
                }
            }
        }
    }
    /**
     * Helper class to handle line information. The text input is partitioned into individual 
     * lines such that:
     * <ul>
     * <li>any leading newline is discarded</li>
     * <li>all following lines are added, even if empty</li>
     * <li>if non-whitespace text remains after the last newline, it is trimmed and added</li>
     * </ul>
     * The lines are managed as LineInfo objects, which also track the starting offset of the
     * line data into the underlying document.
     */
    private static class LineWalker
    {
        private List lines = new ArrayList();
        private Iterator iterator;

        public LineWalker(IDocument document, TypedPosition tposition, int[] positions) throws BadLocationException
        {
            ILineTracker lineTracker = new DefaultLineTracker();

            int textOffset = tposition.getOffset();
            String text = document.get(textOffset, tposition.getLength());
            lineTracker.set(text);

            int lineCount = lineTracker.getNumberOfLines();
            for (int i = 0; i < lineCount; i++)
            {
                int off = lineTracker.getLineOffset(i);
                String delimiter = lineTracker.getLineDelimiter(i);
                int length = lineTracker.getLineLength(i);

                if (delimiter != null)
                {
                    if (i > 0 || length > delimiter.length())
                    {
                        LineInfo info =
                            new LineInfo(
                                textOffset + off,
                                text.substring(off, off + length - delimiter.length()),
                                delimiter.length());
                        info.recordPositionIndex(positions);
                        lines.add(info);
                    }
                } else
                {
                    //only the add last line if it contains non-whitespace text
                    LineInfo info = new LineInfo(textOffset + off, text.substring(off, off + length), 0);
                    info.trimData();
                    info.recordPositionIndex(positions);
                    lines.add(info);
                    if (info.data.length() == 0)
                        info.markEmpty();

                    //                    if (info.data.length() > 0)
                    //                    {
                    //                        info.recordPositionIndex(positions);
                    //                        lines.add(info);
                    //                    }
                }
            }
            iterator = lines.iterator();
        }

        /**
         * @return whether there are any more lines
         */
        public boolean hasMoreLines()
        {
            return iterator.hasNext();
        }

        /**
         * @return the next LineInfo
         * @throws java.util.NoSuchElementException
         */
        public LineInfo nextLine()
        {
            return (LineInfo) iterator.next();
        }
    }

    private boolean fUseTabIndent = false;
    private int fTabSpaces = 4;
    private boolean fPreserveNewline = true;

    /* 
     * run variables reset for every invocation. Defined as state variables
     * so we dont need to pass them around all the time. Note that this makes the 
     * formatter non-threadsafe (which is perfectly OK)
     */
    private IDocument fDocument;
    private int fOffset;
    private String fLineDelimiter;
    private int fInitialIndent;
    private int fIndentLevel;
    private int[] fPositions;
    private List fLineInfos;
    private XMLDocumentPartitioner fPartitioner;
    private XMLNode fRoot;
    private TypedPosition[] fDocumentPositions;

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.XMLContentFormatter.FormattingStrategy#format(org.eclipse.jface.text.IDocument, int, int, int[])
     */
    public String format(IDocument document, int offset, int length, int[] positions)
    {
        try
        {
            if (length < 2)
                return document.get(offset, length);

            fDocument = document;
            fOffset = offset;
            fLineDelimiter = getLineDelimiter(fDocument);
            fInitialIndent = 0;
            fIndentLevel = 0;
            fPositions = positions;
            fLineInfos = new ArrayList();

            fPartitioner =
                new XMLDocumentPartitioner(XMLDocumentPartitioner.SCANNER, XMLDocumentPartitioner.TYPES);
            fPartitioner.connect(fDocument);
            fRoot = XMLNode.createTree(fDocument, -1); //TODO may not need this!
            computeTypedPositions();

            return doFormat(length);
        } catch (BadLocationException e)
        {
            UIPlugin.log(e); //shouldnt happen
            return null;
        } finally
        {
            fDocument = null; //release to GC
            fPositions = null;
            fLineInfos = null;
            fRoot = null;
            fDocumentPositions = null;
            try
            {
                fPartitioner.disconnect();
            } catch (RuntimeException e)
            {
                UIPlugin.log(e);
            } finally
            {
                fPartitioner = null;
            }
        }
    }

    /**
     * compute the array of DocumentArtifacts. 
     * sorted by offset.
     */
    private void computeTypedPositions()
    {
        fDocumentPositions = null;

        try
        {
            Position[] positions = fDocument.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
            Arrays.sort(positions, XMLNode.COMPARATOR);
            fDocumentPositions = new TypedPosition[positions.length];
            System.arraycopy(positions, 0, fDocumentPositions, 0, positions.length);
        } catch (BadPositionCategoryException e)
        {
            UIPlugin.log(e);
        }
    }

    /**
     * do the actual formatting, after all run variables have been initialized
     * @param length
     * @return the ready formatted content string
     * @throws BadLocationException
     */
    private String doFormat(int length) throws BadLocationException
    {
        //determine the enclosing element and the appropriate indent
        TypedPositionWalker walker = new TypedPositionWalker(fDocumentPositions, fOffset);

        for (TypedPosition tposition = walker.previous(); tposition != null; tposition = walker.previous())
        {
            String type = tposition.getType();
            if (type == XMLDocumentPartitioner.TAG || type == XMLDocumentPartitioner.EMPTYTAG)
            {
                fInitialIndent = getIndent(tposition.getOffset());
                if (type != XMLDocumentPartitioner.EMPTYTAG)
                    fIndentLevel++;
                break;
            } else if (type == XMLDocumentPartitioner.ENDTAG)
            {
                fInitialIndent = getIndent(tposition.getOffset());
                break;
            }
        }

        //walk through the partitions and format
        walker = new TypedPositionWalker(fDocumentPositions, fOffset, length);
        StringBuffer buffer = new StringBuffer();

        TypedPosition tposition = walker.next();
        while (tposition != null)
        {
            String type = tposition.getType();
            if (type == XMLDocumentPartitioner.TAG || type == XMLDocumentPartitioner.EMPTYTAG)
            {
                formatStartTag(tposition, buffer);
                if (type == XMLDocumentPartitioner.TAG)
                    fIndentLevel++;
            } else if (type == XMLDocumentPartitioner.ENDTAG)
            {
                if (fIndentLevel > 0)
                    fIndentLevel--;
                formatDefault(tposition, buffer);
            } else if (type == XMLDocumentPartitioner.DECL)
            {
                XMLNode artifact = (XMLNode)tposition;
                String content = artifact.getContent();
                if (content.indexOf("DOCTYPE") >=0) {
                    formatStartTag(tposition, buffer);
                } else {
                    formatCDATA(tposition, buffer);
                }
                   
            } else if (type == XMLDocumentPartitioner.COMMENT)
            {
                formatDefault(tposition, buffer);
            } else if (type == XMLDocumentPartitioner.TEXT || type == XMLDocumentPartitioner.PI)
            {
                formatDefault(tposition, buffer);
            }
            tposition = walker.next();
        }

        //finally, have the line infos update the positions array
        Iterator it = fLineInfos.iterator();
        while (it.hasNext())
        {
            LineInfo info = (LineInfo) it.next();
            info.updatePositions();
        }

        return buffer.toString();
    }

    /**
     * default formatting. Everything is aligned, one indent level above the nearest enclosing 
     * opening element, if any 
     */
    private void formatDefault(TypedPosition region, StringBuffer buffer) throws BadLocationException
    {
        LineWalker lineWalker = new LineWalker(fDocument, region, fPositions);
        boolean alreadyKept = false;
        while (lineWalker.hasMoreLines())
        {
            LineInfo info = lineWalker.nextLine();
            
            if (info.isEmpty())
            {
                info.setWriteOffset(writeEmpty(buffer));
                fLineInfos.add(info);
            } else
            {
                String line = info.trimData();

                if (line.length() > 0)
                {
                    int off = writeLine(info.data, fInitialIndent, fIndentLevel, buffer);

                    info.setWriteOffset(off);
                    fLineInfos.add(info);
                } else if (fPreserveNewline && !alreadyKept)
                {
                    writeLine("", fInitialIndent, fIndentLevel, buffer);
//                    buffer.append(fLineDelimiter);
                    alreadyKept = true;
                }
            }
        }
    }

    /**
     * format a start tag. Attributes, if starting on a new line, are given an 
     * additional indent 
     */
    private void formatStartTag(TypedPosition tposition, StringBuffer buffer) throws BadLocationException
    {
        LineWalker lineWalker = new LineWalker(fDocument, tposition, fPositions);

        int count = 0;
        while (lineWalker.hasMoreLines())
        {
            LineInfo info = lineWalker.nextLine();
            String line = info.trimData();

            if (line.length() > 0)
            {
                int indentLevel = count > 0 ? fIndentLevel + 1 : fIndentLevel;
                int writeOffset = writeLine(info.data, fInitialIndent, indentLevel, buffer);

                info.setWriteOffset(writeOffset);
                fLineInfos.add(info);

                count++;
            }
        }
    }

    /**
     * format a CDATA region, preserving indenting within the CDATA 
     */
    private void formatCDATA(TypedPosition region, StringBuffer buffer) throws BadLocationException
    {
        LineWalker lineWalker = new LineWalker(fDocument, region, fPositions);

        LineInfo info = lineWalker.nextLine();
        int firstIndent = getIndent(info.offset);
        info.trimData();
        int writeOffset = writeLine(info.data, fInitialIndent, fIndentLevel, buffer);

        info.setWriteOffset(writeOffset);
        fLineInfos.add(info);

        while (lineWalker.hasMoreLines())
        {
            info = lineWalker.nextLine();
            int indentDelta = getIndent(info.data) - firstIndent;
            String line = info.trimData();
            if (line.length() > 0)
            {
                int indent = indentDelta > 0 ? fInitialIndent + indentDelta : fInitialIndent;
                writeOffset = writeLine(info.data, indent, fIndentLevel, buffer);

                info.setWriteOffset(writeOffset);
                fLineInfos.add(info);
            } else
            {
                buffer.append(fLineDelimiter);
            }
        }
    }

    /**
     * @param region a region of type START_TAG
     * @return whether the given region is terminated by a closing tag marker ("/>").
     * @deprecated no longer needed.
     */
    private boolean isTagClosed(TypedPosition tposition) throws BadLocationException
    {
        char c = fDocument.getChar(tposition.getOffset() + tposition.getLength() - 2);
        return (c == '/');
    }

    /**
     * Embodies the policy which line delimiter to use when inserting into
     * a document.<br>
     * <em>Copied from org.eclipse.jdt.internal.corext.codemanipulation.StubUtility</em>
     */
    private String getLineDelimiter(IDocument document)
    {
        // new for: 1GF5UU0: ITPJUI:WIN2000 - "Organize Imports" in java editor inserts lines in wrong format
        String lineDelim = null;
        try
        {
            lineDelim = document.getLineDelimiter(0);
        } catch (BadLocationException e)
        {}
        if (lineDelim == null)
        {
            String systemDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            String[] lineDelims = document.getLegalLineDelimiters();
            for (int i = 0; i < lineDelims.length; i++)
            {
                if (lineDelims[i].equals(systemDelimiter))
                {
                    lineDelim = systemDelimiter;
                    break;
                }
            }
            if (lineDelim == null)
            {
                lineDelim = lineDelims.length > 0 ? lineDelims[0] : systemDelimiter;
            }
        }
        return lineDelim;
    }

    /**
     * Returns the indentation of the line of the given offset.
     *
     * @param offset the offset
     * @return the indentation of the line of the offset
     */
    private int getIndent(int offset)
    {

        try
        {
            int start = fDocument.getLineOfOffset(offset);
            start = fDocument.getLineOffset(start);

            int count = 0;
            for (int i = start; i < fDocument.getLength(); ++i)
            {
                char c = fDocument.getChar(i);
                if ('\t' == c)
                    count += fTabSpaces;
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

    /**
     * @param line
     * @return the number of character positions 
     */
    private int getIndent(String line)
    {
        int count = 0;
        for (int i = 0; i < line.length(); ++i)
        {
            char c = line.charAt(i);
            if ('\t' == c)
                count += fTabSpaces;
            else if (' ' == c)
                count++;
            else
                break;
        }
        return count;
    }

    /**
     * Write the line data to the given buffer, using the appropriate indent. Terminate 
     * with a line delimiter. 
     * 
     * @param initialColumns initial columns to indent. Tabs or spaces, depending on preference
     * @param indentLevel the number of indents (tabs or spaces) to add 
     * @param buffer the buffer to write to
     */
    private int writeLine(String line, int initialColumns, int indentLevel, StringBuffer buffer)
    {
        int writeOffset = fOffset + buffer.length();
        writeOffset += writeColumns(initialColumns, buffer);
        writeOffset += writeIndent(indentLevel, buffer);

        buffer.append(line);
        buffer.append(fLineDelimiter);

        return writeOffset;
    }

    /**
     * Handle an empty line (a line that will not be output).
     * This method exists only so that correct offset in the result is
     *  available to update positions.
     * @param buffer the buffer to write to
     * @return the current offset
     */
    private int writeEmpty(StringBuffer buffer)
    {
        return fOffset + buffer.length();
    }

    /**
     * write a given number of whitespace columns, using tabs or spaces depending on preference
     * @param columnCount the columns to write
     * @param buffer the buffer to write to
     * @return the number of characters inserted into the buffer
     */
    private int writeColumns(int columnCount, StringBuffer buffer)
    {
        if (fUseTabIndent)
        {
            int tabs = columnCount / fTabSpaces;
            int spaces = columnCount % fTabSpaces;

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
    private int writeIndent(int indentCount, StringBuffer buffer)
    {
        if (fUseTabIndent)
        {
            for (int i = 0; i < indentCount; i++)
                buffer.append('\t');

            return indentCount;
        } else
        {
            int length = indentCount * fTabSpaces;
            for (int i = 0; i < length; i++)
                buffer.append(' ');

            return length;
        }
    }
    /**
     * @return whether empty lines should be preserved during formatting (default: true)
     */
    public boolean isPreserveEmpty()
    {
        return fPreserveNewline;
    }

    /**
     * determine the number of spaces to use if tabs are not used (default: 4)
     * @return the number of spaces per tab
     */
    public int getTabSpaces()
    {
        return fTabSpaces;
    }

    /**
     * @return whether indenting should be done by tabs (default: true).
     * This will also replace any existing space indents.
     */
    public boolean isUseTabIndent()
    {
        return fUseTabIndent;
    }

    /**
     * determine whether empty lines should be preserved during formatting (default: true)
     * @param preserve true if lines should be preserved
     */
    public void setPreserveEmpty(boolean preserve)
    {
        fPreserveNewline = preserve;
    }

    /**
     * determine the number of spaces to use if tabs are not used (default: 4)
     * @param spaces the number of spaces per tab
     */
    public void setTabSpaces(int spaces)
    {
        fTabSpaces = spaces;
    }

    /**
     * Determine whether indenting should be done by tabs (default: true).
     * This will also replace any existing space indents.
     * 
     * @param <code>true</code> if tabs should be used for indenting. 
     */
    public void setUseTabIndent(boolean useTabs)
    {
        fUseTabIndent = useTabs;
    }
}
