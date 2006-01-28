package net.sf.spindle.xerces.parser;

import net.sf.spindle.core.source.DefaultProblem;

public class ParserProblem extends DefaultProblem
{   
    public ParserProblem(String type, int severity, String message, int lineNumber, int charStart,
            int charEnd, boolean isTemporary, int code)
    {
        super(type, severity, message, lineNumber, charStart, charEnd, isTemporary, code);        
    }
}
