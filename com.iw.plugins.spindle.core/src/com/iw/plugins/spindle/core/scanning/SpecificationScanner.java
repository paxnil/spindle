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

package com.iw.plugins.spindle.core.scanning;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.Tapestry;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.Direction;
import org.apache.tapestry.spec.SpecFactory;
import org.apache.tapestry.util.IPropertyHolder;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 *  Scanner for building Tapestry Specs
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class SpecificationScanner extends AbstractScanner
{

    protected interface IConverter
    {
        public Object convert(String value) throws ScannerException;
    }

    protected static class BooleanConverter implements IConverter
    
    {
        public Object convert(String value) throws ScannerException
        {
            Object result = conversionMap.get(value.toLowerCase());

            if (result == null || !(result instanceof Boolean))
                throw new ScannerException(Tapestry.getString("SpecificationParser.fail-convert-boolean", value));

            return result;
        }
    }

    protected static class IntConverter implements IConverter
    {
        public Object convert(String value) throws ScannerException
        {
            try
            {
                return new Integer(value);
            } catch (NumberFormatException ex)
            {
                throw new ScannerException(Tapestry.getString("SpecificationParser.fail-convert-int", value), ex);
            }
        }
    }

    protected static class LongConverter implements IConverter
    {
        public Object convert(String value) throws ScannerException
        {
            try
            {
                return new Long(value);
            } catch (NumberFormatException ex)
            {
                throw new ScannerException(Tapestry.getString("SpecificationParser.fail-convert-long", value), ex);
            }
        }
    }

    protected static class DoubleConverter implements IConverter
    {
        public Object convert(String value) throws ScannerException
        {
            try
            {
                return new Double(value);
            } catch (NumberFormatException ex)
            {
                throw new ScannerException(Tapestry.getString("SpecificationParser.fail-convert-double", value), ex);
            }
        }
    }

    protected static class StringConverter implements IConverter
    {
        public Object convert(String value)
        {
            return value.trim();
        }
    }

    protected SpecFactory specificationFactory;

    /**
     *  We can share a single map for all the XML attribute to object conversions,
     *  since the keys are unique.
     * 
     **/
    protected static final Map conversionMap = new HashMap();

    // Identify all the different acceptible values.
    // We continue to sneak by with a single map because
    // there aren't conflicts;  when we have 'foo' meaning
    // different things in different places in the DTD, we'll
    // need two maps.

    static {

        conversionMap.put("true", Boolean.TRUE);
        conversionMap.put("t", Boolean.TRUE);
        conversionMap.put("1", Boolean.TRUE);
        conversionMap.put("y", Boolean.TRUE);
        conversionMap.put("yes", Boolean.TRUE);
        conversionMap.put("on", Boolean.TRUE);

        conversionMap.put("false", Boolean.FALSE);
        conversionMap.put("f", Boolean.FALSE);
        conversionMap.put("0", Boolean.FALSE);
        conversionMap.put("off", Boolean.FALSE);
        conversionMap.put("no", Boolean.FALSE);
        conversionMap.put("n", Boolean.FALSE);

        conversionMap.put("none", BeanLifecycle.NONE);
        conversionMap.put("request", BeanLifecycle.REQUEST);
        conversionMap.put("page", BeanLifecycle.PAGE);
        conversionMap.put("render", BeanLifecycle.RENDER);

        conversionMap.put("boolean", new BooleanConverter());
        conversionMap.put("int", new IntConverter());
        conversionMap.put("double", new DoubleConverter());
        conversionMap.put("String", new StringConverter());
        conversionMap.put("long", new LongConverter());

        conversionMap.put("in", Direction.IN);
        conversionMap.put("form", Direction.FORM);
        conversionMap.put("custom", Direction.CUSTOM);
    }

    protected void processProperty(IPropertyHolder holder, Node node)
    {
        String name = getAttribute(node, "name", true);

        // Starting in DTD 1.4, the value may be specified
        // as an attribute.  Only if that is null do we
        // extract the node's value.

        try
        {
            String value = getExtendedAttribute(node, "value", true);
            holder.setProperty(name, value);
        } catch (ScannerException e)
        {
            addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), e.getMessage());
        }

    }

    /**
     *  Used in several places where an element's only possible children are
     *  &lt;property&gt; elements.
     * 
     **/

    protected void processPropertiesInNode(IPropertyHolder holder, Node node)
    {
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (isElement(child, "property"))
            {
                processProperty(holder, child);
                continue;
            }
        }
    }

    /**
     *  Sets the SpecFactory which instantiates Tapestry spec objects.
     * 
     *  @since 1.0.9
     **/

    public void setFactory(SpecFactory factory)
    {
        specificationFactory = factory;
    }

    /**
     *  Returns the current SpecFactory which instantiates Tapestry spec objects.
     * 
     *  @since 1.0.9
     * 
     **/

    public SpecFactory getFactory()
    {
        return specificationFactory;
    }

   

    /** 
     *  Used with many elements that allow a value to be specified as either
     *  an attribute, or as wrapped character data.  This handles that case,
     *  and makes it an error to specify both.
     * 
     **/

    protected String getExtendedAttribute(Node node, String attributeName, boolean required) throws ScannerException
    {

        int DTDVersion = XMLUtil.getDTDVersion(parser.getPublicId());
        String attributeValue = getAttribute(node, attributeName);
        boolean nullAttributeValue = Tapestry.isNull(attributeValue);
        boolean nullBodyValue = true;
        String bodyValue = null;
        if (DTDVersion >= XMLUtil.DTD_1_4)
        {
            bodyValue = getValue(node);
            nullBodyValue = Tapestry.isNull(bodyValue);
        }

        if (!nullAttributeValue && !nullBodyValue)
            throw new ScannerException(
                Tapestry.getString("SpecificationParser.no-attribute-and-body", attributeName, node.getNodeName()));

        if (required && nullAttributeValue && nullBodyValue)
            throw new ScannerException(
                Tapestry.getString("SpecificationParser.required-extended-attribute", node.getNodeName(), attributeName));

        if (nullAttributeValue)
            return bodyValue;

        return attributeValue;
    }    }
