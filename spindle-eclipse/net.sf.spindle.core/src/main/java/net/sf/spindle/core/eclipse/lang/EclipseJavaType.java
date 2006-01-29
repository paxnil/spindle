package net.sf.spindle.core.eclipse.lang;

import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.types.TypeModelException;
import net.sf.spindle.core.util.Assert;
import net.sf.spindle.core.util.eclipse.EclipseUtils;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;


/**
 * @author gwl
 */
/**
 * @author GLONGMAN
 */
public class EclipseJavaType implements IJavaType
{

    private IType type;

    public EclipseJavaType(IType type)
    {
        Assert.isLegal(type != null);
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#exists() 
     */
    public boolean exists()
    {
        return type != null && type.exists();
    }

    public String getFullyQualifiedName() 
    {      
        return type.getFullyQualifiedName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#getUnderlier()
     */
    public Object getUnderlier()
    {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isBinary()
     */
    public boolean isBinary()
    {
        return type.isBinary();
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
            return type.isInterface();
        }
        catch (JavaModelException e)
        {
            // do nothing
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.types.IJavaType#isAnnotation()
     */
    public boolean isAnnotation() 
    {
        try
        {
            return type.isAnnotation();
        }
        catch (JavaModelException e)
        {
            // do nothing
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
            return EclipseUtils.extendsType((IType) candidate.getUnderlier(), type);
        }
        catch (JavaModelException e)
        {
            // do nothing
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
        if (this == arg0)
            return true;

        if (!(arg0 instanceof EclipseJavaType))
            return false;

        return type.equals(((EclipseJavaType) arg0).type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return type.hashCode();
    }
}
