package com.iw.plugins.spindle.core.properties;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.IPropertySource;

/**
 * 
 */
public class NamespacePropertySource implements IPropertySource
{
    protected INamespace fNamespace;

    protected IPropertySource fProject;

    public NamespacePropertySource(IPropertySource projectPropertySource, INamespace namespace)
    {
        fNamespace = namespace;
        fProject = projectPropertySource;
    }

    public String getPropertyValue(String propertyName)
    {
        String result = null;
        if (fNamespace != null)
            result = fNamespace.getPropertyValue(propertyName);
        if (result != null)
            return result;
        return fProject.getPropertyValue(propertyName);
    }
}
