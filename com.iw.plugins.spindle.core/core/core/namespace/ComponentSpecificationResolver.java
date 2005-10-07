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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package core.namespace;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;

import core.util.Assert;

/**
 * Resolver for Component Specifications
 * 
 * @author glongman@gmail.com
 */

public class ComponentSpecificationResolver
{

    protected INamespace fFrameworkNamespace;

    protected INamespace fContainerNamespace;

    public ComponentSpecificationResolver(INamespace framework, INamespace containerNamespace)
    {
        Assert.isTrue(framework != null ? INamespace.FRAMEWORK_NAMESPACE.equals(framework.getId())
                : true);
        fFrameworkNamespace = framework;
        Assert.isNotNull(containerNamespace);
        fContainerNamespace = containerNamespace;
    }

    /**
     * Passed the namespace of a container (to resolve the type in) and the type to resolve,
     * performs the processing. A "bare type" (without a library prefix) may be in the
     * containerNamespace, or the framework namespace (a search occurs in that order).
     * 
     * @param containerNamespace namespace that may contain a library referenced in the type
     * @param type the component specification to find, either a simple name, or prefixed with a
     *            library id (defined for the container namespace)
     */

    public IComponentSpecification resolve(String type)
    {
        int colonx = type.indexOf(':');

        if (colonx > 0)
        {
            String libraryId = type.substring(0, colonx);
            String simpleType = type.substring(colonx + 1);

            return resolve(libraryId, simpleType);
        }
        else
            return resolve(null, type);
    }

    /**
     * Like {@link #resolve(INamespace, String)}, but used when the type has already been parsed
     * into a library id and a simple type.
     * 
     * @param containerNamespace namespace that may contain a library referenced in the type
     * @param libraryId the library id within the container namespace, or null
     * @param type the component specification to find as a simple name (without a library prefix)
     * @throws ApplicationRuntimeException if the type cannot be resolved
     */

    public IComponentSpecification resolve(String libraryId, String type)
    {
        INamespace namespace = null;

        if (libraryId != null && !libraryId.equals(fContainerNamespace.getId()))
            namespace = fContainerNamespace.getChildNamespace(libraryId);
        else
            namespace = fContainerNamespace;

        if (namespace == null)
            return null;

        if (namespace.containsComponentType(type))
            return namespace.getComponentSpecification(type);

        if (libraryId == null)
            return resolveInFramework(type);

        return null;
    }

    protected IComponentSpecification resolveInFramework(String type)
    {
        if (fFrameworkNamespace != null && fFrameworkNamespace.containsComponentType(type))
            return fFrameworkNamespace.getComponentSpecification(type);

        return null;
    }

}