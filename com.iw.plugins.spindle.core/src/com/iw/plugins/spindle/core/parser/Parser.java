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

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
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
import com.iw.plugins.spindle.core.parser.xml.TapestryParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParser;
import com.iw.plugins.spindle.core.parser.xml.pull.TapestryPullParser;
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

    private boolean usePullParser = false;
    private IDocument eclipseDocument;
    private DocumentImpl xmlDocument;

    private XMLPullParserConfiguration pullParseConfiguration;
    private XMLPullParserConfiguration domParseConfiguration;
    private TapestryDOMParser domParser = null;
    private TapestryPullParser pullParser = null;
    private ArrayList collectedProblems = new ArrayList();

    private boolean doValidation = true;
    private boolean hasFatalErrors;

    public Parser()
    {
        this(false);
    }

    public Parser(boolean usePullParser)
    {
        this.usePullParser = usePullParser;
        TapestryEntityResolver.register(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID, "Tapestry_1_3.dtd");
        TapestryEntityResolver.register(SpecificationParser.TAPESTRY_DTD_1_4_PUBLIC_ID, "Tapestry_1_4.dtd");
    }

    /**
         * @return
         */
    public boolean isDoValidation()
    {
        return doValidation;
    }

    /**
     * Can only be called before the first parse!
     * @param b
     */
    public void setDoValidation(boolean b)
    {
        if (domParser != null || pullParser != null)
        {
            throw new IllegalStateException("can only set validation flag before the first parse!");
        }
        doValidation = b;
    }

    private IDocument getEclipseDocument(String content)
    {
        if (eclipseDocument == null)
        {
            eclipseDocument = new Document();
        }
        eclipseDocument.set(content);
        return eclipseDocument;
    }

    public String getPublicId()
    {
        if (xmlDocument != null)
        {
            DocumentType type = xmlDocument.getDoctype();
            if (type != null)
            {
                return type.getPublicId();
            }
        }
        // TODO handle this in pull parser case!
        return null;
    }

    private void checkPullParser()
    {
        Assert.isTrue(usePullParser, "can't pull parse, I'm set to dom parse!");
        if (pullParser == null)
        {
            if (doValidation)
            {
                pullParseConfiguration = new TapestryParserConfiguration(TapestryParserConfiguration.GRAMMAR_POOL);

            } else
            {
                pullParseConfiguration = new TapestryParserConfiguration();
            }
            pullParser = new TapestryPullParser(pullParseConfiguration);
            pullParser.setSourceResolver(this);
            pullParseConfiguration.setDocumentHandler(pullParser);
            pullParseConfiguration.setErrorHandler(this);
            pullParseConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
            pullParseConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
            pullParseConfiguration.setFeature("http://xml.org/sax/features/validation", doValidation);
            pullParseConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);

        }
    }

    private void checkDomParser()
    {
        Assert.isTrue(!usePullParser, "can't dom parse, I'm set to pull parse!");
        if (domParser == null)
        {
            if (doValidation)
            {
                domParseConfiguration = new TapestryParserConfiguration(TapestryParserConfiguration.GRAMMAR_POOL);

            } else
            {
                domParseConfiguration = new TapestryParserConfiguration();
            }
            domParser = new TapestryDOMParser(domParseConfiguration);
            domParseConfiguration.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
            domParseConfiguration.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
            domParseConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
            domParseConfiguration.setFeature("http://xml.org/sax/features/validation", doValidation);
            domParseConfiguration.setFeature("http://intelligentworks.com/xml/features/augmentations-location", true);
            domParser.setSourceResolver(this);
            domParseConfiguration.setDocumentHandler(domParser);
            domParseConfiguration.setErrorHandler(this);           
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
        xmlDocument = null;
        collectedProblems.clear();
        getEclipseDocument(content);
        hasFatalErrors = false;
        if (usePullParser)
        {
            return pullParse(content);
        } else
        {
            return domParse(content);
        }
    }

    protected Node pullParse(String content) throws IOException
    {
        Assert.isTrue(usePullParser, "can't pull parse, I'm set to dom parse!");
        StringReader reader = new StringReader(content);
        try
        {
            checkPullParser();
            pullParseConfiguration.parse(false);
            return (Node) pullParser;

        } catch (ParserRuntimeException e1)
        {
            // this could happen while scanning the prolog
            createFatalProblem(e1, IProblem.ERROR);
        } catch (Exception e1)
        {
            //ignore
        }
        return null;
    }

    protected Node domParse(String content) throws IOException
    {
        Assert.isTrue(!usePullParser, "can't dom parse, I'm set to pull parse!");
        StringReader reader = new StringReader(content);
        Node result = null;

        try
        {
            checkDomParser();
            domParser.parse(new InputSource(reader));
            xmlDocument = (DocumentImpl) domParser.getDocument();
            result = xmlDocument.getDocumentElement();
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
        return hasFatalErrors;
    }

    public DocumentImpl getParsedDocument()
    {
        Assert.isTrue(!usePullParser, "can't get the document as we are using pull parsing!");
        return xmlDocument;
    }

    public ISourceLocationInfo getSourceLocationInfo(Node node)
    {
        if (xmlDocument != null)
        {
            return (ElementSourceLocationInfo) xmlDocument.getUserData(node, TapestryCore.PLUGIN_ID);
        }
        // TODO handle this for Pull Parsing Case!
        return null;
    }

    public int getLineOffset(int parserReportedLineNumber)
    {
        try
        {
            if (eclipseDocument != null)
            {
                return eclipseDocument.getLineOffset(parserReportedLineNumber - 1);
            }
        } catch (BadLocationException e)
        {
            TapestryCore.log(e);
        }
        return 0;
    }

    public int getColumnOffset(int parserReportedLineNumber, int parserReportedColumn)
    {
        int result = getLineOffset(parserReportedLineNumber);
        int lineCount = eclipseDocument.getNumberOfLines();
        int totalLength = eclipseDocument.getLength();
        if (parserReportedColumn > 0)
        {
            if (parserReportedLineNumber > lineCount)
            {
                result = Math.min(totalLength - 2, result + parserReportedColumn - 1);
            } else
            {
                try
                {
                    int lastCharOnLine = result + eclipseDocument.getLineLength(parserReportedLineNumber - 1) - 1;
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
        if (!collectedProblems.contains(problem))
        {
            collectedProblems.add(problem);
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
        if (collectedProblems != null && !collectedProblems.isEmpty())
        {
            return (IProblem[]) collectedProblems.toArray(new IProblem[collectedProblems.size()]);
        }
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
        hasFatalErrors = true;
        addProblem(createFatalProblem(exception, IMarker.SEVERITY_ERROR));
        TapestryCore.log(exception);
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
