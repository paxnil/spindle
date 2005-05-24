package com.iw.plugins.spindle.core.resources.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 * @author gwl
 */
public interface IEclipseResource extends IResourceWorkspaceLocation
{
    IStorage getStorage();
    IProject getProject();
}
