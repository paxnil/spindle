package net.sf.spindle.core.parser;
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
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Node;



/**
 * A container that dispenses information about a DOM parse of an xml document.
 * <p>
 * End users should not instantiate sunclasses of these directly, rather they should be dispensed by
 * calls to an IDOMModelSource.
 * <p>
 * IDOMModel are reference counted by thier created source and they potentially hold data that
 * should be released to garbage collection asap. Get one, use it, and release it.
 * 
 * @author gwl
 * @see IDOMModelSource
 * @see IDOMModelSource#release()
 */
public interface IDOMModel
{
    /**
     * ask the source to release the model on behalf of the requestor
     * @see IDOMModelSource#release()
     */
    void release();

    /**
     * @return the DOM represented by this model. returns null if no document was obtainable due to
     *         a parse error.
     */
    Document getDocument();

    /**
     * @return any problems that were encountered while parsing the document
     */
    IProblem[] getProblems();

    /**
     * @return true iff the parse encountered a fatal error (not well formed)
     */
    boolean hasFatalProblems();

    /**
     * @return true iff the document was validated against a DTD
     */
    boolean wasValidated();

    /**
     * @param node a node in the document represented by this model
     * @return the source location information found in the text xml document for this node
     */
    ISourceLocationInfo getSourceLocationInfo(Node node);
}
