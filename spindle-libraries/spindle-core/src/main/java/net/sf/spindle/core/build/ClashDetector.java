package net.sf.spindle.core.build;

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
import java.util.List;
import java.util.Map;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.PathUtils;
import net.sf.spindle.core.util.Assert;

import org.apache.hivemind.Resource;

public class ClashDetector
{

    /**
     * @param ns
     *            the location of a namespace we want to ensure is not in a clash situation
     * @param existingNS
     *            a list of namespace locations we might clash with
     * @param errorKey
     *            a key into the messages for error reporting.
     * @throws ClashException
     *             if ns is nested in the child locations of any of the extisting.
     */
    public static void checkNamspaceClash(ICoreNamespace ns, List<ICoreNamespace> existingNS,
            String errorKey) throws ClashException
    {
        Assert.isLegal(ns != null);
        Assert.isLegal(existingNS != null);

        ICoreResource candidate = (ICoreResource) ns.getSpecificationLocation();

        for (ICoreNamespace namespace : existingNS)
        {
            // while resources clash with other resources with the same path
            // a namespace will not clash with itself.
            if (ns == namespace)
                continue;

            ICoreResource existing = (ICoreResource) namespace.getSpecificationLocation();

            if (existing.clashesWith(candidate))
                throw new ClashException(candidate, existing, null);
        }
    }

    public static boolean clashesWith(ICoreResource lhs, ICoreResource rhs)
    {
        Assert.isNotNull(lhs);
        Assert.isNotNull(rhs);

        if (lhs.isClasspathResource() != rhs.isClasspathResource())
            return false;

        if (lhs.equals(rhs))
            return true;

        PathUtils left = new PathUtils(lhs.getPath());
        if (!TapestryCore.isNull(lhs.getName()))
            left = left.removeLastSegments(1);

        PathUtils right = new PathUtils(rhs.getPath());
        if (!TapestryCore.isNull(rhs.getName()))
            right = right.removeLastSegments(1);

        if (left.equals(right))
            return true;

        if (left.isPrefixOf(right))
            return true;

        if (right.isPrefixOf(left))
            return true;

        return false;

    }

    private Map<Resource, ICoreNamespace> fNamespaceMap;

    private Map<Resource, ICoreNamespace> fResourceMap;

    /**
     * Some resources, components mostly, we want to ensure are not claimed by more than one
     * namespace.
     * 
     * @param resource
     * @param namespace
     * @param errorKey
     * @throws ClashException
     */
    public void claimResourceForNamespace(ICoreResource resource, ICoreNamespace namespace,
            String errorKey) throws ClashException
    {
        // Assert.isNotNull(resource);
        // Assert.isNotNull(namespace);
        //
        // if (fNamespaceMap == null)
        // fNamespaceMap = new HashMap();
        //
        // ICoreNamespace existing = (ICoreNamespace) fNamespaceMap.get(resource);
        // if (existing != null)
        // {
        // if (!existing.equals(namespace))
        // {
        // ICoreResource requestor = (ICoreResource) namespace.getSpecificationLocation();
        // ICoreResource owner = (ICoreResource) existing.getSpecificationLocation();
        // // TODO add useful info for an error marker.
        // throw new ClashException(requestor, owner, resource);
        // }
        // }
        // else
        // {
        // fNamespaceMap.put(resource, namespace);
        // }
    }

    /**
     * Same thing, templates can belong to at most one component, page.
     * 
     * @param component
     * @param template
     * @throws ClashException
     */
    public void claimTemplateForComponent(ICoreResource component, ICoreResource template)
            throws ClashException
    {
        // Assert.isNotNull(component);
        // Assert.isNotNull(template);
        //
        // if (fResourceMap == null)
        // fResourceMap = new HashMap();
        //
        // ICoreResource existingComponent = (ICoreResource) fResourceMap.get(template);
        // if (existingComponent != null)
        // {
        // if (!existingComponent.equals(component))
        // // TODO add useful info for an error marker.
        // throw new ClashException(component, existingComponent, template);
        // }
        // else
        // {
        // fResourceMap.put(template, component);
        // }
    }

    public void clear()
    {
        if (fNamespaceMap != null)
            fNamespaceMap.clear();
    }
}
