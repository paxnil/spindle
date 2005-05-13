package com.iw.plugins.spindle.core.properties;

import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

/**
 * 
 */
public class ComponentPropertySource extends NamespacePropertySource implements IPropertySource
{
    IComponentSpecification fComponent;
    
    public ComponentPropertySource(IPropertySource projectPropertySource, IComponentSpecification component)
    {
        super(projectPropertySource, ((PluginComponentSpecification)component).getNamespace());
        fComponent = component;
    }
    public String getPropertyValue(String propertyName)
    {
        String result = null;
        if (fComponent != null)
         result = fComponent.getProperty(propertyName);
        if (result != null)
            return result;
        return super.getPropertyValue(propertyName);
    }
}
