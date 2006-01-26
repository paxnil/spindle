package net.sf.spindle.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.IProblemCollector;
import net.sf.spindle.core.source.ISourceLocation;

public class TestProblemCollector implements IProblemCollector
{
    // /CLOVER:OFF

    private List<IProblem> problems;

    public void addProblem(IProblem problem)
    {
        if (problems == null)
            problems = new ArrayList<IProblem>();

        problems.add(problem);

    }

    public void addProblem(int severity, ISourceLocation location, String message,
            boolean isTemporary, int code)
    {
        addProblem(new DefaultProblem(severity, message, location, isTemporary, code));

    }

    public IProblem[] getProblems()
    {
        List<IProblem> problemsList = getProblemsList();
        return (IProblem[]) problemsList.toArray(new IProblem[] {});
    }

    public void beginCollecting()
    {
        // do nothing

    }

    public void endCollecting()
    {
        // do nothing

    }

    public boolean isEmpty()
    {
        if (problems == null)
            return true;
        return problems.isEmpty();
    }

    public int size()
    {
        if (problems == null)
            return 0;
        return problems.size();
    }

    public List<IProblem> getProblemsList()
    {
        if (problems == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(problems);
    }

    public void clear()
    {
        if (problems != null)
            problems.clear();
    }
}
