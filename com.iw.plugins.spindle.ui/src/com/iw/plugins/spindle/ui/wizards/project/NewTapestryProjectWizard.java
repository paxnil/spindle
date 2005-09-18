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

package com.iw.plugins.spindle.ui.wizards.project;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;
import com.iw.plugins.spindle.ui.wizards.NewTapestryElementWizard;
import com.iw.plugins.spindle.ui.wizards.factories.ApplicationFactory;
import com.iw.plugins.spindle.ui.wizards.factories.PageFactory;
import com.iw.plugins.spindle.ui.wizards.factories.TapestryTemplateFactory;

/**
 * Wizard for creating new Tapestry projects.
 * 
 * @author glongman@gmail.com
 */
public class NewTapestryProjectWizard extends NewTapestryElementWizard {
	private NewTapestryProjectPage fMainPage;

	private NewTapestryProjectJavaPage fJavaPage;

	private NewTapestryProjectTemplateSelectionWizardPage fTemplatePage;

	private IWizardPage fEntering = null;

	private boolean beenThere = false;

	private TapestryProjectInstallData fInstallData;

	public NewTapestryProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(UIPlugin.getString("new-project-wizard-window-title"));
		fInstallData = new TapestryProjectInstallData();
		fInstallData.setWritingMetaData(true);
	}

	public void entering(IWizardPage page) {
		fEntering = page;
	}

	public void leaving(IWizardPage page) {
		if (page == fMainPage && fEntering == fJavaPage) {
			fJavaPage.changeToNewProject();
		} else if (page == fJavaPage && fEntering == fMainPage) {
			fJavaPage.removeProject();
		}
	}

	/**
	 * @see Wizard#createPages
	 */
	public void addPages() {

		ImageDescriptor descriptor = Images
				.getImageDescriptor("applicationDialog.gif");

		fTemplatePage = new NewTapestryProjectTemplateSelectionWizardPage(
				"FileGeneration");

		fMainPage = new NewTapestryProjectPage(UIPlugin
				.getString("new-project-wizard-page-title"), this, fInstallData);

		fMainPage.setImageDescriptor(descriptor);
		fMainPage.setDescription(UIPlugin
				.getString("new-project-wizard-page-title"));

		addPage(fMainPage);

		fJavaPage = new NewTapestryProjectJavaPage(fMainPage, fInstallData);
		fJavaPage.setImageDescriptor(descriptor);
		addPage(fJavaPage);

		// bug [ 843021 ] Is this what 3 Beta is supposed to do
		// for some reason sometimes the page's wizard is not set
		//
		// should have been set by addPagr()
		fJavaPage.setWizard(this);
		addPage(fTemplatePage);

	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		if (finishPage(fJavaPage.getRunnable())) {
			IJavaProject jproject = fJavaPage.getJavaProject();
			fInstallData.setProject(jproject.getProject());
			fInstallData.setApplicationFactory(new ApplicationFactory());
			fInstallData.setPageFactory(new PageFactory());
			fInstallData.setTemplateFactory(new TapestryTemplateFactory());
			fInstallData.setTemplateSource(fTemplatePage);

			IRunnableWithProgress operation = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					ArrayList created = new ArrayList();

					IStatus status = new SpindleStatus();
					monitor.beginTask("", 100);

					TapestryProjectInstaller installer = new TapestryProjectInstaller(
							fInstallData);

					status = installer.configureTapestryProject(created,
							new SubProgressMonitor(monitor, 50));

					if (!status.isOK()) {
						ErrorDialog.openError(null,
								"Tapestry Install Problems",
								"Unable to create all Tapestry files.", status);
						return;
					}

					status = installer
							.addTapestryNature(new SubProgressMonitor(monitor,
									50));

					if (!status.isOK()) {
						ErrorDialog.openError(null,
								"Tapestry Install Problems",
								"Unable to install the Tapestry nature.",
								status);

						return;
					}

					for (Iterator iter = created.iterator(); iter.hasNext();) {
						IResource element = (IResource) iter.next();
						selectAndReveal(element);
					}

				}
			};
			finishPage(operation);
			finishPage(fTemplatePage.getRunnable(fInstallData.getProject()));

		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IWizard#performCancel()
	 */
	public boolean performCancel() {
		fJavaPage.performCancel();
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 */
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == fMainPage)
			return false;
		return super.canFinish() && fTemplatePage.isPageComplete();
	}

}