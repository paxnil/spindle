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
import org.apache.tapestry.spec.IApplicationSpecification;
import org.w3c.dom.Node;

/**
 *  Scanner that turns a node tree into a ILibrarySpecification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ApplicationScanner extends LibraryScanner
{

    /* Don't need to throw an exception or add a problem here, the Parser will already have caught this
     * @see com.iw.plugins.spindle.core.scanner.AbstractScanner#beforeScan()
     */
    protected Object beforeScan(Node rootNode) throws ScannerException
    {
        if (!isElement(rootNode, "library-specification"))
        {
            return null;
        }
        return specificationFactory.createApplicationSpecification();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan(Object resultObject, Node rootNode) throws ScannerException
    {
        IApplicationSpecification specification = (IApplicationSpecification)resultObject;
        scanApplicationSpecification(rootNode, specification, null);
    }

    protected IApplicationSpecification scanApplicationSpecification(
        Node rootNode,
        IApplicationSpecification specification,
        IResourceResolver resolver)
        throws ScannerException
    {

        specification.setName(getAttribute(rootNode, "name"));
        specification.setEngineClassName(getAttribute(rootNode, "engine-class"));

        scanLibrarySpecification(rootNode, specification, resolver);

        return specification;
    }
}
