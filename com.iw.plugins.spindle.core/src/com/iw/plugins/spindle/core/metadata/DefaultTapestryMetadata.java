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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.metadata;

import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.Files;

/**
 * Utility class for obtaining and setting the metadata spindle needs to run. This data is stored in
 * the .tapestryplugin file in the project root. <br/>This class has no facility for adding or
 * removing the Tapestry project nature.
 */
public class DefaultTapestryMetadata
{
    //  Persistence properties of projects
    public static final String PROPERTIES_FILENAME = ".tapestryplugin";

    public static final String KEY_TYPE = "project-type";

    public static final String KEY_CONTEXT = "context-root";

    public static final String KEY_LIBRARY = "library-spec";

    public static final String KEY_VALIDATE = "validate-web-xml";

    public static final int APPLICATION_PROJECT_TYPE = 0;

    private IProject fProject;

    protected String fWebContext;

    protected IFolder fWebContextFolder;

    protected boolean fValidateWebXML = true;

    protected boolean forceCreation;

    protected boolean loaded = false;

    protected boolean metaExistedOnCreation;

    public DefaultTapestryMetadata(IProject project, boolean force)
    {
        Assert.isTrue(project != null && project.isAccessible());
        fProject = project;
        forceCreation = force;
        metaExistedOnCreation = metaFileExists();
    }

    public boolean metaFileExistedOnCreation()
    {
        return metaExistedOnCreation;
    }

    public boolean metaFileExists()
    {
        IFile file = getPropertiesFile();
        return file != null && file.exists();
    }

    private void checkLoaded()
    {
        if (loaded)
            return;
        try
        {
            fWebContext = this.readProperty(KEY_CONTEXT);
            initWebContextFolder(false);

            String value = this.readProperty(KEY_VALIDATE);
            if (value == null || "".equals(value))
            {
                fValidateWebXML = true;
            }
            else
            {
                fValidateWebXML = new Boolean(value).booleanValue();
            }
        }
        finally
        {
            loaded = true;
        }
    }

    public String getWebContext()
    {
        checkLoaded();
        return this.readProperty(KEY_CONTEXT);
    }

    public void setWebContext(String context)
    {
        checkLoaded();
        this.fWebContext = context;
        fWebContextFolder = null;
    }

    public IFolder getWebContextFolder()
    {
        checkLoaded();
        if (fWebContextFolder == null && forceCreation)
            initWebContextFolder(true);
        return fWebContextFolder;
    }

    public boolean isValidatingWebXML()
    {
        checkLoaded();
        return fValidateWebXML;

    }

    public void setValidateWebXML(boolean flag)
    {
        fValidateWebXML = flag;
    }

    /**
     * remove the .tapestryplugin file, if possible
     */
    public void clearProperties()
    {
        IFile properties = getPropertiesFile();
        if (properties != null && properties.exists())
            try
            {
                properties.delete(true, false, null);
            }
            catch (CoreException e)
            {
                TapestryCore.log(e);
            }
    }

    private IFile getPropertiesFile()
    {
        return fProject.getFile(new Path(PROPERTIES_FILENAME));
    }

    private String readProperty(String key)
    {
        String result = null;
        try
        {
            result = Files.readPropertyInXMLFile(getPropertiesFile(), key);
        }
        catch (IOException e)
        {
            TapestryCore.log(e);
        }

        if (result == null)
            result = "";

        return result;
    }

    private int readIntProperty(String key)
    {
        String result = readProperty(key);
        if (result == null || result.trim().length() == 0)
            return 0;

        if (result != null)
            return new Integer(result).intValue();

        return -1;
    }

    public void saveProperties(IProgressMonitor monitor)
    {
        // no change needed here as only the regular Spindle wizard and properties page use this!
        // boolean isApplicationProject = APPLICATION_PROJECT_TYPE == fProjectType;
        boolean isApplicationProject = true;
        try
        {
            StringBuffer fileContent = new StringBuffer();
            fileContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fileContent.append("<tapestry-project-properties>\n");
            fileContent
                    .append("    <validate-web-xml>" + fValidateWebXML + "</validate-web-xml>\n");

            fileContent.append("    <context-root>" + (fWebContext != null ? fWebContext : "")
                    + "</context-root>\n");

            fileContent.append("</tapestry-project-properties>\n");
            monitor.subTask("saving project properties");
            Files.toTextFile(getPropertiesFile(), fileContent.toString(), monitor);
            monitor.worked(1);
        }
        catch (Exception ex)
        {
            TapestryCore.log(ex.getMessage());
        }
    }

    private IFolder initWebContextFolder(boolean create)
    {
        IFolder result = null;
        try
        {
            result = initFolder(fWebContext, create);
        }
        catch (CoreException e)
        {
            this.fWebContext = "/";
        }
        fWebContextFolder = result;
        return result;
    }

    private IFolder initFolder(String path, boolean create) throws CoreException
    {
        StringTokenizer tokenizer = new StringTokenizer(path, "/\\:");
        IFolder folder = null;
        while (tokenizer.hasMoreTokens())
        {
            String each = tokenizer.nextToken();
            if (folder == null)
            {
                folder = fProject.getFolder(each);
            }
            else
            {
                folder = folder.getFolder(each);
            }
            if (create && !folder.exists())
                this.createFolder(folder);
        }

        return folder;
    }

    private void createFolder(IFolder folderHandle) throws CoreException
    {
        try
        {
            folderHandle.create(false, true, null);
        }
        catch (CoreException e)
        {
            // If the folder already existed locally, just refresh to get contents
            if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
            {
                folderHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
            }
            else
            {
                throw e;
            }
        }
    }

}