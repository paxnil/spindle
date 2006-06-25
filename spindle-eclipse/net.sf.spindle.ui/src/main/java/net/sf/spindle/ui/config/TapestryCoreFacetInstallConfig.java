package net.sf.spindle.ui.config;

import org.eclipse.wst.common.project.facet.core.IActionConfigFactory;

public final class TapestryCoreFacetInstallConfig
{

    public static final class Factory implements IActionConfigFactory
    {
        public Object create()
        {
            return new TapestryCoreFacetInstallConfig();
        }
    }

    private String applicationName = "app";

    String getApplicationName()
    {
        return applicationName;
    }

    void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }
}
