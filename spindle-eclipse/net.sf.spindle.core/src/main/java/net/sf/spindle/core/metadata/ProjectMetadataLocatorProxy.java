/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sf.spindle.core.metadata;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.TapestryCore;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;



/**
 * Instances of this class represent metadata locators found to be contributed via the locators
 * extension point. Currently, the concrete locator is loaded when the proxy is requested to do
 * work. This will result in the contributing plugin being loaded also.
 */
public class ProjectMetadataLocatorProxy implements IProjectMetadataLocator
{

    /**
     * Creates a new locator proxy based on the given configuration element. Returns the new proxy,
     * or null if the element could not be created.
     */
    public static ProjectMetadataLocatorProxy createProxy(IConfigurationElement element)
    {
        ProjectMetadataLocatorProxy result = new ProjectMetadataLocatorProxy();
        result.fElement = element;
        if ("metadataLocator".equals(element.getName())) //$NON-NLS-1$
            return result;
        TapestryCore.log(CoreMessages.format("project-metadata-unexpected-element", element
                .getDeclaringExtension().getNamespace(), element.getName()));
        return null;
    }

    private IConfigurationElement fElement;

    private boolean fLoaded = false;

    /**
     * The real locator instance
     */
    private IProjectMetadataLocator fLocator;

    /*
     * (non-Javadoc)
     * 
     * @see core.metadata.IProjectMetadataLocator#getWebContextRootFolder(org.eclipse.core.resources.IProject,
     *      java.lang.String)
     */
    public IFolder getWebContextRootFolder(String natureId, IProject project) throws CoreException
    {
        if (!fLoaded)
            loadLocator();
        return fLocator == null ? null : fLocator.getWebContextRootFolder(natureId, project);
    }

    /**
     * Loads the real locator, loading its associated plug-in if required. Returns the real locator
     * if it was successfully loaded.
     */
    IProjectMetadataLocator loadLocator()
    {
        synchronized (this)
        {
            if (fLocator != null || fLoaded)
                return fLocator;
            String bundleId = fElement.getDeclaringExtension().getNamespace();
            //set to true to prevent repeated attempts to load a broken locator
            fLoaded = true;
        }
        try
        {
            fLocator = (IProjectMetadataLocator) fElement.createExecutableExtension("locator"); //$NON-NLS-1$
        }
        catch (CoreException e)
        {
            TapestryCore.log(e);
        }
        return fLocator;
    }

    boolean isLoaded()
    {
        return fLoaded;
    }

    IExtension getExtension()
    {
        return fElement.getDeclaringExtension();
    }

    String getNatureId()
    {
        //cannot return null because it can cause startup failure
        String result = fElement.getAttribute("natureId"); //$NON-NLS-1$
        if (result != null)
            return result;
        TapestryCore.log(CoreMessages.format(CoreMessages.format(
                "project-metadata-missing-natureId",
                fElement.getDeclaringExtension().getNamespace(),
                fElement.getName())));
        return ""; //$NON-NLS-1$
    }
}