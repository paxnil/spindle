/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */



package com.iw.plugins.spindle.util.lookup;

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
