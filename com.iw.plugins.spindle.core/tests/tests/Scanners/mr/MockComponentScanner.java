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

import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.SpecFactory;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;

/**
 *  A Mock ComponentScanner that exposes scan methods for testing
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MockComponentScanner extends ComponentScanner
{   
    
    public void resetForTestingOnly() {
       fProblems.clear(); 
    }

    public MockComponentScanner( SpecFactory factory, IScannerValidator aValidator) {
        super();
        specificationFactory = factory;
        fValidator = aValidator;
        fValidator.setProblemCollector(this);        
    }
    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanAsset(org.apache.tapestry.spec.IComponentSpecification, org.w3c.dom.Node, org.apache.tapestry.spec.AssetType, java.lang.String)
     */
    public void scanAsset(IComponentSpecification specification, Node node, AssetType type, String attributeName)
        throws ScannerException
    {
        super.scanAsset(specification, node, type, attributeName);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanBean(org.apache.tapestry.spec.IComponentSpecification, org.w3c.dom.Node)
     */
    public void scanBean(IComponentSpecification specification, Node node) throws ScannerException
    {
        super.scanBean(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanBinding(org.apache.tapestry.spec.IContainedComponent, org.w3c.dom.Node, org.apache.tapestry.spec.BindingType, java.lang.String)
     */
    public void scanBinding(IContainedComponent component, Node node, BindingType type, String attributeName)
        throws ScannerException
    {
        super.scanBinding(component, node, type, attributeName);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanComponent(org.apache.tapestry.spec.IComponentSpecification, org.w3c.dom.Node)
     */
    public void scanComponent(IComponentSpecification specification, Node node) throws ScannerException
    {
        super.scanComponent(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanComponentSpecification(org.w3c.dom.Node, org.apache.tapestry.spec.IComponentSpecification, boolean)
     */
    public void scanComponentSpecification(Node rootNode, IComponentSpecification specification, boolean isPage)
        throws ScannerException
    {
        super.scanComponentSpecification(rootNode, specification, isPage);
    }

//    /* (non-Javadoc)
//     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanExpressionValue(org.apache.tapestry.spec.BeanSpecification, java.lang.String, org.w3c.dom.Node)
//     */
//    public void scanExpressionValue(BeanSpecification spec, String propertyName, Node node)
//    {
//        super.scanExpressionValue(spec, propertyName, node);
//    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanListenerBinding(org.apache.tapestry.spec.IContainedComponent, org.w3c.dom.Node)
     */
    public void scanListenerBinding(IContainedComponent component, Node node) throws ScannerException
    {
        super.scanListenerBinding(component, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanParameter(org.apache.tapestry.spec.IComponentSpecification, org.w3c.dom.Node)
     */
    public void scanParameter(IComponentSpecification specification, Node node) throws ScannerException
    {
        super.scanParameter(specification, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanPropertySpecification(org.apache.tapestry.spec.IComponentSpecification, org.w3c.dom.Node)
     */
    public void scanPropertySpecification(IComponentSpecification spec, Node node) throws ScannerException
    {
        super.scanPropertySpecification(spec, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanReservedParameter(org.apache.tapestry.spec.IComponentSpecification, org.w3c.dom.Node)
     */
    public void scanReservedParameter(IComponentSpecification spec, Node node)
    {
        super.scanReservedParameter(spec, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanSetProperty(org.apache.tapestry.spec.IBeanSpecification, org.w3c.dom.Node)
     */
    public void scanSetProperty(IBeanSpecification spec, Node node) throws ScannerException
    {
        super.scanSetProperty(spec, node);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.ComponentScanner#scanSetStringProperty(org.apache.tapestry.spec.IBeanSpecification, org.w3c.dom.Node)
     */
    public void scanSetMessageProperty(IBeanSpecification spec, Node node)
    {
        super.scanSetMessageProperty(spec, node);
    }

}
