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
import org.apache.hivemind.Resource;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.spec.IComponentSpecification;

/**
 * Messages for the resolver package.
 * 
 * @author Howard Lewis Ship
 * @since 4.0
 */
public class ResolverMessages
{
    protected static MessageFormatter _formatter = new MessageFormatter("org.apache.tapestry.resolver.ResolverMessages",
            "ResolverStrings");

    public static String noSuchComponentType(String type, INamespace namespace)
    {
        return _formatter.format("no-such-component-type", type, namespace);
    }
    
    public static String noSuchComponentTypeUnknown(String type)
    {
        return _formatter.format("no-such-component-type", type, "unknown");
    }

    public static String noSuchPage(String name, INamespace namespace)
    {
        return _formatter.format("no-such-page", name, namespace.getNamespaceId());
    }

    public static String resolvingComponent(String type, INamespace namespace)
    {
        return _formatter.format("resolving-component", type, namespace);
    }

    public static String checkingResource(Resource resource)
    {
        return _formatter.format("checking-resource", resource);
    }

    public static String installingComponent(String type, INamespace namespace,
            IComponentSpecification specification)
    {
        return _formatter.format("installing-component", type, namespace, specification);
    }

    public static String installingPage(String pageName, INamespace namespace,
            IComponentSpecification specification)
    {
        return _formatter.format("installing-page", pageName, namespace, specification);
    }

    public static String resolvingPage(String pageName, INamespace namespace)
    {
        return _formatter.format("resolving-page", pageName, namespace);
    }

    public static String foundFrameworkPage(String pageName)
    {
        return _formatter.format("found-framework-page", pageName);
    }

    public static String foundHTMLTemplate(Resource resource)
    {
        return _formatter.format("found-html-template", resource);
    }
}