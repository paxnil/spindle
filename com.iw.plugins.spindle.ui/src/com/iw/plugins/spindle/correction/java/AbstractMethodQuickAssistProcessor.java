package com.iw.plugins.spindle.correction.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

public class AbstractMethodQuickAssistProcessor implements IQuickAssistProcessor
{
    public boolean hasAssists(IInvocationContext context) throws CoreException
    {
        // TODO Auto-generated method stub
        return true;
    }

    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
