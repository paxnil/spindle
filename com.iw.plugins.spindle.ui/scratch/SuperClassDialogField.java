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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.core.util.SpindleStatus;

public class SuperClassDialogField extends TypeDialogField
{

    /**
     * Constructor for SuperClassDialogField.
     * @param name
     * @param hierarchyRoot
     * @param labelWidth
     */
    public SuperClassDialogField(String name, String hierarchyRoot, int labelWidth)
    {
        super(name, hierarchyRoot, labelWidth);
    }

    /**
     * Constructor for SuperClassDialogField.
     * @param name
     * @param hierarchyRoot
     */
    public SuperClassDialogField(String name, String hierarchyRoot)
    {
        super(name, hierarchyRoot);
    }

    /**
     * Constructor for SuperClassDialogField.
     * @param name
     * @param labelWidth
     */
    public SuperClassDialogField(String name, int labelWidth)
    {
        super(name, labelWidth);
    }

    /**
     * Constructor for NewClassDialogField
     */
    public SuperClassDialogField(String name)
    {
        super(name);
    }

    public void init(PackageDialogField packageChooser, IRunnableContext context)
    {
        super.init(packageChooser, context);
        setTextValue("java.lang.Object");
    }

    protected IStatus typeChanged()
    {
        SpindleStatus status = new SpindleStatus();
        IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
        //    enableButton(root != null);
        if (root == null)
        {
            return status;
        }
        String typeName = getTextValue();
        chosenType = null;
        try
        {
            IType type = resolveTypeName(root.getJavaProject(), typeName);
            if (type == null)
            {
                status.setWarning(UIPlugin.getString(name + ".warning.TypeNotExists", typeName));
                return status;
            }
            if (type.isInterface())
            {
                status.setWarning(UIPlugin.getString(name + ".warning.TypeIsNotClass", typeName));
                return status;
            }

            int flags = type.getFlags();
            if (Flags.isFinal(flags))
            {
                status.setWarning(UIPlugin.getString(name + ".warning.TypeIsFinal", typeName));
                return status;
            } else if (!CoreUtils.isVisible(type, packageChooser.getPackageFragment()))
            {
                status.setWarning(UIPlugin.getString(name + ".warning.TypeIsNotVisible", typeName));
                return status;
            }
            chosenType = type;
        } catch (JavaModelException e)
        {
            status.setError(UIPlugin.getString(name + ".error.InvalidTypeName"));
            UIPlugin.log(e);
        }
        return status;
    }
    /**
     * @see TypeDialogField#getType()
     */
    public IType getType()
    {

        String typeName = getTextValue();
        if (chosenType == null || (typeName == null && "".equals(typeName)))
        {
            IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
            try
            {
                chosenType = resolveTypeName(root.getJavaProject(), "java.lang.Object");
            } catch (JavaModelException e)
            {}
        }
        return super.getType();
    }

}