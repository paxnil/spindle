package com.iw.plugins.spindle.spec;

import java.util.Collection;
import java.util.List;

import net.sf.tapestry.spec.ParameterSpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface IParameterHolder {
	
	public ParameterSpecification getParameter(String name);
	public void setParameter(String name, PluginParameterSpecification spec);
	public void addParameter(String name, ParameterSpecification spec);
	public List getParameterNames();
	public void removeParameter(String name);

}
