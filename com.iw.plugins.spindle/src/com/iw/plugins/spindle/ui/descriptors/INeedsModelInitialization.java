package com.iw.plugins.spindle.ui.descriptors;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.iw.plugins.spindle.model.ITapestryModel;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface INeedsModelInitialization extends IPropertyDescriptor {
	
	public void initialize(ITapestryModel model);

}
