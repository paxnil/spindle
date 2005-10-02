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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.builder;


/**
 * Namespace reolver for the Tapestry framework and its contained libraries.
 * 
 * @author glongman@gmail.com
 * 
 */
public class FrameworkResolver extends NamespaceResolver
{

  /**
   * @param build
   * @param parser
   */
  public FrameworkResolver(AbstractBuild build)
  {
    super(build);
  }

  /**
   * We need to resolve child libraries late, after this framework is resolved.
   * So we override this method to do nothing - the superclass resolves child
   * libraries here.
   * 
   * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#namespaceConfigured()
   */
  protected void namespaceConfigured()
  {
    //do nothing
  }

  /**
   * We need to resolve child libraries late, after the framework is resolved.
   * We do it here.
   * 
   * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#namespaceResolved()
   */
  protected void namespaceResolved()
  {
    fFrameworkNamespace = fNamespace;

    resolveChildNamespaces();

    super.namespaceResolved();
  }

}