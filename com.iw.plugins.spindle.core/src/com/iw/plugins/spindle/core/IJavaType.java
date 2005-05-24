package com.iw.plugins.spindle.core;

/**
 * 
 * Wrapper around things that are java types. The builder should not care
 * if the underlier is an actual class or an Eclipse IType
 * @author gwl
 *
 */
public interface IJavaType
{
    boolean exists();
    String getFullyQualifiedName();
    Object getUnderlier();
    boolean isBinary();
    boolean isInterface();
    boolean isSuperTypeOf(IJavaType candidate);
    boolean isAnnotation();
}
