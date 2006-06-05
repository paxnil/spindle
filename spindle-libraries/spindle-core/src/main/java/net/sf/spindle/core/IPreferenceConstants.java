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
/**
 * Preference contstants for the Core plugin.
 * <p>
 * Overview:
 * <ul>
 * <li>{@link #BUILDER_MARKER_MISSES}
 * <p>
 * The priority given to an {@link net.sf.spindle.core.source.IProblem} if the build 'misses' a file
 * in the project. A build miss signifies that a tapestry source file exists in the project but is
 * is not in a location where tapestry would pick it up at runtime.
 * </p>
 * <p>
 * An example would be a MyComponent.jwc file in the context root. Tapestry almost never looks in
 * the context root for components and any runtime reference to 'MyComponent' would result in an
 * {@link org.apache.hivemind.ApplicationRuntimeException}.
 * </p>
 * <p>
 * for allowed values see {@link #ERROR}, {@link #WARN}, {@link #INFO}, {@link #IGNORE}
 * <p>
 * </li>
 * <li>{@link #BUILDER_HANDLE_ASSETS}
 * <p>
 * The priority given to an {@link net.sf.spindle.core.source.IProblem} if the build finds that a
 * page or component that has a template only has I18N equivalents.
 * <p>
 * For example 'MyComponent.jwc' has template 'MyComponent_en.html" but not "MyComponent.html"
 * </p>
 * <p>
 * In this case if the say, the current locale was 'en_ca', Tapestry would not find
 * 'MyComponent_en_ca.html' and so it would fall back to looking for 'MyComponent.html', which would
 * result in an {@link org.apache.hivemind.ApplicationRuntimeException} as 'MyComponent.html' does
 * not exist.
 * </p>
 * <p>
 * for allowed values see {@link #ERROR}, {@link #WARN}, {@link #INFO}, {@link #IGNORE}
 * <p>
 * </li>
 * <li>{@link #NAMESPACE_CLASH_SEVERITY}
 * <p>
 * TODO Decide once an for all if clash detection is in or out.
 * <p>
 * for allowed values see {@link #ERROR}, {@link #WARN}, {@link #INFO}, {@link #IGNORE}
 * <p>
 * </li>
 * </ul>
 * 
 * @author glongman@gmail.com
 */
public interface IPreferenceConstants
{

    // default is 'true'
    // I'm not sure this is used anymore
    @Deprecated
    String CACHE_GRAMMAR_PREFERENCE = TapestryCore.IDENTIFIER + ".cachinggrammars";

    String BUILDER_MARKER_MISSES = TapestryCore.IDENTIFIER + ".BUILDER_MARKER_MISSES";

    String BUILDER_HANDLE_ASSETS = TapestryCore.IDENTIFIER + ".BUILDER_HANDLE_ASSETS";

    String BUILDER_HANDLE_NON_EXPLICIT_COMPONENT_CLASS_DECL = TapestryCore.IDENTIFIER
            + ".BUILDER_HANDLE_NON_EXPLICIT_COMPONENT_CLASS_DECL";

    String NAMESPACE_CLASH_SEVERITY = TapestryCore.IDENTIFIER + ".namespaceClashSeverity";

    String INCOMPATABILITY_SERVERITY = TapestryCore.IDENTIFIER + ".incompatabilitySeverity";
}