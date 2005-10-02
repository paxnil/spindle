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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.namespace.NamespaceResourceLookup;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.resources.IResourceAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceRoot;
import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

/**
 * Namespace resolver for applications Given, the framework namespace, Given, the servlet name from
 * web.xml component resolve rules (application)
 * <ul>
 * <li>As declared in the application specification</li>
 * <li>*.jwc in the same folder as the application specification</li>
 * <li>* jwc in the WEB-INF/ <i>servlet-name </i> directory of the context root</li>
 * <li>*.jwc in WEB-INF</li>
 * <li>*.jwc in the application root (within the context root)</li>
 * </ul>
 * page resolve rules (application namespace):
 * <ul>
 * <li>As declared in the application specification</li>
 * <li>*.page in the same folder as the application specification</li>
 * <li>*.page page in the WEB-INF/ <i>servlet-name </i> directory of the context root</li>
 * <li>*.page in WEB-INF</li>
 * <li>*.page in the application root (within the context root)</li>
 * <li>*.html as a template in the application root</li>
 * </ul>
 * 
 * @author glongman@gmail.com
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
    public ApplicationResolver(AbstractBuild build, ICoreNamespace framework)
    {
        super(build);
        fFrameworkNamespace = framework;       
    }    

    /**
     * Every namespace has a Namespace resource lookup object for finding files according to
     * Tapestry lookup rules. Here we configure for the application lookup rules!
     * 
     * @return a properly configured instance of NamespaceResourceLookup
     */
    protected NamespaceResourceLookup create()
    {
        NamespaceResourceLookup lookup = new NamespaceResourceLookup();
        lookup.configure(
                (PluginApplicationSpecification) fNamespace.getSpecification(),
                fBuild.fInfrastructure.fContextRoot,
                fServlet.name);

        return lookup;
    }

   
    /**
     * We adjust here to account for specless pages.
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
        }
        finally
        {
            fDefinitelyNotSpeclessPages = null;
        }
    }

    /**
     * Adjusted to account for specless pages.
     * 
     * @see com.iw.plugins.spindle.core.builder.NamespaceResolver#resolvePages()
     */
    protected void resolvePages()
    {
        super.resolvePages();
        resolveSpeclessPages(fDefinitelyNotSpeclessPages);
    }

    /**
     * Resolve all of the specless pages in the application. Only applications can have specless
     * pages. We find the specless pages by a process of elimination. By definition a specless page
     * is a template file located in the context root. However, pages and components can have thier
     * templates in the context root. So we find the specless page templates by excluding those
     * files we know are page/component templates found in the context root.
     * 
     * @param componentTemplates
     *            a Set of template files we already know can't be specless pages.
     */
    protected void resolveSpeclessPages(Set componentTemplates)
    {
        if (!fNamespace.isApplicationNamespace())
            return;

        //now gather all the templates seen so far.
        //they are definitely not spec-less pages!
        final List allTemplates = new ArrayList(componentTemplates);
        allTemplates.addAll(getAllPageFileTemplates());
        final List speclessPages = new ArrayList();
        //now find all the html files in the application root

        IResourceRoot appRoot = fBuild.fInfrastructure.fContextRoot;

        IResourceAcceptor acceptor = new IResourceAcceptor()
        {
            public boolean accept(ICoreResource location)
            {
                String fullname = location.getName();              
                String extension = null;

                if (fullname != null)
                {
                    int cut = fullname.lastIndexOf('.');
                    if (cut == 0)
                    {
                        extension = fullname;
                    }
                    else if (cut > 0)
                    {
                        
                        extension = fullname.substring(cut + 1);
                    } 
                    if (fNamespaceTemplateExtension.equals(extension)
                            && !allTemplates.contains(location))
                        speclessPages.add(location);
                }
                return true;
            }

            // not used
            public ICoreResource[] getResults()
            {
                return null;
            }
        };

        appRoot.lookup(acceptor);

        // need to filter out localized page templates. They will be picked up
        // again later.

        List filtered = TemplateFinder.filterTemplateList(
                speclessPages,
                fNamespaceTemplateExtension);
        for (Iterator iter = filtered.iterator(); iter.hasNext();)
        {
            ICoreResource location = (ICoreResource) iter.next();
            resolveSpeclessPage(location, fNamespaceTemplateExtension);
        }
    }

    /**
     * Resolve a specless page found at a location. Recall that a specless page is a template file
     * located in the context root folder.
     * 
     * @param location
     *            the location object for the template file.
     */
    protected void resolveSpeclessPage(ICoreResource location, String templateExtension)
    {
        PluginComponentSpecification specification = new PluginComponentSpecification();
        specification.setPageSpecification(true);
        specification.setSpecificationLocation(location);
        specification.setNamespace(fNamespace);

        specification.setTemplateLocations(TemplateFinder.scanForTemplates(
                specification,
                templateExtension,
                fBuild.fInfrastructure.fTapestryProject,
                null));

        String name = location.getName();
        int dotx = name.lastIndexOf('.');
        if (dotx > 0)
            name = name.substring(0, dotx);

        fNamespace.installPageSpecification(name, specification);
        fBuild.parseTemplates(specification);

        fBuild.fBuildQueue.finished(specification.getTemplateLocations());
    }

    /**
     * @return List a list of all the templates for all components in this Namespace
     */
    protected Set getAllComponentTemplates()
    {
        Set result = new HashSet();
        for (Iterator iter = fNamespace.getComponentTypes().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            PluginComponentSpecification spec = (PluginComponentSpecification) fNamespace
                    .getComponentSpecification(type);
            result.addAll(spec.getTemplateLocations());
        }
        return result;
    }

}