package com.iw.plugins.spindle.util.lookup;

import java.util.List;

import net.sf.tapestry.spec.ILibrarySpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public interface INamespaceFragment {
	
	public boolean isDefaultNamespace();
	
	public String getName();
	
	public ILibrarySpecification getSpecification();

}
