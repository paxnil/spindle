/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.builder.State;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;

/**
 * 
 *  A dialog that allows a user to choose a Tapestry project from the workspace.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TapestryProjectDialogField extends StringButtonField
{

    protected String fName;
    private IWorkspaceRoot fWorkspaceRoot;
    private TapestryProject fCurrentTapestryProject;

    public TapestryProjectDialogField(String name, IWorkspaceRoot root, int labelWidth)
    {
        super(UIPlugin.getString(name + ".label"), labelWidth);
        this.fName = name;
        fWorkspaceRoot = root;
        fCurrentTapestryProject = null;
    }

    public TapestryProjectDialogField(String name, IWorkspaceRoot root)
    {
        this(name, root, -1);

    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field)
    {

        TapestryProject chosen = chooseTapestryProject();
        if (chosen != null)
        {
            setTapestryProject(chosen, isEnabled());
        }
    }

    public void dialogFieldChanged(DialogField field)
    {
        if (field == this)
        {
            setStatus(projectChanged());
        }

    }

    public String getContainerText()
    {
        return getTextValue();
    }

    public TapestryProject getTapestryProject()
    {
        return fCurrentTapestryProject;
    }

    public void setTapestryProject(TapestryProject project, boolean canBeModified)
    {
        TapestryProject old = fCurrentTapestryProject;
        fCurrentTapestryProject = project;
        String str = (project == null) ? "" : project.getProject().getFullPath().toString();
        setTextValue(str);
        setEnabled(canBeModified);
    }

    public void init(IJavaElement jelem, IRunnableContext context)
    {
        super.init(context);
        setButtonLabel(UIPlugin.getString(fName + ".button"));
        TapestryProject tproject = null;
        IProject project = null;
        if (jelem != null)
            project = jelem.getJavaProject().getProject();

        if (project != null)
        {
            try
            {
                tproject = (TapestryProject) project.getNature(TapestryCore.NATURE_ID);
            } catch (CoreException e)
            {
                UIPlugin.log(e);
            }
        }

        setTapestryProject(tproject, true);
    }

    private TapestryProject getTapestryNature(IProject project)
    {
        try
        {
            return (TapestryProject) project.getNature(TapestryCore.NATURE_ID);
        } catch (CoreException e)
        {
            UIPlugin.log(e);
        }
        return null;
    }

    private TapestryProject chooseTapestryProject()
    {
        IJavaProject thisProject = null;
        try
        {
            if (fCurrentTapestryProject != null)
                thisProject = fCurrentTapestryProject.getJavaProject();
        } catch (CoreException e)
        {
            UIPlugin.log(e);
        }

        final IJavaProject useProject = thisProject;
        Class[] acceptedClasses = new Class[] { IJavaProject.class };
        ISelectionStatusValidator validator = new TypeSelectionValidator(acceptedClasses, false)
        {
            public boolean isSelectedValid(Object element)
            {
                if (element instanceof IJavaProject)
                    return TapestryProjectDialogField.this.hasTapestryNature(((IJavaProject) element).getProject());

                return false;
            }
        };

        acceptedClasses = new Class[] { IJavaProject.class };
        ViewerFilter filter = new TypeFilter(acceptedClasses)
        {
            public boolean select(Viewer viewer, Object parent, Object element)
            {
                if (element instanceof IJavaProject)
                {
                    if (((IJavaProject) element).equals(useProject))
                        return true;

                    return TapestryProjectDialogField.this.hasTapestryNature(((IJavaProject) element).getProject());
                }
                return super.select(viewer, parent, element);
            }
        };
        StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
        ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
        dialog.setValidator(validator);
        dialog.setSorter(new JavaElementSorter());
        dialog.setTitle(UIPlugin.getString(fName + ".ChooseTapestryProjectDialog.title"));
        dialog.setMessage(UIPlugin.getString(fName + ".ChooseTapestryProjectDialog.description"));
        dialog.addFilter(filter);
        dialog.setInput(JavaCore.create(fWorkspaceRoot));
        dialog.setInitialSelection(thisProject);

        if (dialog.open() == dialog.OK)
        {
            Object element = dialog.getFirstResult();
            if (element instanceof IJavaProject)
            {
                IJavaProject jproject = (IJavaProject) element;
                try
                {
                    return (TapestryProject) jproject.getProject().getNature(TapestryCore.NATURE_ID);
                } catch (CoreException e)
                {
                    UIPlugin.log(e);
                }
            }
        }
        return null;
    }

    protected IStatus projectChanged()
    {
        SpindleStatus status = new SpindleStatus();

        fCurrentTapestryProject = null;
        String str = getContainerText();
        if (str == null || "".equals(str))
        {
            status.setError(UIPlugin.getString(fName + ".error.EnterProjectName"));
            return status;
        }
        IPath path = new Path(str);
        IResource res = fWorkspaceRoot.findMember(path);
        if (res != null)
        {
            if (!res.exists())
            {
                status.setError(UIPlugin.getString(fName + ".error.ProjectDoesNotExist", str));
                return status;
            }
            int resType = res.getType();
            if (res.getType() != IResource.PROJECT)
            {
                status.setError(UIPlugin.getString(fName + ".error.NotAProject", str));
                return status;
            }

            IProject proj = (IProject) res;
            if (!proj.isOpen())
            {
                status.setError(UIPlugin.getString(fName + ".error.ProjectClosed", proj.getFullPath().toString()));
                return status;
            }

            TapestryProject newProject = getTapestryNature(proj);
            if (newProject == null)
            {
                status.setError(
                    UIPlugin.getString(fName + ".error.NotATapestryProject", proj.getFullPath().toString()));
                return status;
            }

            try
            {
                IMarker[] brokenBuildMarkers =
                    proj.findMarkers(ITapestryMarker.TAPESTRY_BUILDBROKEN_MARKER, false, IResource.DEPTH_ZERO);

                if (brokenBuildMarkers.length > 0)
                {
                    status.setError(UIPlugin.getString(fName + ".error.ProjectIsBroken", str));
                    return status;
                }
            } catch (CoreException e)
            {
                UIPlugin.log(e);
            }

            State state = (State) TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(proj, true, fRunnableContext);
            if (state == null)
            {
                status.setError(UIPlugin.getString(fName + ".error.ProjectIsBroken2", str));
                return status;
            }

            fCurrentTapestryProject = newProject;
        } else
        {
            status.setError(UIPlugin.getString(fName + ".error.ProjectDoesNotExist", str));
        }

        return status;
    }

    private TapestryProject getTapestryProject(IProject project)
    {

        try
        {
            return (TapestryProject) project.getNature(TapestryCore.NATURE_ID);
        } catch (CoreException e)
        {
            UIPlugin.log(e);

        }
        return null;
    }

    private boolean hasTapestryNature(IProject project)
    {
        try
        {
            return project.hasNature(TapestryCore.NATURE_ID);
        } catch (CoreException e)
        {
            UIPlugin.log(e);

        }
        return false;
    }

}