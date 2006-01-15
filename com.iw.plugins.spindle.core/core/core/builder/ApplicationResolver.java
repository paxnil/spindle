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

package core.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.hivemind.Resource;


import core.TapestryCore;
import core.namespace.ICoreNamespace;
import core.namespace.NamespaceResourceLookup;
import core.properties.DefaultProperties;
import core.resources.ICoreResource;
import core.resources.IResourceAcceptor;
import core.resources.IResourceRoot;
import core.resources.templates.TemplateFinder;
import core.spec.PluginApplicationSpecification;
import core.spec.PluginComponentSpecification;

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
    protected String servletName;

    private Set fDefinitelyNotSpeclessPages;

    /**
     * @param build
     * @param parser
     */
    public ApplicationResolver(AbstractBuild build, ICoreNamespace framework, String servletName)
    {
        super(build);
        frameworkNamespace = framework;       
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
                (PluginApplicationSpecification) namespace.getSpecification(),
                build.contextRoot,
                servletName);

        return lookup;
    }

   
    /**
     * We adjust here to account for specless pages.
     * 
     * @see core.builder.NamespaceResolver#resolveNamespaceContents()
     */
    protected void resolveNamespaceContents()
    {
        try
        {
            resolveComponents();
            fDefinitelyNotSpeclessPages = getAllComponentTemplates();
            resolvePages();
        } catch (Throwable e) {
            e.printStackTrace();
            TapestryCore.log(e);
        }
        finally
        {
            fDefinitelyNotSpeclessPages = null;
        }
    }

    /**
     * Adjusted to account for specless pages.
     * 
     * @see core.builder.NamespaceResolver#resolvePages()
     */
    protected void resolvePages()
    {
        super.resolvePages();
        resolveSpeclessPages(fDefinitelyNotSpeclessPages);
    }

    /**
     * Resolve all of the specless pages in the application. Only the application namespace can have specless
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
        if (!namespace.isApplicationNamespace())
            return;

        //now gather all the templates seen so far.
        //they are definitely not spec-less pages!
        
        //we know that the application NS and the global properties will provide the
        //template extension for specless pages.
        
        String templateExtension = namespace.getSpecification().getProperty("org.apache.tapestry.template-extension");
        if (templateExtension == null) {
            templateExtension = DefaultProperties.getInstance().getPropertyValue("org.apache.tapestry.template-extension");
        }
        
        final List allTemplates = new ArrayList(componentTemplates);
        allTemplates.addAll(getAllPageSpecTemplates());
        final List speclessPages = new ArrayList();
        final String speclessPageTemplateExtension = templateExtension;
        //now find all the html files in the application root

        IResourceRoot appRoot = build.contextRoot;

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
                    if (speclessPageTemplateExtension.equals(extension)
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
                templateExtension);
        for (Iterator iter = filtered.iterator(); iter.hasNext();)
        {
            ICoreResource location = (ICoreResource) iter.next();
            resolveSpeclessPage(location, templateExtension);
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
        specification.setNamespace(namespace);

        specification.setTemplateLocations(TemplateFinder.scanForTemplates(
                specification,
                templateExtension,
                build.tapestryProject,
                null));

        String name = location.getName();
        int dotx = name.lastIndexOf('.');
        if (dotx > 0)
            name = name.substring(0, dotx);

        namespace.installPageSpecification(name, specification);
        build.parseTemplates(specification);
        build.templateExtensionSeen(templateExtension);
        build.buildQueue.finished(specification.getTemplateLocations());
    }

    /**
     * @return List a list of all the templates for all components in this Namespace (that have specs)
     */
    protected Set<Resource> getAllComponentTemplates()
    {
        Set<Resource> result = new HashSet<Resource>();
        for (Iterator iter = namespace.getComponentTypes().iterator(); iter.hasNext();)
        {
            String type = (String) iter.next();
            PluginComponentSpecification spec = (PluginComponentSpecification) namespace
                    .getComponentSpecification(type);
            result.addAll(spec.getTemplateLocations());
        }
        return result;
    }

}