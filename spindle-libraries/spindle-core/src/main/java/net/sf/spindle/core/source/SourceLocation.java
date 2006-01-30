package net.sf.spindle.core.source;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
public class SourceLocation implements ISourceLocation
{
    
  public static final ISourceLocation FILE_LOCATION = new SourceLocation(1, 0, 0);
  public static final ISourceLocation FOLDER_LOCATION = new SourceLocation(0, 0, 0);

  private int fLineNumber;
  private int fCharStart;
  private int fCharEnd;

  public SourceLocation(int lineNumber, int charStart)
  {
    this(lineNumber, charStart, charStart);
  }

  public SourceLocation(int lineNumber, int charStart, int charEnd)
  {
    this.fLineNumber = lineNumber;
    this.fCharStart = charStart;
    this.fCharEnd = charEnd;
  }
  /**
   * @see core.parser.ISourceLocation#getStartLine()
   */
  public int getLineNumber()
  {
    return fLineNumber;
  }

  /**
   * @see core.parser.ISourceLocation#getCharStart()
   */
  public int getCharStart()
  {
    return fCharStart;
  }

  /**
   * @see core.parser.ISourceLocation#getCharEnd()
   */
  public int getCharEnd()
  {
    return fCharEnd;
  }

  public int getLength()
  {
    return fCharEnd - fCharStart + 1;
  }

  public String toString()
  {
    StringBuffer buffer = new StringBuffer("line:charStart:charEnd[");
    buffer.append(fLineNumber);
    buffer.append(", ");
    buffer.append(fCharStart);
    buffer.append(", ");
    buffer.append(fCharEnd);
    buffer.append("]");
    return buffer.toString();
  }

  /**
   * @param i
   */
  public void setCharEnd(int i)
  {
    fCharEnd = i;
  }

  /**
   * @param i
   */
  public void setCharStart(int i)
  {
    fCharStart = i;
  }

  /**
   * @param i
   */
  public void setLineNumber(int i)
  {
    fLineNumber = i;
  }

  /*
   * (non-Javadoc)
   * 
   * @see core.parser.ISourceLocation#contains(int)
   */
  public boolean contains(int cursorPosition)
  {
    return fCharStart <= cursorPosition && fCharEnd >= cursorPosition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see core.parser.ISourceLocation#getLocationOffset(int)
   */
  public ISourceLocation getLocationOffset(int offset)
  {
    return new SourceLocation(fLineNumber, fCharStart + offset >= fCharEnd
        ? fCharStart : fCharStart + offset, fCharEnd);
  }

}