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

package com.iw.plugins.spindle.editors.spec.actions;

import org.eclipse.jface.viewers.StructuredSelection;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.util.Revealer;

/**
 *  Show something interesting in the PackageExplorer
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ShowInPackageExplorerAction extends OpenDeclarationAction
{
    public static final String ACTION_ID = UIPlugin.PLUGIN_ID + ".spec.showInPackageExplorer";

    public ShowInPackageExplorerAction()
    {
        super();
        //      TODO I10N
        setText("Show In Package Explorer");
        setId(ACTION_ID);
    }

    protected void foundResult(Object result, Object moreInfo)
    {
        Revealer.selectAndReveal(new StructuredSelection(result), UIPlugin.getDefault().getActiveWorkbenchWindow());
    }

}
