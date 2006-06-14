/**
 * 
 */
package net.sf.spindle.core.spec;
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
public enum SpecType {
    UNKNOWN,
    APPLICATION_SPEC,
    ASSET_SPEC,
    BEAN_SPEC,
    BINDING_SPEC,
    COMPONENT_SPEC,
    CONTAINED_COMPONENT_SPEC,
    EXTENSION_CONFIGURATION,
    EXTENSION_SPEC,
    LIBRARY_SPEC,
    LISTENER_BINDING_SPEC,
    PARAMETER_SPEC,
    PROPERTY_SPEC,
    BINDING_BEAN_INIT,
    PROPERTY_DECLARATION,
    PAGE_DECLARATION,
    COMPONENT_TYPE_DECLARATION,
    DESCRIPTION_DECLARATION,
    RESERVED_PARAMETER_DECLARATION,
    ENGINE_SERVICE_DECLARATION,
    LIBRARY_DECLARATION,
    CONFIGURE_DECLARATION,
    INJECT,
    @Deprecated
    EXPRESSION_BEAN_INIT,
    @Deprecated
    FIELD_BEAN_INIT,
    @Deprecated
    STATIC_BEAN_INIT,
    @Deprecated
    STRING_BEAN_INIT
}