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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup;
import com.iw.plugins.spindle.core.parser.DefaultProblem;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceLocationAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.TapestryResourceLocationAcceptor;
import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.Markers;

/**
 *  Resolver for a Namespace
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class NamespaceResolver
{
    /**
     * The parser used to parse Tapestry xml files
     */
    private Parser fParser;

    /**
     *  collector for any problems not handled by the Build
     */
    private ProblemCollector fProblemCollector = new ProblemCollector();
    /**
     * information culled from the servlet - Application namespaces only
     */
    private ServletInfo fServlet;
    /** 
     * the instance of IBuild that instantiated the first Resolver 
     **/
    private Build fBuild;
    /** 
     * the result Namespace 
     **/
    private ICoreNamespace fResultNamespace;
    /** 
     * the Tapestry framwork Namespace 
     **/
    private ICoreNamespace fFrameworkNamespace;
    /** 
     * the location of the library spec that defines the 
     * Namespace being resolved 
     */
    private IResourceWorkspaceLocation fSpecLocation;
    /**
     * The id of the Namespace being resolved
     */
    private String fNamespaceId;
    /**
     * A map of all component names -> locations in
     * the Namespace being resolved
     */
    private Map fJwcFiles;

    /**
     * a stack of the components being resolved
     * its an error for a component to be in the stack more than 
     * once! If this happens, there is a circular dependency!
     */
    private Stack fComponentStack = new Stack();
    /**
     * The resolver is not threadsafe
     */
    private boolean fWorking;
    /**
     * flag to indicate that this resolver is resolving
     * the Tapestry Framework Namespace
     **/
    private boolean fResolvingFramework;

    private boolean fIsIncrementalBuild;

    /**
     * 
     * To resolve a namespace you need:
     * 
     *  Given, the framework namespace, or null if this is the framework
     *  Given, the resource location of the library spec.
     *  Given, the servlet name from web.xml (ignored if its not an application)
     * 
     *  - parse the library spec
     *  - figure out if its a library or an application
     *  - for each <pre><library></pre> tag create and run an new NamespaceResolver
     *    and install the results in this namespace as children.
     * 
     *  - need to keep track somehow of templates and thier owners.
     * 
     *  component resolve rules (application) 
     * 
     * <ul>
     *  <li>As declared in the application specification</li>
     *  <li>*.jwc in the same folder as the application specification</li>
     *  <li>* jwc in the WEB-INF/<i>servlet-name</i> directory of the context root</li>
     *  <li>*.jwc in WEB-INF</li>
     *  <li>*.jwc in the application root (within the context root)</li>
     * </ul>
     * 
     * component resolve rules (library)
     * <ul>
     *   <li>As declared in the library specification</li>
     *   <li>*.jwc in the same folder as the library specification</li>
     * </ul>
     * 
     * page resolve rules (application namespace):
     * 
     * <ul>        
     *  <li>As declared in the application specification</li>
     *  <li>*.page in the same folder as the application specification</li>
     *  <li>*.page page in the WEB-INF/<i>servlet-name</i> directory of the context root</li>
     *  <li>*.page in WEB-INF</li>
     *  <li>*.page in the application root (within the context root)</li>
     *  <li>*.html as a template in the application root</li>
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
     */
    public NamespaceResolver(Build build, Parser parser)
    {
        this(build, parser, false);
    }

    NamespaceResolver(Build build, Parser parser, boolean isIncrementalBuild)
    {
        super();
        fBuild = build;
        fIsIncrementalBuild = isIncrementalBuild;
        fParser = parser;
    }

    public ICoreNamespace resolveFrameworkNamespace()
    {

        IResourceWorkspaceLocation frameworkLocation =
            (IResourceWorkspaceLocation) fBuild.fTapestryBuilder.fClasspathRoot.getRelativeLocation(
                "/org/apache/tapestry/Framework.library");

        if (fIsIncrementalBuild && fBuild.fLastState.fBinaryNamespaces.containsKey(frameworkLocation))
            return (ICoreNamespace) fBuild.fLastState.fBinaryNamespaces.get(frameworkLocation);

        resolve(null, ICoreNamespace.FRAMEWORK_NAMESPACE, frameworkLocation);
        return fResultNamespace;
    }

    public ICoreNamespace resolveApplicationNamespace(ICoreNamespace framework, ServletInfo servlet)
    {
        reset();
        resolve(framework, servlet);
        return fResultNamespace;
    }

    public ICoreNamespace resolveLibrary(
        ICoreNamespace framework,
        String libraryId,
        IResourceWorkspaceLocation location)
    {
        if (fIsIncrementalBuild && fBuild.fLastState.fBinaryNamespaces.containsKey(location))
            return (ICoreNamespace) fBuild.fLastState.fBinaryNamespaces.get(location);

        reset();
        resolve(framework, libraryId, location);
        return fResultNamespace;
    }

    protected ICoreNamespace resolve(ICoreNamespace framework, String namespaceId, IResourceWorkspaceLocation location)
    {
        this.fFrameworkNamespace = framework;
        this.fNamespaceId = namespaceId;
        this.fSpecLocation = location;
        fResultNamespace = fBuild.createNamespace(fParser, fNamespaceId, fSpecLocation);
        doResolve();
        return fResultNamespace;
    }

    protected ICoreNamespace resolve(ICoreNamespace framework, ServletInfo servlet)
    {
        this.fFrameworkNamespace = framework;
        this.fServlet = servlet;
        fSpecLocation = servlet.applicationSpecLocation;
        if (fSpecLocation != null)
        {
            if (!fSpecLocation.exists())
                throw new BuilderException(
                    TapestryCore.getString("build-failed-missing-application-spec", fSpecLocation.toString()));

            fResultNamespace = fBuild.createNamespace(fParser, fNamespaceId, fSpecLocation);
        } else
        {
            fResultNamespace = createStandinApplicationNamespace(servlet);
            fSpecLocation = (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();
        }
        if (fResultNamespace != null)
        {
            ILibrarySpecification spec = fResultNamespace.getSpecification();
            for (Iterator iter = servlet.parameters.keySet().iterator(); iter.hasNext();)
            {
                String key = (String) iter.next();
                spec.setProperty(key, (String) servlet.parameters.get(key));
            }
            doResolve();
        }
        return fResultNamespace;
    }

    protected ICoreNamespace createStandinApplicationNamespace(ServletInfo servlet)
    {

        PluginApplicationSpecification applicationSpec = new PluginApplicationSpecification();
        IResourceLocation virtualLocation = fBuild.fTapestryBuilder.fContextRoot.getRelativeLocation("/WEB-INF/");
        applicationSpec.setSpecificationLocation(virtualLocation);
        applicationSpec.setName(servlet.name);

        CoreNamespace result = new CoreNamespace(null, applicationSpec);

        return result;
    }

    protected void reset()
    {
        fComponentStack.clear();
        fFrameworkNamespace = null;
        fNamespaceId = null;
        fSpecLocation = null;
        fServlet = null;
        fResolvingFramework = false;
        fWorking = false;
        fJwcFiles = null;
        fProblemCollector.reset();

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
            if (fSpecLocation != null)
            {}
            if (fResultNamespace != null)
            {
                //                fResultNamespace.getComponentLookup(fFrameworkNamespace);
                //                fResultNamespace.getPageLookup(fFrameworkNamespace);
                if (fServlet != null)
                    fResultNamespace.setAppNameFromWebXML(fServlet.name);

                NamespaceResourceLookup lookup = new NamespaceResourceLookup();
                if (fResultNamespace.isApplicationNamespace())
                {
                    lookup.configure(
                        (PluginApplicationSpecification) fResultNamespace.getSpecification(),
                        fBuild.fTapestryBuilder.fContextRoot,
                        fServlet.name);
                } else
                {
                    lookup.configure((PluginLibrarySpecification) fResultNamespace.getSpecification());
                }

                fResultNamespace.setResourceLookup(lookup);

                //set a special component resolver that will prompt recusive component/page resolution                
                fResultNamespace.setComponentResolver(new BuilderComponentResolver(fFrameworkNamespace));

                // Special case! can't resolve children of the framework
                // until the framework is resolved!
                if (!fResolvingFramework)
                    resolveChildNamespaces();

                resolveComponents();
                Set definitelyNotSpeclessPages = getAllComponentTemplates();
                resolvePages(definitelyNotSpeclessPages);

                // now we can resolve the child libraries
                // of the framework
                if (fResolvingFramework)
                {
                    fFrameworkNamespace = fResultNamespace;
                    resolveChildNamespaces();
                }

                //replace the special resolver with the normal one.               
                fResultNamespace.setComponentResolver(
                    new ComponentSpecificationResolver(fFrameworkNamespace, fResultNamespace));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            fWorking = false;
        }

    }

    /**
     * @return List a list of all the templates for all components in this Namespace
     */
    private Set getAllComponentTemplates()
    {
        Set result = new HashSet();
        for (Iterator iter = fResultNamespace.getComponentTypes().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            PluginComponentSpecification spec =
                (PluginComponentSpecification) fResultNamespace.getComponentSpecification(type);
            result.addAll(spec.getTemplateLocations());
        }
        return result;
    }

    /**
     * @return List a list of all the templates for all page files in this Namespace
     */
    private Set getAllPageFileTemplates()
    {
        Set result = new HashSet();
        for (Iterator iter = fResultNamespace.getPageNames().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            PluginComponentSpecification spec =
                (PluginComponentSpecification) fResultNamespace.getPageSpecification(name);
            result.addAll(spec.getTemplateLocations());
        }
        return result;
    }

    /**
     * resolve/build all the child namespaces declared in the library specification
     */
    private void resolveChildNamespaces()
    {

        ILibrarySpecification spec = fResultNamespace.getSpecification();
        List ids = spec.getLibraryIds();
        if (!ids.isEmpty())
        {
            NamespaceResolver childResolver = new NamespaceResolver(fBuild, new Parser(false), fIsIncrementalBuild);
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

                //                                IResourceWorkspaceLocation libLocation =
                //                                    (IResourceWorkspaceLocation) fBuild.fTapestryBuilder.fClasspathRoot.getRelativeLocation(
                //                                        spec.getLibrarySpecificationPath(libraryId));
                if (libLocation.exists())
                {
                    ICoreNamespace childNamespace =
                        childResolver.resolveLibrary(fFrameworkNamespace, libraryId, libLocation);

                    if (childNamespace != null)
                        fResultNamespace.installChildNamespace(libraryId, childNamespace);
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
    private void resolveComponents()
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
    private IComponentSpecification resolveComponent(String name, IResourceWorkspaceLocation location)
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
        result = fBuild.resolveIComponentSpecification(fParser, fResultNamespace, location);
        if (result != null)
        {
            fResultNamespace.installComponentSpecification(name, result);
            fBuild.parseTemplates((PluginComponentSpecification) result);
        }
        fComponentStack.pop();
        return result;
    }

    /**
     * build an error message or circular component references
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

    private Map getAllJWCFilesForNamespace()
    {
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();

        Map result = new HashMap();
        ILibrarySpecification spec = fResultNamespace.getSpecification();

        // pull the ones that are defined in the spec.
        for (Iterator iter = spec.getComponentTypes().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            IResourceLocation specLoc = location.getRelativeLocation(spec.getComponentSpecificationPath(type));
            result.put(type, specLoc);
        }

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
                            0)});
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
                                0)});
            }
        }
        return result;
    }

    /**
     * Resolve all the pages in the Namespace.
     * 1st Step resolve all the .page files.
     * 2nd Step resolve all the spec-less pages.
     */
    private void resolvePages(Set componentTemplates)
    {
        Map dotPageFiles = getAllPageFilesForNamespace();
        for (Iterator iter = dotPageFiles.keySet().iterator(); iter.hasNext();)
        {
            String name = (String) iter.next();
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) dotPageFiles.get(name);
            resolvePageFile(name, location);
        }
        resolveSpeclessPages(componentTemplates);

    }

    protected void resolveSpeclessPages(Set componentTemplates)
    {
        if (!fResultNamespace.isApplicationNamespace())
            return;

        //now gather all the templates seen so far.
        //they are definitely not spec-less pages!
        final List allTemplates = new ArrayList(componentTemplates);
        allTemplates.addAll(getAllPageFileTemplates());
        final List speclessPages = new ArrayList();
        final String seek_extension = getTemplateExtension();

        //now find all the html files in the application root

        IResourceWorkspaceLocation appRoot = fBuild.fTapestryBuilder.fContextRoot;

        IResourceLocationAcceptor acceptor = new IResourceLocationAcceptor()
        {
            public boolean accept(IResourceWorkspaceLocation location)
            {
                String fullname = location.getName();
                String name = null;
                String extension = null;

                if (fullname != null)
                {
                    int cut = fullname.lastIndexOf('.');
                    if (cut < 0)
                    {
                        name = fullname;
                    } else if (cut == 0)
                    {
                        extension = fullname;
                    } else
                    {
                        name = fullname.substring(0, cut);
                        extension = fullname.substring(cut + 1);
                    }
                    if (seek_extension.equals(extension) && !allTemplates.contains(location))
                        speclessPages.add(location);
                }
                return true;
            }

            // not used
            public IResourceWorkspaceLocation[] getResults()
            {
                IResourceWorkspaceLocation[] result = new IResourceWorkspaceLocation[speclessPages.size()];
                return (IResourceWorkspaceLocation[]) speclessPages.toArray(result);
            }
        };

        try
        {
            appRoot.lookup(acceptor);
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        }

        // need to filter out localized page templates. They will be picked up
        // again later.
        List filtered = TemplateFinder.filterTemplateList(speclessPages, fResultNamespace);
        for (Iterator iter = filtered.iterator(); iter.hasNext();)
        {
            IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) iter.next();
            resolveSpeclessPage(location);
        }
    }

    private String getTemplateExtension()
    {
        String result = fResultNamespace.getSpecification().getProperty(Tapestry.TEMPLATE_EXTENSION_PROPERTY);
        if (result == null)
            return Tapestry.DEFAULT_TEMPLATE_EXTENSION;
        return result;
    }

    private void resolveSpeclessPage(IResourceWorkspaceLocation location)
    {
        PluginComponentSpecification specification = new PluginComponentSpecification();
        specification.setPageSpecification(true);
        specification.setSpecificationLocation(location);
        specification.setNamespace(fResultNamespace);

        ComponentScanner scanner = new ComponentScanner();
        scanner.scanForTemplates(specification);

        List templates = specification.getTemplateLocations();

        String name = location.getName();
        int dotx = name.lastIndexOf('.');
        if (dotx > 0)
        {
            name = name.substring(0, dotx);
        }
        fResultNamespace.installPageSpecification(name, specification);
        fBuild.parseTemplates(specification);

        fBuild.fBuildQueue.finished(templates);
    }

    /**
     * resolve a single .page file
     * 
     * There could be recursive calls to resolveComponent() downstream from this method
     * But this method will never be called recursively.
     */
    private IComponentSpecification resolvePageFile(String name, IResourceWorkspaceLocation location)
    {
        IComponentSpecification result = fResultNamespace.getComponentSpecification(name);
        if (result != null || location == null)
            return result;

        result = null;

        result = fBuild.resolveIComponentSpecification(fParser, fResultNamespace, location);
        if (result != null)
        {
            fResultNamespace.installPageSpecification(name, result);
            fBuild.parseTemplates((PluginComponentSpecification) result);
            //((PluginComponentSpecification) result).setNamespace(fResultNamespace);
            //fBuild.parseTemplates((PluginComponentSpecification) result);
        }
        return result;
    }

    class BuilderComponentResolver extends ComponentSpecificationResolver
    {

        public BuilderComponentResolver(INamespace framework)
        {
            super(framework, fResultNamespace);
        }

        //        /* (non-Javadoc)
        //         * @see com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver#resolve(org.apache.tapestry.INamespace, java.lang.String)
        //         */
        //        public IComponentSpecification resolve(String type)
        //        {
        //            IComponentSpecification result = null;
        //            if (type.indexOf(':') < 0)
        //            {
        //                result = fContainerNamespace.getComponentSpecification(type);
        //                if (result == null && fJwcFiles.containsKey(type))
        //                {
        //                    result = resolveComponent(type, (IResourceWorkspaceLocation) fJwcFiles.get(type));
        //                } else
        //                {
        //                    result = super.resolve(type);
        //                }
        //
        //            } else
        //            {
        //                result = super.resolve(type);
        //            }
        //            return result;
        //        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver#resolve(org.apache.tapestry.INamespace, java.lang.String, java.lang.String)
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

    class ProblemCollector implements IProblemCollector
    {
        private List problems = new ArrayList();

        public void reset()
        {
            problems.clear();
        }

        public void addProblem(int severity, ISourceLocation location, String message)
        {
            addProblem(
                new DefaultProblem(
                    ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
                    severity,
                    message,
                    location.getLineNumber(),
                    location.getCharStart(),
                    location.getCharEnd()));
        }

        public void addProblem(IProblem problem)
        {
            problems.add(problem);
        }

        public IProblem[] getProblems()
        {
            return (IProblem[]) problems.toArray(new IProblem[problems.size()]);
        }

    }

    /**
     *  This inner class wraps the Namespace being resolved.
     *  Needed so that Components are resolved as needed recursively.
     * 
     *  @see TemporaryNamespace.getComponentSpecification();
     */
    //    public class TemporaryNamespace implements ICoreNamespace
    //    {
    //
    //        /* (non-Javadoc)
    //        * @see org.apache.tapestry.INamespace#constructQualifiedName(java.lang.String)
    //        */
    //        public String constructQualifiedName(String name)
    //        {
    //            return fResultNamespace.constructQualifiedName(name);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#containsComponentType(java.lang.String)
    //         */
    //        public boolean containsComponentType(String type)
    //        {
    //            return fResultNamespace.containsComponentType(type);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#containsPage(java.lang.String)
    //         */
    //        public boolean containsPage(String name)
    //        {
    //            return fResultNamespace.containsPage(name);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallChildNamespace(java.lang.String)
    //         */
    //        public INamespace deinstallChildNamespace(String id)
    //        {
    //            return fResultNamespace.deinstallChildNamespace(id);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallComponentSpecification(java.lang.String)
    //         */
    //        public IComponentSpecification deinstallComponentSpecification(String type)
    //        {
    //            return fResultNamespace.deinstallComponentSpecification(type);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#deinstallPageSpecification(java.lang.String)
    //         */
    //        public IComponentSpecification deinstallPageSpecification(String pageName)
    //        {
    //            return fResultNamespace.deinstallPageSpecification(pageName);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getChildIds()
    //         */
    //        public List getChildIds()
    //        {
    //            return fResultNamespace.getChildIds();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getChildNamespace(java.lang.String)
    //         */
    //        public INamespace getChildNamespace(String id)
    //        {
    //            return fResultNamespace.getChildNamespace(id);
    //        }
    //
    //        /* 
    //         * Callers of this method should catch RuntimeException (for now)
    //         * which is an indication of a circular dependency!
    //         * 
    //         * (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getComponentSpecification(java.lang.String)
    //         */
    //        public IComponentSpecification getComponentSpecification(String type)
    //        {}
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getComponentTypes()
    //         */
    //        public List getComponentTypes()
    //        {
    //            return fResultNamespace.getComponentTypes();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getExtendedId()
    //         */
    //        public String getExtendedId()
    //        {
    //            return fResultNamespace.getExtendedId();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getId()
    //         */
    //        public String getId()
    //        {
    //            return fResultNamespace.getId();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.ILocatable#getLocation()
    //         */
    //        public ILocation getLocation()
    //        {
    //            return fResultNamespace.getLocation();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getNamespaceId()
    //         */
    //        public String getNamespaceId()
    //        {
    //            return fResultNamespace.getId();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getPageNames()
    //         */
    //        public List getPageNames()
    //        {
    //            return fResultNamespace.getPageNames();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getPageSpecification(java.lang.String)
    //         */
    //        public IComponentSpecification getPageSpecification(String pageName)
    //        {
    //            IComponentSpecification specification = getPageLookup(fFrameworkNamespace).lookupSpecification(pageName);
    //            return specification;
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getParentNamespace()
    //         */
    //        public INamespace getParentNamespace()
    //        {
    //            return fResultNamespace.getParentNamespace();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getServiceClassName(java.lang.String)
    //         */
    //        public String getServiceClassName(String id)
    //        {
    //            return fResultNamespace.getServiceClassName(id);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getServiceNames()
    //         */
    //        public List getServiceNames()
    //        {
    //            return fResultNamespace.getServiceNames();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getSpecification()
    //         */
    //        public ILibrarySpecification getSpecification()
    //        {
    //            return fResultNamespace.getSpecification();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#getSpecificationLocation()
    //         */
    //        public IResourceLocation getSpecificationLocation()
    //        {
    //            return fResultNamespace.getSpecificationLocation();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#installChildNamespace(org.apache.tapestry.INamespace)
    //         */
    //        public void installChildNamespace(String id, INamespace child)
    //        {
    //            fResultNamespace.installChildNamespace(id, child);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#installComponentSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
    //         */
    //        public void installComponentSpecification(String type, IComponentSpecification spec)
    //        {
    //            fResultNamespace.installComponentSpecification(type, spec);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#installPageSpecification(java.lang.String, org.apache.tapestry.spec.IComponentSpecification)
    //         */
    //        public void installPageSpecification(String pageName, IComponentSpecification spec)
    //        {
    //            fResultNamespace.installPageSpecification(pageName, spec);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.apache.tapestry.INamespace#isApplicationNamespace()
    //         */
    //        public boolean isApplicationNamespace()
    //        {
    //
    //            return fResultNamespace.isApplicationNamespace();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setParentNamespace(com.iw.plugins.spindle.core.namespace.ICoreNamespace)
    //         */
    //        public void setParentNamespace(ICoreNamespace parent)
    //        {
    //            fResultNamespace.setParentNamespace(parent);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getComponentLookup(com.iw.plugins.spindle.core.namespace.ICoreNamespace)
    //         */
    //        public ComponentLookup getComponentLookup(ICoreNamespace framework)
    //        {
    //            return fResultNamespace.getComponentLookup(framework);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getPageLookup(com.iw.plugins.spindle.core.namespace.ICoreNamespace)
    //         */
    //        public PageLookup getPageLookup(ICoreNamespace framework)
    //        {
    //            return fResultNamespace.getPageLookup(framework);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setAppNameFromWebXML(java.lang.String)
    //         */
    //        public void setAppNameFromWebXML(String name)
    //        {
    //            fResultNamespace.setAppNameFromWebXML(name);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setResourceLookup(com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup)
    //         */
    //        public void setResourceLookup(NamespaceResourceLookup lookup)
    //        {
    //            fResultNamespace.setResourceLookup(lookup);
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getResourceLookup()
    //         */
    //        public NamespaceResourceLookup getResourceLookup()
    //        {
    //            return fResultNamespace.getResourceLookup();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#getComponentResolver()
    //         */
    //        public ComponentSpecificationResolver getComponentResolver()
    //        {
    //
    //            return fResultNamespace.getComponentResolver();
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see com.iw.plugins.spindle.core.namespace.ICoreNamespace#setComponentResolver(com.iw.plugins.spindle.core.namespace.ComponentSpecificationResolver)
    //         */
    //        public void setComponentResolver(ComponentSpecificationResolver resolver)
    //        {
    //            fResultNamespace.setComponentResolver(resolver);
    //        }
    //
    //    }
}
