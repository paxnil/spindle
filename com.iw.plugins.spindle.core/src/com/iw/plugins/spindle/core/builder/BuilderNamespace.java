/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.builder;

import java.util.List;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BuilderNamespace implements INamespace
{

    /**
     * 
     */
    public BuilderNamespace()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getId()
     */
    public String getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getExtendedId()
     */
    public String getExtendedId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getNamespaceId()
     */
    public String getNamespaceId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getParentNamespace()
     */
    public INamespace getParentNamespace()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getChildNamespace(java.lang.String)
     */
    public INamespace getChildNamespace(String id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getChildIds()
     */
    public List getChildIds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getPageSpecification(java.lang.String)
     */
    public IComponentSpecification getPageSpecification(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#containsPage(java.lang.String)
     */
    public boolean containsPage(String name)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getPageNames()
     */
    public List getPageNames()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getComponentSpecification(java.lang.String)
     */
    public IComponentSpecification getComponentSpecification(String type)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#containsComponentType(java.lang.String)
     */
    public boolean containsComponentType(String type)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getComponentTypes()
     */
    public List getComponentTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getServiceClassName(java.lang.String)
     */
    public String getServiceClassName(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getServiceNames()
     */
    public List getServiceNames()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getSpecification()
     */
    public ILibrarySpecification getSpecification()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#constructQualifiedName(java.lang.String)
     */
    public String constructQualifiedName(String pageName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#getSpecificationLocation()
     */
    public IResourceLocation getSpecificationLocation()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#isApplicationNamespace()
     */
    public boolean isApplicationNamespace()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#installPageSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
     */
    public void installPageSpecification(String pageName, IComponentSpecification specification)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.INamespace#installComponentSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
     */
    public void installComponentSpecification(String type, IComponentSpecification specification)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.ILocatable#getLocation()
     */
    public ILocation getLocation()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
