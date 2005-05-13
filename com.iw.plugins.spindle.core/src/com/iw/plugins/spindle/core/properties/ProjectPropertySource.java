package com.iw.plugins.spindle.core.properties;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;

/**
 * 
 */
public class ProjectPropertySource implements IPropertySource
{

    protected IPropertySource fProjectSource;

    public ProjectPropertySource(ITapestryProject project)
    {
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        fProjectSource = manager.getPropertySource(project);
    }

    public ProjectPropertySource(IPropertySource projectPropertySource)
    {
        fProjectSource = projectPropertySource;
    }

    public IPropertySource createNamespacePropertySource(INamespace namespace)
    {
        return new NamespacePropertySource(this, namespace);
    }

    public IPropertySource createComponentPropertySource(IComponentSpecification specification)
    {
        return new ComponentPropertySource(this, specification);
    }

    public String getPropertyValue(String propertyName)
    {
        String result = null;
        if (fProjectSource != null)

            result = fProjectSource.getPropertyValue(propertyName);
        if (result != null)
            return result;
        return DefaultProperties.getInstance().getPropertyValue(propertyName);
    }
}
