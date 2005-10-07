package core;

import com.iw.plugins.spindle.messages.MessageFormatter;


/**
 * @author gwl
 */
public class CoreMessages
{
    protected static MessageFormatter _formatter = new MessageFormatter(TapestryCore.class,
            "resources");    

    public static String format(String key, Object[] args)
    {
        return _formatter.format(key, args);
    }

    public static String format(String key)
    {
        return format(key, new Object [] {});
    }

    public static String format(String key, Object arg)
    {
        return format(key, new Object[]
        { arg });
    }

    public static String format(String key, Object arg1, Object arg2)
    {
        return format(key, new Object[]
        { arg1, arg2 });
    }

    public static String format(String key, Object arg1, Object arg2, Object arg3)
    {
        return format(key, new Object[]
        { arg1, arg2, arg3 });
    }
    
}