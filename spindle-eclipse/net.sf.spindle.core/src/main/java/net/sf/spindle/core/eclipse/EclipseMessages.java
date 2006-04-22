package net.sf.spindle.core.eclipse;

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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

import net.sf.spindle.core.messages.MessageFormatter;
import net.sf.spindle.core.source.IProblem;

/**
 * @author gwl
 */
public class EclipseMessages
{
    private static MessageFormatter FORMATTER = new MessageFormatter(EclipseMessages.class,
            "EclipseMessages");

    public static String mustBeAJavaProject()
    {
        return FORMATTER.getMessage("must_be_a_java_project");
    }

    public static String tapestryProjectNotExist()
    {
        return FORMATTER.getMessage("must_have_tapestry_nature");
    }

    public static String unableToDetermineClasspath()
    {
        return FORMATTER.getMessage("unable_to_determine_project_classpath");
    }

    public static String javaBuilderFailed()
    {
        return FORMATTER.getMessage("java_build_failed_to_run");
    }

    public static String invalidCompilerOutputPath(String string)
    {
        return FORMATTER.getMessage("java_project_folder_must_not_be_the_compiler_output_folder");
    }

    public static String unableToDetermineCompilerOutputPath()
    {
        return FORMATTER.getMessage("java_project_folder_must_not_be_the_compiler_output_folder");
    }

    public static String prerequisiteProjectNotBuilt(IProject p)
    {
        return FORMATTER.format("prereq_project_not_built", p.getFullPath());
    }

    public static String servletSubclassIsBinaryAttachSource(String fullyQualifiedName)
    {
        return FORMATTER.format(
                "servlet_subclass_is_binary_user_should_attach_source_code",
                fullyQualifiedName);
    }

    public static String errorOccuredAccessingStructureOfServlet(String fullyQualifiedName)
    {
        return FORMATTER.format("error_occured_accessing_structure_of_see_log", fullyQualifiedName);
    }

    public static String coreClasspathContainerLabel()
    {
        return FORMATTER.getMessage("core_classpath_container_label");
    }

    public static String logProblemMessage(IStorage storage, IProblem problem)
    {
        return FORMATTER.format("marker_not_resource", storage.toString(), problem.toString());
    }

    public static String projectMetaDataUnexpectedElement(String namespace, String name)
    {
        return FORMATTER.format("project_metadata_unexpected_element", namespace, name);
    }

}