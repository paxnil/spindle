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
package com.iw.plugins.spindle.core.spec;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.spec.IExtensionSpecification;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.SpecificationScanner;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class PluginExtensionConfiguration extends DescribableSpecification
{

    static final String[] typeNames = { "boolean", "String", "double", "int", "long" };
    static final Double trueD = new Double(1.0);
    static final Double falseD = new Double(0.0);
    static final Integer trueI = new Integer(1);
    static final Integer falseI = new Integer(0);
    static final Long trueL = new Long(1);
    static final Long falseL = new Long(0);

    static final public Map classToString;
    static final public Map stringToClass;

    static {

        classToString = new HashMap();
        classToString.put(String.class, "String");
        classToString.put(Boolean.class, "boolean");
        classToString.put(Integer.class, "int");
        classToString.put(Double.class, "double");
        classToString.put(Long.class, "long");

        stringToClass = new HashMap();
        stringToClass.put("String", String.class);
        stringToClass.put("boolean", Boolean.class);
        stringToClass.put("int", Integer.class);
        stringToClass.put("double", Double.class);
        stringToClass.put("long", Long.class);

    }

     public Object fValueObject;
    public Class fType;

    private String fDeclaredType;
    private String fDeclaredValue;
    private boolean fDeclaredValueIsFromAttribute;

    /**
     * Constructor for PluginExtensionConfiguration.
     */
    public PluginExtensionConfiguration(String propertyName, Object value)
    {
        super(BaseSpecification.EXTENSION_CONFIGURATION);
        setIdentifier(propertyName);
        fValueObject = value;
        fType = value == null ? null : value.getClass();
    }

    public PluginExtensionConfiguration(
        String propertyName,
        String declaredValue,
        String declaredType,
        ILocation location)
    {
        this(propertyName, null);
        fDeclaredType = declaredType;
        fDeclaredValue = declaredValue;
        setLocation(location);
    }

    private Class checkType(String newType)
    {
        return (Class) stringToClass.get(newType);
    }

    private Object convertValue(Class type, Object value)
    {

        if (type == value.getClass())
            return value;

        if (type == String.class)
            return value.toString();

        if (type == Boolean.class)
            return convertToBoolean(value);

        if (type == Double.class)
            return convertToDouble(value);

        if (type == Long.class)
            return convertToLong(value);

        if (type == Integer.class)
            return convertToInteger(value);

        return value;
    }

    private Boolean convertToBoolean(Object value)
    {
        Class clazz = value.getClass();
        if (clazz == Boolean.class)
            return (Boolean) value;

        if (value instanceof Number)
        {
            if (((Number) value).longValue() == 0)
            {
                return Boolean.FALSE;
            } else
            {
                return Boolean.TRUE;
            }
        }

        if (clazz == String.class)
        {
            String svalue = (String) value;
            if (svalue.equalsIgnoreCase("true") || svalue.equalsIgnoreCase("yes"))
                return Boolean.TRUE;

            if (svalue.equalsIgnoreCase("false") || svalue.equalsIgnoreCase("no"))
                return Boolean.FALSE;

            return new Boolean(svalue != null && !"".equals(svalue));
        }
        return Boolean.TRUE;
    }

    private Long convertToLong(Object value)
    {
        Class clazz = value.getClass();
        if (clazz == Long.class)
            return (Long) value;

        if (clazz == Boolean.class)
        {
            boolean flag = ((Boolean) value).booleanValue();

            if (flag)
                return trueL;

            return falseL;
        }
        if (value instanceof Number)
            return new Long(((Number) value).longValue());

        if (clazz == String.class)
        {
            try
            {
                return new Long((String) value);
            } catch (NumberFormatException e)
            {}

        }
        return falseL;
    }

    private Double convertToDouble(Object value)
    {
        Class clazz = value.getClass();
        if (clazz == Double.class)
            return (Double) value;

        if (clazz == Boolean.class)
        {
            boolean flag = ((Boolean) value).booleanValue();
            if (flag)
                return trueD;

            return falseD;
        }

        if (value instanceof Number)
        {
            return new Double(((Number) value).doubleValue());
        }

        if (clazz == String.class)
        {
            try
            {
                return new Double((String) value);
            } catch (NumberFormatException e)
            {}
        }
        return falseD;
    }

    private Integer convertToInteger(Object value)
    {
        Class clazz = value.getClass();
        if (clazz == Integer.class)
            return (Integer) value;

        if (clazz == Boolean.class)
        {
            if (((Boolean) value).booleanValue())
                return trueI;

            return falseI;
        }

        if (value instanceof Number)
            return new Integer(((Number) value).intValue());

        if (clazz == String.class)
        {
            try
            {
                return new Integer((String) value);
            } catch (NumberFormatException e)
            {}
        }
        return falseI;
    }

    /**
      *  Revalidate this declaration. Note that validating the existence of the value is only possible
      *  during a parse/scan cycle. But that's ok 'cuz those kinds of problems
      *  would have already been caught.
      * 
      * @param parent the object holding this
      * @param validator a validator helper
      */

    public void validate(Object parent, IScannerValidator validator)
    {

        IExtensionSpecification extension = (IExtensionSpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        try
        {
          
            if (fDeclaredType != null)
            {
                SpecificationScanner.IConverter converter =
                    (SpecificationScanner.IConverter) SpecificationScanner.TYPE_CONVERSION_MAP.get(fDeclaredType);
                Object objectValue = null;

                if (converter == null)
                {
                    validator.addProblem(
                        IProblem.ERROR,
                        sourceInfo.getAttributeSourceLocation("type"),
                        TapestryCore.getTapestryString("SpecificationParser.unknown-static-value-type", fDeclaredType));
                } else if (fDeclaredValue != null)
                {
                    try
                    {
                        objectValue = converter.convert(fDeclaredValue);
                    } catch (ScannerException e2)
                    {
                        ISourceLocation problemLocation = null;

                        if (fDeclaredValueIsFromAttribute)
                        {
                            problemLocation = sourceInfo.getAttributeSourceLocation("value");
                        } else
                        {
                            problemLocation = sourceInfo.getContentSourceLocation();
                        }
                        validator.addProblem(IProblem.ERROR, problemLocation, e2.getMessage());
                    }
                }
            }

        } catch (ScannerException e)
        {
            // TODO remove
            e.printStackTrace();
            TapestryCore.log(e);
        }

    }

    /**
     * @return
     */
    public boolean isDeclaredValueIsFromAttribute()
    {
        return fDeclaredValueIsFromAttribute;
    }

    /**
     * @param b
     */
    public void setDeclaredValueIsFromAttribute(boolean b)
    {
        fDeclaredValueIsFromAttribute = b;
    }

}
