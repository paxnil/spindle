package com.iw.plugins.spindle.ui.wizards;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;




/**
 * 
 * THIS CLASS WILL NOT BUILD WITHOUT ERROR IN ECLIPSE 3.1
 * 
 * THIS IS EXPECTED BEHAVIOUR 
 * 
 * @author Administrator
 *
 */
public class TypeChoosePageHelper
{
    public static IMethod[] createInheritedMethodsEclipse30(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws JavaModelException
    {
        ArrayList newMethods = new ArrayList();
        ITypeHierarchy hierarchy = null;
        CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings();

        if (hierarchy == null)
        {
            hierarchy = type.newSupertypeHierarchy(monitor);
        }
        String[] unimplemented = StubUtility.evalUnimplementedMethods(
                type,
                hierarchy,
                false,
                settings,
                imports);
        if (unimplemented != null)
        {
            for (int i = 0; i < unimplemented.length; i++)
            {
                newMethods.add(unimplemented[i]);
            }
        }

        IMethod[] createdMethods = new IMethod[newMethods.size()];
        for (int i = 0; i < newMethods.size(); i++)
        {
            String content = (String) newMethods.get(i) + '\n'; // content will be formatted, OK to
            // use \n
            createdMethods[i] = type.createMethod(content, null, false, null);
        }
        return createdMethods;
    }
}
