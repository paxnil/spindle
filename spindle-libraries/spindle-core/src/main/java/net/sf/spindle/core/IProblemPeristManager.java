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
import net.sf.spindle.core.source.IProblem;

import org.apache.hivemind.Resource;

/**
 * A service used by the core to record/interrogate/clear problems seen during the build.
 * <p>
 * IDE implementors need to implement this interface.
 * <p>
 * The core works on the assumption that problems are peristed (presented to end users) during (or right after a
 * build) and are available during the next build. A subsequent full or incremental build may clear
 * some or all problems by calling the 'remove' methods in this interface.
 * </p>
 * <p>
 * The core does not check to see if the {@link org.apache.hivemind.Resource} a problem is being
 * recorded against relates to a binary resource or to a file. It's up to implementors to decide
 * what to do with problems recorded against binary resources. The Eclipse implmentation silently
 * discards them.
 * 
 * @author gwl
 * @see net.sf.spindle.core.source.IProblem
 * @see net.sf.spindle.core.ITapestryProject
 * @see org.apache.hivemind.Resource
 */
public interface IProblemPeristManager
{
    /**
     * Record a problem against the project.
     * <p>
     * In Eclipse terms this would mean adding a marker to the project folder.
     * 
     * @param project
     *            the project
     * @param problem
     *            the problem to record.
     */
    void recordProblem(ITapestryProject project, IProblem problem);

    /**
     * Record an array of problems against the project.
     * <p>
     * In Eclipse terms this would mean adding markers to the project folder.
     * 
     * @param project
     *            the project
     * @param problems
     *            the problems to record.
     */
    void recordProblems(ITapestryProject project, IProblem[] problems);

    /**
     * Record an problem against a resource.
     * <p>
     * In Eclipse terms this would mean adding a marker to a file.
     * 
     * @param resource
     *            the resource
     * @param problems
     *            the problem to record.
     */
    void recordProblem(Resource resource, IProblem problem);

    /**
     * Record an array of problems against the project.
     * <p>
     * In Eclipse terms this would mean adding markers to the project folder.
     * 
     * @param resource
     *            the resource
     * @param problems
     *            the problems to record.
     */
    void recordProblems(Resource resource, IProblem[] problems);

    /**
     * If this method returns true, then a problem with type
     * {@link IProblem#TAPESTRY_BUILDBROKEN_MARKER} was recorded against the project on the last
     * build. A few causes of a broken build: missing web.xml, classpath problems like no tapestry
     * jars found.
     * <p>
     * This method is useful only for incremental builds as an incremental build is not possible if
     * the project is broken. A full build will revalidate everything and proceed if the cause of
     * the broken build was fixed.
     * 
     * @param project
     * @return true iff the last build recorded a problem with type
     *         {@link IProblem#TAPESTRY_BUILDBROKEN_MARKER} against the project.
     */
    boolean hasBrokenBuildProblems(ITapestryProject project);

    /**
     * Remove problems of all problem types persisted against the {@link ITapestryProject} and also all
     * problems persisted against any {@link Resource} in the project.
     */
    void removeAllProblems(ITapestryProject project);

    /**
     * Remove all problems persisted against an {@link Resource} where {@link IProblem#isTemporary()}
     * returns true.
     * <p>
     * 
     * @param resource
     *            the resource.
     */
    void removeTemporaryProblemsForResource(Resource resource);

    /**
     * Remove any and all problems recorded against a {@link Resource}.
     * 
     * @param resource
     *            the resource.
     */
    void removeAllProblemsFor(Resource resource);

}
