package com.iw.plugins.spindle.core;

/**
 * @author gwl
 */
public interface IJavaTypeFinder
{
    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.ITapestryProject#findType(java.lang.String)
     */
    IJavaType findType(String fullyQualifiedName);
    
    boolean isCachingJavaTypes();
}