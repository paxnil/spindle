/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
package net.sf.spindle.ui.config.metadata;

import net.sf.spindle.core.metadata.IProjectMetadataLocator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class DynamicWebProjectProjectMetaDataLocator implements IProjectMetadataLocator
{
    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.metadata.IProjectMetadataLocator#getWebContextRootFolder(java.lang.String,
     *      org.eclipse.core.resources.IProject)
     */
    public IFolder getWebContextRootFolder(String natureId, IProject project) throws CoreException
    {
        //org.eclipse.wst.common.project.facet.core.nature
        if (!project.hasNature(natureId))
            return null;

        IProjectFacetVersion facetVersion = getDWPFacetVersion(project);
        if (facetVersion == null)
            return null;

        IVirtualComponent vc = ComponentCore.createComponent(project);
        IVirtualFolder vf = vc.getRootFolder();
        return (IFolder) vf.getUnderlyingFolder();
    }

    private IProjectFacetVersion getDWPFacetVersion(IProject project) throws CoreException
    {
        IFacetedProject fproject = ProjectFacetsManager.create(project);
        if (fproject == null)
            return null;
        return fproject.getInstalledVersion(ProjectFacetsManager.getProjectFacet("jst.web"));
    }
}
