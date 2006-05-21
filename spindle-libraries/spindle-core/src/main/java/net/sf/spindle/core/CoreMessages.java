package net.sf.spindle.core;

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
import net.sf.spindle.core.messages.MessageFormatter;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceRoot;

import org.apache.hivemind.Resource;

/**
 * @author gwl
 */
public class CoreMessages
{
    private static MessageFormatter FORMATTER = new MessageFormatter(CoreMessages.class,
            "CoreMessages");

    public static String resourceDoesNotExist(Resource location)
    {
        return FORMATTER.format("core_resource_does_not_exist", location.toString());
    }

    public static Object getXMLComment()
    {
        return FORMATTER.getMessage("TAPESTRY.xmlComment");
    }

    public static String invalidPublicID()
    {
        return FORMATTER.getMessage("error_invalid_spec_public_id");
    }

    public static String projectNamespace()
    {
        return FORMATTER.getMessage("project_namespace");
    }

    public static String contextRootNotExist(IResourceRoot contextRoot)
    {
        return FORMATTER.format("context_root_not_exist", (contextRoot == null ? FORMATTER
                .getMessage("was_null") : ""));
    }

    public static String classpathRootNotExist(IResourceRoot classpathRoot)
    {
        return FORMATTER.format("classpath_root_not_exist", (classpathRoot == null ? FORMATTER
                .getMessage("was_null") : ""));
    }

    public static String getTapestryServletClassname()
    {
        return FORMATTER.getMessage("tapestry_servlet_classname");
    }

    public static String tapestryJarsMissing()
    {
        return FORMATTER.getMessage("tapestry_servlet_not_found_on_classpath");
    }

    public static String missingWebXMLFile(ICoreResource webXML)
    {
        return FORMATTER.format("missing_web_XML_file", webXML.toString());
    }

    public static String projectMetaDataMissingNatureId(String namespaceIdentifier, String name)
    {
        return FORMATTER.format("project-metadata-missing-natureId", namespaceIdentifier, name);
    }

    public static String componentClassMetaIncompatability()
    {
        return FORMATTER.getMessage("unsupported_namespace_meta_component_classes");
    }

    public static String pageClassMetaIncompatability()
    {
        return FORMATTER.getMessage("unsupported_namespace_meta_page_classes");
    }

    public static String unsupportedPageName(String name)
    {
        return FORMATTER.format("unsupported-page-name", name);
    }
    
    public static String unsupportedComponentName(String name)
    {
        return FORMATTER.format("unsupported-component-name", name);
    }
}