/*
 * Created on Apr 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.iw.plugins.spindle.xmlinspector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DefaultJDOMFactory;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.assist.DTDAccess;
import com.wutka.dtd.DTD;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WebXMLInspector {

	public static final String UNNAMED_SERVLET = "UNNAMED";

	private WebXMLInspector() {
		super();
	}

	public static Document loadWebXML(InputStream is) throws Exception {
		Document result = null;
		if (is != null) {

			SAXBuilder builder = new SAXBuilder();
			builder.setFactory(new DefaultJDOMFactory());
			//				builder.setEntityResolver(new StrutsResolver(version));
			builder.setValidation(false);
			result = builder.build(is);
		}
		return result;
	}

	public static Map getServlets(Document document) {
		Map result = new HashMap();
		int counter = 1;
		List servletElements = document.getRootElement().getChildren("servlet");
		for (Iterator iter = servletElements.iterator(); iter.hasNext();) {
			Element servletElement = (Element) iter.next();

			Element nameElement = servletElement.getChild("servlet-name");
			String name = nameElement != null ? nameElement.getTextTrim()
					: null;

			Element typeElement = servletElement.getChild("servlet-class");
			String classname = typeElement != null ? typeElement.getTextTrim()
					: null;

			name = name == null || name.length() == 0 ? UNNAMED_SERVLET
					+ (counter++) : name;
			result.put(name, classname);
		}
		return result;
	}

	public static Element createServletElement(Namespace namespace, String name, String type) {
		Element servletElement = new Element("servlet", namespace);		
		Element servletName = new Element("servlet-name", namespace);
		Element servletClass = new Element("servlet-class", namespace);

		servletName.setText(name);
		servletElement.addContent(servletName);
		servletClass.setText(type);
		servletElement.addContent(servletClass);

		return servletElement;
	}

	public static Element createServletMapping(Namespace namespace,String name, String urlPattern) {
		Element mappingElement = new Element("servlet-mapping", namespace);		
		Element servletName = new Element("servlet-name", namespace);
		Element pattern = new Element("url-pattern", namespace);

		servletName.setText(name);
		mappingElement.addContent(servletName);
		pattern.setText(urlPattern);
		mappingElement.addContent(pattern);

		return mappingElement;
	}

	public static void addTapestryServlet(Element rootElement,
			String servletName, String servletClass, String urlPattern) {
		Namespace n = rootElement.getNamespace();
		Element servlet = createServletElement(n, servletName, servletClass);
		Element servletMapping = createServletMapping(n, servletName, urlPattern);

		List children = rootElement.getChildren();
		if (children.isEmpty()) {
			rootElement.addContent(servlet);
			rootElement.addContent(servletMapping);
		} else {

			DTD dtd = DOMValidator.getDTD(TapestryCore.SERVLET_2_3_PUBLIC_ID);

			List elements = DTDAccess.getAllowedChildren(dtd, "web-app", null,
					false);

			insertElement(rootElement, servlet, elements);
			insertElement(rootElement, servletMapping, elements);
		}
	}

	private static void insertElement(Element rootElement,
			Element toBeInserted, List allowedChildren) {

		String insertedName = toBeInserted.getName();
		Assert.isLegal(allowedChildren.contains(insertedName),
				"can't insert an element not allowed by DTD");
		List children = rootElement.getChildren();
		int index = allowedChildren.indexOf(insertedName);
		if (children.isEmpty()) {
			// just add it
			rootElement.addContent(toBeInserted);
		} else if (index == allowedChildren.size() - 1) {
			//last one
			children.add(toBeInserted);
			return;
		} else {
			List preList = allowedChildren.subList(0, index);
			Element preceeder = null;
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (!(obj instanceof Element))
					continue;
				Element element = (Element) obj;
				if (preList.contains(element.getName()))
					preceeder = element;

			}
			if (preceeder == null) {
				children.add(0, toBeInserted);
			} else {
				int insert = children.indexOf(preceeder) + 1;
				children.add(insert, toBeInserted);
			}
		}
	}

	public static String printDocument(Document document, String indentString) throws IOException {
		XMLOutputter outputter = new XMLOutputter();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		outputter.setNewlines(true);
		outputter.setIndent(indentString);
		outputter.setLineSeparator("\n");
		outputter.setTextNormalize(true);
		outputter.setExpandEmptyElements(false);

		outputter.output(document, out);
		
		return out.toString();
	}
	
	public static void main(String [] args) throws Exception {
		File f = new File(args[0]);
		Document d = loadWebXML(new FileInputStream(f));
		System.out.println(printDocument(d, "\t"));
		
		addTapestryServlet(d.getRootElement(), "Test", "org.Test", "poo");
		
		System.out.println(printDocument(d, "\t"));
	}

}