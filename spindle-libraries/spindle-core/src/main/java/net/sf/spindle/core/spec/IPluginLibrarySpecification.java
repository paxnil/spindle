package net.sf.spindle.core.spec;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.apache.tapestry.spec.ExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * @author gwl
 * 
 * Copyright 2002, Geoffrey Longman. All Rights Reserved.
 */
public interface IPluginLibrarySpecification
    extends
      PropertyChangeListener,
      ILibrarySpecification,
      IIdentifiable
{

  public void removePageSpecificationPath(String name);

  public void removeComponentSpecificationPath(String name);

  public void removeLibrarySpecificationPath(String name);

  public void removeExtensionSpecification(String name);

  public void setExtensionSpecification(String name, ExtensionSpecification extension);

  public boolean canDeleteService(String name);

  public boolean canRevertService(String name);

  public Set getAllExtensionNames();

}