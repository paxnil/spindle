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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.CoreNamespace;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.resources.IResourceLocationAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

/**
 *  Namespace resolver for applications
 *  Given, the framework namespace,
 *  Given, the servlet name from web.xml
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
* @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ApplicationResolver extends NamespaceResolver
{

    /**
     * information culled from the servlet - Application namespaces only
     */
    protected ServletInfo fServlet;
    private Set fDefinitelyNotSpeclessPages;

    /**
     * @param build
     * @param parser
     */
    public ApplicationResolver(Build build, Parser parser, ICoreNamespace framework, ServletInfo servlet)
    {
        super(build, parser);
        fFrameworkNamespace = framework;
        fServlet = servlet;
    }

    public ICoreNamespace resolve()
    {
        try
        {

            fNamespaceSpecLocation = fServlet.applicationSpecLocation;
            if (fNamespaceSpecLocation != null)
            {
                if (!fNamespaceSpecLocation.exists())
                    throw new BuilderException(
                        TapestryCore.getString(
                            "build-failed-missing-application-spec",
                            fNamespaceSpecLocation.toString()));

                fResultNamespace = fBuild.createNamespace(fParser, fNamespaceId, fNamespaceSpecLocation, null);
            } else
            {
                fResultNamespace = createStandinApplicationNamespace(fServlet);
                fNamespaceSpecLocation = (IResourceWorkspaceLocation) fResultNamespace.getSpecificationLocation();
            }
            if (fResultNamespace != null)
            {
                fResultNamespace.setAppNameFromWebXML(fServlet.name);
                ILibrarySpecification spec = fResultNamespace.getSpecification();
                for (Iterator iter = fServlet.parameters.keySet().iterator(); iter.hasNext();)
                {
                    String key = (String) iter.next();
                    spec.setProperty(key, (String) fServlet.parameters.get(key));
                }
                doResolve();
            }
            return fResultNamespace;
        } finally
        {
            cleanup();
        }
    }

    /**
        *  Every namespace has a Namespace resource lookup object for finding files
        * according to Tapestry lookup rules. Here we configure for the application lookup rules!
        * @return a properly configured instance of NamespaceResourceLookup
        */
    protected NamespaceResourceLookup create()
    {
        NamespaceResourceLookup lookup = new NamespaceResourceLookup();
        lookup.configure(
            (PluginApplicationSpecification) fResultNamespace.getSpecification(),
            fBuild.fTapestryBuilder.fContextRoot,
            fServlet.name);

        return lookup;
    }

    /**
     *  Tapestry allows there to be no explicit application file. In this case we use a standin application spec.
     * 
     * @param servlet the info culled from web.xml
     * @return a namespace rooted in WEB-INF.
     */
    protected ICoreNamespace createStandinApplicationNamespace(ServletInfo servlet)
    {

        PluginApplicationSpecification applicationSpec = new PluginApplicationSpecification();
        IResourceLocation virtualLocation = fBuild.fTapestryBuilder.fContextRoot.getRelativeLocation("/WEB-INF/");
        applicationSpec.setSpecificationLocation(virtualLocation);
        applicationSpec.setName(servlet.name);

        CoreNamespace result = new CoreNamespace(null, applicationSpec);

        return result;
    }

    /**
     *  We adjust here to account for specless pages.
     * 
     * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#resolveNamespaceContents()
     */
    protected void resolveNamespaceContents()
    {
        try
        {
            resolveComponents();
            fDefinitelyNotSpeclessPages = getAllComponentTemplates();
            resolvePages();
        } finally
        {
            fDefinitelyNotSpeclessPages = null;
        }
    }

    /**
     *  Adjusted to account for specless pages.
     * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#resolvePages()
     */
    protected void resolvePages()
    {
        // TODO Auto-generated method stub
        super.resolvePages();
        resolveSpeclessPages(fDefinitelyNotSpeclessPages);
    }

    /**
     *  Resolve all of the specless pages in the application.
     *  Only applications can have specless pages.
     * 
     * We find the specless pages by a process of elimination.
     * By definition a specless page is a template file located in the context root.
     * However, pages and components can have thier templates in the context root.
     * So we find the specless page templates by excluding those files we know are
     * page/component templates found in the context root.
     * 
     * @param componentTemplates a Set of template files we already know can't be specless pages.
     */
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
                //                IResourceWorkspaceLocation[] result = new IResourceWorkspaceLocation[speclessPages.size()];
                //                return (IResourceWorkspaceLocation[]) speclessPages.toArray(result);
                return null;
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

    /**
      * Resolve a specless page found at a location.
      * Recall that a specless page is a template file located in the context root folder.
      * 
      * @param location the location object for the template file.
      */
    protected void resolveSpeclessPage(IResourceWorkspaceLocation location)
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
     * @return List a list of all the templates for all components in this Namespace
     */
    protected Set getAllComponentTemplates()
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

}
