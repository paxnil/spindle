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

import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.parser.Parser;

/**
 *  Full builder for Library projects
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 * @deprecated to be removed
 */
public class LibraryBuild extends FullBuild
{

    protected boolean fIsFrameworkLibrary;

    public LibraryBuild(TapestryBuilder builder)
    {
        super(builder);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.Build#doBuild()
     */
    protected void doBuild()
    {
        if (fIsFrameworkLibrary)
            //already done!
            return;

        System.out.println("do build called");
        //        fNSResolver.resolveLibrary(fFrameworkNamespace, null, somelocation);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.Build#preBuild(com.iw.plugins.spindle.core.parser.Parser)
     */
    protected void preBuild(Parser parser) throws CoreException
    {
        //not needed for library builds
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.builder.Build#resolveFramework()
     */
    protected void resolveFramework()
    {
        super.resolveFramework();
        //is the library in question the framework?      
        fIsFrameworkLibrary = true;

    }

}
