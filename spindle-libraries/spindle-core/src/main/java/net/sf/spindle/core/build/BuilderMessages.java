package net.sf.spindle.core.build;

import net.sf.spindle.core.messages.DefaultTapestryMessages;
import net.sf.spindle.core.messages.MessageFormatter;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.types.IJavaType;

import org.apache.hivemind.Resource;

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
public class BuilderMessages
{

    private static MessageFormatter FORMATTER = new MessageFormatter(BuilderMessages.class,
            "BuilderMessages");

    public static String getApplicationServletClassname()
    {
        return FORMATTER.getMessage("applicationServletClassname");
    }

    public static String namespaceClash(ICoreNamespace ns1, ICoreNamespace ns2)
    {
        return FORMATTER.format("namespaceClash", ns1.getSpecificationLocation(), ns2
                .getSpecificationLocation());
    }

    public static String fullBuildStarting()
    {
        return FORMATTER.getMessage("full-build-starting");
    }

    public static String incrementalBuildStarting()
    {
        return FORMATTER.getMessage("incremental-build-starting");
    }

    public static String buildMissMessage(String name)
    {
        return FORMATTER.format("missed-file-message", name);
    }

    public static String locatingNamespaces()
    {
        return FORMATTER.getMessage("locating-namespaces");
    }

    public static String locatingArtifacts()
    {
        return FORMATTER.getMessage("locating-artifacts");
    }

    public static String noTapestryServlet()
    {
        return FORMATTER
                .format("fatal-no-tapestry-servlet-class", getApplicationServletClassname());
    }

    public static String missingApplicationSpec(Resource nsLocation)
    {
        return FORMATTER.format("failed-missing-application-spec", nsLocation.toString());
    }

    public static String scanning(Resource resource)
    {
        return FORMATTER.format("scanning", resource.toString());
    }

    public static String fatalErrorCouldNotParseWebXML(Resource webXML)
    {
        return FORMATTER.format("could-not-parse-web-xml", webXML.toString());
    }

    public static String fatalErrorNoValidTapestryServlets()
    {
        return FORMATTER.getMessage("abort-no-valid-application-servlets-found");
    }

    public static String fatalErrorTooManyValidTapestryServlets()
    {
        return FORMATTER.getMessage("abort-too-many-valid-servlets-found");
    }

    public static String webXMLDoesNotExist(Resource resource)
    {
        return FORMATTER.format("web-xml-does-not-exist", resource.toString());
    }

    public static String circularError(String extraMessage)
    {
        return FORMATTER.format("build-failed-circular-component-reference", extraMessage);
    }

    public static String hiddenJWCFile(Resource hidden, Resource hiddenBy)
    {
        return FORMATTER.format("builder-hidden-jwc-file", hidden, hiddenBy);
    }

    public static String hiddenPageFile(Resource hidden, Resource hiddenBy)
    {
        return FORMATTER.format("builder-hidden-page-file", hidden, hiddenBy);
    }

    public static String webXMLIgnorepathNotFound(Resource location)
    {
        return webXMLIgnorepathNotFound(location == null ? "no location found" : location
                .toString());
    }

    public static String webXMLIgnorepathNotFound(String location)
    {
        return FORMATTER.format(
                "web-xml-ignore-application-path-not-found",
                location == null ? "no location found" : location.toString());
    }

    public static String webXMLWrongFileExtension(Resource location)
    {
        return webXMLWrongFileExtension(location.toString());
    }

    public static String webXMLWrongFileExtension(String location)
    {
        return FORMATTER.format("web-xml-wrong-file-extension", location);
    }

    public static String webXMLPathParamButServletDefines(String classname)
    {
        return FORMATTER.format("web-xml-application-path-param-but-servlet-defines", classname);
    }

    public static String mustBeClassNotInterface(IJavaType type)
    {
        return FORMATTER
                .format("web-xml-must-be-class-not-interface", type.getFullyQualifiedName());
    }

    public static String typeDoesNotExist(String fqn)
    {
        return DefaultTapestryMessages.format("unable-to-resolve-class", fqn);
    }

    public static String webXMLContextParamNullKey()
    {
        return FORMATTER.getMessage("web-xml-context-param-null-key");
    }

    public static String webXMLContextParamDuplicateKey(String key)
    {
        return FORMATTER.format("web-xml-context-param-duplicate-key", key);
    }

    public static String webXMLContextParamNullValue()
    {
        return FORMATTER.getMessage("web-xml-context-param-null-value");
    }

    public static String webXMLInitParamNullKey()
    {
        return FORMATTER.getMessage("web-xml-init-param-null-key");
    }

    public static String webXMLInitParamDuplicateKey(String key)
    {
        return FORMATTER.format("web-xml-init-param-duplicate-key", key);
    }

    public static String webXMLInitParamNullValue()
    {
        return FORMATTER.getMessage("web-xml-init-param-null-value");
    }

    public static String webXMLServletNullClassname(String servletName)
    {
        return FORMATTER.format("web-xml-servlet-null-classname", servletName);
    }

    public static String webXMLIgnoreInvalidApplicationPath(IJavaType servletType, String path)
    {
        return FORMATTER.format("web-xml-ignore-invalid-application-path", servletType
                .getFullyQualifiedName(), path);
    }

    public static String webXMLServletHasNoName()
    {
        return FORMATTER.getMessage("web-xml-servlet-has-null-name");
    }

    public static String webXMLDuplicateServletName(String name)
    {
        return FORMATTER.format("web-xml-servlet-duplicate-name", name);
    }

    public static String format(String key, Object obj)
    {
        return FORMATTER.format(key, obj);
    }

    public static String format(String key, Object... obj)
    {
        return FORMATTER.format(key, obj);
    }

    public static String missingNonLocalizedAsset(String name, Resource resource)
    {
        return FORMATTER.format("scan-component-missing-asset-but-has-i18n", name, resource
                .toString());
    }

    public static String missingAsset(String name, Resource resource)
    {
        return FORMATTER.format("scan-component-missing-asset", name, resource.toString());
    }

}
