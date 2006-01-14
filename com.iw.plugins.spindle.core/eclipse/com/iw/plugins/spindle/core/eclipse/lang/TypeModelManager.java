package com.iw.plugins.spindle.core.eclipse.lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import core.types.TypeModelException;

public class TypeModelManager
{
    /**
     * Unique handle onto the TypeModel
     */
    final TypeModel typeModel = new TypeModel();

    /**
     * The singleton manager
     */
    private static TypeModelManager MANAGER = new TypeModelManager();

    public static boolean VERBOSE = true;

    private ThreadLocal temporaryCache = new ThreadLocal();

    class TempMap extends HashMap
    {
        public TypeElementInfo getInfo(TypeElement element)
        {
            return (TypeElementInfo) get(element);
        }

        public TypeElementInfo peekAtInfo(TypeElement element)
        {
            return (TypeElementInfo) get(element);
        }

        public void removeElement(TypeElement element)
        {
            remove(element);
        }

        public void putInfo(TypeElement element, TypeElementInfo info)
        {
            put(element, info);
        }

        public void removeInfo(TypeElement element)
        {
            put(element, null);
        }
    }

    private TempMap cache = new TempMap();

    /**
     * Returns the singleton TypeModelManager
     */
    public final static TypeModelManager getTypeModelManager()
    {
        return MANAGER;
    }

    /**
     * Debugging purposes only.
     */
    public static void clearTypeCache()
    {
        MANAGER.cache.clear();
    }

    /**
     * Returns the handle to the active Type Model.
     */
    public final TypeModel getTypeModel()
    {
        return this.typeModel;
    }

    /*
     * Returns whether there is a temporary cache for the current thread.
     */
    public boolean hasTemporaryCache()
    {
        return this.temporaryCache.get() != null;
    }

    /**
     * Returns the temporary cache for newly opened elements for the current thread. Creates it if
     * not already created.
     */
    public HashMap getTemporaryCache()
    {
        HashMap result = (HashMap) this.temporaryCache.get();
        if (result == null)
        {
            result = new HashMap();
            this.temporaryCache.set(result);
        }
        return result;
    }

    /*
     * Resets the temporary cache for newly created elements to null.
     */
    public void resetTemporaryCache()
    {
        this.temporaryCache.set(null);
    }

    public synchronized TypeElementInfo getInfo(TypeElement element)
    {
        HashMap tempCache = (HashMap) this.temporaryCache.get();

        if (tempCache != null)
        {
            TypeElementInfo result = (TypeElementInfo) tempCache.get(element);
            if (result != null)
                return result;

        }
        return this.cache.getInfo(element);
    }

    /**
     * Returns the info for this element without disturbing the cache ordering.
     */
    protected synchronized Object peekAtInfo(TypeElement element)
    {
        HashMap tempCache = (HashMap) this.temporaryCache.get();

        if (tempCache != null)
        {
            Object result = tempCache.get(element);
            if (result != null)
                return result;
        }
        return this.cache.peekAtInfo(element);
    }

    protected synchronized void putInfos(TypeElement openedElement, Map newElements)
    {
        // remove children
        Object existingInfo = this.cache.peekAtInfo(openedElement);
        if (existingInfo instanceof TypeElementInfo)
        {
            TypeElement[] children = ((TypeElementInfo) existingInfo).getChildren();
            for (int i = 0, size = children.length; i < size; ++i)
            {
                TypeElement child = (TypeElement) children[i];
                try
                {
                    child.close();
                }
                catch (TypeModelException e)
                {
                    // ignore
                }
            }
        }

        // Need to put any JarPackageFragmentRoot in first.
        // This is due to the way the LRU cache flushes entries.
        // When a JarPackageFragment is flused from the LRU cache, the entire
        // jar is flushed by removing the JarPackageFragmentRoot and all of its
        // children (see ElementCache.close()). If we flush the JarPackageFragment
        // when its JarPackageFragmentRoot is not in the cache and the root is about to be
        // added (during the 'while' loop), we will end up in an inconsist state.
        // Subsequent resolution against package in the jar would fail as a result.
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=102422
        // (theodora)
        // for(Iterator it = newElements.entrySet().iterator(); it.hasNext(); ){
        // Map.Entry entry = (Map.Entry)it.next();
        // IJavaElement element = (IJavaElement)entry.getKey();
        // if( element instanceof JarPackageFragmentRoot ){
        // Object info = entry.getValue();
        // it.remove();
        // this.cache.putInfo(element, info);
        // }
        // }

        Iterator iterator = newElements.keySet().iterator();
        while (iterator.hasNext())
        {
            TypeElement element = (TypeElement) iterator.next();
            TypeElementInfo info = (TypeElementInfo) newElements.get(element);
            this.cache.putInfo(element, info);
        }
    }

    public synchronized Object removeInfoAndChildren(TypeElement element) throws TypeModelException
    {
        TypeElementInfo info = this.cache.peekAtInfo(element);
        if (info != null)
        {
            // boolean wasVerbose = false;
            try
            {
                element.closing(info);
                TypeElement[] children = ((TypeElementInfo) info).getChildren();                
                for (int i = 0, size = children.length; i < size; ++i)
                {
                    TypeElement child = (TypeElement) children[i];
                    child.close();
                }
                this.cache.removeInfo(element);
                // if (wasVerbose) {
                // System.out.println(this.cache.toStringFillingRation("-> ")); //$NON-NLS-1$
                // }
            }
            finally
            {
                // JavaModelManager.VERBOSE = wasVerbose;
            }
            return info;
        }
        return null;
    }
}
