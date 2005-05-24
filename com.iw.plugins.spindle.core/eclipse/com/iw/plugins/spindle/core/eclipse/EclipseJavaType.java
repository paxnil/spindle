package com.iw.plugins.spindle.core.eclipse;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.core.IJavaType;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.eclipse.EclipseUtils;


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
     * @see com.iw.plugins.spindle.core.IJavaType#exists()
     */
    public boolean exists()
    {
        return fType.exists();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.IJavaType#getName()
     */
    public String getFullyQualifiedName()
    {
        return fType.getElementName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.IJavaType#getUnderlier()
     */
    public Object getUnderlier()
    {
        return fType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.IJavaType#isBinary()
     */
    public boolean isBinary()
    {
        return fType.isBinary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.IJavaType#isInterface()
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
     * @see com.iw.plugins.spindle.core.IJavaType#isInterface()
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
     * @see com.iw.plugins.spindle.core.IJavaType#isSuperTypeOf(com.iw.plugins.spindle.core.IJavaType)
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

        return fType.equals(arg0);
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
