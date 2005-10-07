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

package core.scanning;

import org.apache.tapestry.spec.IApplicationSpecification;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.messages.ParseMessages;

import core.source.IProblem;
import core.spec.PluginApplicationSpecification;

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
     * @see core.scanner.AbstractScanner#beforeScan()
     */
    protected Object beforeScan() throws ScannerException
    {
        return super.beforeScan();
    }

    protected Object createResult()
    {
        return new PluginApplicationSpecification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan() throws ScannerException
    {
        validate(fSource);
        IApplicationSpecification specification = (IApplicationSpecification) fResultObject;
        specification.setPublicId(fPublicId);
        specification.setSpecificationLocation(fResourceLocation);
        specification.setLocation(getSourceLocationInfo(fRootNode));
        String rootName = fRootNode.getNodeName();
        if (!rootName.equals("application"))
        {
            addProblem(
                    IProblem.ERROR,
                    getBestGuessSourceLocation(fRootNode, false),
                    ParseMessages.incorrectDocumentType("application", rootName),
                    false,
                    IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_APPLICATION);
        }
        else
        {
            scanApplicationSpecification(fRootNode, specification);
        }
    }

    protected void scanApplicationSpecification(Node rootNode,
            IApplicationSpecification specification) throws ScannerException
    {

        specification.setName(getAttribute(rootNode, "name"));

        specification.setEngineClassName(getAttribute(rootNode, "engine-class"));

        ((PluginApplicationSpecification) specification).validateSelf(fValidator);

        scanLibrarySpecification(rootNode, specification);
    }
}