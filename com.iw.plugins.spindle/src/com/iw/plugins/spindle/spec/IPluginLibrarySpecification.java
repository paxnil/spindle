package com.iw.plugins.spindle.spec;

import net.sf.tapestry.spec.ILibrarySpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface IPluginLibrarySpecification extends ILibrarySpecification {
	
	  public void removePageSpecificationPath(String name);
	  
      public void removeComponentSpecificationPath(String name);
      
      public void removeLibrarySpecificationPath(String name);
      
      public void removeExtensionSpecification(String name);
      
      public boolean canDeleteService(String name);
      
      public boolean canRevertService(String name);

}
