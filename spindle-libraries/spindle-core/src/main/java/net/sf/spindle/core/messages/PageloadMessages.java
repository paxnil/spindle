package net.sf.spindle.core.messages;

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
import org.apache.tapestry.IComponent;
import org.apache.tapestry.spec.IComponentSpecification;

public class PageloadMessages
{
    protected static MessageFormatter _formatter = new MessageFormatter(
            "org.apache.tapestry.pageload.PageloadMessages", "PageloadStrings");

    public static String parameterMustHaveNoDefaultValue(IComponent component, String name)
    {
        return _formatter.format(
                "parameter-must-have-no-default-value",
                component.getExtendedId(),
                name);
    }

    public static String parameterMustHaveNoDefaultValue(String componentName, String name)
    {
        return _formatter.format("parameter-must-have-no-default-value", componentName, name);
    }

    public static String unableToInitializeProperty(String propertyName, IComponent component,
            Throwable cause)
    {
        return _formatter.format("unable-to-initialize-property", propertyName, component, cause);
    }

    public static String requiredParameterNotBound(String name, String containedName)
    {
        return _formatter.format("required-parameter-not-bound", name, containedName);
    }

    public static String inheritInformalInvalidComponentFormalOnly(String containedName)
    {
        return _formatter.format("inherit-informal-invalid-component-formal-only", containedName);
    }

    public static String inheritInformalInvalidContainerFormalOnly(
            IComponentSpecification container, IComponentSpecification component)
    {
        return _formatter.format("inherit-informal-invalid-container-formal-only", container
                .getSpecificationLocation().getName(), component.getSpecificationLocation()
                .getName());
    }

    public static String formalParametersOnly(String containedName, String parameterName)
    {
        return _formatter.format("formal-parameters-only", containedName, parameterName);
    }

    public static String unableToInstantiateComponent(IComponent container, Throwable cause)
    {
        return _formatter.format(
                "unable-to-instantiate-component",
                container.getExtendedId(),
                cause);
    }

    public static String classNotComponent(Class componentClass)
    {
        return _formatter.format("class-not-component", componentClass.getName());
    }

    public static String unableToInstantiate(String className, Throwable cause)
    {
        return _formatter.format("unable-to-instantiate", className, cause);
    }

    public static String pageNotAllowed(String componentId)
    {
        return _formatter.format("page-not-allowed", componentId);
    }

    public static String classNotPage(Class componentClass)
    {
        return _formatter.format("class-not-page", componentClass.getName());
    }

    public static String defaultParameterName(String name)
    {
        return _formatter.format("default-parameter-name", name);
    }

    public static String initializerName(String propertyName)
    {
        return _formatter.format("initializer-name", propertyName);
    }

    public static String parameterName(String name)
    {
        return _formatter.format("parameter-name", name);
    }

}