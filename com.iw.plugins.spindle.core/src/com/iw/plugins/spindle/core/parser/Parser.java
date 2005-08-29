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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParser;
import com.iw.plugins.spindle.core.parser.xml.dom.TapestryDOMParserConfiguration;
import com.iw.plugins.spindle.core.parser.xml.pull.TapestryPullParser;
import com.iw.plugins.spindle.core.parser.xml.pull.TapestryPullParserConfiguration;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationResolver;
import com.iw.plugins.spindle.core.source.SourceLocation;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.Files;

/**
 * The xml parser used in the builds. can be used to dom-parse or pull-parse XML
 * content Validates by default
 * 
 * @author glongman@gmail.com
 */
public class Parser implements ISourceLocationResolver, XMLErrorHandler,
		IProblemCollector {

	private boolean fUsePullParser = false;

	private IDocument fEclipseDocument;

	private DocumentImpl fXmlDocument;

	private TapestryPullParserConfiguration fPullParseConfiguration;

	private TapestryDOMParserConfiguration fDomParseConfiguration;

	private TapestryDOMParser fDomParser = null;

	private TapestryPullParser fPullParser = null;

	private List fCollectedProblems = new ArrayList();

	private boolean fDoValidation = false;

	private boolean fHasFatalErrors;

	public Parser() {
		this(false);
	}

	public Parser(boolean usePullParser) {
		fUsePullParser = usePullParser;
	}

	public boolean isDoValidation() {
		return fDoValidation;
	}

	/**
	 * Can only be called before the first parse!
	 * 
	 * @param b
	 */
	public void setDoValidation(boolean flag) {
		if (fDomParser != null || fPullParser != null)
			throw new IllegalStateException(
					"can only set validation flag before the first parse!");

		fDoValidation = flag;
	}

	private IDocument getEclipseDocument(String content) {
		if (fEclipseDocument == null)
			fEclipseDocument = new Document();

		fEclipseDocument.set(content);
		return fEclipseDocument;
	}

	public IDocument getEclipseDocument() {
		return fEclipseDocument;
	}

	// public String getPublicId()
	// {
	// if (!fUsePullParser)
	// {
	// DocumentType type = fXmlDocument.getDoctype();
	// if (type != null)
	// return type.getPublicId();
	//
	// } else
	// {
	// return fPullParser.getPublicId();
	// }
	// return null;
	// }

	private void checkPullParser() {
		Assert
				.isTrue(fUsePullParser,
						"can't pull parse, I'm set to dom parse!");
		if (fPullParser == null) {

			fPullParseConfiguration = new TapestryPullParserConfiguration();
			fPullParser = new TapestryPullParser(fPullParseConfiguration);
			fPullParser.setSourceResolver(this);
			fPullParseConfiguration.setDocumentHandler(fPullParser);
			fPullParseConfiguration.setErrorHandler(this);
			fPullParseConfiguration
					.setFeature(
							"http://apache.org/xml/features/continue-after-fatal-error",
							false);
			fPullParseConfiguration.setFeature(
					"http://xml.org/sax/features/validation", fDoValidation);
			fPullParseConfiguration
					.setFeature(
							"http://intelligentworks.com/xml/features/augmentations-location",
							true);
			if (fDoValidation)
				fPullParseConfiguration
						.setProperty(
								"http://apache.org/xml/properties/internal/grammar-pool",
								TapestryDOMParserConfiguration.GRAMMAR_POOL);

		}
	}

	private void checkDomParser() {
		Assert.isTrue(!fUsePullParser,
				"can't dom parse, I'm set to pull parse!");
		if (fDomParser == null) {

			fDomParseConfiguration = new TapestryDOMParserConfiguration(
					TapestryDOMParserConfiguration.GRAMMAR_POOL);

			fDomParser = new TapestryDOMParser(fDomParseConfiguration);
			fDomParseConfiguration.setFeature(
					"http://apache.org/xml/features/dom/defer-node-expansion",
					false);
			fDomParseConfiguration
					.setFeature(
							"http://apache.org/xml/features/continue-after-fatal-error",
							false);
			fDomParseConfiguration
					.setFeature(
							"http://apache.org/xml/features/dom/include-ignorable-whitespace",
							false);
			fDomParseConfiguration.setFeature(
					"http://xml.org/sax/features/validation", fDoValidation);
			fDomParseConfiguration
					.setFeature(
							"http://intelligentworks.com/xml/features/augmentations-location",
							true);
			fDomParser.setSourceResolver(this);
			fDomParseConfiguration.setDocumentHandler(fDomParser);
			fDomParseConfiguration.setErrorHandler(this);
		}
	}

	public DocumentImpl parse(IStorage storage, String encoding)
			throws IOException, CoreException {

		try {
			return parse(storage.getContents(), encoding);

		} catch (CoreException e) {
			// if (e.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL)
			// {
			// ErrorDialog.openError(TapestryCore.getDefault().getActiveWorkbenchShell(),
			// "Resource
			// out of Sync", null, e.getStatus());
			// } else {

			e.printStackTrace();
			// }
			throw e;
		}
	}

	public DocumentImpl parse(InputStream input, String encoding)
			throws IOException {
		String content = Files.readFileToString(input, encoding);
		return parse(content);
	}

	public DocumentImpl parse(String content) throws IOException {
		fXmlDocument = null;
		beginCollecting();
		try {
			getEclipseDocument(content);
			fHasFatalErrors = false;
			if (false) {
				return pullParse(content);
			} else {
				return domParse(content);
			}
		} finally {
			endCollecting();
		}
	}

	protected DocumentImpl pullParse(String content) throws IOException {
		Assert.isTrue(false, "pull parser on the way out!");
		// Node result = null;
		// StringReader reader = new StringReader(content);
		// try
		// {
		//
		// checkPullParser();
		// fPullParseConfiguration.setInputSource(new XMLInputSource(null, "",
		// null,
		// reader, null));
		// fPullParseConfiguration.parse();
		// if (!fHasFatalErrors)
		// result = (Node) fPullParser.getRoot();
		//
		// } catch (ParserRuntimeException e1)
		// {
		// // this could happen while scanning the prolog
		// createFatalProblem(e1, IProblem.ERROR);
		// } catch (Exception e1)
		// {
		// e1.printStackTrace();
		// }
		// return result;
		return null;
	}

	protected DocumentImpl domParse(String content) throws IOException {
		Assert.isTrue(!fUsePullParser,
				"can't dom parse, I'm set to pull parse!");
		StringReader reader = new StringReader(content);
		Node result = null;

		try {
			checkDomParser();
			fDomParser.parse(new InputSource(reader));
		} catch (Exception e) {
			e.printStackTrace();
			// there was a fatal error - return null
			// all the exceptions are collected already because I am an
			// XMLErrorHandler
			return null;
		} finally {
			reader.close();
		}

		fXmlDocument = (DocumentImpl) fDomParser.getDocument();

		return fXmlDocument;
	}

	public boolean getHasFatalErrors() {
		return fHasFatalErrors;
	}

	public DocumentImpl getParsedDocument() {
		Assert.isTrue(!fUsePullParser,
				"can't get the document as we are using pull parsing!");
		return fXmlDocument;
	}

	public int getLineOffset(int parserReportedLineNumber) {
		try {
			if (fEclipseDocument != null)
				return fEclipseDocument
						.getLineOffset(parserReportedLineNumber - 1);

		} catch (BadLocationException e) {
			TapestryCore.log(e);
		}
		return 0;
	}

	public int getColumnOffset(int parserReportedLineNumber,
			int parserReportedColumn) {
		return getColumnOffset(parserReportedLineNumber, parserReportedColumn,
				(char) 0);
	}

	public int getColumnOffset(int parserReportedLineNumber,
			int parserReportedColumn, char expected) {
		int result = getLineOffset(parserReportedLineNumber);
		int lineCount = fEclipseDocument.getNumberOfLines();
		int totalLength = fEclipseDocument.getLength();
		if (parserReportedColumn > 0) {
			if (parserReportedLineNumber > lineCount) {
				result = Math.min(totalLength - 2, result
						+ parserReportedColumn - 1);
			} else {
				try {
					int lastCharOnLine = result
							+ fEclipseDocument
									.getLineLength(parserReportedLineNumber - 1);
					result = Math.min(lastCharOnLine, result
							+ parserReportedColumn - 1);
					result = Math.min(result, totalLength - 1);
					char c;
					while (true) {
						c = fEclipseDocument.getChar(result);
						if (c == '\r' || c == '\n') {
							result--;
						} else {
							break;
						}
					}
					if (expected > 0 && c != expected) {
						while (true) {
							c = fEclipseDocument.getChar(--result);
							if (c == expected)
								break;
						}
					}
				} catch (BadLocationException e) {
					TapestryCore.log(e);
				}
			}

		}
		return result;
	}

	public int[] trim(int startOffset, int stopOffset) {
		int[] result = new int[] { startOffset, stopOffset };
		try {
			String content = fEclipseDocument.get(startOffset, stopOffset
					- startOffset + 1);
			int oldLength = content.length();
			if (oldLength == 0)
				return result;

			int index = 0;

			while (index < oldLength
					&& Character.isWhitespace(content.charAt(index)))
				index++;

			if (index == oldLength)
				return result;

			oldLength -= (index - 1);

			content = content.trim();
			int newLength = content.length();
			if (newLength == 0)
				return result;

			result[0] = startOffset + index;
			result[1] = result[0] + newLength;

			return result;

		} catch (BadLocationException e) {
			// eat it.
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iw.plugins.spindle.core.source.ISourceLocationResolver#getTagNameLocation(com.iw.plugins.spindle.core.source.ISourceLocation)
	 */
	public ISourceLocation getTagNameLocation(String elementName,
			ISourceLocation elementStartLocation) {
		int offset = -1;
		int line = -1;

		try {
			FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(
					fEclipseDocument);
			IRegion region = finder.find(elementStartLocation.getCharStart(),
					elementName, true, false, true, false);
			if (region != null) {
				offset = region.getOffset();
				line = fEclipseDocument.getLineOfOffset(offset) + 1;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		if (offset == -1 || offset > elementStartLocation.getCharEnd())
			return elementStartLocation;

		int start = offset;
		int end = offset + elementName.length();
		return new SourceLocation(line, start, end);
	}

	public void beginCollecting() {
		fCollectedProblems.clear();
	}

	public void endCollecting() {
	}

	public void addProblem(IProblem problem) {
		if (!fCollectedProblems.contains(problem)) {
			fCollectedProblems.add(problem);
		}
	}

	public void addProblem(int severity, ISourceLocation location,
			String message, boolean isTemporary, int code) {
		addProblem(new DefaultProblem(ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
				severity, message, location.getLineNumber(), location
						.getCharStart(), location.getCharEnd(), isTemporary,
				code));
	}

	public void addProblem(IStatus status, ISourceLocation location,
			boolean isTemporary) {
		addProblem(new DefaultProblem(ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
				status, location.getLineNumber(), location.getCharStart(),
				location.getCharEnd(), isTemporary));
	}

	public IProblem[] getProblems() {
		if (fCollectedProblems != null && !fCollectedProblems.isEmpty())
			return (IProblem[]) fCollectedProblems
					.toArray(new IProblem[fCollectedProblems.size()]);

		return new IProblem[0];
	}

	private IProblem createFatalProblem(XMLParseException parseException,
			int severity) {
		return createParserProblem(
				ITapestryMarker.TAPESTRY_FATAL_PROBLEM_MARKER, parseException,
				severity);
	}

	private IProblem createErrorProblem(XMLParseException parseException,
			int severity) {
		return createParserProblem(
				ITapestryMarker.TAPESTRY_SOURCE_PROBLEM_MARKER, parseException,
				severity);
	}

	private IProblem createParserProblem(String type, XMLParseException ex,
			int severity) {

		int lineNumber = Math.max(ex.getLineNumber() - 1, 0);
		int charStart = -1;
		int charEnd = -1;

		try {
			charStart = fEclipseDocument.getLineOffset(lineNumber);
			charEnd = Math.max(charStart, charStart
					+ fEclipseDocument.getLineLength(lineNumber) - 1);
		} catch (BadLocationException e) {
			TapestryCore.log("exception line:" + ex.getLineNumber()
					+ "document line count:"
					+ fEclipseDocument.getNumberOfLines(), e);
		}

		return new DefaultProblem(type, severity, ex.getMessage(), lineNumber,
				charStart, charEnd, false, IProblem.NOT_QUICK_FIXABLE);
	}

	// *** XMLErrorHandler for DOM && PULL Parsing **

	/**
	 * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(String, String,
	 *      XMLParseException)
	 */
	public void error(String domain, String key, XMLParseException exception)
			throws XNIException {
		addProblem(createErrorProblem(exception, IMarker.SEVERITY_ERROR));
	}

	/**
	 * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(String,
	 *      String, XMLParseException)
	 */
	public void fatalError(String domain, String key,
			XMLParseException exception) throws XNIException {
		fHasFatalErrors = true;
		addProblem(createFatalProblem(exception, IMarker.SEVERITY_ERROR));
	}

	/**
	 * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(String, String,
	 *      XMLParseException)
	 */
	public void warning(String domain, String key, XMLParseException exception)
			throws XNIException {
		addProblem(createErrorProblem(exception, IMarker.SEVERITY_WARNING));
	}

	// /*** END OF XMLErrorHandler for DOM && PULL Parsing **

}