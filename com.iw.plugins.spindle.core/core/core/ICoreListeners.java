package core;

/**
 * Intereface for clients interested in knowing when Core changed
 * <p>
 * TODO this has got to be cleaned up
 * 
 * @author gwl
 */
public interface ICoreListeners
{
    void addCoreListener(ICoreListener listener);

    void removeCoreListener(ICoreListener listener);

    void buildOccurred();

    void fireCoreListenerEvent();
}
