package com.iw.plugins.spindle.dialogfields;

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

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class RadioDialogField extends DialogField {

	private Button[] radioButtons;
	private String[] radioLabels;
	private int orientation = SWT.HORIZONTAL;

	public RadioDialogField(String label, String[] radioLabels, int orientation) {
		this(label, -1, radioLabels, orientation);
	}

	public RadioDialogField(
		String label,
		int labelWidth,
		String[] radioLabels,
		int orientation) {
		super(label, labelWidth);
		this.radioLabels = radioLabels;
		this.orientation = orientation;
	}

	public Control getControl(Composite parent) {
		checkOrientation();
		Composite container = new Composite(parent, SWT.NULL);

		FormLayout layout = new FormLayout();
		container.setLayout(layout);

		FormData formData;

		if (orientation == SWT.HORIZONTAL) {

			Label labelControl = getLabelControl(container);
			Control[] radioControls = getRadioButtonControls(container);

			formData = new FormData();
			formData.width = getLabelWidth();
			formData.top = new FormAttachment(0, 5);
			formData.left = new FormAttachment(0, 0);

			labelControl.setLayoutData(formData);

			formData = new FormData();
			formData.top = new FormAttachment(0, 5);
			formData.left = new FormAttachment(labelControl, 0);
			radioControls[0].setLayoutData(formData);

			for (int i = 1; i < radioControls.length; i++) {
				formData = new FormData();
				formData.top = new FormAttachment(0, 5);
				formData.left = new FormAttachment(radioControls[i - 1], 8);
				radioControls[i].setLayoutData(formData);
			}

		} else {

			Composite labelComp = new Composite(container, SWT.NULL);
			formData = new FormData();
			formData.width = getLabelWidth();
			formData.top = new FormAttachment(0, 0);
			formData.left = new FormAttachment(0, 0);
			formData.bottom = new FormAttachment(100, 0);
			labelComp.setLayoutData(formData);

			layout = new FormLayout();
			labelComp.setLayout(layout);

			Label labelControl = getLabelControl(labelComp);

			formData = new FormData();
			formData.width = getLabelWidth();
			formData.top = new FormAttachment(0, 5);
			formData.left = new FormAttachment(0, 0);
			labelControl.setLayoutData(formData);

			Composite radioComp = new Composite(container, SWT.NULL);
			formData = new FormData();

			formData.top = new FormAttachment(0, 0);
			formData.left = new FormAttachment(labelComp, 0);
			formData.right = new FormAttachment(100, 0);
			formData.bottom = new FormAttachment(100, 0);
			radioComp.setLayoutData(formData);

			layout = new FormLayout();
			radioComp.setLayout(layout);

			Control[] radioControls = getRadioButtonControls(radioComp);
			formData = new FormData();
			formData.top = new FormAttachment(0, 4);
			formData.left = new FormAttachment(0, 0);
			formData.right = new FormAttachment(100, 0);
			radioControls[0].setLayoutData(formData);
			for (int i = 1; i < radioControls.length; i++) {
				formData = new FormData();
				formData.top = new FormAttachment(radioControls[i - 1], 4);
				formData.left = new FormAttachment(0, 0);
				radioControls[i].setLayoutData(formData);
			}
		}

		return container;
	}

	public Control[] getRadioButtonControls(Composite parent) {
		if (radioButtons == null) {
			radioButtons = new Button[radioLabels.length];
			final DialogField field = this;
			for (int i = 0; i < radioLabels.length; i++) {
				radioButtons[i] = new Button(parent, SWT.RADIO);
				radioButtons[i].setText(radioLabels[i]);
				radioButtons[i].setData(new Integer(i));
				radioButtons[i].addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {					  	
						fireDialogChanged(field);
					}

					public void widgetSelected(SelectionEvent e) {
					    
						fireDialogChanged(field);
					}

				});
			}
			return radioButtons;
		}

		return null;
	}

	private void checkOrientation() {
		if (orientation != SWT.HORIZONTAL && orientation != SWT.VERTICAL) {
			orientation = SWT.HORIZONTAL;
		}
	}

	public void setSelected(int index) {
		if (radioButtons[0] != null && !radioButtons[0].isDisposed()) {
			radioButtons[index].setSelection(true);
		}
	}

	public int getSelectedIndex() {
		if (radioButtons[0] != null && !radioButtons[0].isDisposed()) {
			for (int i = 0; i < radioButtons.length; i++) {
				if (radioButtons[i].getSelection()) {
					return i;
				}
			}
		} 
		return -1;
	}

	public void clearSelection() {
		if (radioButtons[0] != null && !radioButtons[0].isDisposed()) {
			for (int i = 0; i < radioButtons.length; i++) {
				radioButtons[i].setSelection(false);
			}
		}

	}

}