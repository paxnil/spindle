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
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;

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
    protected Object beforeScan(Node rootNode) throws ScannerException
    {
        if (!isElement(rootNode, "library-specification"))
        {
            return null;
        }
        return specificationFactory.createLibrarySpecification();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan(Object resultObject, Node rootNode) throws ScannerException
    {
        ILibrarySpecification specification = (ILibrarySpecification) resultObject;
        scanLibrarySpecification(rootNode, specification, null);
    }

    protected void scanLibrarySpecification(Node rootNode, ILibrarySpecification specification, IResourceResolver resolver)
        throws ScannerException
    {
        specification.setPublicId(parser.getPublicId());
        //   TODO figure out specLocation & ResourceResolver
        //     specification.setSpecificationLocation(getResourceLocation());
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
                scanProperty(specification, node);
                continue;
            }

            if (isElement(node, "service"))
            {
                scanService(specification, node);
                continue;
            }

            if (isElement(node, "description"))
            {
                specification.setDescription(getValue(node));
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

        validatePattern(
            type,
            SpecificationParser.COMPONENT_ALIAS_PATTERN,
            "SpecificationParser.invalid-component-type",
            IProblem.ERROR,
            getAttributeSourceLocation(node, "name"));

        String path = getAttribute(node, "specification-path");

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
            value = getExtendedAttribute(node, "value", true);
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
        boolean immediate = getBooleanAttribute(node, "immediate");

        validatePattern(
            name,
            SpecificationParser.EXTENSION_NAME_PATTERN,
            "SpecificationParser.invalid-extension-name",
            IProblem.ERROR,
            getAttributeSourceLocation(node, "name"));

        IExtensionSpecification exSpec = specificationFactory.createExtensionSpecification();

        exSpec.setClassName(className);
        exSpec.setImmediate(immediate);

        ISourceLocationInfo location = parser.getSourceLocationInfo(node);
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
                scanProperty(exSpec, child);
                continue;
            }
        }

        specification.addExtensionSpecification(name, exSpec);
    }
    protected void scanLibrary(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String id = getAttribute(node, "id");

        if (id != null)
        {

            validatePattern(
                id,
                SpecificationParser.LIBRARY_ID_PATTERN,
                "SpecificationParser.invalid-library-id",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "id"));

            if (id.equals(INamespace.FRAMEWORK_NAMESPACE))
            {
                addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "id"),
                TapestryCore.getTapestryString("SpecificationParser.framework-library-id-is-reserved", INamespace.FRAMEWORK_NAMESPACE));
            }

            String specificationPath = getAttribute(node, "specification-path");

            specification.setLibrarySpecificationPath(id, specificationPath);
        }
    }

    protected void scanPage(ILibrarySpecification specification, Node node) throws ScannerException
    {
        String name = getAttribute(node, "name");

        if (name != null)
        {

            validatePattern(
                name,
                SpecificationParser.PAGE_NAME_PATTERN,
                "SpecificationParser.invalid-page-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"));

            String specificationPath = getAttribute(node, "specification-path");

            specification.setPageSpecificationPath(name, specificationPath);
        }
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

        if (className != null)
        {
            validateTypeName(className, IProblem.ERROR, getAttributeSourceLocation(node, "class"));
        }
        spec.setServiceClassName(name, className);
    }

}