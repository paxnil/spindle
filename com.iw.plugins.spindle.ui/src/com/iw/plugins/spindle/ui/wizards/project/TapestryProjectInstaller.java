/*
 * Created on Apr 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.iw.plugins.spindle.ui.wizards.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jdom.Document;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryCorePlugin;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.metadata.DefaultTapestryMetadata;
import com.iw.plugins.spindle.core.util.IndentingWriter;
import com.iw.plugins.spindle.core.util.XMLPublicIDUtil;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;
import com.iw.plugins.spindle.ui.properties.ProjectPropertyPage;
import com.iw.plugins.spindle.ui.wizards.factories.ApplicationFactory;
import com.iw.plugins.spindle.ui.wizards.factories.IFactoryTemplateSource;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TapestryTemplateFactory;
import com.iw.plugins.spindle.xmlinspector.WebXMLInspector;

/**
 * @author glongman@gmail.com
 *  
 */
public class TapestryProjectInstaller {

	TapestryProjectInstallData installData;

	private Map tapestryJarNames;

	public TapestryProjectInstaller(TapestryProjectInstallData data) {
		super();
		installData = data;
	}

	public IStatus configureTapestryProject(ArrayList filesBuilt,
			IProgressMonitor monitor) throws InterruptedException {

		monitor.beginTask(UIPlugin
				.getString("new-project-wizard-page-initializing"), 6);

		IStatus status = new SpindleStatus();

		String applicationName = installData.getApplicationName();
		IFactoryTemplateSource templateSource = installData.getTemplateSource();
		PageFactory pageFactory = installData.getPageFactory();
		ApplicationFactory applicationFactory = installData
				.getApplicationFactory();
		TapestryTemplateFactory templateFactory = installData
				.getTemplateFactory();

		IFolder contextFolder = installData.getProject().getFolder(
				installData.getContextPath());

		try {
			filesBuilt.add(contextFolder);
			if (!contextFolder.exists()) {
				contextFolder.create(true, true, monitor);				
			}
		} catch (CoreException e1) {
			return e1.getStatus();
		}

		monitor.worked(1);

		IFolder webInfFolder = contextFolder.getFolder("WEB-INF");

		try {
			filesBuilt.add(webInfFolder);
			if (!webInfFolder.exists()) {
				webInfFolder.create(true, true, monitor);				
			}
		} catch (CoreException e2) {
			return e2.getStatus();
		}

		monitor.worked(1);

		IFile webXML = webInfFolder.getFile("web.xml");
		if (!webXML.exists()) {

			status = configureNewWebXML(applicationName, webInfFolder,
					filesBuilt, installData.isWriteRedirectFilter(), monitor);
		} else {
			status = configureExistingWebXML(webInfFolder, monitor);
		}
		if (status.getSeverity() == IStatus.ERROR)
			return status;

		monitor.worked(1);

		try {
			// the project application spec
			filesBuilt.add(applicationFactory.createApplication(webInfFolder,
					installData.getApplicationFileTemplate(), applicationName,
					CoreMessages.format("TapestryEngine.defaultEngine"),
					"Home.page", monitor));
		} catch (CoreException e3) {
			return e3.getStatus();
		}

		monitor.worked(1);

		try {
			//the home page spec
			IFile pageFile = webInfFolder.getFile("Home.page");
			filesBuilt.add(pageFactory.createPage(pageFile, templateSource
					.getTemplate(pageFactory), CoreMessages
					.format("TapestryPageSpec.defaultSpec"), monitor));
		} catch (CoreException e4) {
			return e4.getStatus();
		}

		monitor.worked(1);

		// the home page template
		IFile templateFile = webInfFolder.getFile("Home.html");
		try {
			filesBuilt.add(templateFactory.createTapestryTemplate(templateFile,
					templateSource.getTemplate(templateFactory), monitor));
		} catch (CoreException e) {
			return e.getStatus();
		}

		monitor.done();

		return SpindleStatus.OK_STATUS;
	}

	private IStatus configureNewWebXML(String projectName,
			IFolder webInfFolder, ArrayList builtFiles,
			boolean writeRedirectFilter, IProgressMonitor monitor) {

		IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
		boolean useTabs = store
				.getBoolean(PreferenceConstants.FORMATTER_TAB_CHAR);
		int tabSize = store.getInt(PreferenceConstants.FORMATTER_TAB_SIZE);
		StringWriter swriter = new StringWriter();
		IndentingWriter iwriter = new IndentingWriter(swriter, useTabs,
				tabSize, 0, null);

		XMLUtil.writeWebDOTXML(projectName, XMLPublicIDUtil.getPublicId(installData
				.getServletSpecPublicId()), writeRedirectFilter, iwriter);
		iwriter.flush();
		IFile webDotXML = webInfFolder.getFile("web.xml");
		builtFiles.add(webDotXML);
		InputStream contents = new ByteArrayInputStream(swriter.toString()
				.getBytes());
		try {
			webDotXML
					.create(contents, true, new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			return e.getStatus();
		}
		return SpindleStatus.OK_STATUS;
	}

	private IStatus configureExistingWebXML(IFolder webInfFolder,
			IProgressMonitor monitor) {

		SpindleStatus result = new SpindleStatus();
		IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
		boolean useTabs = store
				.getBoolean(PreferenceConstants.FORMATTER_TAB_CHAR);
		int tabSize = store.getInt(PreferenceConstants.FORMATTER_TAB_SIZE);

		String indentString = "\t";
		if (!useTabs) {
			indentString = "";
			for (int i = 0; i < tabSize; i++)
				indentString += ' ';
		}

		IFile webxml = webInfFolder.getFile("web.xml");
		String newContents = null;
		try {
			Document document = WebXMLInspector
					.loadWebXML(webxml.getContents());
			WebXMLInspector.addTapestryServlet(document.getRootElement(),
					installData.getApplicationName(),
					"org.apache.tapestry.ApplicationServlet", "/app");
			newContents = WebXMLInspector.printDocument(document, indentString);
		} catch (CoreException e1) {
			return e1.getStatus();
		} catch (IOException e1) {
			result.setError(e1.getMessage());
		} catch (Exception e1) {
			result.setError(e1.getMessage());
		}

		try {
			webxml.setContents(
					new ByteArrayInputStream(newContents.getBytes()), true,
					true, monitor);
		} catch (CoreException e) {
			return e.getStatus();
		}
		return result;
	}

	public IStatus addTapestryNature(IProgressMonitor monitor) {

		String contextPath = installData.getContextPath();
		IProject project = installData.getProject();
		boolean writeMetaData = installData.isWritingMetaData();
		try {
			project.setPersistentProperty(new QualifiedName("",
					ProjectPropertyPage.CONTEXT_ROOT_PROPERTY), "/"
					+ contextPath);

			boolean validate = installData.getServletSpecPublicId() != XMLPublicIDUtil.UNKNOWN_DTD;

			project.setPersistentProperty(new QualifiedName("",
					ProjectPropertyPage.VALIDATE_WEBXML_PROPERTY), Boolean
					.toString(writeMetaData));
		} catch (CoreException e) {
			//no need to stop the install.
		}

		// now configure the project nature
		if (writeMetaData) {
			DefaultTapestryMetadata meta = new DefaultTapestryMetadata(project,
					true);
			meta.setWebContext("/" + contextPath);
			meta.getWebContextFolder();//force creation
			meta.setValidateWebXML(false);
			meta.saveProperties(monitor);
		}
		return TapestryProject
				.addTapestryNature(installData.getProject(), true);
	}

	public IStatus installTapestryFrameworkLibrary(IProgressMonitor monitor) {
		try {
			IJavaProject project = JavaCore.create(installData.getProject());
			IClasspathEntry[] entries = project.getRawClasspath();
			entries = addTapestryFrameworkLibrary(entries);
			project.setRawClasspath(entries, monitor);

		} catch (CoreException e) {
			return e.getStatus();
		}
		return SpindleStatus.OK_STATUS;
	}

	public List findLibraryCollisions() {
		if (tapestryJarNames == null)
			collectTapestryJars();

		List result = Collections.EMPTY_LIST;

		IFolder webInfLib = installData.getProject().getFolder(
				installData.getContextPath() + "/WEB-INF/lib");

		if (!webInfLib.exists())
			return result;

		if (tapestryJarNames.isEmpty())
			return Collections.EMPTY_LIST;

		try {
			Set jarNames = tapestryJarNames.keySet();
			result = new ArrayList();
			IResource[] members = webInfLib.members();
			for (int i = 0; i < members.length; i++) {
				if (members[i].getType() != IResource.FILE)
					continue;

				String name = members[i].getName();
				if (jarNames.contains(name))
					result.add(name);
			}
		} catch (CoreException e) {
			UIPlugin.log_it(e);
		}
		return result;

	}

	public IStatus installLibraries(boolean overwriteExisting, List collisions,
			IProgressMonitor monitor) {

		if (tapestryJarNames == null)
			collectTapestryJars();

		IFolder root = installData.getProject().getFolder(
				installData.getContextPath());
		try {
			if (!root.exists()) {
				root.create(true, true, monitor);
			}
		} catch (CoreException e1) {
			return e1.getStatus();
		}

		IFolder webInf = root.getFolder("WEB-INF");

		try {
			if (!webInf.exists()) {
				webInf.create(true, true, monitor);
			}
		} catch (CoreException e1) {
			return e1.getStatus();
		}

		IFolder lib = webInf.getFolder("lib");

		try {
			if (!webInf.exists()) {
				webInf.create(true, true, monitor);
			}
		} catch (CoreException e1) {
			return e1.getStatus();
		}

		int totalCount = tapestryJarNames.size();
		int count = collisions.isEmpty() ? totalCount
				: (overwriteExisting ? totalCount : totalCount
						- collisions.size());

		monitor.beginTask("copying Tapestry jar files..", count);

		File destFolder = new File(lib.getLocation().toString());

		List toCopy = new ArrayList();
		List statuses = new ArrayList();
		for (Iterator iter = tapestryJarNames.keySet().iterator(); iter
				.hasNext();) {

			String jarName = (String) iter.next();
			if (!overwriteExisting && collisions.contains(jarName))
				continue;

			File src = (File) tapestryJarNames.get(jarName);
			IStatus status = copyJar(src, lib, jarName, monitor);
			if (!status.isOK())
				statuses.add(status);
			monitor.worked(1);
			toCopy.add(tapestryJarNames.get(jarName));
		}

		monitor.done();
		
		if (statuses.isEmpty()) {
			return SpindleStatus.OK_STATUS;
		} else {
			return new MultiStatus(UIPlugin.PLUGIN_ID, 0,
					(IStatus[]) statuses.toArray(new IStatus[statuses.size()]),
					"Unable to populate project with all Tapestry libraries.",
					null);
		}
	}

	private IStatus copyJar(File src, IFolder destFolder, String name,
			IProgressMonitor monitor) {
		
		SpindleStatus result = new SpindleStatus();
		IFile dest = destFolder.getFile(name);
		
		try {
			
			dest.create(new FileInputStream(src), true, monitor);
		} catch (CoreException e) {
			
			UIPlugin.log_it(e);
			result.setError("Error creating file: " + dest.getName());
		} catch (FileNotFoundException e) {
			
			UIPlugin.log_it(e);
			result.setError("Error opening file: " + src.toString());
		}
		return result;
	}

	/**
	 *  
	 */
	private void collectTapestryJars() {
		URL installUrl = TapestryCore.getDefault().getBundle().getEntry("/");
		tapestryJarNames = Collections.EMPTY_MAP;
		File lib = null;
		File ext = null;
		try {
			installUrl = Platform.resolve(installUrl);
			URL libFolderUrl = new URL(installUrl, "lib/");
			libFolderUrl = Platform.resolve(libFolderUrl);
			lib = new File(new URI(libFolderUrl.toString()));

			URL extFolderUrl = new URL(libFolderUrl, "ext/");
			extFolderUrl = Platform.resolve(extFolderUrl);
			ext = new File(new URI(extFolderUrl.toString()));

		} catch (Exception e) {
			UIPlugin.log_it(e);
			return;
		}
		tapestryJarNames = new HashMap();
		collectJars(lib, tapestryJarNames);
		collectJars(ext, tapestryJarNames);

	}

	private void collectJars(File directory, Map result) {

		if (directory == null || !directory.exists()
				|| !directory.isDirectory())
			return;

		String[] jars = directory.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});

		for (int i = 0; i < jars.length; i++)
			result.put(jars[i], new File(directory, jars[i]));

	}

	public TapestryProjectInstallData getInstallData() {
		return installData;
	}

	public boolean projectHasTapestryFrameworkLibrary() {
		IJavaProject project = JavaCore.create(installData.getProject());
		if (project == null || !project.exists())
			return false;
		try {
			return hasTapestryFrameworkLibrary(project.getRawClasspath());

		} catch (CoreException e) {
			// do nothing

		}
		return false;
	}

	private boolean hasTapestryFrameworkLibrary(IClasspathEntry[] entries)
			throws CoreException {

		boolean hasTapestryEntry = false;
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				if (!hasTapestryEntry)
					hasTapestryEntry = entry.getPath().segment(0).equals(
							TapestryCorePlugin.CORE_CONTAINER);
			}
		}
		return hasTapestryEntry;
	}

	private IClasspathEntry[] addTapestryFrameworkLibrary(
			IClasspathEntry[] entries) throws CoreException {

		if (hasTapestryFrameworkLibrary(entries))
			return entries;

		List allEntries = new ArrayList(Arrays.asList(entries));

		allEntries.add(TapestryProjectInstallData.TAPESTRY_FRAMEWORK);

		return (IClasspathEntry[]) allEntries
				.toArray(new IClasspathEntry[allEntries.size()]);
	}
}