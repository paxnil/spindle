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
   * @see core.builder.NamespaceResolver#namespaceConfigured()
   */
  protected void namespaceConfigured()
  {
    //do nothing
  }

  /**
   * We need to resolve child libraries late, after the framework is resolved.
   * We do it here.
   * 
   * @see core.builder.NamespaceResolver#namespaceResolved()
   */
  protected void namespaceResolved()
  {
    frameworkNamespace = namespace;

    resolveChildNamespaces();

    super.namespaceResolved();
  }

}