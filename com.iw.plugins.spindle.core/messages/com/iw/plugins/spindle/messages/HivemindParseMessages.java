//Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.iw.plugins.spindle.messages;

import org.apache.hivemind.Location;

/**
 * Messages for the parse package.
 * 
 * @author Howard Lewis Ship
 */
public class HivemindParseMessages
{
    protected static MessageFormatter _formatter = new MessageFormatter(
            "org.apache.hivemind.parse.ParseMessages", "ParseStrings");

    //    public static String dupeAttributeMapping(AttributeMappingDescriptor newDescriptor,
    //            AttributeMappingDescriptor existingDescriptor)
    //    {
    //        return _formatter.format(
    //                "dupe-attribute-mapping",
    //                newDescriptor.getAttributeName(),
    //                existingDescriptor.getLocation());
    //    }
    //
    //    public static String extraMappings(Collection extraNames, ElementModel model)
    //    {
    //        return _formatter.format("extra-mappings", extraNames, model.getElementName());
    //    }
    //
    //    public static String multipleContributionsSchemas(String configurationId, Location location)
    //    {
    //        return _formatter.format("multiple-contributions-schemas", configurationId, location);
    //    }
    //
    //    public static String multipleParametersSchemas(String serviceId, Location location)
    //    {
    //        return _formatter.format("multiple-parameters-schemas", serviceId, location);
    //    }
    //
    //    public static String notModule(String elementName, Location location)
    //    {
    //        return _formatter.format("not-module", elementName, location);
    //    }

    public static String requiredAttribute(String name, String path, Location location)
    {
        return _formatter.format("required-attribute", name, path, location);
    }

    //    public static String unknownAttribute(String name, String path)
    //    {
    //        return _formatter.format("unknown-attribute", name, path);
    //    }
    //
    //    public static String booleanAttribute(String value, String name, String path)
    //    {
    //        return _formatter.format("boolean-attribute", new Object[]
    //        { value, name, path });
    //    }
    //
    //    public static String invalidAttributeValue(String value, String name, String path)
    //    {
    //        return _formatter.format("invalid-attribute-value", new Object[]
    //        { value, name, path });
    //
    //    }
    //
    //    public static String invalidNumericValue(String value, String name, String path)
    //    {
    //        return _formatter.format("invalid-numeric-value", new Object[]
    //        { value, name, path });
    //    }
    //
    //    public static String unableToInitialize(Throwable cause)
    //    {
    //        return _formatter.format("unable-to-initialize", cause);
    //    }
    //
    //    public static String badRuleClass(String className, Location location, Throwable cause)
    //    {
    //        return _formatter.format("bad-rule-class", className, location, cause);
    //    }
    //
    //    public static String errorReadingDescriptor(Resource resource, Throwable cause)
    //    {
    //        return _formatter.format("error-reading-descriptor", resource, cause);
    //    }
    //
    //    public static String missingResource(Resource resource)
    //    {
    //        return _formatter.format("missing-resource", resource);
    //    }
    //
    //    public static String unexpectedElement(String elementName, String elementPath)
    //    {
    //        return _formatter.format("unexpected-element", elementName, elementPath);
    //    }
    //
    //    public static String invalidAttributeFormat(String attributeName, String value, String
    // elementPath,
    //            String formatKey)
    //    {
    //        String inputValueFormat = _formatter.getMessage(formatKey);
    //
    //        return _formatter.format("invalid-attribute-format", new Object[]
    //        { attributeName, value, elementPath, inputValueFormat });
    //    }
    //
    //    public static String duplicateSchema(String schemaId, Schema existingSchema)
    //    {
    //        return _formatter.format("duplicate-schema", schemaId, existingSchema.getLocation());
    //    }
    //
    //    public static String invalidElementKeyAttribute(String schemaId, Throwable cause)
    //    {
    //        return _formatter.format("invalid-element-key-attribute", schemaId, cause);
    //    }
}