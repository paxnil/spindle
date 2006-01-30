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
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

/**
 * Extension interface for {@link net.sf.spindle.core.types.IJavaType}.
 * <p>
 * Introduces the ability to handle Tapestry annotations.
 * <p>
 * This is modeled as an extension as the next version of the JDK (mustang) changes the api naming
 * scheme from com.sun.mirror.* to javax.annotation.*
 * <p>
 * The core will gracefully degrade if IDE implementors choose not use this extension. (ie.
 * annotation processing will not be available).
 * <p>
 * Usage: if your IDE implementation includes an implementation of the mirror api, simple make your
 * implementation of {@link net.sf.spindle.core.types.IJavaType} also implement (@link
 * IJavaTypeExtension).
 */
public interface IJavaTypeExtension
{
    public AnnotationProcessorEnvironment getEnvironment();
}
