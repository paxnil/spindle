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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class JavaClassnameField extends AbstractNameField
{

    /**
     * Constructor for JavaClassnameField.
     * @param fieldName
     * @param labelWidth
     */
    public JavaClassnameField(String fieldName, int labelWidth)
    {
        super(fieldName, labelWidth);
    }

    /**
     * Constructor for JavaClassnameField.
     * @param fieldName
     */
    public JavaClassnameField(String fieldName)
    {
        super(fieldName);
    }

    protected IStatus nameChanged()
    {
        SpindleStatus status = new SpindleStatus();
        String appname = getTextValue();
        if ("".equals(appname))
        {
            status.setError("");
            return status;
        }
        if (appname.indexOf('.') != -1)
        {
            status.setError(UIPlugin.getString(fName + ".error.QualifiedName"));
            return status;
        }

        IStatus val = JavaConventions.validateJavaTypeName(appname);
        if (!val.isOK())
        {
            if (val.getSeverity() == IStatus.ERROR)
            {
                status.setError(UIPlugin.getString(fName + ".error.InvalidClassName", val.getMessage()));
                return status;
            } else if (val.getSeverity() == IStatus.WARNING)
            {
                status.setWarning(UIPlugin.getString(fName + ".warning.ClassNameDiscouraged", val.getMessage()));
                return status;
            }
        }
        if (fPackageField != null && fPackageField.getPackageFragment() != null)
        {
            try
            {
                IContainer folder = (IContainer) fPackageField.getPackageFragment().getUnderlyingResource();
                IFile file = folder.getFile(new Path(appname + ".java"));
                if (file.exists())
                {
                    status.setError(UIPlugin.getString(fName + ".error.ClassAlreadyExists", appname));
                    return status;
                }
            } catch (JavaModelException e)
            {
                // do nothing
            }
        }
        char first = appname.charAt(0);
        if (Character.isLowerCase(first))
        {
            status.setWarning(
                UIPlugin.getString(fName + ".warning.ClassNameDiscouraged", "first character is lowercase"));
        }

        return status;

    }

}
