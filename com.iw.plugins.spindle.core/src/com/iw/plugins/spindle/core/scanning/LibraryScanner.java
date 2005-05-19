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

package com.iw.plugins.spindle.core.scanning;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.IPluginDescribable;
import com.iw.plugins.spindle.core.spec.IPluginPropertyHolder;
import com.iw.plugins.spindle.core.spec.PluginComponentTypeDeclaration;
import com.iw.plugins.spindle.core.spec.PluginExtensionConfiguration;
import com.iw.plugins.spindle.core.spec.PluginExtensionSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibraryDeclaration;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.spec.PluginPageDeclaration;
import com.iw.plugins.spindle.messages.ParseMessages;

/**
 * Scanner that turns a node tree into a ILibrarySpecification
 * 
 * @author glongman@gmail.com
 */
public class LibraryScanner extends SpecificationScanner
{

    /*
     * Don't need to throw an exception or add a problem here, the Parser will already have caught
     * this
     * 
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

    /*
     * (non-Javadoc)
     * 
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
                    ParseMessages.incorrectDocumentType("library-specification", rootName),
                    false,
                    IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_LIBRARY);
            return;
        }
        scanLibrarySpecification(fRootNode, specification);
    }

    protected void scanLibrarySpecification(Node rootNode, ILibrarySpecification specification)
            throws ScannerException
    {

        for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (scanPage(specification, node))
                continue;

            if (scanComponentType(specification, node))
                continue;

            if (scanMeta((IPluginPropertyHolder) specification, node))
                continue;

            if (!fIsTapestry_4_0 && scanService_3_0(node))
                continue;

            if (scanDescription((IPluginDescribable) specification, node))
                continue;

            if (scanLibrary(specification, node))
                continue;

            if (scanExtension(specification, node))
                continue;
        }
    }

    protected boolean scanComponentType(ILibrarySpecification specification, Node node)
            throws ScannerException
    {
        if (!isElement(node, "component-type"))
            return false;

        String type = getAttribute(node, "type", true);

        fValidator.validatePattern(
                type,
                SpecificationParser.COMPONENT_ALIAS_PATTERN,
                "invalid-component-type",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "type"),
                IProblem.LIBRARY_INVALID_COMPONENT_TYPE);

        if (specification.getComponentTypes().contains(type))
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "type"),
                    TapestryCore.getTapestryString(
                            "LibrarySpecification.duplicate-component-alias",
                            type),
                    false,
                    IProblem.LIBRARY_DUPLICATE_COMPONENT_TYPE);
        }

        String path = getAttribute(node, "specification-path");

        PluginComponentTypeDeclaration declaration = new PluginComponentTypeDeclaration(type, path,
                getSourceLocationInfo(node));

        declaration.validate(specification, fValidator);

        ((PluginLibrarySpecification) specification).addComponentTypeDeclaration(declaration);

        return true;

    }

    /** @since 2.2 * */

    protected boolean scanConfigure(IExtensionSpecification spec, Node node)
            throws ScannerException
    {
        if (!isElement(node, "configure"))
            return false;

        String propertyName = getAttribute(node, "property-name", false);

        fValidator.validatePattern(
                propertyName,
                SpecificationParser.PROPERTY_NAME_PATTERN,
                "SpecificationParser.invalid-property-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "property-name"),
                IProblem.SPINDLE_INVALID_PROPERTY_ID);

        String type = getAttribute(node, "type");

        if (spec.getConfiguration().containsKey(propertyName))
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "property-name"),
                    TapestryCore.getTapestryString(
                            "ExtensionSpecification.duplicate-property",
                            "extension",
                            propertyName),
                    false,
                    IProblem.EXTENSION_DUPLICATE_PROPERTY);
        }

        // must be done now - not revalidatable
        ExtendedAttributeResult result = null;
        String value = null;
        try
        {
            result = getExtendedAttribute(node, "value", true);
            value = result.value;
        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
        }

        PluginExtensionConfiguration configuration = new PluginExtensionConfiguration(propertyName,
                value, type, getSourceLocationInfo(node));

        configuration.setDeclaredValueIsFromAttribute(result == null ? true : result.fromAttribute);

        configuration.validate(spec, fValidator);

        ((PluginExtensionSpecification) spec).addConfiguration(configuration);

        return true;
    }

    protected boolean scanExtension(ILibrarySpecification specification, Node node)
            throws ScannerException
    {
        String name = getAttribute(node, "name", true);

        fValidator.validatePattern(
                name,
                SpecificationParser.EXTENSION_NAME_PATTERN,
                "invalid-extension-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                IProblem.EXTENSIION_INVALID_NAME);

        String className = getAttribute(node, "class");

        boolean immediate = getBooleanAttribute(node, "immediate");

        if (specification.getExtensionNames().contains(name))
        {
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    TapestryCore.getTapestryString(
                            "LibrarySpecification.duplicate-extension-name",
                            name),
                    false,
                    IProblem.LIBRARY_DUPLICATE_EXTENSION_NAME);
        }

        PluginExtensionSpecification exSpec = (PluginExtensionSpecification) new PluginExtensionSpecification();

        exSpec.setIdentifier(name);
        exSpec.setClassName(className);
        exSpec.setImmediate(immediate);

        ISourceLocationInfo location = getSourceLocationInfo(node);
        location.setResource(specification.getSpecificationLocation());
        exSpec.setLocation(location);

        exSpec.validateSelf(specification, fValidator);

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (scanConfigure(exSpec, child))
                continue;

            if (scanMeta(exSpec, node))
                continue;
        }

        ((PluginLibrarySpecification) specification).addExtension(exSpec);

        return true;
    }

    protected boolean scanLibrary(ILibrarySpecification specification, Node node)
            throws ScannerException
    {
        if (!isElement(node, "library"))
            return false;

        String id = getAttribute(node, "id", false);

        String specificationPath = getAttribute(node, "specification-path", true);

        fValidator.validatePattern(
                id,
                SpecificationParser.LIBRARY_ID_PATTERN,
                "invalid-library-id",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "id"),
                IProblem.LIBRARY_INVALID_CHILD_LIB_ID);

        if (id != null && id.equals(INamespace.FRAMEWORK_NAMESPACE))
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "id"),
                    ParseMessages.frameworkLibraryIdIsReserved(id),
                    false,
                    IProblem.LIBRARY_INVALID_CHILD_LIB_ID);

        PluginLibraryDeclaration declaration = new PluginLibraryDeclaration(id, specificationPath,
                getSourceLocationInfo(node));

        ((PluginLibrarySpecification) specification).addLibraryDeclaration(declaration);

        declaration.validate(specification, fValidator);

        return true;

    }

    protected boolean scanPage(ILibrarySpecification specification, Node node)
            throws ScannerException
    {
        if (!isElement(node, "page"))
            return false;

        String name = getAttribute(node, "name", false);

        fValidator.validatePattern(
                name,
                SpecificationParser.PAGE_NAME_PATTERN,
                "invalid-page-name",
                IProblem.ERROR,
                getAttributeSourceLocation(node, "name"),
                IProblem.LIBRARY_INVALID_PAGE_NAME);

        //must be validated here
        if (specification.getPageNames().contains(name))
            addProblem(
                    IProblem.ERROR,
                    getAttributeSourceLocation(node, "name"),
                    TapestryCore
                            .getTapestryString("LibrarySpecification.duplicate-page-name", name),
                    false,
                    IProblem.LIBRARY_DUPLICATE_PAGE_NAME);

        String specificationPath = getAttribute(node, "specification-path");

        PluginPageDeclaration declaration = new PluginPageDeclaration(name, specificationPath,
                getSourceLocationInfo(node));

        declaration.validate(specification, fValidator);

        ((PluginLibrarySpecification) specification).addPageDeclaration(declaration);
        return true;

    }

    protected boolean scanService_3_0(Node node) throws ScannerException
    {
        if (!isElement(node, "service"))
            return false;

        addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), ParseMessages
                .serviceElementNotSupported(), false, IProblem.NOT_QUICK_FIXABLE);

        return true;
    }

}