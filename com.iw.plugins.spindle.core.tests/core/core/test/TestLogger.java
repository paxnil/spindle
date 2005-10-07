package core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import core.ILogger;


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

    private List events;

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
            events = new ArrayList();
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

    public List getEvents()
    {
        if (events == null)
            return Collections.EMPTY_LIST;
        return Collections.unmodifiableList(events);
    }
    
    public void clear() {
        if (events != null)
            events.clear();
    }

}
