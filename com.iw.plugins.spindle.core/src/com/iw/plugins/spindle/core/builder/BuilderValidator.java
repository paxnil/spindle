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

import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  A validator that knows about the project
 *  <p>
 *  i.e. it can resolve type names in the project buildpath
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BuilderValidator extends BaseValidator
{
    Build fBuild;
    ICoreNamespace fFramework;

    public BuilderValidator(Build build)
    {
        super();
        this.fBuild = build;
    }

    public BuilderValidator(Build build, ICoreNamespace framework)
    {
        this(build);
        Assert.isNotNull(framework);
        this.fFramework = framework;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.BaseValidator#findType(java.lang.String)
     */
    protected Object findType(String fullyQualifiedName)
    {
        return fBuild.fTapestryBuilder.getType(fullyQualifiedName);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.IScannerValidator#validateContainedComponent(org.apache.tapestry.spec.IComponentSpecification, org.apache.tapestry.spec.IContainedComponent, com.iw.plugins.spindle.core.parser.ISourceLocationInfo)
     */
    public boolean validateContainedComponent(
        PluginComponentSpecification specification,
        IContainedComponent component,
        ISourceLocationInfo info)
        throws ScannerException
    {
        ICoreNamespace use_namespace = (ICoreNamespace) specification.getNamespace();
        String type = component.getType();
        String namespaceId = null;
        IComponentSpecification containedSpecification;

        if (TapestryCore.isNull(type))
            // already caught by the scanner
            return true;

        int colonx = type.indexOf(':');

        if (colonx > 0)
        {
            type = type.substring(colonx + 1);
            namespaceId = type.substring(0, colonx);
        }

        if (TapestryCore.isNull(namespaceId))
        {
            containedSpecification = use_namespace.getComponentSpecification(type);
            if (containedSpecification == null)
            {
                if (fBuild != null)
                {
                    //check to see if its not built yet
                    String path = use_namespace.getSpecification().getComponentSpecificationPath(type);
                    if (!TapestryCore.isNull(path))
                        // build it
                        containedSpecification = null; //TODO build it

                }
                // must check again - it might've got built
                if (containedSpecification != null)
                {
                    // look in the framework namespace
                    use_namespace = fFramework;
                    containedSpecification = use_namespace.getComponentSpecification(type);
                }
            }
        } else
        {
            ICoreNamespace sub_namespace = (ICoreNamespace) use_namespace.getChildNamespace(namespaceId);
            if (sub_namespace == null)
            {
                reportProblem(
                    IProblem.ERROR,
                    info.getAttributeSourceLocation("type"),
                    "Unable to resolve " + TapestryCore.getTapestryString("Namespace.nested-namespace", namespaceId));
                return false;
            }
            use_namespace = sub_namespace;
            containedSpecification = use_namespace.getComponentSpecification(type);

        }

        if (containedSpecification == null)
        {
            reportProblem(
                IProblem.ERROR,
                info.getAttributeSourceLocation("type"),
                TapestryCore.getTapestryString(
                    "Namespace.no-such-component-type",
                    type,
                    use_namespace.getExtendedId()));
            return false;
        }
        return true;
    }

}
