package com.iw.plugins.spindle.core.eclipse.lang;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeHierarchy;

import core.types.TypeModelException;
import core.util.Assert;

/*package*/abstract class TypeElement
{
    public static int MODEL = 0;

    public static int PROJECT = 1;

    public static int TYPE = 2;

    protected static final TypeElement[] EMPTY_TYPE_ELEMENT_ARRAY = new TypeElement[0];

    protected static final Object NO_INFO = new Object();

    IJavaElement element;

    public TypeElement(IJavaElement element)
    {
        this.element = element;
    }

    public IJavaElement getJavaElement()
    {
        return element;
    }

    public abstract int getTypeElementType();

    public TypeElementInfo getInfo() throws TypeModelException
    {
        return getInfo(null);
    }

    public TypeElementInfo getInfo(IProgressMonitor monitor) throws TypeModelException
    {
        TypeModelManager manager = TypeModelManager.getTypeModelManager();
        TypeElementInfo info = manager.getInfo(this);
        if (info != null)
            return info;
        return openWhenClosed(createElementInfo(), monitor);
    }

    public void close() throws TypeModelException
    {
        TypeModelManager.getTypeModelManager().removeInfoAndChildren(this);
    }

    public TypeElementInfo openWhenClosed(TypeElementInfo info, IProgressMonitor monitor)
            throws TypeModelException
    {
        TypeModelManager manager = TypeModelManager.getTypeModelManager();
        boolean hadTemporaryCache = manager.hasTemporaryCache();
        try
        {
            HashMap newElements = manager.getTemporaryCache();

            generateInfos(info, newElements, null, monitor);

            if (info == null)
                info = (TypeElementInfo) newElements.get(this);

            if (info == null)
                throw newNotPresentException();

            if (!hadTemporaryCache)
                manager.putInfos(this, newElements);

        }
        finally
        {
            if (!hadTemporaryCache)
                manager.resetTemporaryCache();

        }
        return info;
    }

    protected abstract void closing(TypeElementInfo info) throws TypeModelException;

    public TypeModelException newNotPresentException()
    {
        return new TypeModelException("TypeElement does not exist", this);
    }

    protected abstract void generateInfos(TypeElementInfo info, HashMap newElements,
            ITypeHierarchy hierarchy, IProgressMonitor pm) throws TypeModelException;

    protected abstract TypeElementInfo createElementInfo();

    public TypeElement[] getChildren() throws TypeModelException
    {
        TypeElementInfo elementInfo = getInfo();
        if (elementInfo instanceof TypeElementInfo)
        {
            return ((TypeElementInfo) elementInfo).getChildren();
        }
        else
        {
            return EMPTY_TYPE_ELEMENT_ARRAY;
        }
    }

    public String getElementName()
    {
        return "";
    }

    protected TypeElement getParent() throws TypeModelException
    {
        TypeElementInfo elementInfo = getInfo();
        return elementInfo.getParent();
    }

    public boolean isOpen()
    {
        return TypeModelManager.getTypeModelManager().getInfo(this) != null;
    }

    protected void openParent(TypeElementInfo childInfo, HashMap newElements,
            ITypeHierarchy hierarchy, IProgressMonitor pm) throws TypeModelException
    {
        TypeElement parent = getParent();

        if (parent != null && !parent.isOpen())
        {
            parent.generateInfos(parent.createElementInfo(), newElements, hierarchy, pm);

            TypeElementInfo parentInfo = (TypeElementInfo) newElements.get(parent);

            if (parentInfo != null)
                parentInfo.addChild(this);
        }

    }
}
