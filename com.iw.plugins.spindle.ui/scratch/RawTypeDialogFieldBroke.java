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
package com.iw.plugins.spindle.ui.wizards.fields;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.StringButtonField;
import com.iw.plugins.spindle.ui.widgets.TypeSearchDialog;

public class RawTypeDialogFieldBroke extends StringButtonField
{

    protected String fName;
    protected IType fChosenType;
    private String fHierarchyRoot;
    private IJavaProject fJavaProject;
    private IType fRequiredType;

    public RawTypeDialogFieldBroke(String name, String hierarchyRoot, int labelWidth)
    {
        super(UIPlugin.getString(name + ".label"), labelWidth);
        this.fName = name;
        this.fHierarchyRoot = hierarchyRoot;
    }

    public RawTypeDialogFieldBroke(String name, String hierarchyRoot)
    {
        this(name, hierarchyRoot, -1);
    }

    /**
     * Constructor for TypeDialogField
     */
    public RawTypeDialogFieldBroke(String name)
    {
        this(name, null, -1);

    }

    public RawTypeDialogFieldBroke(String name, int labelWidth)
    {
        this(name, null, labelWidth);
    }

    public void init(IJavaProject jproject, IRunnableContext context)
    {
        super.init(context);
        this.fJavaProject = jproject;
        try
        {
            fRequiredType = resolveTypeName(fHierarchyRoot);
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }
    }

    /**
     * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
     */
    public void dialogFieldButtonPressed(DialogField field)
    {

        if (field != this)
        {
            return;
        }
        IType type = chooseType();
        if (type != null)
        {
            String old = getTextValue();
            setTextValue(type.getFullyQualifiedName());

        }
    }

    public void dialogFieldChanged(DialogField field)
    {
        if (field == this)
        {
            setStatus(typeChanged());
        }
    }

    protected IStatus typeChanged()
    {
        //IPackageFragmentRoot root = packageChooser.getContainer().getPackageFragmentRoot();
        fChosenType = null;
        SpindleStatus status = new SpindleStatus();
        String typeName = getTextValue();
        if ("".equals(typeName))
        {
            status.setError(UIPlugin.getString(fName + ".error.EnterTypeName"));
            return status;
        }
        IStatus val = JavaConventions.validateJavaTypeName(typeName);
        if (!val.isOK())
        {
            if (val.getSeverity() == IStatus.ERROR)
            {
                status.setError(UIPlugin.getString(fName + ".error.InvalidTypeName", val.getMessage()));
                return status;
            } else if (val.getSeverity() == IStatus.WARNING)
            {
                status.setWarning(UIPlugin.getString(fName + ".warning.TypeNameDiscouraged", val.getMessage()));
            }
        }
        try
        {
            fChosenType = resolveTypeName(typeName);
            if (fChosenType == null)
            {
                status.setError(UIPlugin.getString(fName + ".error.TypeNameNotExist"));
                return status;
            }
            if (fRequiredType != null)
            {
                if (fRequiredType.isInterface())
                {
                    if (!CoreUtils.implementsInterface(fChosenType, fHierarchyRoot))
                    {
                        status.setError(UIPlugin.getString(fName + ".error.MustImplementInterface", fHierarchyRoot));
                    } else if (!extendsType(fChosenType, fRequiredType))
                    {
                        status.setError(UIPlugin.getString(fName + ".error.MustExtendClass", fHierarchyRoot));
                    }
                }

            }
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }

        return status;
    }

    private IType chooseType()
    {
        String message = UIPlugin.getString(fName + ".TypeDialog.message");
        TypeSearchDialog dialog =
            new TypeSearchDialog(
                getShell(),
                getRunnableContext(),
                fJavaProject,
                fRequiredType,
                UIPlugin.getString(fName + ".TypeDialog.title"),
                fHierarchyRoot == null ? message : message + " (extends/implements " + fHierarchyRoot + ")");

        if (dialog.open() == dialog.OK)
        {
            return dialog.getResultType();
        }

        return null;
    }

    protected IType resolveTypeName(String typeName) throws JavaModelException
    {
        return fJavaProject.findType(typeName);
    }

    public IType getType()
    {
        return fChosenType;
    }

    protected boolean extendsType(IType candidate, IType baseType) throws JavaModelException
    {
        boolean match = false;
        ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
        if (hierarchy.exists())
        {
            IType[] superClasses = hierarchy.getAllSupertypes(candidate);
            for (int i = 0; i < superClasses.length; i++)
            {
                if (superClasses[i].equals(baseType))
                {
                    match = true;
                }
            }
        }
        return match;
    }

    

}