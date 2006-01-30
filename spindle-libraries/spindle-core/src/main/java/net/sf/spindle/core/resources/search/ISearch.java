package net.sf.spindle.core.resources.search;
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

import net.sf.spindle.core.TapestryCoreException;

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