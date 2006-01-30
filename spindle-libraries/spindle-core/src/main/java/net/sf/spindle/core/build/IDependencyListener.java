package net.sf.spindle.core.build;
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
import org.apache.hivemind.Resource;


/**
 * Various components in the build will fire simple events when the Tapestry
 * artifacts they are working on have dependencies on other things.
 * <p>
 * What's tracked:
 * <ul>
 * <li>Java type dependencies</li>
 * <li>Workspace resource dependencies</li>
 * </ul>
 * <p>
 * These are raw dependencies, the type or resource dependency may not even
 * exist.
 * 
 * Implementers must decide what to with dependancies for binary artifacts. I
 * think that in most cases these dependencies should be ignored as binary
 * artifacts never change.
 * 
 * 
 * @author glongman@gmail.com
 * 
 */
public interface IDependencyListener
{
  /**
   * Notification that a dependant has a type dependency.
   * 
   * @param dependant the location of the dependant.
   * @param fullyQualifiedTypeName the fully qualified type name.
   */
  void foundTypeDependency(
      Resource dependant,
      String fullyQualifiedTypeName);
  /**
   * Notification that a dependant has a dependency on another resource.
   * 
   * @param dependant the location of the dependant.
   * @param dependancy the location of the dependancy.
   */
  void foundResourceDependency(
      Resource dependant,
      Resource dependancy);
}