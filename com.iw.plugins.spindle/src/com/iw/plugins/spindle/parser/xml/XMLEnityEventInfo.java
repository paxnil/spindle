package com.iw.plugins.spindle.parser.xml;

/**
 * This interface is used to carry location information 
 *
 * @version $Id$
 */
public interface XMLEnityEventInfo
{

    /** @return the line number of the beginning of this event.*/
    public int getBeginLineNumber();

    /** @return the column number of the beginning of this event.*/
    public int getBeginColumnNumber();

    /** @return the line number of the end of this event.*/
    public int getEndLineNumber();

    /** @return the column number of the end of this event.*/
    public int getEndColumnNumber();

}
