package net.sf.spindle.core.extensions;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import org.apache.tapestry.spec.IBeanSpecification;

/**
 * Interface for validating a bean specification in a Tapestry page or component.
 * <p>
 * Spindle does not validate the data in the bean spec in the context of the bean itself. For
 * example if the bean implementor intended that one or more of its properties are required, Spindle
 * can't enforce that. Perhaps someday with Java 5 annotations.
 * <p>
 * To enable this kind of checking Plugins may contribute implementations of this interface using
 * the:
 * <ul>
 * <li>core.beanSpecificationValidators - extension point</li>
 * </ul>
 * <p>
 * How it works.
 * <p>
 * One instance of each contributed validator is created. If the 'class' attribute of the
 * &lt;bean&gt; tag resolves successfully, each resolver in turn is asked:
 * <ul>
 * <li><code>canValidate(IBeanSpecification)</code> the first one that returns true wins
 * </ul>
 * <p>
 * Then <code>validate()</code> is called on the winner. The instance should return a status of OK
 * if the resolution was successful. If not OK, Spindle will use the message in the status to inform
 * the user.
 * <p>
 * The order that contributed extensions are invoked is fixed but is arbitrary in that the order
 * that extensions are added to the extension point is not determinable before hand..
 * 
 * @author glongman@gmail.com
 */
public interface IBeanSpecificationValidator
{

    /**
     * Check to see if this validator can validate an instance of IBeanSpecification
     * <p>
     * Called first
     * 
     * @param bean
     *            the IBeanSpecification in question
     * @return true if this instance can proceed to validate, false otherwise.
     */
    boolean canValidate(IBeanSpecification bean);

    /**
     * validate the bean spec and throw an exception if the result is a failure.
     * <p>
     * Called second
     * 
     * @param bean
     *            the IBeanSpecification instance
     */
    void validate(IBeanSpecification bean) throws SpindleExtensionException;

}