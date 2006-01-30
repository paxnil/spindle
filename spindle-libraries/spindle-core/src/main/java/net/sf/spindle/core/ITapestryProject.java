package net.sf.spindle.core;

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
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.types.IJavaTypeFinder;

/**
 * In core terms a project consists of the classpath root and the context root of a web application.
 * <p>
 * The project is also a source of {@link net.sf.spindle.core.types.IJavaType}s, wrappers around
 * Java types found in the project classpath.
 * <p>
 * In Eclipse terms the ITapestryProject is a wrapper around an Eclipse project (a folder in the
 * Eclipse workbench) and the contect root is a folder in the project that represents the root of an
 * exploded war layout. The classpath root is a root containing all of the sourcefolders in the
 * project as well as any jars in the project classpath. Other projects in the Eclipse workbench may
 * contribute source folders and jar files if those projects are included in the project classpath.
 * 
 * @see net.sf.spindle.core.resources.IResourceRoot
 * @see net.sf.spindle.core.resources.ICoreResource
 * @see net.sf.spindle.core.types.IJavaTypeFinder
 * @see net.sf.spindle.core.types.IJavaType
 * @see net.sf.spindle.core.types.IJavaTypeExtension
 */
public interface ITapestryProject extends IJavaTypeFinder
{
    /**
     * The core has its own validator. A validator that does not understand XML schema. If the
     * project web.xml validates against a schema (servlet 2.4+) this method should return false.
     * 
     * @return true if the core should validate web.xml
     */
    public boolean isValidatingWebXML();

    public IResourceRoot getClasspathRoot();

    public IResourceRoot getWebContextLocation();

}