package com.iw.plugins.spindle.ui.wizards.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.templates.Template;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.ui.wizards.factories.ApplicationFactory;
import com.iw.plugins.spindle.ui.wizards.factories.IFactoryTemplateSource;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TapestryTemplateFactory;

/**
 * @author geoff
 */
public class TapestryProjectInstallData {

	static public final IClasspathEntry TAPESTRY_FRAMEWORK = JavaCore
			.newContainerEntry(new Path(TapestryCore.CORE_CONTAINER), false);

	private IProject project;

	private String applicationName;

	private String contextPath;

	private int servletSpecPublicId;

	private boolean writeRedirectFilter;

	private ApplicationFactory applicationFactory;

	private PageFactory pageFactory;

	private TapestryTemplateFactory templateFactory;

	private IFactoryTemplateSource templateSource;

	private Template applicationFileTemplate;

	private boolean writingMetaData;

	public TapestryProjectInstallData() {
		this(null);
	}

	public TapestryProjectInstallData(IProject project) {
		super();
		this.project = project;
	}

	public ApplicationFactory getApplicationFactory() {
		return applicationFactory;
	}

	public void setApplicationFactory(ApplicationFactory applicationFactory) {
		this.applicationFactory = applicationFactory;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public PageFactory getPageFactory() {
		return pageFactory;
	}

	public void setPageFactory(PageFactory pageFactory) {
		this.pageFactory = pageFactory;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public int getServletSpecPublicId() {
		return servletSpecPublicId;
	}

	public void setServletSpecPublicId(int servletSpecPublicId) {
		this.servletSpecPublicId = servletSpecPublicId;
	}

	public TapestryTemplateFactory getTemplateFactory() {
		return templateFactory;
	}

	public void setTemplateFactory(TapestryTemplateFactory templateFactory) {
		this.templateFactory = templateFactory;
	}

	public IFactoryTemplateSource getTemplateSource() {
		return templateSource;
	}

	public void setTemplateSource(IFactoryTemplateSource templateSource) {
		this.templateSource = templateSource;
	}

	public boolean isWriteRedirectFilter() {
		return writeRedirectFilter;
	}

	public void setWriteRedirectFilter(boolean writeRedirectFilter) {
		this.writeRedirectFilter = writeRedirectFilter;
	}

	public Template getApplicationFileTemplate() {
		return applicationFileTemplate;
	}

	public void setApplicationFileTemplate(Template applicationFileTemplate) {
		this.applicationFileTemplate = applicationFileTemplate;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public boolean isWritingMetaData() {
		return writingMetaData;
	}

	public void setWritingMetaData(boolean writingMetaData) {
		this.writingMetaData = writingMetaData;
	}
}