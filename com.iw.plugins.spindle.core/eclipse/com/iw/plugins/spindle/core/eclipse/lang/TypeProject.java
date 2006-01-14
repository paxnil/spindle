package com.iw.plugins.spindle.core.eclipse.lang;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeHierarchy;

import core.types.TypeModelException;

public class TypeProject extends TypeElement
{

    

    public TypeProject(IJavaElement element)
    {
        super(element);       
    }

    public int getTypeElementType()
    {
        return PROJECT;
    }

    protected void closing(TypeElementInfo info) throws TypeModelException
    {
        // TODO Auto-generated method stub
        
    }

    protected void generateInfos(TypeElementInfo info, HashMap newElements, ITypeHierarchy hierarchy, IProgressMonitor pm) throws TypeModelException
    {
        // TODO Auto-generated method stub
        
    }

    protected TypeElementInfo createElementInfo()
    {
        // TODO Auto-generated method stub
        return null;
    }

    

}
