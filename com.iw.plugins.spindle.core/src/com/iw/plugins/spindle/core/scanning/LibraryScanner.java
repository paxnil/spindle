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

package com.iw.plugins.spindle.core.scanning;

import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.IPluginPropertyHolder;
import com.iw.plugins.spindle.core.spec.PluginComponentTypeDeclaration;
import com.iw.plugins.spindle.core.spec.PluginDescriptionDeclaration;
import com.iw.plugins.spindle.core.spec.PluginEngineServiceDeclaration;
import com.iw.plugins.spindle.core.spec.PluginExtensionConfiguration;
import com.iw.plugins.spindle.core.spec.PluginExtensionSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibraryDeclaration;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.spec.PluginPageDeclaration;

/**
 *  Scanner that turns a node tree into a ILibrarySpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class LibraryScanner extends SpecificationScanner
{

    /* Don't need to throw an exception or add a problem here, the Parser will already have caught this
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        if (super.beforeScan(source) == null)
            return null;

        return createResult();
    }

    protected Object createResult()
    {
        if (!isElement(fRootNode, "library-specification"))
        {
            return null;
        }
        return fSpecificationFactory.createLibrarySpecification();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {
        validate(source);
        ILibrarySpecification specification = (ILibrarySpecification) resultObject;
        specification.setPublicId(fPublicId);
        specification.setSpecificationLocation(fResourceLocation);
        specification.setLocation(getSourceLocationInfo(fRootNode));
        String rootName = fRootNode.getNodeName();

        // this check can only be done during a parse/scan
        if (!rootName.equals("library-specification"))
        {
            addProblem(
                IProblem.ERROR,
                getBestGuessSourceLocation(fRootNode, false),
                TapestryCore.getTapestryString(
                    "AbstractDocumentParser.incorrect-document-type",
                    "library-specification",
                    rootName));
            return;
        }
        scanLibrarySpecification(fRootNode, specification, null);
    }

    protected void scanLibrarySpecification(
        Node rootNode,
        ILibrarySpecification specification,
        IResourceResolver resolver)
        throws ScannerException
    {
        //   not needed by Spindle
        //        specification.setResourceResolver(resolver);

        for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "page"))
            {
                scanPage(specification, node);
                continue;
            }

            // component-type is in DTD 1.4, component-alias in DTD 1.3

            if (isElement(node, "component-alias") || isElement(node, "component-type"))
            {
                scanComponentType(specification, node);
                continue;
            }

            if (isElement(node, "property"))
            {
                scanProperty((IPluginPropertyHolder) specification, node);
                continue;
            }

            if (isElement(node, "service"))
            {
                scanService(specification, node);
                continue;
            }

            if (isElement(node, "description"))
            {
                String value = getValue(node);
                specification.setDescription(value);
                PluginDescriptionDeclaration declaration =
                    new PluginDescriptionDeclaration(null, value, getSourceLocationInfo(node));
                ((PluginLibrarySpecification) specification).addDescriptionDeclaration(declaration);
                continue;
            }

            if (isElement(node, "library"))
            {
                scanLibrary(specification, node);
                continue;
            }

            if (isElement(node, "extension"))
            {
                scanExtension(specification, node);
                continue;
            }
        }
    }

    protected void scanComponentType(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String type = getAttribute(node, "type", true);

        // must be validated now TODO reimplement in PluginLibrarySpecification
        if (specification.getComponentTypes().contains(type))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "type"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-component-alias", type));
        }

        String path = getAttribute(node, "specification-path");

        PluginComponentTypeDeclaration declaration =
            new PluginComponentTypeDeclaration(type, path, getSourceLocationInfo(node));

        declaration.validate(specification, fValidator);

        ((PluginLibrarySpecification) specification).addComponentTypeDeclaration(declaration);

    }
    /** @since 2.2 **/

    protected void scanConfigure(IExtensionSpecification spec, Node node) throws ScannerException
    {
        String propertyName = getAttribute(node, "property-name", false);

        String type = getAttribute(node, "type");

        //  TODO reimplement differently in PluginLibrarySpecification
        if (spec.getConfiguration().containsKey(propertyName))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "property-name"),
                TapestryCore.getTapestryString("ExtensionSpecification.duplicate-property", "extension", propertyName));
        }

        // must be done now - not revalidatable
        ExtendedAttributeResult result = null;
        String value = null;
        try
        {
            result = getExtendedAttribute(node, "value", true);
            value = result.value;
        } catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage());
        }

        PluginExtensionConfiguration configuration =
            new PluginExtensionConfiguration(propertyName, value, type, getSourceLocationInfo(node));

        configuration.setDeclaredValueIsFromAttribute(result == null ? true : result.fromAttribute);

        configuration.validate(spec, fValidator);

        ((PluginExtensionSpecification) spec).addConfiguration(configuration);

    }

    protected void scanExtension(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);
        String className = getAttribute(node, "class");

        boolean immediate = getBooleanAttribute(node, "immediate");

        //   TODO reimplement in PluginLibrarySpecification
        if (specification.getExtensionNames().contains(name))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-extension-name", name));
        }

        PluginExtensionSpecification exSpec =
            (PluginExtensionSpecification) fSpecificationFactory.createExtensionSpecification();

        exSpec.setIdentifier(name);
        exSpec.setClassName(className);
        exSpec.setImmediate(immediate);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResourceLocation(specification.getSpecificationLocation());
        exSpec.setLocation(location);

        exSpec.validateSelf(specification, fValidator);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "configure"))
            {
                scanConfigure(exSpec, child);
                continue;
            }

            if (isElement(child, "property"))
            {
                scanProperty((IPluginPropertyHolder) exSpec, child);
                continue;
            }
        }

        specification.addExtensionSpecification(name, exSpec);
    }
    protected void scanLibrary(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String id = getAttribute(node, "id", false);

        String specificationPath = getAttribute(node, "specification-path", true);

        PluginLibraryDeclaration declaration =
            new PluginLibraryDeclaration(id, specificationPath, getSourceLocationInfo(node));

        ((PluginLibrarySpecification) specification).addLibraryDeclaration(declaration);

        declaration.validate(specification, fValidator);

    }

    protected void scanPage(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        //must be validated here
        if (specification.getPageNames().contains(name))
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-page-name", name));

        String specificationPath = getAttribute(node, "specification-path");

        PluginPageDeclaration declaration =
            new PluginPageDeclaration(name, specificationPath, getSourceLocationInfo(node));

        declaration.validate(specification, fValidator);

        ((PluginLibrarySpecification) specification).addPageDeclaration(declaration);
 
    }

    protected void scanService(ILibrarySpecification spec, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        String className = getAttribute(node, "class");

        //must be done here

        if (spec.getServiceNames().contains(name))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-service-name", name));
        }

        PluginEngineServiceDeclaration declaration =
            new PluginEngineServiceDeclaration(name, className, getSourceLocationInfo(node));

        declaration.validate(spec, fValidator);

        ((PluginLibrarySpecification) spec).addEngineServiceDeclaration(declaration);
    }

}
