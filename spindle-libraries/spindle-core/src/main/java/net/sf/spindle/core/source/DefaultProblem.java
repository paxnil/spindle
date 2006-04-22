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
/**
 * Default impl of IProblem
 * 
 * @author glongman@gmail.com
 */
public class DefaultProblem implements IProblem
{

    private int fCharEnd;

    private int fCharStart;

    private int fLineNumber;

    private String fMessage;

    private int fSeverity;

    private String fType;

    private boolean fTemporary;

    private int fCode;

    public DefaultProblem(int severity, String message, ISourceLocation location,
            boolean isTemporary, int code)
    {
        this(IProblem.TAPESTRY_PROBLEM_MARKER, severity, message, location, isTemporary, code);

    }

    public DefaultProblem(String type, int severity, String message, ISourceLocation location,
            boolean isTemporary, int code)
    {
        this(type, severity, message, location.getLineNumber(), location.getCharStart(), location
                .getCharEnd(), isTemporary, code);
    }

    protected DefaultProblem(int severity, String message, int lineNumber, int charStart,
            int charEnd, boolean isTemporary, int code)
    {
        this(IProblem.TAPESTRY_PROBLEM_MARKER, severity, message, lineNumber, charStart, charEnd,
                isTemporary, code);
    }

    protected DefaultProblem(String type, int severity, String message, int lineNumber,
            int charStart, int charEnd, boolean isTemporary, int code)
    {
        fType = type;
        fSeverity = severity;
        fMessage = message;
        fLineNumber = lineNumber;
        fCharStart = charStart;
        fCharEnd = charEnd;
        fTemporary = isTemporary;
        fCode = code;
    }

    public String toString()
    {
        String name = getClass().getName();
        int index = name.lastIndexOf(".");
        if (index > 0)
            name = name.substring(index + 1);

        StringBuffer buffer = new StringBuffer(name);
        buffer.append("[");
        switch (fSeverity)
        {
            case ERROR:
                buffer.append("ERROR");
                break;
            case WARNING:
                buffer.append("WARNING");
                break;
            case INFO:
                buffer.append("INFO");
                break;

            default:
                buffer.append("NOT SET");
                break;
        }
        buffer.append(", ");
        buffer.append("L=");
        buffer.append(getLineNumber());
        buffer.append(", ");
        buffer.append("CS=");
        buffer.append(getCharStart());
        buffer.append(", ");
        buffer.append("CE=");
        buffer.append(getCharEnd());
        buffer.append(", ");
        buffer.append(getMessage());
        buffer.append("]");
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.parser.IProblem#getCharEnd()
     */
    public int getCharEnd()
    {
        return fCharEnd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.parser.IProblem#getCharStart()
     */
    public int getCharStart()
    {
        return fCharStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.parser.IProblem#getLineNumber()
     */
    public int getLineNumber()
    {
        return fLineNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.parser.IProblem#getMessage()
     */
    public String getMessage()
    {
        return fMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.parser.IProblem#getSeverity()
     */
    public int getSeverity()
    {
        return fSeverity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.parser.IProblem#getType()
     */
    public String getType()
    {
        return fType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.IProblem#isTemporary()
     */
    public boolean isTemporary()
    {
        return fTemporary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.source.IProblem#getCode()
     */
    public int getCode()
    {
        return fCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof DefaultProblem))
            return false;
        DefaultProblem other = (DefaultProblem)obj;
        return this.fTemporary == other.fTemporary
        && this.fCharStart == other.fCharStart
        && this.fLineNumber == other.fLineNumber
        && this.fType == other.fType
        && this.fCode == other.fCode
        && this.fSeverity == other.fSeverity
        && (this.fMessage == null ? other.fMessage == null : this.fMessage.equals(other.fMessage));
    }
    
    

}