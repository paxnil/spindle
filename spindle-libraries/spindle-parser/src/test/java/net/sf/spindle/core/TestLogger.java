package net.sf.spindle.core;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestLogger implements ILogger
{
    // /CLOVER:OFF

    static public class LoggingEvent
    {
        private String message;

        private Throwable exception;

        public LoggingEvent(String message, Throwable ex)
        {
            this.message = message;
            this.exception = ex;
        }

        public Throwable getException()
        {
            return exception;
        }

        public String getMessage()
        {
            return message;
        }
    }

    private List<LoggingEvent> events;

    /*
     * (non-Javadoc)
     * 
     * @see spindle.core.ILogger#log(java.lang.String)
     */
    public void log(String msg)
    {
        log(msg, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see spindle.core.ILogger#log(java.lang.Throwable)
     */
    public void log(Throwable ex)
    {
        log(null, ex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see spindle.core.ILogger#log(java.lang.String, java.lang.Throwable)
     */
    public void log(String message, Throwable ex)
    {
        if (events == null)
            events = new ArrayList<LoggingEvent>();
        events.add(new LoggingEvent(message, ex));
    }

    public boolean isEmpty()
    {
        if (events == null)
            return true;
        return events.isEmpty();
    }

    public int size()
    {
        if (events == null)
            return 0;
        return events.size();
    }

    public List<LoggingEvent> getEvents()
    {
        if (events == null)
            return Collections.emptyList();
        return events;
    }

    public void clear()
    {
        if (events != null)
            events.clear();
    }

}
