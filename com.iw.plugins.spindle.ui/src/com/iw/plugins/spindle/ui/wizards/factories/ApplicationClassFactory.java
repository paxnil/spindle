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
package com.iw.plugins.spindle.ui.wizards.factories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.IImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.util.UIUtils;

public class ApplicationClassFactory
{

    static public IType createClass(
        IPackageFragmentRoot root,
        IPackageFragment pack,
        String classname,
        IType superClass,
        IMethodEvaluator methodEvaluator,
        IProgressMonitor monitor)
        throws CoreException, InterruptedException
    {

        IType createdType;
        ImportsStructure imports;
        int indent = 0;
        String superclassName = (superClass == null ? "java.lang.Object" : superClass.getElementName());

        monitor.beginTask(UIPlugin.getString("ClassFactory.operationdesc", classname), 10);
        if (pack == null)
        {
            pack = root.getPackageFragment("");
        }
        if (!pack.exists())
        {
            String packName = pack.getElementName();
            pack = root.createPackageFragment(packName, true, null);
        }

        monitor.worked(1);

        String lineDelimiter = null;
        ICompilationUnit parentCU = pack.getCompilationUnit(classname + ".java");
        imports = getImports(parentCU);
        lineDelimiter = StubUtility.getLineDelimiterUsed(parentCU);
        String content = createClassBody(classname, superClass, imports, lineDelimiter);
        createdType = parentCU.createType(content, null, false, new SubProgressMonitor(monitor, 5));
        // add imports for sclass, so the type can be parsed correctly
        if (imports != null)
        {
            imports.create(true, new SubProgressMonitor(monitor, 1));
        }
        createAllNewMethods(methodEvaluator, createdType, imports, new SubProgressMonitor(monitor, 1));

        monitor.worked(1);

        String originalContent = createdType.getSource();
        String formattedContent = null;

        try
        {
            formattedContent = StubUtility.codeFormat(originalContent, indent, lineDelimiter);
        } catch (NoSuchMethodError e)
        {
            formattedContent = originalContent;
            UIPlugin.log("See spindle bug 898181 - will go away when Spindle is ported to 3.0");
        }
        save(createdType, formattedContent, monitor);
        monitor.done();
        return createdType;
    }

    private static void save(IType createdType, String formattedContent, IProgressMonitor monitor)
        throws JavaModelException
    {
        ISourceRange range = createdType.getSourceRange();
        IBuffer buf = createdType.getCompilationUnit().getBuffer();
        buf.replace(range.getOffset(), range.getLength(), formattedContent);
        buf.save(new SubProgressMonitor(monitor, 1), false);
    }

    private static ImportsStructure getImports(ICompilationUnit parentCU)
    {
        ImportsStructure imports = null;
        String[] prefOrder = UIUtils.getImportOrderPreference();
        int threshold = UIUtils.getImportNumberThreshold();
        try
        {
            imports = new ImportsStructure(parentCU, prefOrder, threshold, false);
        } catch (CoreException correx)
        {}
        return imports;
    }

    private static void createAllNewMethods(
        IMethodEvaluator methodEvaluator,
        IType createdType,
        ImportsStructure imports,
        IProgressMonitor monitor)
        throws CoreException, JavaModelException
    {

        IMethod[] inherited = null;
        inherited = createMethods(getInheritedMethods(createdType, imports, monitor), createdType, imports);
        if (methodEvaluator != null)
        {
            methodEvaluator.newInheritedMethods(createdType, inherited);
            String[] emethods = methodEvaluator.methodsToCreate();
            if (emethods.length > 0)
            {
                methodEvaluator.createdMethods(createdType, createMethods(emethods, createdType, imports));
            }

        }
    }

    private static IMethod[] createMethods(String[] methods, IType createdType, ImportsStructure imports)
        throws CoreException, JavaModelException
    {

        IMethod[] newMethods = new IMethod[methods.length];
        if (methods.length > 0)
        {
            for (int i = 0; i < methods.length; i++)
            {
                newMethods[i] = createdType.createMethod(methods[i], null, false, null);
            }
            // add imports
            imports.create(true, null);
        }
        return newMethods;
    }

    private static String[] getInheritedMethods(IType createdType, ImportsStructure imports, IProgressMonitor monitor)
        throws CoreException
    {

        ITypeHierarchy hierarchy = createdType.newSupertypeHierarchy(monitor);
        CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings();

        try
        {
            // TODO fudge to allow things to work in M7                        
            return StubUtility.evalUnimplementedMethods(createdType, hierarchy, false, settings, null, imports);
        } catch (NoSuchMethodError e)
        {
            UIPlugin.log("See spindle bug 898181 - will go away when Spindle is ported to 3.0");
            return new String[] {};
        }
    }

    /*
     * Called from createType to construct the source for this type
     */
    private static String createClassBody(
        String classname,
        IType superclass,
        IImportsStructure imports,
        String lineDelimiter)
    {
        StringBuffer buf = new StringBuffer();

        buf.append("public class");
        buf.append(' ');
        buf.append(classname);
        writeSuperClass(superclass, buf, imports);
        buf.append(" {");
        buf.append(lineDelimiter);
        buf.append(lineDelimiter);
        buf.append('}');
        buf.append(lineDelimiter);
        return buf.toString();
    }

    static private void writeSuperClass(IType superclass, StringBuffer buf, IImportsStructure imports)
    {
        if (superclass == null)
        {
            return;
        }
        String typename = superclass.getElementName();
        if (!"".equals(typename) && !"java.lang.Object".equals(typename))
        {
            buf.append(" extends ");
            buf.append(Signature.getSimpleName(typename));
            if (!"java.lang.Object".equals(typename))
            {
                imports.addImport(superclass.getFullyQualifiedName());
            } else
            {
                imports.addImport(typename);
            }
        }
    }

}