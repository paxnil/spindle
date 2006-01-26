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

package net.sf.spindle.core.scanning;

import net.sf.spindle.core.source.IProblemCollector;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.hivemind.Resource;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

/**
 * Validators used by Scanners to find problems in the Tapestry artifacts they process
 * 
 * @author glongman@gmail.com
 */
public interface IScannerValidator
{

    /**
     * Scanners may choose to represent null values with dummy strings.
     * 
     * @return the prefix used to generate dummy strings
     */
    public String getDummyStringPrefix();

    /**
     * @return the next unique dummy String
     */
    public String getNextDummyString();

    /**
     * Scanners report the problems they find to the problem collector registered by calling this
     * method
     * 
     * @param collector
     *            an instance of ProblemCollector
     */
    public void setProblemCollector(IProblemCollector collector);

    /**
     * A Scanner calls this method to validate all the aspects of an AssetSpecification that it has
     * just processed
     * 
     * @param specification
     *            the Component owning the Asset
     * @param asset
     *            the AssetSpecification to be validated
     * @param sourceLocation
     *            the source location information for the asset tag
     * @return true iff the asset being validated is completely valid.
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validateAsset(IComponentSpecification specification, IAssetSpecification asset,
            ISourceLocationInfo sourceLocation) throws ScannerException;

    /**
     * A Scanner calls this method to validate all the aspects of an ConatinedComponent that it has
     * just processed
     * 
     * @param specification
     *            the Component owning the contained component
     * @param component
     *            the ConatinedComponent to be validated
     * @param node
     *            the DOM node that contains the declaration of the ContainedComponent
     * @return true iff the ConatinedComponent being validated is completely valid.
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validateContainedComponent(IComponentSpecification specification,
            IContainedComponent component, ISourceLocationInfo node) throws ScannerException;

    /**
     * A Scanner calls this method to validate an OGNL expression Currently it is only possible to
     * check for OGNL well-formedness
     * 
     * @param expression
     *            the OGNL expression
     * @param severity
     *            the severity of the problem to reported, if one.
     * @return true iff the expression being validated is completely valid.
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validateExpression(String expression, int severity) throws ScannerException;

    /**
     * A Scanner calls this method to validate an OGNL expression Currently it is only possible to
     * check for OGNL well-formedness
     * 
     * @param expression
     *            the OGNL expression
     * @param severity
     *            the severity of the problem to reported, if one.
     * @param location
     *            the source location information to be used in problem reporting
     * @return true iff the expression being validated is completely valid.
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validateExpression(String expression, int severity, ISourceLocation location)
            throws ScannerException;

    /**
     * A Scanner calls this method to verify that a perl pattern matches the supplied value
     * 
     * @param value
     *            the value to check
     * @param pattern
     *            the perl pattern
     * @param errorKey
     *            the key to use to find the error message via TapestryCore.getTapestryString()
     * @param severity
     *            the severity to use when reporting problems
     * @return true iff the value matches the pattern
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validatePattern(String value, String pattern, String errorKey, int severity,
            int code) throws ScannerException;

    /**
     * A Scanner calls this method to verify that a perl pattern matches the supplied value
     * 
     * @param value
     *            the value to check
     * @param pattern
     *            the perl pattern
     * @param errorKey
     *            the key to use to find the error message via TapestryCore.getTapestryString()
     * @param severity
     *            the severity to use when reporting problems
     * @param location
     *            the source location information to be used in problem reporting
     * @return true iff the value matches the pattern
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validatePattern(String value, String pattern, String errorKey, int severity,
            ISourceLocation location, int code) throws ScannerException;

    /**
     * A Scanner calls this method to determine if a resource exists in the project
     * 
     * @param location
     *            the root location to evaluate the path against
     * @param relativePath
     *            the path to evaluate relative to the root
     * @param errorKey
     *            the key to use to find the error message via TapestryCore.getTapestryString()
     * @param source
     *            the source location information to be used in problem reporting
     * @return true iff the resource exists
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validateResource(Resource location, String relativePath,
            String errorKey, ISourceLocation source) throws ScannerException;

    /**
     * A Scanner calls this to determine if the location of a Tapestry library exists in the project
     * If the spec declaring the library is on the classpath, the path is resolved relative to the
     * spec location. Otherwise, its resolved relative to the classpath root.
     * 
     * @param the
     *            location of the spec containing the library decl
     * @param path
     *            the path to evaluate relative to the classpath root
     * @param errorKey
     *            the key to use to find the error message via TapestryCore.getTapestryString()
     * @param sourcethe
     *            source location information to be used in problem reporting
     * @return true iff the resource exists
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public boolean validateLibraryResource(Resource specLocation, String path,
            String errorKey, ISourceLocation source) throws ScannerException;

    /**
     * A Scanner calls this method to determine if a type exists in the project
     * 
     * @param dependant
     *            the resource that depends on this type
     * @param fullyQualifiedType
     * @param severity
     *            the severity to use when reporting problems
     * @return non null iff the type exists in the project. The object returned is implementation
     *         specific
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public Object validateTypeName(Resource dependant, String fullyQualifiedType,
            int severity) throws ScannerException;

    /**
     * A Scanner calls this method to determine if a type exists in the project
     * 
     * @param dependant
     *            the resource that depends on this type
     * @param fullyQualifiedType
     * @param severity
     *            the severity to use when reporting problems
     * @param location
     *            the source location information to be used in problem reporting
     * @return the IType instance or null if the type does not exist.
     * @throws ScannerException
     *             optional, called if the validator method cannot properly report a problem.
     */
    public Object validateTypeName(Resource dependant, String fullyQualifiedType,
            int severity, ISourceLocation location) throws ScannerException;

    public Object findType(Resource dependant, String fullyQualifiedName);

    public void addListener(IScannerValidatorListener listener);

    public void removeListener(IScannerValidatorListener listener);

    /**
     * Allow users record ad hoc problems
     * 
     * @param severity
     * @param sourceLocation
     *            the location in the source code
     * @param message
     *            a String describing the problem
     * @param isTemporary
     *            flag indicating that the problem is temporary.
     */
    public void addProblem(int severity, ISourceLocation sourceLocation, String message,
            boolean isTemporary, int code) throws ScannerException;
    

    public void validateBindingReference(int severity, ISourceLocation sourceLocation,
            String reference) throws ScannerException;

}