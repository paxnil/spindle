package core;

/**
 * Wrapper interface for things are Java types (classes ir interfaces).
 * <p>
 * Examples of underlying types would be {@link java.lang.Class} instances in a runtime
 * implementation or {@link org.eclipse.jdt.core.IType} instances in an Eclipse IDE.
 * <p>
 * Instances of IJavaType are created via calls to
 * {@link core.IJavaTypeFinder#findType(String)}.
 * <p>
 * Clients should never directly instantiate instances of an implementation.
 * 
 * @author gwl
 */
public interface IJavaType
{
    /**
     * @return true iff the underlying type represented by this instance exists
     */
    boolean exists();

    /**
     * @return the FQN of the underlier represented by this instance
     */
    String getFullyQualifiedName();

    /**
     * @return the underlying type represented by this instance
     */
    Object getUnderlier();

    /**
     * Any type obtained from a complied form (.class file) is considered binary. In an IDE like
     * Eclipse a type can be represented by an uncompiled source file and is not considered binary.
     * 
     * @return true iff this instance represents a compiled binary type
     */
    boolean isBinary();

    /**
     * @return true iff this instance represents an interface
     */
    boolean isInterface();

    boolean isSuperTypeOf(IJavaType candidate);

    boolean isAnnotation();
}
