package core.builder;

import java.util.Map;

import org.apache.tapestry.engine.IPropertySource;

import core.properties.DefaultProperties;

/**
 * Container of interesting things we scraped from web.xml
 */
public class WebAppDescriptor implements IPropertySource
{
    ServletInfo[] fServletInfos;

    Map fContextParameters;

    /**
     * @return Returns the contextParameters.
     */
    public Map getContextParameters()
    {
        return fContextParameters;
    }

    /**
     * @param contextParameters
     *            The contextParameters to set.
     */
    public void setContextParameters(Map contextParameters)
    {
        this.fContextParameters = contextParameters;
    }

    /**
     * @return Returns the servletInfos.
     */
    public ServletInfo[] getServletInfos()
    {
        return fServletInfos;
    }

    /**
     * @param servletInfos
     *            The servletInfos to set.
     */
    public void setServletInfos(ServletInfo[] servletInfos)
    {
        this.fServletInfos = servletInfos;
    }
    
    public String getPropertyValue(String propertyName)
    {
        String result = internalGetPropertyValue(propertyName);
        if (result != null)
            return result;
        
        return DefaultProperties.getInstance().getPropertyValue(propertyName);
    }

    public String internalGetPropertyValue(String propertyName)
    {
        if (fServletInfos == null || fServletInfos.length == 0)
            return null;
        String result = (String) fServletInfos[0].parameters.get(propertyName);
        if (result != null)
            return result;
        
        if (fContextParameters == null)
            return null;
        
        return (String) fContextParameters.get(propertyName);
    }
}
