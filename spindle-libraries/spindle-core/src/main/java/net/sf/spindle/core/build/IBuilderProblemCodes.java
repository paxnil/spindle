package net.sf.spindle.core.build;

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
/**
 * Codes added to problem markers. To allow for future QuickFix capability
 */
public interface IBuilderProblemCodes
{
    int NOT_QUICK_FIXABLE = -1;

    int MANUAL_FIX_ONLY = 0;

    // somebody asked for a page that does not exist
    int TAP_NAMESPACE_NO_SUCH_PAGE = 1;

    // somebody asked for a .script file that does not exist
    int SPINDLE_MISSING_SCRIPT = 2;

    // a jwc file is being overriden by something else
    // in a way that a user would not expect
    // this is a tough one.
    int SPINDLE_BUILDER_HIDDEN_JWC_FILE = 3;

    // a page is being overriden by something else
    // in a way that a user would not expect
    // this is a tough one.
    int SPINDLE_BUILDER_HIDDEN_PAGE_FILE = 4;

    // expected doc root to be "application" got something else
    int SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_APPLICATION = 5;

    int SPINDLE_MALFORMED_OGNL_EXPRESSION = 6;

    // a pattern mismatch - TODO remove
    int SPINDLE_PATTERN_MISMATCH = 7;

    // location does not exist
    int SPINDLE_RESOURCE_LOCATION_DOES_NOT_EXIST = 8;

    // unable to resolve a fully qualified type name
    int SPINDLE_MISSING_TYPE = 9;

    // asset ids must be unique
    int COMPONENT_SPEC_DUPLICATE_ASSET_ID = 10;

    // bean ids must be unique
    int COMPONENT_SPEC_DUPLICATE_BEAN_ID = 11;

    // binding names must be unique within a component tag
    int COMPONENT_SPEC_DUPLICATE_BINDING_NAME = 12;

    // component ids must be unique
    int COMPONENT_SPEC_DUPLICATE_COMPONENT_NAME = 13;

    // pattern violation
    int COMPONENT_SPEC_INVALID_COMPONENT_ID = 14;

    // must have one of "type" or "copy-of" attrs
    int COMPONENT_SPEC_MISSING_TYPE_COPY_OF = 15;

    // can't have both "type" and "copy-of" attrs
    int COMPONENT_SPEC_BOTH_TYPE_COPY_OF = 16;

    // component to copy is missing
    int COMPONENT_SPEC_COPY_OF_MISSING = 17;

    // pattern violation
    int COMPONENT_SPEC_INVALID_COMPONENT_TYPE = 18;

    // field bindings went out with Tapestry 3.0
    // the fix is to replace with a regular binding
    // and use OGNL expression to get the static info
    // TODO insert example xml here
    int COMPONENT_SPEC_FIX_FIELD_BINDING = 19;

    // expected doc root to be "page-specification" got something else
    int SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_PAGE_SPECIFICATION = 20;

    // expected doc root to be "component-specification" got something else
    int SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_COMPONENT_SPECIFICATION = 21;

    // "reserved-paramter" element not allowed in .page files
    int COMPONENT_RESERVED_PARAMETER_NOT_ALLOWED = 22;

    // parameter names must be unique within a component
    int COMPONENT_SPEC_DUPLICATE_PARAMETER_NAME = 23;

    // self explanetory
    int COMPONENT_SPEC_REQUIRED_PARAMTER_MAY_NOT_HAVE_DEFAULT = 24;

    // property ids must be unique
    int SPINDLE_DUPLICATE_PROPERTY_ID = 25;

    // pattern violation
    int SPINDLE_INVALID_PROPERTY_ID = 26;

    // reserved parm names must be unique
    int COMPONENT_DUPLICATE_RESERVED_PARAMETER_NAME = 27;

    // spindle can't verify the existence of a template
    // file defined using an external asset
    int TEMPLATE_FROM_EXTERNAL_ASSET = 28;

    // an asset was defined but nothing exists there.
    int COMPONENT_MISSING_ASSET = 29;

    // an asset was defined but path value.
    int COMPONENT_NULL_ASSET = 30;

    // cases where informal parameters are disallowed.
    int COMPONENT_INFORMALS_NOT_ALLOWED = 31;

    // somebody missed a required parameter!
    int COMPONENT_REQUIRED_PARAMETER_NOT_BOUND = 32;

    // the value of attr "type" in element "component" can
    // not be resolved
    int COMPONENT_TYPE_DOES_NOT_EXIST = 33;

    int WEB_XML_MISSING_APPLICATION_SERVLET_CLASS = 34;

    int WEB_XML_INCORRECT_APPLICATION_SERVLET_CLASS = 35;

    // pattern violation
    int COMPONENT_INVALID_PARAMETER_NAME = 36;

    // pattern violation
    int COMPONENT_INVALID_BEAN_NAME = 37;

    // pattern violation
    int COMPONENT_INVALID_ASSET_NAME = 38;

    // expected doc root to be "library" got something else
    int SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_LIBRARY = 39;

    // pattern violation
    int LIBRARY_INVALID_COMPONENT_TYPE = 40;

    // pattern violation
    int LIBRARY_DUPLICATE_COMPONENT_TYPE = 41;

    int EXTENSION_DUPLICATE_PROPERTY = 42;

    int EXTENSIION_INVALID_NAME = 43;

    int LIBRARY_DUPLICATE_EXTENSION_NAME = 44;

    int LIBRARY_INVALID_CHILD_LIB_ID = 45;

    int LIBRARY_INVALID_PAGE_NAME = 46;

    int LIBRARY_DUPLICATE_PAGE_NAME = 47;

    int LIBRARY_INVALID_SERVICE_NAME = 48;

    int LIBRARY_DUPLICATE_SERVICE_NAME = 49;

    int SPINDLE_MISSING_PUBLIC_ID = 50;

    int SPINDLE_INVALID_PUBLIC_ID = 51;

    int TAP_FAILED_TO_CONVERT_TO_BOOLEAN = 52;

    int TAP_FAILED_TO_CONVERT_TO_INT = 53;

    int TAP_FAILED_TO_CONVERT_TO_LONG = 54;

    int TAP_FAILED_TO_CONVERT_TO_DOUBLE = 55;

    int EXTENDED_ATTRIBUTE_NO_VALUE_OR_BODY = 56;

    int EXTENDED_ATTRIBUTE_BOTH_VALUE_AND_BODY = 57;

    int TEMPLATE_SCANNER_DUPLICATE_ID = 58;

    int TEMPLATE_SCANNER_REQUIRED_PARAMETER_NOT_BOUND = 59;

    int TEMPLATE_SCANNER_NO_INFORMALS_ALLOWED = 60;

    int TEMPLATE_SCANNER_CHANGE_TO_EXPRESSION = 61;

    int TEMPLATE_SCANNER_BOUND_IN_BOTH_SPEC_AND_TEMPLATE = 62;

    int TEMPLATE_SCANNER_TEMPLATE_EXPRESSION_FOR_RESERVED_PARM = 63;

    int TEMPLATE_SCANNER_FORMAL_STRING_BINDING_ALREADY_BOUND_IN_SPEC = 64;

    int EXTENSION_CONFIG_MISSING_TYPE_CONVERTER = 65;

    int LIBRARY_CHILD_LIBRARY_INVALID_PATH = 66;

    int LIBRARY_CHILD_LIBRARY_MISSING_PATH = 67;

    // pattern violation
    int COMPONENT_INVALID_ASSET_PROPERTY_NAME = 68;

    int ASSET_UNRECOGNIZED_PREFIX = 69;

    int COMPONENT_INVALID_INJECT_PROPERTY_NAME = 70;

    int SPINDLE_UNSUPPORTED_PAGE_CLASS_META = 71;

    int SPINDLE_UNSUPPORTED_COMPONENT_CLASS_META = 72;

    int SPINDLE_UNSUPPORTED_PAGE_NAME = 73;

    int SPINDLE_NO_EXPLICIT_PAGE_CLASS = 74;

    int SPINDLE_NO_EXPLICIT_COMPONENT_CLASS = 75;

    int SPINDLE_UNSUPPORTED_COMPONENT_NAME = 76;
    
    int IMPLICIT_ASSET_BINDING_MISSING_ASSET = 77;
    
    int IMPLICIT_COMPONENT_BINDING_MISSING_COMPONENT = 78;
    
    int IMPLICIT_BEAN_BINDING_MISSING_BEAN = 79;

    int INJECT_MISSING_META = 80;
    
    int INJECT_MISSING_PAGE = 81;
    
    int INJECT_INCORRECT_SCRIPT_NAME = 82;
}