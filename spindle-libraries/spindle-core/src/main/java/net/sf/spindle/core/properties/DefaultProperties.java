package net.sf.spindle.core.properties;

import java.util.HashMap;
import java.util.Map;

import net.sf.spindle.core.TapestryCore;

import org.apache.tapestry.engine.IPropertySource;





/**
 * @author geoff
 */
public class DefaultProperties implements IPropertySource
{

    private static DefaultProperties INSTANCE;

    public static synchronized DefaultProperties getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new DefaultProperties();
        return INSTANCE;
    }

    private Map<String, String> properties;

    private DefaultProperties()
    {
        properties = new HashMap<String, String>();
        properties.put("org.apache.tapestry.engine-class", "org.apache.tapestry.engine.BaseEngine");
        properties.put("org.apache.tapestry.default-script-language", "jython");
        properties.put("org.apache.tapestry.visit-class", "java.util.HashMap");
        properties.put("org.apache.tapestry.output-encoding", "UTF-8");
        properties.put("org.apache.tapestry.enhance.disable-abstract-method-validation", "false");
        properties.put(
                "org.apache.tapestry.default-page-class",
                "org.apache.tapestry.html.BasePage");
        properties.put("org.apache.tapestry.template-extension", "html");
        properties.put("org.apache.tapestry.jwcid-attribute-name", "jwcid");
    }

    public String getPropertyValue(String propertyName)
    {
        if (!TapestryCore.isNull(propertyName))
            return (String) properties.get(propertyName);
        return null;
    }

}
