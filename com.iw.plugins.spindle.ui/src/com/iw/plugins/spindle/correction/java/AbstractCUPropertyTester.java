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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.correction.java;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class AbstractCUPropertyTester extends PropertyTester
{

    private static final String PROPERTY_IS_ABSTRACT = "isAbstract"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String,
     *      java.lang.Object[], java.lang.Object)
     */
public boolean test(Object receiver, String method, Object[] args, Object expectedValue)
    {
        ICompilationUnit cu = null;
        if (receiver instanceof IAdaptable)
            cu = (ICompilationUnit) ((IAdaptable) receiver).getAdapter(ICompilationUnit.class);

        if (cu == null || !cu.exists())
            return false;                

        IType type = cu.getType(Signature.getQualifier(cu.getElementName()));

        if (type != null)
        {
            if (PROPERTY_IS_ABSTRACT.equals(method))
            {
                try
                {
                    return Flags.isAbstract(type.getFlags());
                }
                catch (JavaModelException e)
                {
                }               
            }
        }
        return false;
    }}
