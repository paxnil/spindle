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
 package com.iw.plugins.spindle.ui.dialogfields;

import java.util.Iterator;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class StringButtonField extends StringField {

	private Button fButtonControl;
	
	protected IRunnableContext fContextcontext;

	public StringButtonField(String label) {
		super(label);
	}
	
	public void init(IRunnableContext context) {
	  this.fContextcontext = context;
	}
	
	protected IRunnableContext getRunnableContext() {
	  return (fContextcontext == null ? new ProgressMonitorDialog(getShell()) : fContextcontext);
	}

	public StringButtonField(String label, int labelWidth) {
		super(label, labelWidth);
	}

	public Control getControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);

		Label labelWidget = getLabelControl(container);
		Text textControl = getTextControl(container);
		Button buttonControl = getButtonControl(container);

		FormLayout layout = new FormLayout();
		container.setLayout(layout);

		FormData formData;

		formData = new FormData();
		formData.height = 20;
		formData.width = getLabelWidth();
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 0);
		//formData.right = new FormAttachment(text, SWT.CENTER);
		labelWidget.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 3);
		formData.left = new FormAttachment(labelWidget, 4);
		formData.right = new FormAttachment(buttonControl, -4);
		textControl.setLayoutData(formData);

		buttonControl.setText("Browse...");
		formData = new FormData();
		formData.width = 75;
		formData.height = 25;
		//formData.top = new FormAttachment(0, 60);
		formData.right = new FormAttachment(100, 0);
		buttonControl.setLayoutData(formData);
		return container;

	}

	public Button getButtonControl(Composite parent) {
		if (fButtonControl == null) {

			fButtonControl = new Button(parent, SWT.PUSH);
			fButtonControl.setFont(parent.getFont());

			final DialogField field = this;
			fButtonControl.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					fireButtonPressed();
				}

				public void widgetSelected(SelectionEvent e) {
					fireButtonPressed();
				}
			});

		}
		return fButtonControl;
	}

	private void fireButtonPressed() {
		for (Iterator iterator = getListeners().iterator(); iterator.hasNext();) {
			IDialogFieldChangedListener element =
				(IDialogFieldChangedListener) iterator.next();
			element.dialogFieldButtonPressed(this);
		}
	}

	public void setEnabled(boolean flag) {
		if (fButtonControl != null && !fButtonControl.isDisposed()) {
			fButtonControl.setEnabled(flag);
		}
		super.setEnabled(flag);
	}

	public void setButtonLabel(String value) {
		if (fButtonControl != null && !fButtonControl.isDisposed()) {
			fButtonControl.setText(value);
		}
	}

	public void enableButton(boolean flag) {
		if (fButtonControl != null && !fButtonControl.isDisposed()) {
			fButtonControl.setEnabled(flag);
		}
	}

}