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

import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.parser.ISourceLocationInfo;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public interface IScannerValidator
{
    
    public String getDummyStringPrefix();
    
    public String getNextDummyString();

    public void setProblemCollector(IProblemCollector collector);
    
    public void validateAsset(IComponentSpecification specification, IAssetSpecification asset, ISourceLocationInfo sourceLocation) throws ScannerException;
    
    public void validateContainedComponent(IComponentSpecification specification, IContainedComponent component, ISourceLocationInfo node) throws ScannerException;
    
    public void validateExpression(String expression, int severity) throws ScannerException;
    
    public void validateExpression(String expression, int severity, ISourceLocation location) throws ScannerException;
    
    public void validatePattern(String value, String pattern, String errorKey, int severity) throws ScannerException;
    
    public void validatePattern(String value, String pattern, String errorKey, int severity, ISourceLocation location) throws ScannerException;

    public void validateResourceLocation(IResourceLocation location, String relativePath); 
    
    public void validateTypeName(String fullyQualifiedType, int severity) throws ScannerException;
    
    public void validateTypeName(String fullyQualifiedType, int severity, ISourceLocation location) throws ScannerException;

}
