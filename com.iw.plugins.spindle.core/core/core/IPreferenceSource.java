package core;

/**
 * @author gwl
 *
 */
public interface IPreferenceSource
{
    public double getDouble(String name);
 
    public float getFloat(String name);
   
    public int getInt(String name);
   
    public long getLong(String name);
    
    public String getString(String name);
}
