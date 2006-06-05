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

import junit.framework.AssertionFailedError;

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

    private boolean failOnAnyEvent = false;

    public void setFailOnAnyEvent(boolean fail)
    {
        failOnAnyEvent = fail;
    }

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
        if (failOnAnyEvent) {
            dump();
            throw new AssertionFailedError("TestLoggerFailOnAnyEvent");
        }
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

    public List getEvents()
    {
        if (events == null)
            return Collections.EMPTY_LIST;
        return Collections.unmodifiableList(events);
    }

    public void clear()
    {
        if (events != null)
            events.clear();
    }

    public void dump()
    {
        int count = 1;
        for (LoggingEvent event : events)
        {
            System.err.println("EVENT#" + (count++) + "\t\t" + event.message);
            if (event.exception != null)
                event.exception.printStackTrace(System.err);
            System.err.println("-----------------------------------------------------");
        }

    }

}
