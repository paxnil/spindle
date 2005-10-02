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

package com.iw.plugins.spindle.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.hivemind.Resource;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.PicassoMigration;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup;
import com.iw.plugins.spindle.core.namespace.PageSpecificationResolver;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.PathUtils;
import com.iw.plugins.spindle.core.resources.TapestryResourceLocationAcceptor;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.SourceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;

/**
 * Resolver for a Namespace To resolve a namespace you need: Given, the framework namespace, or null
 * if this is the framework 
 * <ul>
 * <li>As declared in the library specification</li>
 * <li>*.jwc in the same folder as the library specification</li>
 * </ul>
 * page resolve rules (ordinary library)
 * <ul>
 * <li>As declared in the library specification</li>
 * <li>*.page in the same folder as the library specification</li>
 * </ul>
 * template lookup rules:
 * <ul>
 * <li>If the component has a $template asset, use that</li>
 * <li>Look for a template in the same folder as the component</li>
 * <li>If a page in the application namespace, search in the application root</li>
 * </ul>
 * 
 * @author glongman@gmail.com
 */
public class NamespaceResolver
{
    /**
     * collector for any problems not handled by the AbstractBuild
     */
    // private ProblemCollector fProblemCollector = new ProblemCollector();
    /**
     * the instance of IBuild that instantiated the first Resolver
     */
    protected AbstractBuild fBuild;

    /**
     * the result Namespace
     */
    protected CoreNamespace fNamespace;

    protected IPropertySource fResultNamespacePropertySource;

    protected String fNamespaceTemplateExtension;

    /**
     * the Tapestry framwork Namespace
     */
    protected ICoreNamespace fFrameworkNamespace;

    /**
     * the location of the library spec that defines the Namespace being resolved
     */
    protected ICoreResource fNamespaceSpecLocation;

    /**
     * The id of the Namespace being resolved
     */
    protected String fNamespaceId;

    /**
     * A map of all component names -> locations in the Namespace being resolved
     */
    protected Map fJwcFiles;

    /**
     * a stack of the components being resolved its an error for a component to be in the stack more
     * than once! If this happens, there is a circular dependency!
     */
    protected Stack fComponentStack = new Stack();

    /**
     * The resolver is not threadsafe
     */
    protected boolean fWorking;

    /**
     * flag to indicate that this resolver is resolving the Tapestry Framework Namespace
     */

    public NamespaceResolver(AbstractBuild build)
    {
        super();
        fBuild = build;
    }

    public NamespaceResolver(AbstractBuild build, ICoreNamespace framework)
    {
        this(build);
        fFrameworkNamespace = framework;
    }

    public void resolve(CoreNamespace namespace)
    {
        fNamespace = namespace;
        doResolve();
        fNamespaceTemplateExtension = namespace.getPropertyValue(
                "org.apache.tapestry.template-extension");       
    }

    protected void cleanup()
    {
        fComponentStack.clear();
        fFrameworkNamespace = null;
        fNamespaceId = null;
        fNamespaceSpecLocation = null;
        fWorking = false;
        fJwcFiles = null;
    }

    protected void doResolve()
    {
        if (fWorking)
        {
            throw new RuntimeException("can't call resolve while resolving!");
        }

        try
        {
            fWorking = true;
            fComponentStack.clear();
            // fProblemCollector.beginCollecting();
            if (fNamespace == null)
                throw new RuntimeException("Null namespace!");

            NamespaceResourceLookup lookup = create();

            fNamespace.setResourceLookup(lookup);

            // set a special component resolver that will prompt recursive
            // component/page resolution
            fNamespace.setComponentResolver(new BuilderComponentResolver(fFrameworkNamespace));

            // no special page resolver needed
            fNamespace.setPageResolver(new PageSpecificationResolver(fFrameworkNamespace,
                    fNamespace));

            // do any work needed before we go ahead and resolve the pages and
            // components
            // for libraries other than the framework, child libraries are resolved
            // here.
            namespaceConfigured();

            // resolve the pages/components in the Namespace
            resolveNamespaceContents();

            // now that we have resolved the pages/components, we need to replace the
            // special ComponentResolver
            // with a regular one. Also gives subclasses the opportunity to do some
            // final adjustments.
            namespaceResolved();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            fWorking = false;
        }

    }

    /**
     * Code that actually finds and resolves all the libraries, pages and components declared in
     * this Namespace.
     */
    protected void resolveNamespaceContents()
    {
        resolveComponents();
        resolvePages();
    }

    /**
     * Called during a doResolve to setup the Namespace when it has been configures but before any
     * of the contents are resolved.
     */
    protected void namespaceConfigured()
    {
        resolveChildNamespaces();
    }

    /**
     * Called in doResolve() right after the Namespace has been resolved. Usually this just entails
     * setting the ComponentResolver for the Namespace.
     */
    protected void namespaceResolved()
    {
        fNamespace.setComponentResolver(new ComponentSpecificationResolver(fFrameworkNamespace,
                fNamespace));
    }

    /**
     * Every namespace has a Namespace resource lookup object for finding files according to
     * Tapestry lookup rules.
     * 
     * @return a properly configured instance of NamespaceResourceLookup
     */
    protected NamespaceResourceLookup create()
    {
        NamespaceResourceLookup lookup = new NamespaceResourceLookup();
        lookup.configure((PluginLibrarySpecification) fNamespace.getSpecification());
        return lookup;
    }

    /**
     * @return List a list of all the templates for all page files in this Namespace
     */
    protected Set getAllPageFileTemplates()
    {
        Set result = new HashSet();
        List pageNames = fNamespace.getPageNames();
        int count = pageNames.size();
        for (int i = 0; i < count; i++)
        {
            PluginComponentSpecification spec = (PluginComponentSpecification) fNamespace
                    .getPageSpecification((String) pageNames.get(i));

            result.addAll(spec.getTemplateLocations());
        }
        return result;
    }

    /**
     * resolve/build all the child namespaces declared in the library/application specification
     */
    protected void resolveChildNamespaces()
    {
        for (Iterator iter = fNamespace.getChildIds().iterator(); iter.hasNext();)
        {
            String childId = (String) iter.next();
            CoreNamespace nsChild = (CoreNamespace) fNamespace.getChildNamespace(childId);
            if (nsChild == null)
                continue;
            new NamespaceResolver(fBuild, fFrameworkNamespace).resolve(nsChild);
        }
    }

    /**
     * resolve all the components declared in the spec (or found in the appropriate locations!)
     */
    protected void resolveComponents()
    {
        fJwcFiles = getAllJWCFilesForNamespace();

        for (Iterator iter = fJwcFiles.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            ICoreResource location = (ICoreResource) fJwcFiles.get(name);
            resolveComponent(name, location);
        }
    }

    /**
     * resolve a single component. As this method is called recursively, we keep a stack recording
     * the components being resolved. If we are asked to resolve a component that is already on the
     * stack, we throw a runtime exception as this is an indication that there is a circular
     * dependancy between components.
     */
    protected IComponentSpecification resolveComponent(String name, ICoreResource location)
    {
        IComponentSpecification result = fNamespace.getComponentSpecification(name);

        if (result != null || location == null)
            return result;

        result = null;

        if (fComponentStack.contains(location))
        {
            throw new BuilderException(CoreMessages.format(
                    "build-failed-circular-component-reference",
                    getCircularErrorMessage(location)));
        }

        fComponentStack.push(location);

        result = fBuild.parseComponentSpecification(
                fNamespace,
                location,
                fNamespaceTemplateExtension,
                null);

        if (result != null)
        {
            fNamespace.installComponentSpecification(name, result);
            fBuild.parseTemplates((PluginComponentSpecification) result);
        }
        fComponentStack.pop();
        return result;
    }

    /**
     * build an error message for circular component references
     * 
     * @param location
     * @return
     */
    private String getCircularErrorMessage(ICoreResource location)
    {
        List result = new ArrayList();
        result.add(location);
        Stack clone = (Stack) fComponentStack.clone();

        ICoreResource sloc = (ICoreResource) clone.pop();

        if (sloc.equals(location))
            return location.toString() + " refers to itself";

        result.add(sloc);
        while (!sloc.equals(location))
        {

            result.add(sloc);
            sloc = (ICoreResource) clone.pop();
        }

        return result.toString();

    }

    /**
     * Scan the namespace looking for all jwc files that are in allowed places.
     * 
     * @return a Map of Component Type name -> location
     */
    private Map getAllJWCFilesForNamespace()
    {
        ICoreResource location = (ICoreResource) fNamespace.getSpecificationLocation();

        Map result = new HashMap();
        ILibrarySpecification spec = fNamespace.getSpecification();

        // pull the ones that are defined in the spec.
        List cTypes = spec.getComponentTypes();
        int count = cTypes.size();
        for (int i = 0; i < count; i++)
        {
            String type = (String) cTypes.get(i);
            Resource specLoc = location.getRelativeResource(spec
                    .getComponentSpecificationPath(type));
            result.put(type, specLoc);
        }

        TapestryResourceLocationAcceptor acceptor = new TapestryResourceLocationAcceptor("*",
                false, TapestryResourceLocationAcceptor.ACCEPT_JWC);
        Resource[] jwcs = fNamespace.getResourceLookup().lookup(acceptor);

        // remaining typed by thier filename
        for (int i = 0; i < jwcs.length; i++)
        {
            String type = new PathUtils(jwcs[i].getName()).removeFileExtension().toString();
            if (!result.containsKey(type))
                result.put(type, jwcs[i]);
            else if (!jwcs[i].equals(result.get(type)))

                fBuild.fInfrastructure.fProblemPersister.recordProblem(jwcs[i], new DefaultProblem(
                        IProblem.ERROR, CoreMessages.format(
                                "builder-hidden-jwc-file",
                                jwcs[i],
                                result.get(type)), SourceLocation.FILE_LOCATION, false,
                        IProblem.SPINDLE_BUILDER_HIDDEN_JWC_FILE));

        }
        return result;
    }

    /**
     * @return Map a map of the names and file locations of all the .page files for the Namespace
     */
    private Map getAllPageFilesForNamespace()
    {
        ICoreResource location = (ICoreResource) fNamespace.getSpecificationLocation();

        Map result = new HashMap();
        ILibrarySpecification spec = fNamespace.getSpecification();

        // pull the ones that are defined in the spec.
        // They are named as defined in the spec.
        for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            Resource specLoc = location.getRelativeResource(spec.getPageSpecificationPath(type));
            result.put(type, specLoc);
        }

        TapestryResourceLocationAcceptor acceptor = new TapestryResourceLocationAcceptor("*",
                false, TapestryResourceLocationAcceptor.ACCEPT_PAGE);
        Resource[] pages = fNamespace.getResourceLookup().lookup(acceptor);

        // remaining named by thier filename
        for (int i = 0; i < pages.length; i++)
        {
            String name = new PathUtils(pages[i].getName()).removeFileExtension().toString();
            if (!result.containsKey(name))
            {
                result.put(name, pages[i]);

            }
            else if (!result.get(name).equals(pages[i]))
                fBuild.fInfrastructure.fProblemPersister.recordProblem(
                        pages[i],
                        new DefaultProblem(IProblem.ERROR, CoreMessages.format(
                                "builder-hidden-page-file",
                                pages[i],
                                result.get(name)), SourceLocation.FILE_LOCATION, false,
                                IProblem.SPINDLE_BUILDER_HIDDEN_PAGE_FILE));

        }
        return result;
    }

    /**
     * Resolve all the .page files in the namespace
     */
    protected void resolvePages()
    {
        Map dotPageFiles = getAllPageFilesForNamespace();
        for (Iterator iter = dotPageFiles.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            ICoreResource location = (ICoreResource) dotPageFiles.get(name);
            resolvePageFile(name, location);
        }
    }

    /**
     * Library and Application Specs can specify what file extension to use instead of the default
     * "html"
     * 
     * @return the specified extension or the Tapestry default extension if none is defined.
     */
    protected String getTemplateExtension()
    {
        String result = fNamespace.getSpecification().getProperty(
                Tapestry.TEMPLATE_EXTENSION_PROPERTY);
        if (result == null)
            return PicassoMigration.DEFAULT_TEMPLATE_EXTENSION;
        return result;
    }

    /**
     * resolve a single .page file There could be recursive calls to resolveComponent() downstream
     * from this method But this method will never be called recursively.
     */
    protected IComponentSpecification resolvePageFile(String name, ICoreResource location)
    {
        IComponentSpecification result = fNamespace.getPageSpecification(name);
        if (result != null || location == null)
            return result;

        result = null;

        result = fBuild.parseComponentSpecification(
                fNamespace,
                location,
                fNamespaceTemplateExtension,
                null);

        if (result != null)
        {
            fNamespace.installPageSpecification(name, result);
            fBuild.parseTemplates((PluginComponentSpecification) result);
        }
        return result;
    }

    class BuilderComponentResolver extends ComponentSpecificationResolver
    {

        public BuilderComponentResolver(INamespace framework)
        {
            super(framework, fNamespace);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver#resolve(java.lang.String,
         *      java.lang.String)
         */
        public IComponentSpecification resolve(String libraryId, String type)
        {
            if (libraryId != null && !libraryId.equals(fContainerNamespace.getId()))
                return super.resolve(libraryId, type);

            IComponentSpecification result = null;
            result = fContainerNamespace.getComponentSpecification(type);

            if (result == null && fJwcFiles.containsKey(type))
                result = resolveComponent(type, (ICoreResource) fJwcFiles.get(type));

            if (result == null)
                result = resolveInFramework(type);

            return result;
        }

    }

}