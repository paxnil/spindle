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
package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.core.util.SpindleStatus;

public class SpecClassDialogField extends TypeDialogField
{

    /**
     * Constructor for EngineClassDialog
     */
    public SpecClassDialogField(String name)
    {
        super(name);
    }

    /**
     * Constructor for SpecClassDialogField.
     * @param name
     * @param hierarchyRoot
     * @param labelWidth
     */
    public SpecClassDialogField(String name, String hierarchyRoot, int labelWidth)
    {
        super(name, hierarchyRoot, labelWidth);
    }

    /**
     * Constructor for SpecClassDialogField.
     * @param name
     * @param labelWidth
     */
    public SpecClassDialogField(String name, int labelWidth)
    {
        super(name, labelWidth);
    }

    public SpecClassDialogField(String name, String hierarchyRoot)
    {
        super(name, hierarchyRoot);
    }

    public void init(PackageDialogField packageChooser, IRunnableContext context)
    {
        super.init(packageChooser, context);
        setTextValue(UIPlugin.getString("TapestryComponentSpec.defaultSpec"));
    }

    protected IStatus typeChanged()
    {
        SpindleStatus status = new SpindleStatus();

        String specClassname = getTextValue();
        IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
        chosenType = null;
        enableButton(root != null);
        if (root == null || "".equals(specClassname.trim()))
        {
            status.setError(UIPlugin.getString(name + ".error.EnterTypeName"));
            return status;
        }
        try
        {
            IType specType = resolveTypeName(root.getJavaProject(), specClassname);
            if (specType == null)
            {
                status.setError(UIPlugin.getString(name + ".warning.TypeNotExists", specClassname));
                return status;
            }
            if (specType.isInterface())
            {
                status.setError(UIPlugin.getString(name + ".error.ClassIsNotClass"));
                return status;
            }
            boolean isBinary = specType.isBinary();
            String specBaseTypeName = UIPlugin.getString("TapestryComponentSpec.specBaseClass");
            String specInterfaceName = UIPlugin.getString("TapestryComponentSpec.specInterface");
            if (specBaseTypeName == null || specInterfaceName == null)
            {
                throw new Error("tapestry component wizard resources missing. Unable to continue");
            }
            IType specBaseType = resolveTypeName(root.getJavaProject(), specBaseTypeName);
            if (specBaseType == null || !specBaseType.exists())
            {
                status.setError(UIPlugin.getString(name + ".warning.BaseTypeNotExists", specBaseTypeName));
                return status;
            }

            if (!CoreUtils.extendsType(specType, specBaseType))
            {
                if (!CoreUtils.implementsInterface(specType, specInterfaceName))
                {
                    status.setError(UIPlugin.getString(name + ".error.DoesNotExtendOrImplement"));
                    return status;
                }
            }
            chosenType = specType;
        } catch (JavaModelException e)
        {
            status.setError(name + ".error.couldn't.do.it");
        }
        return status;
    }

}