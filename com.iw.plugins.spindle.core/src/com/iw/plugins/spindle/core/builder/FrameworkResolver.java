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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.builder;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 *  Namespace reolver for the Tapestry framework and its contained libraries.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class FrameworkResolver extends NamespaceResolver
{
    private IResourceWorkspaceLocation fFrameworkLocation;

    /**
     * @param build
     * @param parser
     */
    public FrameworkResolver(Build build, Parser parser, IResourceWorkspaceLocation location)
    {
        super(build, parser);
        fFrameworkLocation = location;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#doResolve()
     */
    public ICoreNamespace resolve()
    {
        reset();
        resolve(ICoreNamespace.FRAMEWORK_NAMESPACE, fFrameworkLocation);
        return fResultNamespace;
    }

    /**
     *  We need to resolve child libraries late, after this framework is resolved.
     *  So we override this method to do nothing - the superclass resolves child libraries here.
     * 
     * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#namespaceConfigured()
     */
    protected void namespaceConfigured()
    {
        //do nothing
    }

    /**
     *  We need to resolve child libraries late, after the framework is resolved.
     *  We do it here.
     * 
     * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#namespaceResolved()
     */
    protected void namespaceResolved()
    {
        fFrameworkNamespace = fResultNamespace;

        resolveChildNamespaces();

        super.namespaceResolved();
    }

}
