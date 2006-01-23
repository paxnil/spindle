package core.types;

import org.eclipse.jdt.core.IMethod;


/**
 * Wrapper interface for things are Java types (classes ir interfaces).
 * <p>
 * Examples of underlying types would be {@link java.lang.Class} instances in a runtime
 * implementation or {@link org.eclipse.jdt.core.IType} instances in an Eclipse IDE.
 * <p>
 * Instances of IJavaType are created via calls to {@link core.types.IJavaTypeFinder#findType(String)}.
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
     * @return the name minus the package parts
     */
    String getSimpleName();

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
    boolean isInterface() throws TypeModelException;

    boolean isSuperTypeOf(IJavaType candidate) throws TypeModelException;

    boolean isAnnotation() throws TypeModelException;

    boolean isArray();

    int getModifiers() throws TypeModelException;

    IJavaType getArrayType();

    IJavaType getSuperClass() throws TypeModelException;

    /** following are Eclipse specific TODO break from Eclipse * */

    /**
     * Returns an array containing {@link org.eclipse.jdt.core.IMethod} objects reflecting all the
     * public constructors of the class represented by this type.
     */
    IMethod[] getConstructors() throws TypeModelException;

    /**
     * Returns an array of {@link org.eclipse.jdt.core.IMethod} objects reflecting all the
     * constructors declared by the class represented by this type.
     * <p>
     * These are public, protected, default (package) access, and private constructors.
     * <p>
     * The elements in the array returned are not sorted and are not in any particular order.
     */
    IMethod[] getDeclaredConstructors() throws TypeModelException;

    /**
     * Returns an array of {@link org.eclipse.jdt.core.IMethod} objects reflecting all the methods
     * declared by the class or interface represented by this type.
     * <p>
     * This includes public, protected, default (package) access, and private methods, but excludes
     * inherited methods.
     * <p>
     * The elements in the array returned are not sorted and are not in any particular order.
     */
    IMethod[] getDeclaredMethods() throws TypeModelException;

    IMethod[] getMethods() throws TypeModelException;
}
