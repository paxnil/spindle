package com.iw.plugins.spindle.dialogfields;

import org.eclipse.core.runtime.IStatus;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public interface IDialogFieldChangedListener {
	
	public void dialogFieldChanged(DialogField field);
	
	public void dialogFieldButtonPressed(DialogField field);
	
	public void dialogFieldStatusChanged(IStatus status, DialogField field);

}
