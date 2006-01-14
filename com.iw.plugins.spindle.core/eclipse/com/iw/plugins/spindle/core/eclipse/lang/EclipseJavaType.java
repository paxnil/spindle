package com.iw.plugins.spindle.core.eclipse.lang;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import com.iw.plugins.spindle.core.util.eclipse.EclipseUtils;

import core.types.IJavaType;
import core.types.TypeModelException;

/**
 * @author gwl
 */
/**
 * @author GLONGMAN
 */
public class EclipseJavaType extends TypeElement implements IJavaType
{

    public EclipseJavaType(IType type)
    {
        super(type);
    }

    protected IType getType()
    {
        return (IType) element;
    }

    public int getTypeElementType()
    {
        return TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#exists()
     */
    public boolean exists()
    {
        return getType().exists();
    }

    public IJavaType getSuperClass() throws TypeModelException
    {
        EclipseJavaTypeInfo info = (EclipseJavaTypeInfo) getInfo();
        return (IJavaType) info.getParent();
    }

    public IJavaType[] getInterfaces() throws TypeModelException
    {
        EclipseJavaTypeInfo info = (EclipseJavaTypeInfo) getInfo();
        return info.getDeclaredInterfaces();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#getName()
     */
    public String getFullyQualifiedName()
    {
        return getType().getFullyQualifiedName();
    }

    public String getSimpleName()
    {
        return getType().getElementName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#getUnderlier()
     */
    public Object getUnderlier()
    {
        return getType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isBinary()
     */
    public boolean isBinary()
    {
        return getType().isBinary();
    }

    public boolean isArray()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public IJavaType getArrayType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.types.IJavaType#getModifiers()
     */
    public int getModifiers() throws TypeModelException
    {
        try
        {
            return getType().getFlags();
        }
        catch (JavaModelException e)
        {
            throw new TypeModelException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isInterface()
     */
    public boolean isInterface() throws TypeModelException
    {
        try
        {
            return getType().isInterface();
        }
        catch (JavaModelException e)
        {
            throw new TypeModelException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isInterface()
     */
    public boolean isAnnotation() throws TypeModelException
    {
        try
        {
            return getType().isAnnotation();
        }
        catch (JavaModelException e)
        {
            throw new TypeModelException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.IJavaType#isSuperTypeOf(core.IJavaType)
     */
    public boolean isSuperTypeOf(IJavaType candidate) throws TypeModelException
    {
        try
        {
            return EclipseUtils.extendsType((IType) candidate.getUnderlier(), getType());
        }
        catch (JavaModelException e)
        {
            throw new TypeModelException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.types.IJavaType#getConstructors()
     */
    public IMethod[] getConstructors() throws TypeModelException
    {
        if (isInterface())
            return new IMethod[] {};
        EclipseJavaTypeInfo info = (EclipseJavaTypeInfo) getInfo();
        return copyMethods(info.getDeclaredConstructors(getType(), true));
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.types.IJavaType#getDeclaredConstructors()
     */
    public IMethod[] getDeclaredConstructors() throws TypeModelException
    {
        if (isInterface())
            return new IMethod[] {};
        EclipseJavaTypeInfo info = (EclipseJavaTypeInfo) getInfo();
        return copyMethods(info.getDeclaredConstructors(null, false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.types.IJavaType#getDeclaredMethods()
     */
    public IMethod[] getDeclaredMethods() throws TypeModelException
    {
        EclipseJavaTypeInfo info = (EclipseJavaTypeInfo) getInfo();
        return copyMethods(info.getDeclaredMethods(getType(), false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.types.IJavaType#getMethods()
     */
    public IMethod[] getMethods() throws TypeModelException
    {
        EclipseJavaTypeInfo info = (EclipseJavaTypeInfo) getInfo();
        return copyMethods(info.getPublicMethods(getType()));
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
        return getType().equals(((EclipseJavaType) arg0).getType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return getType().hashCode();
    }

    protected TypeElementInfo createElementInfo()
    {
        return new EclipseJavaTypeInfo(toString());
    }

    protected void generateInfos(TypeElementInfo info, HashMap newElements,
            ITypeHierarchy hierarchy, IProgressMonitor monitor) throws TypeModelException
    {
        try
        {
            if (hierarchy == null)
                hierarchy = getType().newSupertypeHierarchy(monitor);
        }
        catch (JavaModelException jmex)
        {

            throw new TypeModelException(jmex);
        }

        // puts the info before building the structure so that questions to the handle behave as if
        // the element existed
        newElements.put(this, info);

        // build the structure
        try
        {
            buildStructure(info, monitor, newElements, hierarchy);

            // open the parent if necessary

            openParent(info, newElements, hierarchy, monitor);
        }
        catch (TypeModelException e)
        {
            newElements.remove(this);
            throw e;
        }

        if (monitor != null && monitor.isCanceled())
            throw new OperationCanceledException();

    }

    protected void buildStructure(TypeElementInfo info, IProgressMonitor pm, HashMap newElements,
            ITypeHierarchy hierarchy) throws TypeModelException
    {
        EclipseJavaTypeInfo ejtInfo = (EclipseJavaTypeInfo) info;

        IType myType = getType();
        IType superclass = hierarchy.getSuperclass(myType);
        if (superclass != null)
            ejtInfo.setParent(new EclipseJavaType(superclass));

        IType[] interfaces = hierarchy.getSuperInterfaces(myType);
        for (int i = 0; i < interfaces.length; i++)
        {
            EclipseJavaType iface = new EclipseJavaType(interfaces[i]);
            ejtInfo.addDeclaredInterface(iface);

            EclipseJavaTypeInfo ifaceInfo = (EclipseJavaTypeInfo) TypeModelManager
                    .getTypeModelManager().peekAtInfo(iface);
            if (ifaceInfo == null)
            {
                iface.generateInfos(iface.createElementInfo(), newElements, hierarchy, pm);
                ifaceInfo = (EclipseJavaTypeInfo) newElements.get(iface);
            }
            ifaceInfo.addChild(this);
        }
    }

    public void close() throws TypeModelException
    {
        EclipseJavaTypeInfo myInfo = (EclipseJavaTypeInfo) TypeModelManager.getTypeModelManager()
                .peekAtInfo(this);
        if (myInfo != null)
        {
            doClose(myInfo.getParent());
            doClose(myInfo.getRawInterfaces());
        }
        super.close();
    }

    protected void closing(TypeElementInfo info) throws TypeModelException
    {

    }

    private void doClose(TypeElement[] parents)
    {
        for (int i = 0; i < parents.length; i++)
        {
            doClose(parents[i]);
        }
    }

    private void doClose(TypeElement parent)
    {
        if (parent == null)
            return;

        EclipseJavaTypeInfo parentInfo = (EclipseJavaTypeInfo) TypeModelManager
                .getTypeModelManager().peekAtInfo((TypeElement) parent);

        if (parentInfo != null)
            parentInfo.removeChild(this);
    }

    public String getElementName()
    {
        return getSimpleName();
    }

    public String toString()
    {
        return getElementName();
    }

    private static IMethod[] copyMethods(IMethod[] arg)
    {
        if (arg.length == 0)
            return arg;

        IMethod[] out = new IMethod[arg.length];
        System.arraycopy(arg, 0, out, 0, arg.length);
        return out;
    }

}
