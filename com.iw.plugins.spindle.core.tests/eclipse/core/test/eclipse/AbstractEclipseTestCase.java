package core.test.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;

import core.TapestryCore;
import core.source.IProblem;
import core.test.AbstractTestCase;

/**
 * Tests that creates project from prepackaged ones delivered in the plugin. These projects may have
 * any nature. To specifically set up prepackaged Java or Tapestry projects, use CoreJavaTestCase
 * instead
 * 
 * @author gwl
 */
public abstract class AbstractEclipseTestCase extends AbstractTestCase
{
    private static String[] COMMON_MARKER_PROPERTIES = new String[]
        {            
            IMarker.SEVERITY,
            IMarker.LOCATION,
            IMarker.MESSAGE,
            IMarker.LINE_NUMBER,
            IMarker.CHAR_START,
            IMarker.CHAR_END,
            IMarker.PRIORITY,
            IProblem.TEMPORARY_FLAG,
            IProblem.PROBLEM_CODE };

    private static Properties COMMON_MARKER_PROPERTIES_DESC;

    static
    {
        COMMON_MARKER_PROPERTIES_DESC = new Properties();
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.MARKER, "Type");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.SEVERITY, "Severity");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.LOCATION, "Location");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.LINE_NUMBER, "Line");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.CHAR_START, "CharStart");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.CHAR_END, "CharEnd");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.MESSAGE, "Message");
        COMMON_MARKER_PROPERTIES_DESC.put(IMarker.PRIORITY, "Priority");
        COMMON_MARKER_PROPERTIES_DESC.put(IProblem.TEMPORARY_FLAG, "Temporary");
        COMMON_MARKER_PROPERTIES_DESC.put(IProblem.PROBLEM_CODE, "Code");
    }

    public static String convertToIndependantLineDelimiter(String source)
    {
        if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1)
            return source;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = source.length(); i < length; i++)
        {
            char car = source.charAt(i);
            if (car == '\r')
            {
                buffer.append('\n');
                if (i < length - 1 && source.charAt(i + 1) == '\n')
                {
                    i++; // skip \n after \r
                }
            }
            else
            {
                buffer.append(car);
            }
        }
        return buffer.toString();
    }

    public AbstractEclipseTestCase(String name)
    {
        super(name);
    }

    protected void setUpTapestryCore()
    {
        // TapestryCore already exists in the Eclipse env - we need to
        // intercept log entries.
        TapestryCore.getDefault().setLogger(logger);
    }

    protected IProject setUpProject(final String projectName) throws CoreException, IOException
    {
        // copy files in project from source workspace to target workspace
        String sourceWorkspacePath = getSourceWorkspacePath();
        String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
        copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath,
                projectName));

        final IProject project = getWorkspaceRoot().getProject(projectName);
        IWorkspaceRunnable populate = new IWorkspaceRunnable()
        {
            public void run(IProgressMonitor monitor) throws CoreException
            {
                project.create(null);
                project.open(null);
            }
        };
        getWorkspace().run(populate, null);
        waitForEclipseBuilder();
        return project;
    }

    protected void deleteProject(String projectName) throws CoreException
    {
        IProject project = this.getProject(projectName);
        if (project.exists() && !project.isOpen())
        { // force opening so that project can be deleted without logging (see bug 23629)
            project.open(null);
        }
        deleteResource(project);
    }

    public void deleteResource(IResource resource) throws CoreException
    {
        CoreException lastException = null;
        try
        {
            resource.delete(true, null);
        }
        catch (CoreException e)
        {
            lastException = e;
            // just print for info
            System.out.println(e.getMessage());
        }
        catch (IllegalArgumentException iae)
        {
            // just print for info
            System.out.println(iae.getMessage());
        }
        int retryCount = 60; // wait 1 minute at most
        while (resource.isAccessible() && --retryCount >= 0)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
            }
            try
            {
                resource.delete(true, null);
            }
            catch (CoreException e)
            {
                lastException = e;
                // just print for info
                System.out.println("Retry " + retryCount + ": " + e.getMessage());
            }
            catch (IllegalArgumentException iae)
            {
                // just print for info
                System.out.println("Retry " + retryCount + ": " + iae.getMessage());
            }
        }
        if (!resource.isAccessible())
            return;
        System.err.println("Failed to delete " + resource.getFullPath());
        if (lastException != null)
        {
            throw lastException;
        }
    }

    protected void copyDirectory(File source, File target) throws IOException
    {
        if (!target.exists())
        {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++)
        {
            File sourceChild = files[i];
            String name = sourceChild.getName();
            if (name.equals("CVS"))
                continue;
            File targetChild = new File(target, name);
            if (sourceChild.isDirectory())
            {
                copyDirectory(sourceChild, targetChild);
            }
            else
            {
                copy(sourceChild, targetChild);
            }
        }
    }

    /**
     * Copy file from src (path to the original file) to dest (path to the destination file).
     */
    public void copy(File src, File dest) throws IOException
    {
        // read source bytes
        byte[] srcBytes = this.read(src);

        if (convertToIndependantLineDelimiter(src))
        {
            String contents = new String(srcBytes);
            contents = convertToIndependantLineDelimiter(contents);
            srcBytes = contents.getBytes();
        }

        // write bytes to dest
        FileOutputStream out = new FileOutputStream(dest);
        out.write(srcBytes);
        out.close();
    }

    public boolean convertToIndependantLineDelimiter(File file)
    {
        return false;
    }

    public byte[] read(java.io.File file) throws IOException
    {
        int fileLength;
        byte[] fileBytes = new byte[fileLength = (int) file.length()];
        java.io.FileInputStream stream = new java.io.FileInputStream(file);
        int bytesRead = 0;
        int lastReadSize = 0;
        while ((lastReadSize != -1) && (bytesRead != fileLength))
        {
            lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
            bytesRead += lastReadSize;
        }
        stream.close();
        return fileBytes;
    }

    public String read(InputStream contentStream, String encoding) throws IOException
    {
        Reader in = null;
        try
        {
            if (encoding == null)
                in = new BufferedReader(new InputStreamReader(contentStream));
            else
                in = new BufferedReader(new InputStreamReader(contentStream, encoding));
            int chunkSize = contentStream.available();
            StringBuffer buffer = new StringBuffer(chunkSize);
            int c = -1;
            while ((c = in.read()) != -1)
                buffer.append((char) c);
            return buffer.toString();
        }
        finally
        {
            if (in != null)
                in.close();
        }
    }

    public String getSourceWorkspacePath()
    {
        return getPluginDirectoryPath() + java.io.File.separator + "workspace";
    }

    protected String getPluginDirectoryPath()
    {
        try
        {
            URL platformURL = Platform.getBundle("spindle.core.tests").getEntry("/");
            return new File(Platform.asLocalURL(platformURL).getFile()).getAbsolutePath();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    protected IProject getProject(String project)
    {
        return getWorkspaceRoot().getProject(project);
    }

    public IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public IWorkspaceRoot getWorkspaceRoot()
    {
        return getWorkspace().getRoot();
    }

    public void waitForEclipseBuilder()
    {
        IJobManager jobManager = Platform.getJobManager();
        try
        {
            jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
            jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
        }
        catch (InterruptedException e)
        {
            // just continue
        }
    }

    protected void assertBuildSpecHasBuilder(IProject project, String builderID)
            throws CoreException
    {
        assertNotNull(project);
        assertNotNull(builderID);
        IProjectDescription description = project.getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(builderID))
                return;
        }

        throw new AssertionFailedError("Project '" + project + "' does not have builder '"
                + builderID);
    }

    protected void assertProjectHasNature(IProject project, String natureID) throws CoreException
    {
        assertNotNull(project);
        assertNotNull(natureID);
        assertNotNull("Project '" + project + "' does not have nature '" + natureID, project
                .getNature(natureID));
    }

    protected IMarker[] getMarkers(IResource resource, String type, boolean includeSubtypes,
            int depth)
    {
        try
        {
            return resource.findMarkers(type, includeSubtypes, depth);
        }
        catch (CoreException e)
        {
            fail(e.getMessage());
        }
        return null;
    }

    protected void assertProjectHasNoTapestryErrorMarkers(IProject project)
    {
        IMarker[] markers = getMarkers(
                project,
                IProblem.TAPESTRY_PROBLEM_MARKER,
                true,
                IResource.DEPTH_INFINITE);
        if (markers.length > 0)
        {
            System.out.println(createMarkerReport(markers));

            fail("project '" + project + "' has " + markers.length + " tapestry error markers");
        }
    }

    String createMarkerReport(IMarker[] markers)
    {

        StringBuffer report = new StringBuffer();

        final String NEWLINE = System.getProperty("line.separator");
        final char DELIMITER = '\t';

        // create header
        for (int i = 0; i < COMMON_MARKER_PROPERTIES.length; i++)
        {
            report.append(COMMON_MARKER_PROPERTIES_DESC.get(COMMON_MARKER_PROPERTIES[i]));
            if (i == COMMON_MARKER_PROPERTIES.length - 1)
                report.append(NEWLINE);
            else
                report.append(DELIMITER);
        }

        for (int i = 0; i < markers.length; i++)
        {
            for (int j = 0; j < COMMON_MARKER_PROPERTIES.length; j++)
            {
                String value = markers[i].getAttribute(COMMON_MARKER_PROPERTIES[j], "no value");               
                report.append(value);
                if (j == COMMON_MARKER_PROPERTIES.length - 1)
                    report.append(NEWLINE);
                else
                    report.append(DELIMITER);
            }
        }

        return report.toString();
    }
}
