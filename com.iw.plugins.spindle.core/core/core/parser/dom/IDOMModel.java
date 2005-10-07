package core.parser.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import core.source.IProblem;
import core.source.ISourceLocationInfo;

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
