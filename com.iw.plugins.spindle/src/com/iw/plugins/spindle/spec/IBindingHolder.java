package com.iw.plugins.spindle.spec;

import java.util.Collection;

import net.sf.tapestry.spec.BindingSpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface IBindingHolder {
	
  public Collection getBindingNames();

  public BindingSpecification getBinding(String name);

  public void setBinding(String name, BindingSpecification binding);

  public void removeBinding(String name);	

}
