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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.extensions;

import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;


/**
 * Interface for resolving a Tapestry page/component's type.
 * <p>
 * Spindle assumes that pages and components have a Java type. However, other
 * languages like Groovy may also be used.
 * <p>
 * Plugins may contribute implementations of this interface using the:
 * <ul>
 * <li>com.iw.plugins.spindle.core.componentTypeResolver - extension point</li>
 * </ul>
 * <p>
 * How it works.
 * <p>
 * One instance of each contributed resolver is created. When appropriate, each
 * resolver in turn is asked:
 * <ul>
 * <li><code>canResolve(IType type)</code> the first one that returns true
 * wins
 * </ul>
 * <p>
 * Then <code>doResolve()</code> is called. The instance should return a
 * status of OK if the resolution was successful. Spindle will not proceed to
 * the next step unless the returned status's <code>isOK()</code> returns true
 * If not OK, Spindle will use the message in the status to inform the user.
 * <p>
 * Once an OK status is recieved, <code>getStorage()</code> is called to
 * retrieve the resolved object.
 * <p>
 * The order that contributed extensions are invoked is fixed but is arbitrary
 * in that the order that extensions are added to the extension point is not
 * determinable before hand..
 * 
 * @author glongman@gmail.com
 *  
 */
public interface IComponentTypeResourceResolver
{

  /**
   * Give the contribution a chance to check and see if it is capable of
   * resolving based on the type.
   * <p>
   * Called first
   * 
   * @param type IType the type found in the component spec xml.
   * @return true if this instance can proceed to resolve, false otherwise.
   */
  boolean canResolve(IType type);

  /**
   * resolve the component type's IStorage object and return a status indicating
   * sucesss or failure
   * <p>
   * Called second
   * @param specificationLocation the location of the specification we are trying
   *                     to resolve a type for
   * @param componentSpec the parsed IComponentSpecification object found at the
   *                     above location (may be null for various reasons. one reason would
   *                     be if the spec xml is not well formed).
   * 
   * @return an instance of IStatus. <code>getStorage()</code> will only be
   *                 called if the status returned is OK.
   */
  IStatus doResolve(
      IResourceWorkspaceLocation specificationLocation,
      IComponentSpecification componentSpec);

  /**
   * Called only if <code>doResolve()</code> returned an OK status.
   * <p>
   * called last
   * 
   * @return the component type's resolve storage object. Must not be null!
   */
  IStorage getStorage();

}