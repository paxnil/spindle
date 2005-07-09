package com.iw.plugins.spindle.core.resources.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

import com.iw.plugins.spindle.core.resources.ICoreResource;

/**
 * @author gwl
 */
public interface IEclipseResource extends ICoreResource
{
    IStorage getStorage();
    IProject getProject();
}
