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

package core.resources.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.Resource;
import org.apache.tapestry.services.TemplateSource;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.messages.ImplMessages;

import core.ITapestryProject;
import core.PicassoMigration;
import core.resources.I18NResourceAcceptor;
import core.resources.ICoreResource;
import core.source.IProblem;
import core.source.IProblemCollector;
import core.source.ISourceLocation;
import core.source.ISourceLocationInfo;
import core.spec.PluginComponentSpecification;
import core.util.XMLPublicIDUtil;

/**
 * A utility class used to find all the templates for a component
 * 
 * @author glongman@gmail.com
 */
public class TemplateFinder
{

    /**
     * Filter a list of template locations. Use the extension defined in the library spec, or the
     * default (html) Exclude any localized names. Klunky imlementation - will have to do until I
     * figure out a regular expression
     * 
     * @param locations
     * @param namespace
     * @return List a list with any non templates or localized templates removed.
     */
    public static List<Resource> filterTemplateList(List<Resource> locations, String expectedTemplateExtension)
    {
        List<Resource> result = new ArrayList<Resource>();

        for (Iterator iter = locations.iterator(); iter.hasNext();)
        {
            ICoreResource location = (ICoreResource) iter.next();
            String name = location.getName();
            String foundName = null;
            String foundExtension = null;
            int dotx = name.lastIndexOf('.');
            if (dotx > 0)
            {
                foundName = name.substring(0, dotx);
                foundExtension = name.substring(dotx + 1);
                if (foundName.length() == 0 || !expectedTemplateExtension.equals(foundExtension))
                    continue;
            }
            boolean ok = true;
            for (int i = 0; i < I18NResourceAcceptor.ALL_I18N_SUFFIXES.length; i++)
            {
                if (foundName.endsWith(I18NResourceAcceptor.ALL_I18N_SUFFIXES[i]))
                {
                    ok = false;
                    break;
                }
            }
            if (ok)
                result.add(location);

        }
        return result;
    }

    private ArrayList<ICoreResource> fFindResults = new ArrayList<ICoreResource>();

    private String fExtension;

    private IProblemCollector fProblemCollector;

    private ITapestryProject fTapestryProject;

    private I18NResourceAcceptor fAcceptor = new I18NResourceAcceptor();

    private String fTemplateBaseName;

    /**
     * Obtain all the template locations for a component specification
     * 
     * @param specification the IComponentSpecification we want to find templates for
     * @param project the Tapestry project containing the component specF
     * @param collector an IProblemCollector to collect any problems encountered.
     * @return an array of ICoreResource - the template locations.
     */
    public static ICoreResource[] scanForTemplates(IComponentSpecification specification,
            String templateExtension, ITapestryProject project, IProblemCollector collector)
    {
        TemplateFinder finder = new TemplateFinder();
            
        return finder.getTemplates(specification, project, templateExtension, collector);
    }

    protected ICoreResource[] getTemplates(IComponentSpecification specification,
            ITapestryProject project, String templateExtension, IProblemCollector collector)

    {
        fTapestryProject = project;
        fExtension = templateExtension;
        fProblemCollector = collector;
        fFindResults.clear();
        findTemplates((PluginComponentSpecification) specification);
        return (ICoreResource[]) fFindResults.toArray(new ICoreResource[fFindResults.size()]);
    }

    /**
     * template lookup rules:
     * <ul>
     * <li>If the component has a $template asset, use that</li>
     * <li>Look for a template in the same folder as the component</li>
     * <li>If a page in the application namespace, search in the application root</li>
     * </ul>
     * 
     * @param specification
     * @throws CoreException
     */
    private void findTemplates(PluginComponentSpecification specification)
    {
        IAssetSpecification templateAsset = specification
                .getAsset(TemplateSource.TEMPLATE_ASSET_NAME);

        if (templateAsset != null)
            readTemplatesFromAsset(specification, templateAsset);

        String name = specification.getSpecificationLocation().getName();
        int dotx = name.lastIndexOf('.');
        fTemplateBaseName = name.substring(0, dotx);

        findStandardTemplates(specification);

        if ((fFindResults.isEmpty()) && specification.isPageSpecification()
                && specification.getNamespace().isApplicationNamespace())
        {
            findPageTemplateInApplicationRoot(specification);
        }
    }

    private void readTemplatesFromAsset(PluginComponentSpecification specification,
            IAssetSpecification templateAsset)
    {
        String path = templateAsset.getPath();
        ICoreResource templateLocation = null;
        boolean isDTD_4_0 = XMLPublicIDUtil.getDTDVersion(specification.getPublicId()) == XMLPublicIDUtil.DTD_4_0;

        int colonx = path.indexOf(':');
        if (colonx < 0)
        {
            addProblem(
                    IProblem.WARNING,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getTagNameLocation(),
                    "Spindle can't resolve templates from external assets",
                    IProblem.NOT_QUICK_FIXABLE);
            return;
        }

        int assetType = PicassoMigration.getAssetType(path.substring(0, colonx));
        String truePath = path.substring(colonx + 1);
        switch (assetType)
        {
            case PicassoMigration.CONTEXT_ASSET:

                ICoreResource contextRoot = (ICoreResource) fTapestryProject
                        .getWebContextLocation();
                if (contextRoot != null)
                    templateLocation = (ICoreResource) contextRoot.getRelativeResource(truePath);

                if (templateLocation == null || !templateLocation.exists())
                {
                    ISourceLocation attributeLocation = getAssetSourceLocation(
                            (ISourceLocationInfo) templateAsset.getLocation(),
                            assetType,
                            isDTD_4_0);
                    addProblem(IProblem.ERROR, attributeLocation, ImplMessages
                            .unableToReadTemplate(truePath), IProblem.NOT_QUICK_FIXABLE);
                    return;
                }
                break;
            case PicassoMigration.CLASSPATH_ASSET:

                ICoreResource cpRoot = (ICoreResource) fTapestryProject.getClasspathRoot();
                if (cpRoot != null)
                    templateLocation = (ICoreResource) cpRoot.getRelativeResource(truePath);

                if (templateLocation == null || !templateLocation.exists())
                {
                    ISourceLocation attributeLocation = getAssetSourceLocation(
                            (ISourceLocationInfo) templateAsset.getLocation(),
                            assetType,
                            isDTD_4_0);
                    addProblem(IProblem.ERROR, attributeLocation, ImplMessages
                            .unableToReadTemplate(truePath), IProblem.NOT_QUICK_FIXABLE);
                    return;
                }

        }
        if (templateLocation != null)
            fFindResults.add(templateLocation);

    }

    private ISourceLocation getAssetSourceLocation(ISourceLocationInfo sourceInfo, int assetType,
            boolean isDTD_4_0)
    {
        if (isDTD_4_0)
            return sourceInfo.getAttributeSourceLocation("path");

        switch (assetType)
        {
            case PicassoMigration.CONTEXT_ASSET:
                return sourceInfo.getAttributeSourceLocation("path");

            case PicassoMigration.CLASSPATH_ASSET:
                return sourceInfo.getAttributeSourceLocation("resource-path");

            default:
                return sourceInfo.getAttributeSourceLocation("URL");
        }
    }

    private void findStandardTemplates(PluginComponentSpecification specification)

    {
        find((ICoreResource) specification.getSpecificationLocation());
    }

    private void findPageTemplateInApplicationRoot(PluginComponentSpecification specification)

    {
        find((ICoreResource) fTapestryProject.getWebContextLocation());
    }

    private void find(ICoreResource location)
    {
        // need to ensure the base template exists.
        // if it does we look for localized versions of it!
        ICoreResource baseLocation = (ICoreResource) location.getRelativeResource(fTemplateBaseName
                + "." + fExtension);
        if (baseLocation.exists())
        {
            fAcceptor.configure(fTemplateBaseName, fExtension);
            location.lookup(fAcceptor);
            fFindResults.addAll(Arrays.asList(fAcceptor.getResults()));
        }
    }

    private void addProblem(int severity, ISourceLocation location, String message, int code)
    {
        if (fProblemCollector != null)
            fProblemCollector.addProblem(severity, location, message, true, code);

    }
    
//    public ICoreResource[] getResults()
//    {
//        ICoreResource[] results = new ICoreResource[fFindResults.size()];
//        return (ICoreResource[]) fFindResults.toArray(results);
//    }

}