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

package tests.Scanners.mr;

import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.apache.tapestry.util.IPropertyHolder;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;

/**
 *  A Mock Scanner that exposes the scan methods for testing
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MockLibraryScanner extends LibraryScanner
{

    public void resetForTestingOnly()
    {
        fProblems.clear();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#processService(org.apache.tapestry.spec.ILibrarySpecification, org.w3c.dom.Node)
     */
    public void scanService(ILibrarySpecification spec, Node node) throws ScannerException
    {
        super.scanService(spec, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#scanComponentType(org.apache.tapestry.spec.ILibrarySpecification, org.w3c.dom.Node)
     */
    public void scanComponentType(ILibrarySpecification specification, Node node) throws ScannerException
    {
        super.scanComponentType(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#scanConfigure(org.apache.tapestry.spec.IExtensionSpecification, org.w3c.dom.Node)
     */
    public void scanConfigure(IExtensionSpecification spec, Node node) throws ScannerException
    {
        super.scanConfigure(spec, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#scanExtension(org.apache.tapestry.spec.ILibrarySpecification, org.w3c.dom.Node)
     */
    public void scanExtension(ILibrarySpecification specification, Node node) throws ScannerException
    {
        super.scanExtension(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#scanLibrary(org.apache.tapestry.spec.ILibrarySpecification, org.w3c.dom.Node)
     */
    public void scanLibrary(ILibrarySpecification specification, Node node) throws ScannerException
    {
        super.scanLibrary(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#scanLibrarySpecification(org.w3c.dom.Node, org.apache.tapestry.spec.ILibrarySpecification, org.apache.tapestry.IResourceResolver)
     */
    public void scanLibrarySpecification(Node rootNode, ILibrarySpecification specification, IResourceResolver resolver)
        throws ScannerException
    {
        super.scanLibrarySpecification(rootNode, specification, resolver);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.LibraryScanner#scanPage(org.apache.tapestry.spec.ILibrarySpecification, org.w3c.dom.Node)
     */
    public void scanPage(ILibrarySpecification specification, Node node) throws ScannerException
    {
        super.scanPage(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.SpecificationScanner#getExtendedAttribute(org.w3c.dom.Node, java.lang.String, boolean)
     */
    public String getExtendedAttribute(Node node, String attributeName, boolean required) throws ScannerException
    {
        // TODO Auto-generated method stub
        return super.getExtendedAttribute(node, attributeName, required);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.SpecificationScanner#scanPropertiesInNode(org.apache.tapestry.util.IPropertyHolder, org.w3c.dom.Node)
     */
    public void scanPropertiesInNode(IPropertyHolder holder, Node node)
    {
        super.scanPropertiesInNode(holder, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.SpecificationScanner#scanProperty(org.apache.tapestry.util.IPropertyHolder, org.w3c.dom.Node)
     */
    public void scanProperty(IPropertyHolder holder, Node node)
    {
        super.scanProperty(holder, node);
    }

}
