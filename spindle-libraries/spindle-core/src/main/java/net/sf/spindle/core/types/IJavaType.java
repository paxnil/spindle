package net.sf.spindle.core.types;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
/**
 * Wrapper interface for things are Java types (classes ir interfaces).
 * <p>
 * Examples of underlying types would be {@link java.lang.Class} instances in a runtime
 * implementation or {@link org.eclipse.jdt.core.IType} instances in an Eclipse IDE.
 * <p>
 * Instances of IJavaType are created via calls to
 * {@link core.types.IJavaTypeFinder#findType(String)}.
 * <p>
 * Clients should never directly instantiate instances of an implementation.
 * 
 * @author gwl
 */
public interface IJavaType
{

    /**
     * IJavaType's may be cached and if an instance if based on a source files it may become invalid
     * if the file is deleted/renamed.
     * <p>
     * When this is the case this method should return false.
     * 
     * @return true if the underlying type still exists.
     */
    boolean exists();

    /**
     * The FQN of the underlier represented by this instance.
     * 
     * @return the FQN
     */
    String getFullyQualifiedName();

    /**
     * The underlying type represented by this instance
     * 
     * @return the underlying type
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
     * Answer true of the underlying type is an interface.
     * 
     * @return true iff this instance represents an interface
     */
    boolean isInterface();

    /**
     * Answer true if the underlying type is a supertype of the candidate type.
     * 
     * @param candidate
     * @return true iff the candidate is a subclass of <code>this</code>
     */
    boolean isSuperTypeOf(IJavaType candidate);

    /**
     * Answer true if this type represents and annotation type.
     * 
     * @return true iff this type is an Annotation type.
     */
    boolean isAnnotation();

}
