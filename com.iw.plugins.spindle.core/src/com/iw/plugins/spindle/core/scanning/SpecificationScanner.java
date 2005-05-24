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

package com.iw.plugins.spindle.core.scanning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hivemind.Resource;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.util.IPropertyHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.spec.IPluginDescribable;
import com.iw.plugins.spindle.core.spec.IPluginPropertyHolder;
import com.iw.plugins.spindle.core.spec.PluginDescriptionDeclaration;
import com.iw.plugins.spindle.core.spec.PluginPropertyDeclaration;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.XMLPublicIDUtil;
import com.iw.plugins.spindle.messages.ParseMessages;

/**
 * Scanner for building Tapestry Specs - this is the base class for Component, Library, and
 * Application scanners. This class handles some setup and validates the document by default.
 * 
 * @author glongman@gmail.com
 */
public abstract class SpecificationScanner extends AbstractScanner
{

    protected Resource fResourceLocation;

    protected String fPublicId;

    protected int fPublicIdCode;

    protected boolean fIsTapestry_4_0;

    protected Node fRootNode;

    protected IPropertySource fPropertySource;

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#beforeScan(java.lang.Object)
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        if (!isDocument(source))
            return null;

        Document document = (Document) source;
        setPublicId(W3CAccess.getPublicId(document));
        if (fPublicId == null)
            throw new ScannerException(CoreMessages.format(XMLPublicIDUtil.SPEC_DTD_ERROR_KEY), false,
                    IProblem.SPINDLE_MISSING_PUBLIC_ID);
        if (!checkPublicId())
        {
            throw new ScannerException(CoreMessages.format(XMLPublicIDUtil.SPEC_DTD_ERROR_KEY), false,
                    IProblem.SPINDLE_INVALID_PUBLIC_ID);
        }
        fRootNode = document.getDocumentElement();
        return document;
    }

    /**
     * @return
     */
    protected boolean checkPublicId()
    {
        boolean ok = false;
        if (fPublicId != null)
        {
            int version = XMLPublicIDUtil.getDTDVersion(fPublicId);
            if (version != XMLPublicIDUtil.UNKNOWN_DTD)
            {
                for (int i = 0; i < XMLPublicIDUtil.ALLOWED_SPEC_DTDS.length; i++)
                {
                    if (XMLPublicIDUtil.ALLOWED_SPEC_DTDS[i] == version)
                    {
                        ok = true;
                        break;
                    }
                }
            }
        }
        return ok;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(java.lang.Object,
     *      java.lang.Object)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {
        validate(source);
    }

    protected void validate(Object source)
    {
        DOMValidator validator = new DOMValidator();
        validator.validate((Document) source);
        IProblem[] validationProblems = validator.getProblems();
        for (int i = 0; i < validationProblems.length; i++)
        {
            addProblem(validationProblems[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#afterScan(java.lang.Object)
     */
    protected Object afterScan(Object scanResults) throws ScannerException
    {
        fResourceLocation = null;
        return super.afterScan(scanResults);
    }

    public void setResourceLocation(Resource location)
    {
        fResourceLocation = location;
    }

    protected void setPublicId(String publicId)
    {
        fPublicId = publicId;
        fPublicIdCode = XMLPublicIDUtil.getDTDVersion(fPublicId);
        fIsTapestry_4_0 = fPublicIdCode == XMLPublicIDUtil.DTD_4_0;
    }

    public static interface IConverter
    {
        public Object convert(String value) throws ScannerException;
    }

    public static class BooleanConverter implements IConverter

    {
        public Object convert(String value) throws ScannerException
        {
            Object result = TYPE_CONVERSION_MAP.get(value.toLowerCase());

            if (result == null || !(result instanceof Boolean))
                throw new ScannerException(ParseMessages.failConvertBoolean(value), false,
                        IProblem.TAP_FAILED_TO_CONVERT_TO_BOOLEAN);

            return result;
        }
    }

    public static class IntConverter implements IConverter
    {
        public Object convert(String value) throws ScannerException
        {
            try
            {
                return new Integer(value);
            }
            catch (NumberFormatException ex)
            {
                throw new ScannerException(ParseMessages.failConvertInt(value), ex, false,
                        IProblem.TAP_FAILED_TO_CONVERT_TO_INT);
            }
        }
    }

    public static class LongConverter implements IConverter
    {
        public Object convert(String value) throws ScannerException
        {
            try
            {
                return new Long(value);
            }
            catch (NumberFormatException ex)
            {
                throw new ScannerException(ParseMessages.failConvertLong(value), ex, false,
                        IProblem.TAP_FAILED_TO_CONVERT_TO_LONG);
            }
        }
    }

    public static class DoubleConverter implements IConverter
    {
        public Object convert(String value) throws ScannerException
        {
            try
            {
                return new Double(value);
            }
            catch (NumberFormatException ex)
            {
                throw new ScannerException(ParseMessages.failConvertDouble(value), ex, false,
                        IProblem.TAP_FAILED_TO_CONVERT_TO_DOUBLE);
            }
        }
    }

    public static class StringConverter implements IConverter
    {
        public Object convert(String value)
        {
            return value.trim();
        }
    }

    static class IgnoreNodeException extends ScannerException
    {

        public IgnoreNodeException()
        {
            super("", false, -1);
        }

    }

//    /**
//     * @deprecated
//     */
//    protected SpecFactory fSpecificationFactory;

    /**
     * We can share a single map for all the XML attribute to object conversions, since the keys are
     * unique.
     */
    public static final Map TYPE_CONVERSION_MAP = new HashMap();

    public static final List TYPE_LIST = new ArrayList();

    // Identify all the different acceptible values.
    // We continue to sneak by with a single map because
    // there aren't conflicts; when we have 'foo' meaning
    // different things in different places in the DTD, we'll
    // need two maps.

    static
    {

        TYPE_CONVERSION_MAP.put("true", Boolean.TRUE);
        TYPE_CONVERSION_MAP.put("t", Boolean.TRUE);
        TYPE_CONVERSION_MAP.put("1", Boolean.TRUE);
        TYPE_CONVERSION_MAP.put("y", Boolean.TRUE);
        TYPE_CONVERSION_MAP.put("yes", Boolean.TRUE);
        TYPE_CONVERSION_MAP.put("on", Boolean.TRUE);
        TYPE_CONVERSION_MAP.put("aye", Boolean.TRUE);

        TYPE_CONVERSION_MAP.put("false", Boolean.FALSE);
        TYPE_CONVERSION_MAP.put("f", Boolean.FALSE);
        TYPE_CONVERSION_MAP.put("0", Boolean.FALSE);
        TYPE_CONVERSION_MAP.put("off", Boolean.FALSE);
        TYPE_CONVERSION_MAP.put("no", Boolean.FALSE);
        TYPE_CONVERSION_MAP.put("n", Boolean.FALSE);
        TYPE_CONVERSION_MAP.put("nay", Boolean.FALSE);

        TYPE_CONVERSION_MAP.put("none", BeanLifecycle.NONE);
        TYPE_CONVERSION_MAP.put("request", BeanLifecycle.REQUEST);
        TYPE_CONVERSION_MAP.put("page", BeanLifecycle.PAGE);
        TYPE_CONVERSION_MAP.put("render", BeanLifecycle.RENDER);

        TYPE_CONVERSION_MAP.put("boolean", new BooleanConverter());
        TYPE_CONVERSION_MAP.put("int", new IntConverter());
        TYPE_CONVERSION_MAP.put("double", new DoubleConverter());
        TYPE_CONVERSION_MAP.put("String", new StringConverter());
        TYPE_CONVERSION_MAP.put("long", new LongConverter());

        TYPE_LIST.add("boolean");
        TYPE_LIST.add("boolean[]");

        TYPE_LIST.add("short");
        TYPE_LIST.add("short[]");

        TYPE_LIST.add("int");
        TYPE_LIST.add("int[]");

        TYPE_LIST.add("long");
        TYPE_LIST.add("long[]");

        TYPE_LIST.add("float");
        TYPE_LIST.add("float[]");

        TYPE_LIST.add("double");
        TYPE_LIST.add("double[]");

        TYPE_LIST.add("char");
        TYPE_LIST.add("char[]");

        TYPE_LIST.add("byte");
        TYPE_LIST.add("byte[]");

        TYPE_LIST.add("java.lang.Object");
        TYPE_LIST.add("java.lang.Object[]");

        TYPE_LIST.add("java.lang.String");
        TYPE_LIST.add("java.lang.String[]");
    }

    protected boolean scanMeta(IPluginPropertyHolder holder, Node node) throws ScannerException
    {
        if (!fIsTapestry_4_0)
            return scanProperty_3_0(holder, node);
        if (!isElement(node, "meta"))
            return false;
        scanProperty(holder, node, "key", "value");
        return true;
    }

    private boolean scanProperty_3_0(IPluginPropertyHolder holder, Node node)
            throws ScannerException
    {
        if (!isElement(node, "property"))
            return false;
        scanProperty(holder, node, "name", "value");
        return true;
    }

    private void scanProperty(IPluginPropertyHolder holder, Node node, String keyAttribute,
            String valueAttribute) throws ScannerException
    {
        String name = getAttribute(node, keyAttribute, false);

        if (holder.getProperty(name) != null)
        {
            addProblem(
                    IProblem.WARNING,
                    getAttributeSourceLocation(node, keyAttribute),
                    "duplicate definition of property: " + name,
                    false,
                    IProblem.SPINDLE_DUPLICATE_PROPERTY_ID);
        }

        // must be done now - not revalidatable
        ExtendedAttributeResult result = null;
        String value = null;
        try
        {
            result = getExtendedAttribute(node, valueAttribute, true);
            value = result.value;
        }
        catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false, e.getCode());
        }

        PluginPropertyDeclaration declaration = new PluginPropertyDeclaration(name, value);
        declaration.setLocation(getSourceLocationInfo(node));
        declaration.setValueIsFromAttribute(result == null ? false : result.fromAttribute);

        if (value != null && value.trim().length() == 0)
        {
            addProblem(
                    IProblem.WARNING,
                    result.fromAttribute ? getAttributeSourceLocation(node, valueAttribute)
                            : getBestGuessSourceLocation(node, true),
                    "missing value of property: " + name,
                    false,
                    IProblem.MANUAL_FIX_ONLY);
        }

        holder.addPropertyDeclaration(declaration);
    }

    /**
     * Used in several places where an element's only possible children are &lt;property&gt;
     * elements.
     */

    protected void allowMeta(IPropertyHolder holder, Node node) throws ScannerException
    {
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (scanMeta((IPluginPropertyHolder) holder, node))
                continue;
        }
    }

//    /**
//     * Sets the SpecFactory which instantiates Tapestry spec objects.
//     * @de
//     * @since 1.0.9
//     */
//
//    public void setFactory(SpecFactory factory)
//    {
//        fSpecificationFactory = factory;
//    }
//
//    /**
//     * Returns the current SpecFactory which instantiates Tapestry spec objects.
//     * 
//     * @since 1.0.9
//     */
//
//    public SpecFactory getFactory()
//    {
//        return fSpecificationFactory;
//    }

    /**
     * Used with many elements that allow a value to be specified as either an attribute, or as
     * wrapped character data. This handles that case, and makes it an error to specify both.
     */

    protected ExtendedAttributeResult getExtendedAttribute(Node node, String attributeName,
            boolean required) throws ScannerException
    {

        String attributeValue = getAttribute(node, attributeName);
        boolean nullAttributeValue = TapestryCore.isNull(attributeValue);
        String bodyValue = getValue(node);
        boolean nullBodyValue = TapestryCore.isNull(bodyValue);

        if (!nullAttributeValue && !nullBodyValue)
            throw new ScannerException(ParseMessages.noAttributeAndBody(attributeName, node
                    .getNodeName()), getNodeBodySourceLocation(node), false,
                    IProblem.EXTENDED_ATTRIBUTE_BOTH_VALUE_AND_BODY);

        if (required && nullAttributeValue && nullBodyValue)
            throw new ScannerException(ParseMessages.requiredExtendedAttribute(node
                    .getNodeName(), attributeName), getNodeStartSourceLocation(node), false,
                    IProblem.EXTENDED_ATTRIBUTE_NO_VALUE_OR_BODY);

        ExtendedAttributeResult result = new ExtendedAttributeResult();
        if (nullAttributeValue)
        {

            result.value = bodyValue;
        }
        else
        {
            result.value = attributeValue;
            result.fromAttribute = true;

        }

        return result;
    }

    static public class ExtendedAttributeResult
    {
        public String value;

        public boolean fromAttribute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#beforeScan(java.lang.Object)
     */
    protected boolean isDocument(Object source) throws ScannerException
    {
        Assert.isLegal(source instanceof Document);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#cleanup()
     */
    protected void cleanup()
    {
    }

    /**
     * @return Returns the propertySource.
     */
    public IPropertySource getPropertySource()
    {
        return fPropertySource;
    }

    /**
     * @param propertySource
     *            The propertySource to set.
     */
    public void setPropertySource(IPropertySource propertySource)
    {
        this.fPropertySource = propertySource;
    }

    protected boolean scanDescription(IPluginDescribable describable, Node node)
    {
        if (!isElement(node, "description"))
            return false;

        String value = getValue(node);
        describable.setDescription(value);
        PluginDescriptionDeclaration declaration = new PluginDescriptionDeclaration(null, value,
                getSourceLocationInfo(node));
        describable.addDescriptionDeclaration(declaration);

        return true;
    }
}