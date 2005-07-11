package com.iw.plugins.spindle.xerces.parser;

import java.io.IOException;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.dom.IDOMModel;
import com.iw.plugins.spindle.core.parser.dom.IDOMModelSource;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * This implementation DOES not use reference counting - a new model is constructed each time one is
 * requested.
 * 
 * @author Administrator
 */
public class XercesDOMModelSource implements IDOMModelSource
{
   

    public XercesDOMModelSource()
    {
        super();
    }

    public IDOMModel parseDocument(ICoreResource resource, boolean validate, Object requestor)
    {
        return parseDocument(resource, null, validate, requestor);
    }

    public IDOMModel parseDocument(ICoreResource resource, String encoding, boolean validate,
            Object requestor)
    {

        Parser p = new Parser();
        Document document = null;
        p.setDoValidation(validate);
        try
        {
            document = p.parse(resource.getContents(), encoding);
        }
        catch (IOException e)
        {
            TapestryCore.log(e);
        }
        
        return new XercesDOMModel(this, requestor, document, p.getProblems(),
                p.getHasFatalErrors(), validate);

    }

    public void release(IDOMModel model, Object requestor)
    {
        ((XercesDOMModel) model).dispose();
    }

    class XercesDOMModelInternal
    {

        IDOMModelSource source;

        Document document;

        IProblem[] problems;

        boolean hasFatalProblems;

        boolean wasValidated;

        public XercesDOMModelInternal(IDOMModelSource source, Object requestor, Document document,
                IProblem[] problems, boolean hasFatalProblems, boolean wasValidated)
        {
            super();
            this.source = source;
            this.document = document;
            this.problems = problems;
            this.hasFatalProblems = hasFatalProblems;
            this.wasValidated = wasValidated;
        }
    }

    class XercesDOMModel implements IDOMModel
    {

        XercesDOMModelInternal internal;

        Object requestor;

        IDOMModelSource source;

        Document document;

        IProblem[] problems;

        boolean hasFatalProblems;

        boolean wasValidated;

        public XercesDOMModel(IDOMModelSource source, Object requestor, Document document,
                IProblem[] problems, boolean hasFatalProblems, boolean wasValidated)
        {
            super();
            this.source = source;
            this.requestor = requestor;
            this.document = document;
            this.problems = problems;
            this.hasFatalProblems = hasFatalProblems;
            this.wasValidated = wasValidated;
        }

        public Document getDocument()
        {
            return document;
        }

        public IProblem[] getProblems()
        {
            return problems;
        }

        public boolean hasFatalProblems()
        {
            return hasFatalProblems;
        }

        public boolean wasValidated()
        {
            return wasValidated;
        }

        public ISourceLocationInfo getSourceLocationInfo(Node node)
        {
            if (document == null)
                return null;
            DocumentImpl document = (DocumentImpl) node.getOwnerDocument();
            return (ISourceLocationInfo) document.getUserData(node, TapestryCore.IDENTIFIER);
        }

        void dispose()
        {
            this.source = null;
            this.requestor = null;
            this.document = null;
            this.problems = null;
        }

        public void release()
        {
            source.release(this, requestor);
        }

    }
}
