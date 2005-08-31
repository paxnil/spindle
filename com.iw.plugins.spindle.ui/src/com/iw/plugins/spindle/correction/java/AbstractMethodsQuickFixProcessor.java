package com.iw.plugins.spindle.correction.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

public class AbstractMethodsQuickFixProcessor implements IQuickFixProcessor
{

    public boolean hasCorrections(ICompilationUnit unit, int problemId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
            IProblemLocation[] locations) throws CoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
