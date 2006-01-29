package net.sf.spindle.core.resources.eclipse;

import net.sf.spindle.core.resources.ICoreResource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

/**
 * @author gwl
 */
public interface IEclipseResource extends ICoreResource
{
    IStorage getStorage();
    IProject getProject();
}
