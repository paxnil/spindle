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

package com.iw.plugins.spindle.editors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Access Tapestry Artifacts for various UI tasks.
 *  public methods are not meant to be resued. Create one when you need it, or just call the static methods.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class UITapestryAccess
{

    public static class Result
    {
        Result()
        {}

        Result(String name, String description)
        {
            this.name = name;
            this.displayName = name;
            this.description = description;
        }
        public String name;
        public String displayName;
        public String description;
        public boolean required = false;
    }

    public static Result[] getAllChildNamespaceComponents(ICoreNamespace namespace)
    {
        if (namespace == null)
            return new Result[] {};

        List result = new ArrayList();

        List libIds = namespace.getChildIds();
        for (Iterator iter = libIds.iterator(); iter.hasNext();)
        {
            String libId = (String) iter.next();
            ICoreNamespace childNamespace = (ICoreNamespace) namespace.getChildNamespace(libId);
            if (childNamespace == null)
                continue;
            List types = childNamespace.getComponentTypes();
            ComponentSpecificationResolver resolver = childNamespace.getComponentResolver();

            for (Iterator iter2 = types.iterator(); iter2.hasNext();)
            {
                String typeId = (String) iter2.next();
                IComponentSpecification component = resolver.resolve(typeId);
                result.add(
                    createComponentInformationResult(
                        libId + ":" + typeId,
                        typeId + " - from '" + libId + "'",
                        component));
            }
        }
        return (Result[]) result.toArray(new Result[result.size()]);
    }

    public static Result[] getChildNamespaceComponents(ICoreNamespace namespace, String libraryId)
    {
        if (namespace == null)
            return new Result[] {};

        List result = new ArrayList();

        INamespace childNamespace = namespace.getChildNamespace(libraryId);
        if (childNamespace == null)
            return new Result[] {};

        ILibrarySpecification childLibSpec = namespace.getSpecification();
        List types = childLibSpec.getComponentTypes();
        for (Iterator iter = types.iterator(); iter.hasNext();)
        {
            String typeId = (String) iter.next();
            IComponentSpecification component = namespace.getComponentSpecification(typeId);
            result.add(UITapestryAccess.createComponentInformationResult(typeId, typeId, component));
        }

        return (Result[]) result.toArray(new Result[result.size()]);
    }

    public static Result[] getChildNamespaceIds(ICoreNamespace namespace)
    {
        if (namespace == null)
            return new Result[] {};

        List result = new ArrayList();
        for (Iterator iter = namespace.getChildIds().iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();

            result.add(createChildNamespaceResult(id, namespace.getSpecification()));

        }
        return (Result[]) result.toArray(new Result[result.size()]);
    }

    public static UITapestryAccess.Result[] getLibraryIds(INamespace namespace)
    {
        if (namespace == null)
            return new UITapestryAccess.Result[] {};

        List result = new ArrayList();
        List libIds = namespace.getChildIds();
        for (Iterator iter = libIds.iterator(); iter.hasNext();)
        {
            String libId = (String) iter.next();
            INamespace libNamespace = namespace.getChildNamespace(libId);
            ILibrarySpecification libSpec = namespace.getSpecification();

            String description = libSpec.getDescription();
            result.add(
                new UITapestryAccess.Result(libId, description == null ? "no description available" : description));
        }
        return (UITapestryAccess.Result[]) result.toArray(new UITapestryAccess.Result[result.size()]);
    }

    private static Result createChildNamespaceResult(String libId, ILibrarySpecification namespaceSpec)
    {
        Result result = new Result(libId, null);

        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);

        pwriter.println(libId);
        pwriter.println();
        XMLUtil.writeLibrary(libId, namespaceSpec.getLibrarySpecificationPath(libId), pwriter, 0);

        result.description = swriter.toString();
        return result;
    }

    public static Result[] getComponents(ICoreNamespace framework, ICoreNamespace currentNamespace)
    {
        List result = new ArrayList();

        ILibrarySpecification libSpec = currentNamespace.getSpecification();

        List types = libSpec.getComponentTypes();

        Map applicationTypes = new HashMap();

        for (Iterator iter = types.iterator(); iter.hasNext();)
        {
            String typeId = (String) iter.next();
            IComponentSpecification component = currentNamespace.getComponentSpecification(typeId);
            applicationTypes.put(typeId, createComponentInformationResult(typeId, typeId, component));
        }

        if (framework != null)
        {
            libSpec = framework.getSpecification();
            types = libSpec.getComponentTypes();
            for (Iterator iter = types.iterator(); iter.hasNext();)
            {
                String typeId = (String) iter.next();
                if (applicationTypes.containsKey(typeId))
                    continue;
                IComponentSpecification component = framework.getComponentSpecification(typeId);
                applicationTypes.put(typeId, createComponentInformationResult(typeId, typeId, component));
            }
        }

        for (Iterator iter = applicationTypes.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entries = (Map.Entry) iter.next();
            result.add(entries.getValue());
        }

        return (Result[]) result.toArray(new Result[result.size()]);
    }

    public static Result[] findParameters(PluginComponentSpecification component, String match, Set existing)
    {
        if (component == null)
            return new Result[] {};
        List result = new ArrayList();
        Map parameterMap = component.getParameterMap();
        for (Iterator iter = parameterMap.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IParameterSpecification parameterSpec = (IParameterSpecification) parameterMap.get(name);

            if ((match != null && match.trim().length() > 0) && !name.startsWith(match))
                continue;

            if (existing != null && existing.contains(name.toLowerCase()))
                continue;

            result.add(UITapestryAccess.createParameterResult(name, parameterSpec, component.getPublicId()));
        }

        return (Result[]) result.toArray(new Result[result.size()]);
    }

    public static Result[] getContainedIds(IComponentSpecification component)
    {
        if (component == null)
            return new Result[] {};
        List result = new ArrayList();
        List ids = component.getComponentIds();
        for (Iterator iter = ids.iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();
            result.add(new Result(id, null));
        }
        return (Result[]) result.toArray(new Result[result.size()]);
    }

    public static Result createComponentInformationResult(
        String name,
        String displayName,
        IComponentSpecification componentSpec)
    {
        Result result = new Result(name, null);
        result.displayName = displayName;
        if (componentSpec != null)
        {
            StringWriter swriter = new StringWriter();
            PrintWriter pwriter = new PrintWriter(swriter);
            pwriter.println(name);
            pwriter.println();
            String description = componentSpec.getDescription();
            pwriter.println(description == null ? "no description available" : description);
            pwriter.println();
            XMLUtil.writeComponentSpecificationHeader(pwriter, componentSpec, 0);
            result.description = swriter.toString();
        }

        return result;
    }

    public static Result createParameterResult(IComponentSpecification component, String parameterName)
    {
        if (component == null)
            return null;

        IParameterSpecification parameterSpec = component.getParameter(parameterName);
        if (parameterSpec == null)
            return null;

        return createParameterResult(parameterName, parameterSpec, component.getPublicId());
    }

    public static Result createParameterResult(String name, IParameterSpecification parameterSpec, String publicId)
    {

        Result result = new Result(name, null);
        String description = parameterSpec.getDescription();

        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);
        pwriter.println(name);
        pwriter.println();
        pwriter.println(description == null ? "no description available" : description);
        pwriter.println();
        if (publicId != null)
            XMLUtil.writeParameter(name, parameterSpec, pwriter, 0, publicId);

        result.description = swriter.toString();

        if (parameterSpec.isRequired())
            result.required = true;
        return result;
    }

    protected ICoreNamespace fNamespace;
    protected ICoreNamespace fFrameworkNamespace;

    public UITapestryAccess(Editor editor) throws IllegalArgumentException
    {
        fNamespace = editor.getNamespace();
        Assert.isLegal(fNamespace != null);
        IStorage storage = (IStorage) editor.getEditorInput().getAdapter(IStorage.class);
        IProject project = TapestryCore.getDefault().getProjectFor(storage);
        setFrameworkNamespace(
            (ICoreNamespace) TapestryArtifactManager.getTapestryArtifactManager().getFrameworkNamespace(project));
    }

    public ICoreNamespace getFrameworkNamespace()
    {
        return fFrameworkNamespace;
    }

    public void setFrameworkNamespace(ICoreNamespace namespace)
    {
        fFrameworkNamespace = namespace;
    }

    /** return resuls for the current namespace **/
    public Result[] getComponents()
    {
        return getComponents(fFrameworkNamespace, fNamespace);
    }

    public Result[] getChildNamespaceComponents(String libraryId)
    {

        return getChildNamespaceComponents(fNamespace, libraryId);

    }

    public Result[] getChildNamespaceIds()
    {
        return getChildNamespaceIds(fNamespace);
    }

    public Result[] getAllChildNamespaceComponents()
    {
        return getAllChildNamespaceComponents(fNamespace);
    }

    public IComponentSpecification resolveComponentType(String type)
    {
        ComponentSpecificationResolver resolver = fNamespace.getComponentResolver();
        IComponentSpecification containedSpecification = resolver.resolve(type);
        if (fFrameworkNamespace != null && containedSpecification == null)
        {
            resolver = fFrameworkNamespace.getComponentResolver();
            containedSpecification = resolver.resolve(type);
        }
        return containedSpecification;
    }

}
