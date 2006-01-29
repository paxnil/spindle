package net.sf.spindle.core.types;


/**
 * <code>IJavaTypeFinder</code>s are used to create instances of
 * {@link core.types.IJavaType}.
 * <p>
 * Implementations may cache results and will indicate this having implemented
 * {@link #isCachingJavaTypes()}
 * 
 * @author gwl
 * @see core.types.IJavaType
 */
public interface IJavaTypeFinder
{
    /**
     * Perform a lookup keyed on fully qualified name. Implementors may cache the result for
     * subsequent calls.
     * <p>
     * @param fullyQualifiedName the fqn of the type or interface we want to locate
     * @return an instance of IJavaType that corresponds to the fqn or null if no underlying type
     *         was found.
     * @see #isCachingJavaTypes()
     */
    IJavaType findType(String fullyQualifiedName);

    /**
     * @return true iff this finder is caching the results of calls to {@link #findType(String)}
     */
    boolean isCachingJavaTypes();
}