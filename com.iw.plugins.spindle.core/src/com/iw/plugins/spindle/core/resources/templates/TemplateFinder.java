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

package com.iw.plugins.spindle.core.resources.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.resources.IResourceLocationAcceptor;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;

/**
 *  A utility class used to find all the templates for a component
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TemplateFinder implements IResourceLocationAcceptor
{

    public static Perl5Util Perl;
    public static final String PatternPrefix = "/^";
    public static String PatternSuffix;

    static {
        Locale[] all = Locale.getAvailableLocales();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < all.length; i++)
        {
            buffer.append("_" + all[i].toString());
            if (i < all.length - 1)
            {
                buffer.append('|');
            }
        }

        PatternSuffix = "(" + buffer.toString() + "){0,1}$/i";
        Perl = new Perl5Util(new PatternCacheLRU(100));
    }

    private List fFindResults = new ArrayList();
    private String fExtension;
    private IProblemCollector fProblemCollector;
    private TapestryProject fTapestryProject;

    private String fTemplateBaseName;
    private String fPerlExpression;

    public IResourceWorkspaceLocation[] getTemplates(
        TapestryProject tproject,
        IComponentSpecification specification,
        IProblemCollector collector)
        throws CoreException
    {
        this.fTapestryProject = tproject;
        this.fProblemCollector = collector;
        fFindResults.clear();
        findTemplates((PluginComponentSpecification) specification);
        return (IResourceWorkspaceLocation[]) fFindResults.toArray(new IResourceWorkspaceLocation[fFindResults.size()]);
    }

    private void findTemplates(PluginComponentSpecification specification) throws CoreException
    {
        IAssetSpecification templateAsset = specification.getAsset(ITemplateSource.TEMPLATE_ASSET_NAME);

        if (templateAsset != null)
            readTemplatesFromAsset(specification, templateAsset);

        fExtension = getTemplateExtension(specification);
        String name = specification.getSpecificationLocation().getName();
        int dotx = name.lastIndexOf('.');
        fTemplateBaseName = name.substring(0, dotx + 1);

        findStandardTemplates(specification);

        if (fFindResults.isEmpty()
            && specification.isPageSpecification()
            && specification.getNamespace().isApplicationNamespace())
        {
            findPageTemplateInApplicationRoot(specification);
        }
    }

    /**
     * @param specification
     * @return
     */
    private String getTemplateExtension(PluginComponentSpecification specification)
    {
        String extension = specification.getProperty(Tapestry.TEMPLATE_EXTENSION_PROPERTY);
        if (extension != null)
            return extension;

        extension = specification.getNamespace().getSpecification().getProperty(Tapestry.TEMPLATE_EXTENSION_PROPERTY);
        if (extension != null)
            return extension;

        return Tapestry.DEFAULT_TEMPLATE_EXTENSION;
    }

    private void readTemplatesFromAsset(PluginComponentSpecification specification, IAssetSpecification templateAsset)
    {
        AssetType type = templateAsset.getType();
        String templatePath = templateAsset.getPath();
        IResourceWorkspaceLocation templateLocation = null;
        if (type == AssetType.EXTERNAL)
        {
            addProblem(
                IProblem.WARNING,
                ((ISourceLocationInfo) templateAsset.getLocation()).getStartTagSourceLocation(),
                "Spindle can't resolve templates from external assets");
            return;
        }
        if (type == AssetType.CONTEXT)
        {
            if (fTapestryProject.getProjectType() != TapestryProject.APPLICATION_PROJECT_TYPE)
            {
                addProblem(
                    IProblem.WARNING,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getStartTagSourceLocation(),
                    "Spindle can't resolve templates from context assets in Library projects");
                return;
            }
            IResourceWorkspaceLocation contextRoot =
                (IResourceWorkspaceLocation) fTapestryProject.getWebContextLocation();

            if (contextRoot != null)
                templateLocation = (IResourceWorkspaceLocation) contextRoot.getRelativeLocation(templatePath);

            if (templateLocation == null || !templateLocation.exists())
            {
                addProblem(
                    IProblem.ERROR,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getAttributeSourceLocation("path"),
                    TapestryCore.getTapestryString("DefaultTemplateSource.unable-to-read-template", templatePath));
                return;
            }
        }
        if (type == AssetType.PRIVATE)
        {
            templateLocation =
                (IResourceWorkspaceLocation) specification.getSpecificationLocation().getRelativeLocation(templatePath);
            if (templateLocation == null || !templateLocation.exists())
            {
                addProblem(
                    IProblem.ERROR,
                    ((ISourceLocationInfo) templateAsset.getLocation()).getAttributeSourceLocation("resource-path"),
                    TapestryCore.getTapestryString("DefaultTemplateSource.unable-to-read-template", templatePath));
                return;
            }
        }
        if (templateLocation != null)
            fFindResults.add(templateLocation);

    }

    private void findStandardTemplates(PluginComponentSpecification specification) throws CoreException
    {
        find((IResourceWorkspaceLocation) specification.getSpecificationLocation());
    }

    private void findPageTemplateInApplicationRoot(PluginComponentSpecification specification) throws CoreException
    {
        find((IResourceWorkspaceLocation) fTapestryProject.getWebContextLocation());
    }

    private void find(IResourceWorkspaceLocation location) throws CoreException
    {
        if (location == null || !location.exists())
            return;

        // need to ensure the base template exists.
        // if it does we look for localized versions of it!
        IResourceWorkspaceLocation baseLocation =
            (IResourceWorkspaceLocation) location.getRelativeLocation(fTemplateBaseName + "." + fExtension);
        if (baseLocation.exists())
        {
            fPerlExpression = PatternPrefix + fTemplateBaseName + PatternSuffix;
            location.lookup(this);
        }
    }

    private void addProblem(int severity, ISourceLocation location, String message)
    {
        if (fProblemCollector != null)
            fProblemCollector.addProblem(severity, location, message);

    }

    public boolean accept(IResourceWorkspaceLocation location)
    {
        String name = location.getName();
        if (name == null && name.trim().length() > 0)
        {
            String foundName = null;
            String foundExtension = null;
            int dotx = name.lastIndexOf('.');
            if (dotx > 0)
            {
                foundName = name.substring(0, dotx);
                foundExtension = name.substring(dotx + 1);
            }
            if (fExtension.equals(foundExtension))
            {
                if (Perl.match(fPerlExpression, foundName))
                    fFindResults.add(location);
            }
        }
        return true;
    }

}
