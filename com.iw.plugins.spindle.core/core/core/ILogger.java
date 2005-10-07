package core;

/**
 * @author gwl
 */
public interface ILogger
{
    void log(String msg);
    void log(Throwable ex);
    void log(String message, Throwable ex);
}
