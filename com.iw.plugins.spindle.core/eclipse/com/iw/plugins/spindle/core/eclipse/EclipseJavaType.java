package com.iw.plugins.spindle.core.eclipse;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;


import com.iw.plugins.spindle.core.util.eclipse.EclipseUtils;

import core.IJavaType;
import core.TapestryCore;
import core.util.Assert;


/**
 * @author gwl
 */
public class EclipseJavaType implements IJavaType
{
    private IType fType;

    public EclipseJavaType(IType type)
    {
        fType = type;
        Assert.isNotNull(fType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#exists()
     */
    public boolean exists()
    {
        return fType.exists();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#getName()
     */
    public String getFullyQualifiedName()
    {
        return fType.getElementName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#getUnderlier()
     */
    public Object getUnderlier()
    {
        return fType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isBinary()
     */
    public boolean isBinary()
    {
        return fType.isBinary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isInterface()
     */
    public boolean isInterface()
    {
        try
        {
            return fType.isInterface();
        }
        catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isInterface()
     */
    public boolean isAnnotation()
    {
        try
        {
            return fType.isAnnotation();
        }
        catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isSuperTypeOf(core.IJavaType)
     */
    public boolean isSuperTypeOf(IJavaType candidate)
    {
        try
        {
            return EclipseUtils.extendsType((IType) candidate.getUnderlier(), fType);
        }
        catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0)
    {
        if (!(arg0 instanceof EclipseJavaType))
            return false;
        return fType.equals(((EclipseJavaType)arg0).fType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return fType.hashCode();
    }
}
