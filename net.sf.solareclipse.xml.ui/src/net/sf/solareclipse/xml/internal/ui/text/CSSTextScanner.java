/**********************************************************************
Copyright (c) 2003  Vasanth Dharmaraj and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:

$Id$
**********************************************************************/
package net.sf.solareclipse.xml.internal.ui.text;

import net.sf.solareclipse.xml.ui.text.ICSSSyntaxConstants;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 
 * 
 * @author Vasanth Dharmaraj
 */
public class CSSTextScanner extends RuleBasedScanner {
	// TODO: move to the properties or xml...
	public static final String[] fgUnits = {
		"", "cm", "em", "ex", "in", "pc", "pt", "px",
	};

	// TODO: move to the properties or xml...
	public static final String[] fgProperties = {
		"azimuth",
		"background",
		"background-attachment",
		"background-color",
		"background-image",
		"background-position",
		"background-repeat",
		"border",
		"border-collapse",
		"border-color",
		"border-spacing",
		"border-style",
		"border-bottom",
		"border-bottom-color",
		"border-bottom-style",
		"border-bottom-width",
		"border-left",
		"border-left-color",
		"border-left-style",
		"border-left-width ",
		"border-right",
		"border-right-color",
		"border-right-style",
		"border-right-width",
		"border-top",
		"border-top-color",
		"border-top-style",
		"border-top-width",
		"border-width",
		"bottom",
		"caption",
		"clear",
		"clip",
		"color",
		"content",
		"counter-increment",
		"counter-reset",
		"cue",
		"cue-after",
		"cue-before",
		"cursor",
		"direction",
		"display",
		"elevation",
		"empty-cells",
		"float",
		"font",
		"font-family",
		"font-size",
		"font-size-adjust ",
		"font-stretch",
		"font-style",
		"font-variant",
		"font-weight",
		"height",
		"left",
		"letter-spacing",
		"line-height",
		"list-style",
		"list-style-image",
		"list-style-position",
		"list-style-type",
		"margin",
		"margin-top",
		"marker-offset",
		"marks",
		"max-height",
		"max-width",
		"min-height",
		"min-width",
		"orphans",
		"outline",
		"outline-color",
		"outline-style",
		"outline-width",
		"overflow",
		"padding",
		"padding-top",
		"padding-right",
		"padding-bottom",
		"padding-left",
		"page",
		"page-break-after",
		"page-break-before",
		"page-break-inside",
		"pause",
		"pause-after",
		"pause-before",
		"pitch",
		"pitch-range",
		"play-during",
		"position",
		"quotes",
		"richness",
		"right",
		"size",
		"speak",
		"speak-header",
		"speak-numeral",
		"speak-punctuation",
		"speech-rate",
		"stress",
		"table-layout",
		"text-align",
		"text-decoration",
		"text-indent",
		"text-shadow",
		"text-transform",
		"top",
		"unicode-bidi",
		"vertical-align",
		"visibility",
		"voice-family",
		"volume",
		"white-space",
		"widows",
		"width",
		"word-spacing",
		"z-index",
		"margin-left",
		"margin-right",
		"medium",
		"margin-bottom"
	};
	private static final String[] fgPropertiesUC = toUpper(fgProperties);

	public static final String[] fgHTML = {
		"visited",
		"hover",
		"active",
		"a",
		"abbr",
		"acronym",
		"address",
		"applet",
		"area",
		"b",
		"base",
		"basefont",
		"bdo",
		"big",
		"blockquote",
		"body",
		"br",
		"button",
		"caption",
		"center",
		"cite",
		"code",
		"col",
		"colgroup",
		"dd",
		"del",
		"dfn",
		"dir",
		"div",
		"dl",
		"dt",
		"em",
		"fieldset",
		"font",
		"form",
		"frame",
		"frameset",
		"h1",
		"h2",
		"h3",
		"h4",
		"h5",
		"h6",
		"head",
		"hr",
		"html",
		"i",
		"iframe",
		"img",
		"input",
		"ins",
		"isindex",
		"kbd",
		"label",
		"legend",
		"li",
		"link",
		"map",
		"menu",
		"meta",
		"noframes",
		"noscript",
		"object",
		"ol",
		"optgroup",
		"option",
		"p",
		"param",
		"pre",
		"q",
		"s",
		"samp",
		"script",
		"select",
		"small",
		"span",
		"strike",
		"strong",
		"style",
		"sub",
		"sup",
		"table",
		"tbody",
		"td",
		"textarea",
		"tfoot",
		"th",
		"thead",
		"title",
		"tr",
		"tt",
		"u",
		"ul",
		"var",
		"block",
		"list-item",
		"none",
		"table-row",
		"table-header-group",
		"table-row-group",
		"table-footer-group",
		"table-column",
		"table-column-group",
		"table-cell",
		"table-caption",
		"bolder",
		"italic",
		"monospace",
		"super",
		"line-through",
		"inset",
		"underline",
		"small-caps",
		"thin",
		"dotted",
		"invert",
		"focus",
		"ltr",
		"bidi-override",
		"rtl",
		"bidi-override",
		"embed",
		"avoid",
		"x-low",
		"low",
		"high",
		"x-high",
		"decimal"
	};
	private static final String[] fgHTMLUC = toUpper(fgHTML);

	private static String[] toUpper(String[] lower) {
		int length = lower.length;
		String[] upper = new String[length];
		for (int i = 0; i < length; i++) {
			upper[i] = lower[i].toUpperCase();
		}
		return upper;
	}

	public CSSTextScanner(Map tokens) {
		IToken other = (Token) tokens.get(ICSSSyntaxConstants.CSS_DEFAULT);

		setDefaultReturnToken(other);

		IToken idRule = (Token) tokens.get(ICSSSyntaxConstants.CSS_ID_RULE);
		IToken atRule = (Token) tokens.get(ICSSSyntaxConstants.CSS_AT_RULE);

		IToken property = (Token) tokens.get(ICSSSyntaxConstants.CSS_PROPERTY);
		IToken value = (Token) tokens.get(ICSSSyntaxConstants.CSS_VALUE);
// TODO: colors
//		IToken clazz = (Token) tokens.get(ICSSSyntaxConstants.CSS_CLASS);
		IToken number = (Token) tokens.get(ICSSSyntaxConstants.CSS_NUMBER);
		IToken string = (Token) tokens.get(ICSSSyntaxConstants.CSS_STRING);

		List rules = new ArrayList();

		rules.add(new WhitespaceRule(new WhitespaceDetector()));

		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		rules.add(new SingleLineRule("'", "'", string, '\\'));

		rules.add(new CSSColorRule(value));

		rules.add(new CSSNameRule('#', idRule));
		rules.add(new CSSNameRule('@', atRule));

		CSSNumberRule numberRule = new CSSNumberRule(other);
		for (int i = 0; i < fgUnits.length; i++) {
			numberRule.addPostfix(fgUnits[i], number);
		}
		rules.add(numberRule);

// TODO: colors
//		rules.add(new WordRule(new CSSClassDetector(), clazz));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule = new WordRule(new CSSWordDetector(), other);
		for (int i = 0; i < fgProperties.length; i++) {
			wordRule.addWord(fgProperties[i], property);
		}
		for (int i = 0; i < fgPropertiesUC.length; i++) {
			wordRule.addWord(fgPropertiesUC[i], property);
		}
		for (int i = 0; i < fgHTML.length; i++) {
			wordRule.addWord(fgHTML[i], value);
		}
		for (int i = 0; i < fgHTMLUC.length; i++) {
			wordRule.addWord(fgHTMLUC[i], value);
		}
		rules.add(wordRule);

		setRules((IRule[]) rules.toArray(new IRule[rules.size()]));
	}
}
