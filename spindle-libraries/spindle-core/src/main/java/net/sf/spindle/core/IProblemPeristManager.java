package net.sf.spindle.core;

import net.sf.spindle.core.source.IProblem;

import org.apache.hivemind.Resource;

/**
 * @author gwl
 */
public interface IProblemPeristManager
{
    void recordProblem(ITapestryProject project, IProblem problem);

    void recordProblems(ITapestryProject project, IProblem[] problems);

    void recordProblem(Resource resource, IProblem problem);

    void recordProblems(Resource resource, IProblem[] problems);

    boolean hasBrokenBuildProblems(ITapestryProject project);

    /**
     * get rid of all but the xml parse problems
     */
    void removeProblems(ITapestryProject project);

    /**
     * get rid of *all* the problems, including xml source ones.
     */
    void removeAllProblems(ITapestryProject project);

    void removeTemporaryProblemsForResource(Resource resource);

    void removeAllProblemsFor(Resource resource);

}
