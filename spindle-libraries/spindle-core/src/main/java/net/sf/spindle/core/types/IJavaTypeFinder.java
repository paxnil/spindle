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
 * <code>IJavaTypeFinder</code>s are used to create instances of {@link core.types.IJavaType}.
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
     * 
     * @param fullyQualifiedName
     *            the fqn of the type or interface we want to locate
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