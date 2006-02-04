package net.sf.spindle.xerces.parser;
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
import java.io.IOException;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.parser.IDOMModelSource;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
            document = p.parse(resource, encoding);
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
