package com.iw.plugins.spindle.ui.propertysheet;

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     glongman@intelligentworks.com - spindle customization
 *******************************************************************************/

/**
 * This action resets the <code>PropertySheetViewer</code> values back
 * to the default values.
 *
 * [Issue: should listen for selection changes in the viewer and set enablement]
 */
/*package*/ class DefaultsAction extends PropertySheetAction {
/**
 * Create the Defaults action. This action is used to set
 * the properties back to their default values.
 */
public DefaultsAction(PropertySheetViewer viewer, String name) {
	super(viewer, name);
	setToolTipText("Restore Default Value");//PropertiesMessages.getString("DefaultAction.toolTip")); //$NON-NLS-1$
//	WorkbenchHelp.setHelp(this, IPropertiesHelpContextIds.DEFAULTS_ACTION);
}
/**
 * Reset the properties to their default values.
 */
public void run() {
	getPropertySheet().deactivateCellEditor();
	getPropertySheet().resetProperties();
}
}
