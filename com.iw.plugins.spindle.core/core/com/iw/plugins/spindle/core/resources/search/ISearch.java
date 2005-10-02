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

package com.iw.plugins.spindle.core.resources.search;


import com.iw.plugins.spindle.core.TapestryCoreException;

/**
 * Interface for all kinds of searches in the Tapestry context
 * 
 * @author glongman@gmail.com
 */
public interface ISearch
{
    /**
     * Each search starts from a root. Any implementation may allow extra parameters to be passed as a 
     * Properties object. 
     * <p>
     * Implementers decide what roots and parameters to accept but they all must accept a null value for parameters.
     * @param root the root of the search.
     * @throws TapestryCoreException 
     */
    public abstract void configure(Object root)
            throws TapestryCoreException;

    public abstract void search(ISearchAcceptor acceptor);
}