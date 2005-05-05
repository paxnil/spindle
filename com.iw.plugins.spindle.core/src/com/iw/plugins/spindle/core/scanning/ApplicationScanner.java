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

import org.apache.tapestry.spec.IApplicationSpecification;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.spec.PluginApplicationSpecification;

/**
 * Scanner that turns a node tree into a IApplicationSpecification
 * 
 * @author glongman@gmail.com
 */
public class ApplicationScanner extends LibraryScanner
{

    /*
     * Don't need to throw an exception or add a problem here, the Parser will already have caught
     * this
     * 
     * @see com.iw.plugins.spindle.core.scanner.AbstractScanner#beforeScan()
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        return super.beforeScan(source);
    }

    protected Object createResult()
    {
        if (!isElement(fRootNode, "application"))
        {
            return null;
        }
        return fSpecificationFactory.createApplicationSpecification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {
        validate(source);
        IApplicationSpecification specification = (IApplicationSpecification) resultObject;
        specification.setPublicId(fPublicId);
        specification.setSpecificationLocation(fResourceLocation);
        specification.setLocation(getSourceLocationInfo(fRootNode));
        String rootName = fRootNode.getNodeName();
        if (!rootName.equals("application"))
        {
            addProblem(
                    IProblem.ERROR,
                    getBestGuessSourceLocation(fRootNode, false),
                    TapestryCore.getTapestryString(
                            "AbstractDocumentParser.incorrect-document-type",
                            "application",
                            rootName),
                    false,
                    IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_APPLICATION);
            return;
        }
        scanApplicationSpecification(fRootNode, specification);
    }

    protected IApplicationSpecification scanApplicationSpecification(Node rootNode,
            IApplicationSpecification specification) throws ScannerException
    {

        specification.setName(getAttribute(rootNode, "name"));

        specification.setEngineClassName(getAttribute(rootNode, "engine-class"));

        ((PluginApplicationSpecification) specification).validateSelf(fValidator);

        scanLibrarySpecification(rootNode, specification);

        return specification;
    }
}