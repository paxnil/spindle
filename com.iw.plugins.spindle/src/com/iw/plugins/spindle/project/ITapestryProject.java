package com.iw.plugins.spindle.project;

import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.util.lookup.TapestryLookup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface ITapestryProject {
	
	public TapestryApplicationModel getApplicationModel();
	
	public TapestryLookup getLookup();

}
