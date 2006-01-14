package com.iw.plugins.spindle.core.eclipse.lang;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import core.types.IJavaType;
import core.types.TypeModelException;

/*package*/class EclipseJavaTypeInfo extends TypeElementInfo
{
    public EclipseJavaTypeInfo(String label)
    {
        super(label);
    }

    private TypeElement[] declaredInterfaces = TypeElement.EMPTY_TYPE_ELEMENT_ARRAY;;

    private IMethod[] publicConstructors = null;

    private IMethod[] declaredConstructors = null;

    private IMethod[] declaredPublicMethods = null;

    private IMethod[] declaredMethods = null;

    private IMethod[] publicMethods = null;

    // private IMethod[] privateGetDeclaredMethods(boolean publicOnly)
    // {
    //
    // }

    public void addDeclaredInterface(EclipseJavaType iface)
    {
        if (this.declaredInterfaces == TypeElement.EMPTY_TYPE_ELEMENT_ARRAY)
        {
            setDeclaredInterfaces(new TypeElement[]
            { iface });
        }
        else
        {
            if (!includesDeclaredInterface(iface))
                setDeclaredInterfaces(growAndAddToArray(this.declaredInterfaces, iface));
        }
    }

    public void setDeclaredInterfaces(TypeElement[] declaredInterfaces)
    {
        this.declaredInterfaces = declaredInterfaces;
    }

    public TypeElement[] getRawInterfaces()
    {
        return this.declaredInterfaces;
    }

    public IJavaType[] getDeclaredInterfaces()
    {
        IJavaType[] result = new IJavaType[this.declaredInterfaces.length];
        if (result.length > 0)
        {
            System.arraycopy(this.declaredInterfaces, 0, result, 0, this.declaredInterfaces.length);
        }
        return result;
    }

    protected boolean includesDeclaredInterface(EclipseJavaType iface)
    {

        for (int i = 0; i < this.declaredInterfaces.length; i++)
        {
            if (this.declaredInterfaces[i].equals(iface))
            {
                return true;
            }
        }
        return false;
    }

    public IMethod[] getDeclaredConstructors(IType type, boolean publicOnly)
            throws TypeModelException
    {
        IMethod[] result = null;

        if (publicOnly)
            result = publicConstructors;
        else
            result = declaredConstructors;

        if (result != null)
            return result;

        scanMethods(type);

        if (publicOnly)
            result = publicConstructors;
        else
            result = declaredConstructors;

        return result;
    }

    public IMethod[] getDeclaredMethods(IType type, boolean publicOnly) throws TypeModelException
    {
        IMethod[] result = null;
        if (publicOnly)
            result = declaredPublicMethods;
        else
            result = declaredMethods;

        if (result != null)
            return result;

        scanMethods(type);

        if (publicOnly)
            result = declaredPublicMethods;
        else
            result = declaredMethods;

        return result;
    }

    private void scanMethods(IType type) throws TypeModelException
    {
        IMethod[] allMethods = null;
        try
        {
            publicConstructors = new IMethod[0];
            declaredConstructors = new IMethod[0];
            declaredMethods = new IMethod[0];
            declaredPublicMethods = new IMethod[0];

            allMethods = type.getMethods();

            boolean isConstructor, isPublic;
            for (int i = 0; i < allMethods.length; i++)
            {
                isConstructor = allMethods[i].isConstructor();
                isPublic = Flags.isPublic(allMethods[i].getFlags());

                if (isConstructor)
                {
                    declaredConstructors = growAndAddToArray(declaredConstructors, allMethods[i]);
                    if (isPublic)
                        publicConstructors = growAndAddToArray(publicConstructors, allMethods[i]);
                }
                else
                {
                    declaredMethods = growAndAddToArray(declaredMethods, allMethods[i]);
                    if (isPublic)
                        declaredPublicMethods = growAndAddToArray(
                                declaredPublicMethods,
                                allMethods[i]);
                }
            }
        }
        catch (JavaModelException e)
        {
            throw new TypeModelException(e);
        }
    }

    public IMethod[] getPublicMethods(IType type) throws TypeModelException
    {
        try
        {
            IMethod[] result = publicMethods;
            if (result != null)
                return result;

            MethodArray methods = new MethodArray();
            methods.addAll(getDeclaredMethods(type, true));

            // recurse over supclass & superinterfaces
            MethodArray inherited = new MethodArray();
            for (int i = 0; i < declaredInterfaces.length; i++)
            {
                EclipseJavaType iface = (EclipseJavaType) declaredInterfaces[i];
                EclipseJavaTypeInfo ifaceInfo = (EclipseJavaTypeInfo) iface.getInfo();
                inherited.addAll(ifaceInfo.getPublicMethods(iface.getType()));
            }
            // get public methods from superclasses
            EclipseJavaType superClass = (EclipseJavaType) getParent();
            if (superClass != null)
            {
                EclipseJavaTypeInfo superInfo = (EclipseJavaTypeInfo) superClass.getInfo();
                MethodArray supers = new MethodArray();
                supers.addAll(superInfo.getPublicMethods(superClass.getType()));
                // filter out concrete implementations of iface methods
                for (int i = 0; i < supers.length; i++)
                {
                    IMethod m = supers.get(i);
                    if (m != null && !Flags.isAbstract(m.getFlags()))
                    {
                        inherited.removeByNameAndSignature(m);
                    }
                }
                // insert superclass inherited before superinterface's
                supers.addAll(inherited);
                inherited = supers;
            }
            // filter out local methods from inherited ones.
            for (int i = 0; i < methods.length; i++)
            {
                IMethod m = methods.get(i);
                inherited.removeByNameAndSignature(m);
            }
            methods.addAllIfNotPresent(inherited);
            methods.compactAndTrim();
            result = methods.getArray();
            publicMethods = result;
            return result;
        }
        catch (JavaModelException e)
        {
            throw new TypeModelException(e);
        }

    }

    private IMethod[] growAndAddToArray(IMethod[] array, IMethod addition)
    {
        IMethod[] old = array;
        array = new IMethod[old.length + 1];
        System.arraycopy(old, 0, array, 0, old.length);
        array[old.length] = addition;
        return array;
    }

    static class MethodArray
    {
        private IMethod[] methods;

        private int length;

        MethodArray()
        {
            methods = new IMethod[20];
            length = 0;
        }

        void add(IMethod m)
        {
            if (length == methods.length)
            {
                IMethod[] newMethods = new IMethod[2 * methods.length];
                System.arraycopy(methods, 0, newMethods, 0, methods.length);
                methods = newMethods;
            }
            methods[length++] = m;
        }

        void addAll(IMethod[] ma)
        {
            for (int i = 0; i < ma.length; i++)
            {
                add(ma[i]);
            }
        }

        void addAll(MethodArray ma)
        {
            for (int i = 0; i < ma.length(); i++)
            {
                add(ma.get(i));
            }
        }

        void addIfNotPresent(IMethod newMethod)
        {
            for (int i = 0; i < length; i++)
            {
                IMethod m = methods[i];
                if (m == newMethod || (m != null && m.equals(newMethod)))
                {
                    return;
                }
            }
            add(newMethod);
        }

        void addAllIfNotPresent(MethodArray newMethods)
        {
            for (int i = 0; i < newMethods.length(); i++)
            {
                IMethod m = newMethods.get(i);
                if (m != null)
                {
                    addIfNotPresent(m);
                }
            }
        }

        int length()
        {
            return length;
        }

        IMethod get(int i)
        {
            return methods[i];
        }

        void removeByNameAndSignature(IMethod toRemove) throws JavaModelException
        {
            for (int i = 0; i < length; i++)
            {
                IMethod m = methods[i];
                if (m != null && m.getReturnType().equals(toRemove.getReturnType())
                        && m.getElementName().equals(toRemove.getElementName())
                        && arrayContentsEq(m.getParameterTypes(), toRemove.getParameterTypes()))
                {
                    methods[i] = null;
                }
            }
        }

        void compactAndTrim()
        {
            int newPos = 0;
            // Get rid of null slots
            for (int pos = 0; pos < length; pos++)
            {
                IMethod m = methods[pos];
                if (m != null)
                {
                    if (pos != newPos)
                    {
                        methods[newPos] = m;
                    }
                    newPos++;
                }
            }
            if (newPos != methods.length)
            {
                IMethod[] newMethods = new IMethod[newPos];
                System.arraycopy(methods, 0, newMethods, 0, newPos);
                methods = newMethods;
            }
        }

        IMethod[] getArray()
        {
            return methods;
        }
    }

    private static boolean arrayContentsEq(Object[] a1, Object[] a2)
    {
        if (a1 == null)
        {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null)
        {
            return a1.length == 0;
        }

        if (a1.length != a2.length)
        {
            return false;
        }

        for (int i = 0; i < a1.length; i++)
        {
            if (a1[i].equals(a2[i]))
            {
                return false;
            }
        }

        return true;
    }

}
