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

import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.parse.SpecificationParser;
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
        String rootName = fRootNode.getNodeName();
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
                scanProperty((IPluginPropertyHolder)specification, node);
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

        boolean valid =
            validatePattern(
                type,
                SpecificationParser.COMPONENT_ALIAS_PATTERN,
                "SpecificationParser.invalid-component-type",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "type"));

        if (valid && specification.getComponentTypes().contains(type))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "type"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-component-alias", type));
        }

        String path = getAttribute(node, "specification-path");

        validateResourceLocation(
            specification.getSpecificationLocation(),
            path,
            "scan-library-missing-component",
            getAttributeSourceLocation(node, "specification-path"));

        PluginComponentTypeDeclaration declaration =
            new PluginComponentTypeDeclaration(type, path, getSourceLocationInfo(node));
        ((PluginLibrarySpecification) specification).addComponentTypeDeclaration(declaration);
        specification.setComponentSpecificationPath(type, path);
    }
    /** @since 2.2 **/

    protected void scanConfigure(IExtensionSpecification spec, Node node) throws ScannerException
    {
        String propertyName = getAttribute(node, "property-name", true);
        String type = getAttribute(node, "type");

        validatePattern(
            propertyName,
            SpecificationParser.PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-property-name",
            IProblem.ERROR,
            getNodeStartSourceLocation(node));

        String value = null;
        try
        {
            value = getExtendedAttribute(node, "value", true).value;
        } catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), e.getMessage());
        }

        IConverter converter = (IConverter) SpecificationScanner.conversionMap.get(type);
        Object objectValue = null;

        if (converter == null)
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "type"),
                TapestryCore.getTapestryString("SpecificationParser.unknown-static-value-type", type));
        } else
        {
            try
            {
                objectValue = converter.convert(value);
            } catch (ScannerException e2)
            {
                addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), e2.getMessage());
            }
        }

        spec.addConfiguration(propertyName, objectValue);
    }

    protected void scanExtension(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);
        String className = getAttribute(node, "class");

        validateTypeName(className, IProblem.ERROR, getAttributeSourceLocation(node, "class"));

        boolean immediate = getBooleanAttribute(node, "immediate");

        boolean valid =
            validatePattern(
                name,
                SpecificationParser.EXTENSION_NAME_PATTERN,
                "SpecificationParser.invalid-extension-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));

        if (valid && specification.getExtensionNames().contains(name))
        {
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-extension-name", name));
        }

        IExtensionSpecification exSpec = fSpecificationFactory.createExtensionSpecification();

        exSpec.setClassName(className);
        exSpec.setImmediate(immediate);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResourceLocation(specification.getSpecificationLocation());
        exSpec.setLocation(location);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "configure"))
            {
                scanConfigure(exSpec, child);
                continue;
            }

            if (isElement(child, "property"))
            {
                scanProperty((IPluginPropertyHolder)exSpec, child);
                continue;
            }
        }

        specification.addExtensionSpecification(name, exSpec);
    }
    protected void scanLibrary(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String id = getAttribute(node, "id", false);

        if (id == null)
            return;

        validatePattern(
            id,
            SpecificationParser.LIBRARY_ID_PATTERN,
            "SpecificationParser.invalid-library-id",
            IProblem.ERROR,
            getAttributeSourceLocation(node, "id"));

        if (id.equals(INamespace.FRAMEWORK_NAMESPACE))
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "id"),
                TapestryCore.getTapestryString(
                    "SpecificationParser.framework-library-id-is-reserved",
                    INamespace.FRAMEWORK_NAMESPACE));

        String specificationPath = getAttribute(node, "specification-path", true);

        if (specificationPath.startsWith(getDummyStringPrefix()))
        {
            addProblem(IProblem.ERROR, getAttributeSourceLocation(node, "specification-path"), "blank value");
        } else
        {
            boolean validLibLoc =
                validateLibraryResourceLocation(
                    specification.getSpecificationLocation(),
                    specificationPath,
                    "scan-library-missing-library",
                    getAttributeSourceLocation(node, "specification-path"));
        }

        PluginLibraryDeclaration declaration =
            new PluginLibraryDeclaration(id, specificationPath, getSourceLocationInfo(node));
        ((PluginLibrarySpecification) specification).addLibraryDeclaration(declaration);
        specification.setLibrarySpecificationPath(id, specificationPath);

    }

    protected void scanPage(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        boolean valid =
            validatePattern(
                name,
                SpecificationParser.PAGE_NAME_PATTERN,
                "SpecificationParser.invalid-page-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));

        if (valid && specification.getPageNames().contains(name))
            addProblem(
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                TapestryCore.getTapestryString("LibrarySpecification.duplicate-page-name", name));

        String specificationPath = getAttribute(node, "specification-path");

        validateResourceLocation(
            specification.getSpecificationLocation(),
            specificationPath,
            "scan-library-missing-page",
            getAttributeSourceLocation(node, "specification-path"));

        PluginPageDeclaration declaration =
            new PluginPageDeclaration(name, specificationPath, getSourceLocationInfo(node));
        ((PluginLibrarySpecification) specification).addPageDeclaration(declaration);
        specification.setPageSpecificationPath(name, specificationPath);

    }

    protected void scanService(ILibrarySpecification spec, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        validatePattern(
            name,
            SpecificationParser.SERVICE_NAME_PATTERN,
            "SpecificationParser.invalid-service-name",
            IProblem.ERROR,
            getAttributeSourceLocation(node, "name"));

        String className = getAttribute(node, "class");
        validateTypeName(className, IProblem.ERROR, getAttributeSourceLocation(node, "class"));
        spec.setServiceClassName(name, className);
        PluginEngineServiceDeclaration declaration =
            new PluginEngineServiceDeclaration(name, className, getSourceLocationInfo(node));
        ((PluginLibrarySpecification) spec).addEngineServiceDeclaration(declaration);
    }

}
