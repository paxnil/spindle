package com.iw.plugins.spindle.core.parser;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.xml.TapestryEntityResolver;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParser;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.pull.TapestryPullParser;
import com.iw.plugins.spindle.core.parser.xml.pull.TapestryPullParserConfiguration;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.Files;

/**
 * The xml parser used in the builds.
 * can be used to dom-parse or pull-parse XML content
 * 
 * Validates by default
 *
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class Parser implements ISourceLocationResolver, XMLErrorHandler, IProblemCollector
{

    private boolean fUsePullParser = false;
    private IDocument fEclipseDocument;
    private DocumentImpl fXmlDocument;

    private TapestryPullParserConfiguration fPullParseConfiguration;
    private TapestryDOMParserConfiguration fDomParseConfiguration;
    private TapestryDOMParser fDomParser = null;
    private TapestryPullParser fPullParser = null;
    private List fCollectedProblems = new ArrayList();

    private boolean fDoValidation = true;
    private boolean fHasFatalErrors;

    public Parser()
    {
        this(false);
    }

    public Parser(boolean usePullParser)
    {
        fUsePullParser = usePullParser;
        TapestryEntityResolver.register(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID, "Tapestry_1_3.dtd");
        TapestryEntityResolver.register(SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID, "Tapestry_3_0.dtd");
    }

    /**
         * @return
         */
    public boolean isDoValidation()
    {
        return fDoValidation;
    }

    /**
     * Can only be called before the first parse!
     * @param b
     */
    public void setDoValidation(boolean flag)
    {
        if (fDomParser != null || fPullParser != null)
            throw new IllegalStateException("can only set validation flag before the first parse!");

        fDoValidation = flag;
    }

    private IDocument getEclipseDocument(String content)
    {
        if (fEclipseDocument == null)
            fEclipseDocument = new Document();

        fEclipseDocument.set(content);
        return fEclipseDocument;
    }

    public String getPublicId()
    {
        if (!fUsePullParser)
        {
            DocumentType type = fXmlDocument.getDoctype();
            if (type != null)
                return type.getPublicId();

        } else
        {
            return fPullParser.getPublicId();
        }
        return null;
    }

    private void checkPullParser()
    {
        Assert.isTrue(fUsePullParser, "can't pull parse, I'm set to dom parse!");
        if (fPullParser == null)
        {

            fPullParseConfiguration = new TapestryPullParserConfiguration();
            fPullParser = new TapestryPullParser(fPullParseConfiguration);
            fPullParser.setSourceResolver(this);
            fPullParseConfiguration.setDocumentHandler(fPullParser);
            fPullParseConfiguration.setErrorHandler(this);
            fPullParseConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
            fPullParseConfiguration.setFeature("http://xml.org/sax/features/validation", fDoValidation);
            fPullParseConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);
            if (fDoValidation)
                fPullParseConfiguration.setProperty(
                    "http://apache.org/xml/properties/internal/grammar-pool",
                    TapestryDOMParserConfiguration.GRAMMAR_POOL);

        }
    }

    private void checkDomParser()
    {
        Assert.isTrue(!fUsePullParser, "can't dom parse, I'm set to pull parse!");
        if (fDomParser == null)
        {
            if (fDoValidation)
            {
                fDomParseConfiguration =
                    new TapestryDOMParserConfiguration(TapestryDOMParserConfiguration.GRAMMAR_POOL);

            } else
            {
                fDomParseConfiguration = new TapestryDOMParserConfiguration();
            }
            fDomParser = new TapestryDOMParser(fDomParseConfiguration);
            fDomParseConfiguration.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
            fDomParseConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
            fDomParseConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
            fDomParseConfiguration.setFeature("http://xml.org/sax/features/validation", fDoValidation);
            fDomParseConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);
            fDomParser.setSourceResolver(this);
            fDomParseConfiguration.setDocumentHandler(fDomParser);
            fDomParseConfiguration.setErrorHandler(this);
        }
    }

    public Node parse(IStorage storage) throws IOException, CoreException
    {
        return parse(storage.getContents());
    }

    public Node parse(InputStream input) throws IOException
    {
        String content = Files.readFileToString(input, null);
        return parse(content);
    }

    public Node parse(String content) throws IOException
    {
        fXmlDocument = null;
        fCollectedProblems.clear();
        getEclipseDocument(content);
        fHasFatalErrors = false;
        if (fUsePullParser)
        {
            return pullParse(content);
        } else
        {
            return domParse(content);
        }
    }

    protected Node pullParse(String content) throws IOException
    {
        Assert.isTrue(fUsePullParser, "can't pull parse, I'm set to dom parse!");
        Node result = null;
        StringReader reader = new StringReader(content);
        try
        {

            checkPullParser();
            fPullParseConfiguration.setInputSource(new XMLInputSource(null, "", null, reader, null));
            fPullParseConfiguration.parse();
            if (!fHasFatalErrors)
                result = (Node) fPullParser.getRootNode();

        } catch (ParserRuntimeException e1)
        {
            // this could happen while scanning the prolog
            createFatalProblem(e1, IProblem.ERROR);
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
        return result;
    }

    protected Node domParse(String content) throws IOException
    {
        Assert.isTrue(!fUsePullParser, "can't dom parse, I'm set to pull parse!");
        StringReader reader = new StringReader(content);
        Node result = null;

        try
        {
            checkDomParser();
            fDomParser.parse(new InputSource(reader));
            fXmlDocument = (DocumentImpl) fDomParser.getDocument();
            result = fXmlDocument.getDocumentElement();
        } catch (SAXException e)
        {
            // there was a fatal error - return null
            // all the exceptions are collected already because I am an XMLErrorHandler
            return null;

        } finally
        {
            reader.close();
        }

        return result;

    }

    public boolean getHasFatalErrors()
    {
        return fHasFatalErrors;
    }

    public DocumentImpl getParsedDocument()
    {
        Assert.isTrue(!fUsePullParser, "can't get the document as we are using pull parsing!");
        return fXmlDocument;
    }

    public int getLineOffset(int parserReportedLineNumber)
    {
        try
        {
            if (fEclipseDocument != null)
                return fEclipseDocument.getLineOffset(parserReportedLineNumber - 1);

        } catch (BadLocationException e)
        {
            TapestryCore.log(e);
        }
        return 0;
    }

    public int getColumnOffset(int parserReportedLineNumber, int parserReportedColumn)
    {
        int result = getLineOffset(parserReportedLineNumber);
        int lineCount = fEclipseDocument.getNumberOfLines();
        int totalLength = fEclipseDocument.getLength();
        if (parserReportedColumn > 0)
        {
            if (parserReportedLineNumber > lineCount)
            {
                result = Math.min(totalLength - 2, result + parserReportedColumn - 1);
            } else
            {
                try
                {
                    int lastCharOnLine = result + fEclipseDocument.getLineLength(parserReportedLineNumber - 1) - 1;
                    result = Math.min(lastCharOnLine, result + parserReportedColumn - 1);
                } catch (BadLocationException e)
                {
                    TapestryCore.log(e);
                }
            }

        }
        return result;
    }

    public void addProblem(IProblem problem)
    {
        if (!fCollectedProblems.contains(problem))
        {
            fCollectedProblems.add(problem);
        }
    }

    public void addProblem(int severity, ISourceLocation location, String message)
    {
        addProblem(
            new DefaultProblem(
                ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
                severity,
                message,
                location.getLineNumber(),
                location.getCharStart(),
                location.getCharEnd()));
    }

    public IProblem[] getProblems()
    {
        if (fCollectedProblems != null && !fCollectedProblems.isEmpty())
            return (IProblem[]) fCollectedProblems.toArray(new IProblem[fCollectedProblems.size()]);

        return new IProblem[0];
    }

    private IProblem createFatalProblem(XMLParseException parseException, int severity)
    {
        return createProblem(ITapestryMarker.TAPESTRY_FATAL_PROBLEM_MARKER, parseException, severity);
    }

    private IProblem createProblem(XMLParseException parseException, int severity)
    {
        return createProblem(ITapestryMarker.TAPESTRY_PROBLEM_MARKER, parseException, severity);
    }

    private IProblem createProblem(String type, XMLParseException ex, int severity)
    {

        int lineNumber = ex.getLineNumber();
        int columnNumber = ex.getColumnNumber();
        int charStart = 0;
        int charEnd = 0;

        charStart = getColumnOffset(lineNumber, columnNumber);
        charEnd = charStart + 1;

        return new DefaultProblem(type, severity, ex.getMessage(), lineNumber, charStart, charEnd);
    }

    //*** XMLErrorHandler for DOM && PULL Parsing **

    /**
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(String, String, XMLParseException)
     */
    public void error(String domain, String key, XMLParseException exception) throws XNIException
    {
        addProblem(createProblem(exception, IMarker.SEVERITY_ERROR));
    }

    /**
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(String, String, XMLParseException)
     */
    public void fatalError(String domain, String key, XMLParseException exception) throws XNIException
    {
        fHasFatalErrors = true;
        addProblem(createFatalProblem(exception, IMarker.SEVERITY_ERROR));
    }

    /**
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(String, String, XMLParseException)
     */
    public void warning(String domain, String key, XMLParseException exception) throws XNIException
    {
        addProblem(createProblem(exception, IMarker.SEVERITY_WARNING));
    }

    ///*** END OF XMLErrorHandler for DOM && PULL Parsing **

}
