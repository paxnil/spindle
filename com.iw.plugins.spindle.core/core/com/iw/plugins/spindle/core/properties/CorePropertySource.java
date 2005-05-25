package com.iw.plugins.spindle.core.properties;

import org.apache.tapestry.engine.IPropertySource;

/**
 * @author gwl
 *
 */
public class CorePropertySource implements IPropertySource
{
    
    IPropertySource fParent;
    IPropertySource fChild;

    //Defaults are implied parent
    public CorePropertySource(IPropertySource child)
    {
       this(DefaultProperties.getInstance(), child);
    }
    
    private CorePropertySource(IPropertySource parent, IPropertySource child) {
        fParent = parent;
        fChild = child;
    }
    
    public IPropertySource createChildPropertySource(IPropertySource child) {
        return new CorePropertySource(this, child);
    }

    public String getPropertyValue(String arg0)
    {
        String result = fChild.getPropertyValue(arg0);
        if (result != null)
            return result;
        
        return fParent.getPropertyValue(arg0);
    }

}
