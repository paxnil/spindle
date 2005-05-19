package com.iw.plugins.spindle.core.spec;

import org.apache.tapestry.spec.InjectSpecification;

/**
 * @author gwl
 */
public class PluginInjectSpecification extends BaseSpecification implements InjectSpecification
{
    private String object;
    private String property;
    private String type;
    
    public PluginInjectSpecification()
    {
        super(BaseSpecification.INJECT);       
    }

    /**
     * @return Returns the object.
     */
    public String getObject()
    {
        return object;
    }
    /**
     * @return Returns the property.
     */
    public String getProperty()
    {
        return property;
    }
    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param object The object to set.
     */
    public void setObject(String object)
    {
        this.object = object;
    }
    /**
     * @param property The property to set.
     */
    public void setProperty(String property)
    {
        this.property = property;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type)
    {
        this.type = type;
    }

   

}
