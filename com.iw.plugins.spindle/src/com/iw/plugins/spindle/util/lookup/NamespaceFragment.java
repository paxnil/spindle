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
public class NamespaceFragment implements INamespaceFragment {
	
  private String name;
  
  private ILibrarySpecification specification;

  /**
   * Constructor for NamespaceFragment.
   */
  public NamespaceFragment() {
    super();
  }
  
  public NamespaceFragment(String name, ILibrarySpecification specification) {
  	
  	this.name = name;
  	this.specification = specification;
  	
  }
  
  public boolean isDefaultNamespace() {
  	return false;
  }

  /**
   * Returns the name.
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the specification.
   * @return ILibrarySpecification
   */
  public ILibrarySpecification getSpecification() {
    return specification;
  }

  /**
   * Sets the name.
   * @param name The name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the specification.
   * @param specification The specification to set
   */
  public void setSpecification(ILibrarySpecification specification) {
    this.specification = specification;
  }



}
