package com.iw.plugins.spindle.dialogfields;

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

	private Button buttonControl;
	
	protected IRunnableContext context;

	public StringButtonField(String label) {
		super(label);
	}
	
	public void init(IRunnableContext context) {
	  this.context = context;
	}
	
	protected IRunnableContext getRunnableContext() {
	  return (context == null ? new ProgressMonitorDialog(getShell()) : context);
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
		if (buttonControl == null) {

			buttonControl = new Button(parent, SWT.PUSH);
			buttonControl.setFont(parent.getFont());

			final DialogField field = this;
			buttonControl.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					fireButtonPressed();
				}

				public void widgetSelected(SelectionEvent e) {
					fireButtonPressed();
				}
			});

		}
		return buttonControl;
	}

	private void fireButtonPressed() {
		for (Iterator iterator = getListeners().iterator(); iterator.hasNext();) {
			IDialogFieldChangedListener element =
				(IDialogFieldChangedListener) iterator.next();
			element.dialogFieldButtonPressed(this);
		}
	}

	public void setEnabled(boolean flag) {
		if (buttonControl != null && !buttonControl.isDisposed()) {
			buttonControl.setEnabled(flag);
		}
		super.setEnabled(flag);
	}

	public void setButtonLabel(String value) {
		if (buttonControl != null && !buttonControl.isDisposed()) {
			buttonControl.setText(value);
		}
	}

	public void enableButton(boolean flag) {
		if (buttonControl != null && !buttonControl.isDisposed()) {
			buttonControl.setEnabled(flag);
		}
	}

}