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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup;
import com.iw.plugins.spindle.core.namespace.PageSpecificationResolver;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.TapestryResourceLocationAcceptor;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.Markers;

/**
  *  Resolver for a Namespace
 * 
 * 
 * To resolve a namespace you need:
 * 
 *  Given, the framework namespace, or null if this is the framework
 *  Given, the resource location of the library spec.
 * 
 * component resolve rules (library)
 * <ul>
 *   <li>As declared in the library specification</li>
 *   <li>*.jwc in the same folder as the library specification</li>
 * </ul>
 * 
 *  page resolve rules (ordinary library)
 * 
 *  <ul>        
 *    <li>As declared in the library specification</li>
 *    <li>*.page in the same folder as the library specification</li>
 * </ul>
 * 
 *  template lookup rules:
 * 
 * <ul>
 *  <li>If the component has a $template asset, use that</li>
 *  <li>Look for a template in the same folder as the component</li>
 *  <li>If a page in the application namespace, search in the application root</li>
 * </ul>
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class NamespaceResolver
{
    /**
     * The parser used to parse Tapestry xml files
     */
    protected Parser fParser;

    /**
     *  collector for any problems not handled by the Build
     */
    //    private ProblemCollector fProblemCollector = new ProblemCollector();
    /** 
     * the instance of IBuild that instantiated the first Resolver 
     **/
    protected Build fBuild;
    /** 
     * the result Namespace 
     **/
    protected ICoreNamespace fResultNamespace;
    /** 
     * the Tapestry framwork Namespace 
     **/
    protected ICoreNamespace fFrameworkNamespace;
    /** 
     * the location of the library spec that defines the 
     * Namespace being resolved 
     */
    protected IResourceWorkspaceLocation fNamespaceSpecLocation;
    /**
     * The id of the Namespace being resolved
     */
    protected String fNamespaceId;
    /**
     * A map of all component names -> locations in
     * the Namespace being resolved
     */
    protected Map fJwcFiles;

    /**
     * a stack of the components being resolved
     * its an error for a component to be in the stack more than 
     * once! If this happens, there is a circular dependency!
     */
    protected Stack fComponentStack = new Stack();
    /**
     * The resolver is not threadsafe
     */
    protected boolean fWorking;
    /**
     * flag to indicate that this resolver is resolving
     * the Tapestry Framework Namespace
     **/

    public NamespaceResolver(Build build, Parser parser)
    {
        super();
        fBuild = build;
        fParser = parser;
    }

    /**
     * Invoke the resolve. If the namespace can't be resolved, problems have already been added.
     * 
     * @return the resolved namespage or null if it couldn't be parsed.
     */
    public abstract ICoreNamespace resolve();

    protected ICoreNamespace resolve(String namespaceId, IResourceWorkspaceLocation location)
    {
        this.fNamespaceId = namespaceId;
        this.fNamespaceSpecLocation = location;
        ICoreNamespace prebuilt = fBuild.getPreBuiltNamespace(fNamespaceSpecLocation);
        if (prebuilt != null)
        {
            // this can only happen in an incremental build!
            fResultNamespace = prebuilt;
        } else
        {
            fResultNamespace =
                fBuild.createNamespace(
                    fParser,
                    fNamespaceId,
                    fNamespaceSpecLocation.getStorage(),
                    fNamespaceSpecLocation,
                    null);
            doResolve();
        }
        return fResultNamespace;
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
            //            fProblemCollector.beginCollecting();
            if (fResultNamespace == null)
                throw new RuntimeException("Null namespace!");

            IResourceWorkspaceLocation specLocation =
                (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();

            NamespaceResourceLookup lookup = create();

            fResultNamespace.setResourceLookup(lookup);

            //set a special component resolver that will prompt recursive component/page resolution                
            fResultNamespace.setComponentResolver(new BuilderComponentResolver(fFrameworkNamespace));

            //no special page resolver needed
            fResultNamespace.setPageResolver(new PageSpecificationResolver(fFrameworkNamespace, fResultNamespace));

            // do any work needed before we go ahead and resolve the pages and components
            // for libraries other than the framework, child libraries are resolved here.
            namespaceConfigured();

            //resolve the pages/components in the Namespace
            resolveNamespaceContents();

            //now that we have resolved the pages/components, we need to replace the special ComponentResolver
            //with a regular one. Also gives subclasses the opportunity to do some final adjustments.
            namespaceResolved();

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            fWorking = false;
        }

    }

    /**
     *  Code that actually finds and resolves all the libraries, pages and components declared in this
     *  Namespace.
     */
    protected void resolveNamespaceContents()
    {
        resolveComponents();
        resolvePages();
    }

    /**
     *  Called during a doResolve to setup the Namespace when it has been configures but just before 
     *  child libraries, pages, and components are resolved.
     */
    protected void namespaceConfigured()
    {
        resolveChildNamespaces();
    }

    /**
     *  Called in doResolve() right after the Namespace has been resolved.
     *  Usually this just entails setting the ComponentResolver for the Namespace. 
     */
    protected void namespaceResolved()
    {
        fResultNamespace.setComponentResolver(
            new ComponentSpecificationResolver(fFrameworkNamespace, fResultNamespace));
    }

    /**
     *  Every namespace has a Namespace resource lookup object for finding files
     * according to Tapestry lookup rules.
     * @return a properly configured instance of NamespaceResourceLookup
     */
    protected NamespaceResourceLookup create()
    {
        NamespaceResourceLookup lookup = new NamespaceResourceLookup();
        lookup.configure((PluginLibrarySpecification) fResultNamespace.getSpecification());
        return lookup;
    }

    /**
     * @return List a list of all the templates for all page files in this Namespace
     */
    protected Set getAllPageFileTemplates()
    {
        Set result = new HashSet();
        List pageNames = fResultNamespace.getPageNames();
        int count = pageNames.size();
        for (int i = 0; i < count; i++)
        {
            PluginComponentSpecification spec =
                (PluginComponentSpecification) fResultNamespace.getPageSpecification((String) pageNames.get(i));

            result.addAll(spec.getTemplateLocations());
        }
        //        for (Iterator iter = fResultNamespace.getPageNames().iterator(); iter.hasNext();)
        //        {
        //            String name = (String) iter.next();
        //
        //            PluginComponentSpecification spec =
        //                (PluginComponentSpecification) fResultNamespace.getPageSpecification(name);
        //
        //            result.addAll(spec.getTemplateLocations());
        //        }
        return result;
    }

    /**
     * resolve/build all the child namespaces declared in the library/application specification
     */
    protected void resolveChildNamespaces()
    {

        ILibrarySpecification spec = fResultNamespace.getSpecification();
        List ids = spec.getLibraryIds();
        if (!ids.isEmpty())
        {
            for (Iterator iter = ids.iterator(); iter.hasNext();)
            {
                String libraryId = (String) iter.next();

                if (fResultNamespace.getChildNamespace(libraryId) != null)
                    continue;

                IResourceWorkspaceLocation namespaceLocation =
                    (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();

                IResourceWorkspaceLocation libLocation;

                if (namespaceLocation.isOnClasspath())
                    libLocation =
                        (IResourceWorkspaceLocation) namespaceLocation.getRelativeLocation(
                            spec.getLibrarySpecificationPath(libraryId));
                else
                    libLocation =
                        (IResourceWorkspaceLocation) fBuild.fTapestryBuilder.fClasspathRoot.getRelativeLocation(
                            spec.getLibrarySpecificationPath(libraryId));
                            
                if (libLocation.getStorage() != null)
                {
                    NamespaceResolver childResolver =
                        new LibraryResolver(
                            fBuild,
                            fParser,
                            fFrameworkNamespace,
                            fResultNamespace,
                            libraryId,
                            libLocation);

                    ICoreNamespace childNamespace = childResolver.resolve();

                } else if (fBuild.fTapestryBuilder.DEBUG)
                {
                    System.out.println("not found:" + libLocation);
                }
            }
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
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) fJwcFiles.get(name);
            resolveComponent(name, location);
        }
    }

    /**
     * resolve a single component
     * 
     * as this method is called recursively, we keep a stack recording the components being resolved.
     * If we are asked to resolve a component that is already on the stack, we throw a runtime exception
     * as this is an indication that there is a circular dependancy between components.
     */
    protected IComponentSpecification resolveComponent(String name, IResourceWorkspaceLocation location)
    {

        IComponentSpecification result = fResultNamespace.getComponentSpecification(name);

        if (result != null || location == null)
            return result;

        result = null;

        if (fComponentStack.contains(location))
        {
            throw new BuilderException(
                TapestryCore.getString("build-failed-circular-component-reference", getCircularErrorMessage(location)));
        }

        fComponentStack.push(location);

        result =
            fBuild.resolveIComponentSpecification(fParser, fResultNamespace, location.getStorage(), location, null);

        if (result != null)
        {
            fResultNamespace.installComponentSpecification(name, result);
            fBuild.parseTemplates((PluginComponentSpecification) result);
        }
        fComponentStack.pop();
        return result;
    }

    /**
     * build an error message for circular component references
     * @param location
     * @return
     */
    private String getCircularErrorMessage(IResourceWorkspaceLocation location)
    {
        List result = new ArrayList();
        result.add(location);
        Stack clone = (Stack) fComponentStack.clone();

        IResourceWorkspaceLocation sloc = (IResourceWorkspaceLocation) clone.pop();

        if (sloc.equals(location))
            return location.toString() + " refers to itself";

        result.add(sloc);
        while (!sloc.equals(location))
        {

            result.add(sloc);
            sloc = (IResourceWorkspaceLocation) clone.pop();
        }

        return result.toString();

    }

    /**
     *  Scan the namespace looking for all jwc files that are in allowed places.
     *  
     * @return a Map of Component Type name -> location
     */
    private Map getAllJWCFilesForNamespace()
    {
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();

        Map result = new HashMap();
        ILibrarySpecification spec = fResultNamespace.getSpecification();

        // pull the ones that are defined in the spec.
        List cTypes = spec.getComponentTypes();
        int count = cTypes.size();
        for (int i = 0; i < count; i++)
        {
            String type = (String) cTypes.get(i);
            IResourceLocation specLoc = location.getRelativeLocation(spec.getComponentSpecificationPath(type));
            result.put(type, specLoc);
        }
        //        for (Iterator iter = spec.getComponentTypes().iterator(); iter.hasNext();)
        //        {
        //            String type = (String) iter.next();
        //            IResourceLocation specLoc = location.getRelativeLocation(spec.getComponentSpecificationPath(type));
        //            result.put(type, specLoc);
        //        }

        TapestryResourceLocationAcceptor acceptor =
            new TapestryResourceLocationAcceptor("*", false, TapestryResourceLocationAcceptor.ACCEPT_JWC);
        IResourceWorkspaceLocation[] jwcs = fResultNamespace.getResourceLookup().lookup(acceptor);

        // remaining typed by thier filename
        for (int i = 0; i < jwcs.length; i++)
        {
            String type = new Path(jwcs[i].getName()).removeFileExtension().toString();
            if (!result.containsKey(type))
            {
                result.put(type, jwcs[i]);

            } else
            {

                Markers.recordProblems(
                    jwcs[i],
                    new IProblem[] {
                         new DefaultProblem(
                            Markers.TAPESTRY_MARKER_TAG,
                            IProblem.ERROR,
                            TapestryCore.getString("builder-hidden-jwc-file", jwcs[i], result.get(type)),
                            1,
                            0,
                            0,
                            false)});
            }
        }
        return result;
    }

    /**     
     * @return Map a map of the names and file locations of all the .page files for the Namespace
     */
    private Map getAllPageFilesForNamespace()
    {
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();

        Map result = new HashMap();
        ILibrarySpecification spec = fResultNamespace.getSpecification();

        // pull the ones that are defined in the spec.
        // They are named as defined in the spec.
        for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            IResourceLocation specLoc = location.getRelativeLocation(spec.getPageSpecificationPath(type));
            result.put(type, specLoc);
        }

        TapestryResourceLocationAcceptor acceptor =
            new TapestryResourceLocationAcceptor("*", false, TapestryResourceLocationAcceptor.ACCEPT_PAGE);
        IResourceWorkspaceLocation[] pages = fResultNamespace.getResourceLookup().lookup(acceptor);

        // remaining named by thier filename
        for (int i = 0; i < pages.length; i++)
        {
            String name = new Path(pages[i].getName()).removeFileExtension().toString();
            if (!result.containsKey(name))
            {
                result.put(name, pages[i]);

            } else
            {
                if (!result.get(name).equals(pages[i]))
                    Markers.recordProblems(
                        pages[i],
                        new IProblem[] {
                             new DefaultProblem(
                                Markers.TAPESTRY_MARKER_TAG,
                                IProblem.ERROR,
                                TapestryCore.getString("builder-hidden-page-file", pages[i], result.get(name)),
                                1,
                                0,
                                0,
                                false)});
            }
        }
        return result;
    }

    /**
     *  Resolve all the .page files in the namespace
     */
    protected void resolvePages()
    {
        Map dotPageFiles = getAllPageFilesForNamespace();
        for (Iterator iter = dotPageFiles.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) dotPageFiles.get(name);
            resolvePageFile(name, location);
        }
    }

    /**
     *  Library and Application Specs can specify what file extension to use instead of the default "html"
     * @return the specified extension or the Tapestry default extension if none is defined.
     */
    protected String getTemplateExtension()
    {
        String result = fResultNamespace.getSpecification().getProperty(Tapestry.TEMPLATE_EXTENSION_PROPERTY);
        if (result == null)
            return Tapestry.DEFAULT_TEMPLATE_EXTENSION;
        return result;
    }

    /**
     * resolve a single .page file
     * 
     * There could be recursive calls to resolveComponent() downstream from this method
     * But this method will never be called recursively.
     */
    protected IComponentSpecification resolvePageFile(String name, IResourceWorkspaceLocation location)
    {
        IComponentSpecification result = fResultNamespace.getPageSpecification(name);
        if (result != null || location == null)
            return result;

        result = null;

        result =
            fBuild.resolveIComponentSpecification(fParser, fResultNamespace, location.getStorage(), location, null);
        if (result != null)
        {
            fResultNamespace.installPageSpecification(name, result);
            fBuild.parseTemplates((PluginComponentSpecification) result);
        }
        return result;
    }

    class BuilderComponentResolver extends ComponentSpecificationResolver
    {

        public BuilderComponentResolver(INamespace framework)
        {
            super(framework, fResultNamespace);
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver#resolve(java.lang.String, java.lang.String)
         */
        public IComponentSpecification resolve(String libraryId, String type)
        {
            if (libraryId != null && !libraryId.equals(fContainerNamespace.getId()))
                return super.resolve(libraryId, type);

            IComponentSpecification result = null;
            result = fContainerNamespace.getComponentSpecification(type);
            if (result == null && fJwcFiles.containsKey(type))
            {
                result = resolveComponent(type, (IResourceWorkspaceLocation) fJwcFiles.get(type));
            }

            if (result == null)
                result = resolveInFramework(type);

            return result;
        }

    }
    //TODO remove
    //    class ProblemCollector implements IProblemCollector
    //    {
    //        private List problems = new ArrayList();
    //
    //        private void reset()
    //        {
    //            problems.clear();
    //        }
    //
    //        public void addProblem(int severity, ISourceLocation location, String message)
    //        {
    //            addProblem(ITapestryMarker.TAPESTRY_PROBLEM_MARKER, severity, location, message);
    //        }
    //
    //        private void addProblem(String type, int severity, ISourceLocation location, String message)
    //        {
    //            addProblem(
    //                new DefaultProblem(
    //                    type,
    //                    severity,
    //                    message,
    //                    location.getLineNumber(),
    //                    location.getCharStart(),
    //                    location.getCharEnd(),
    //                    false));
    //        }
    //
    //        public void addProblem(IProblem problem)
    //        {
    //            problems.add(problem);
    //        }
    //
    //        public IProblem[] getProblems()
    //        {
    //            return (IProblem[]) problems.toArray(new IProblem[problems.size()]);
    //        }
    //
    //        public void beginCollecting()
    //        {
    //            reset();
    //        }
    //
    //        public void endCollecting()
    //        {}
    //
    //    }

}
