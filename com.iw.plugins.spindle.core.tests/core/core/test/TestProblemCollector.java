package core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import core.source.DefaultProblem;
import core.source.IProblem;
import core.source.IProblemCollector;
import core.source.ISourceLocation;



public class TestProblemCollector implements IProblemCollector
{
    // /CLOVER:OFF
    
    private List problems;

    public void addProblem(IProblem problem)
    {
        if (problems == null)
            problems = new ArrayList();

        problems.add(problem);

    }

    public void addProblem(int severity, ISourceLocation location, String message,
            boolean isTemporary, int code)
    {
        addProblem(new DefaultProblem(severity, message, location, isTemporary, code));

    }

    public IProblem[] getProblems()
    {
        List problemsList = getProblemsList();
        return (IProblem[]) problemsList.toArray(new IProblem[problemsList.size()]);
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

    public List getProblemsList()
    {
        if (problems == null)
            return Collections.EMPTY_LIST;
        return Collections.unmodifiableList(problems);
    }
    
    public void clear() {
        if (problems != null)
            problems.clear();
    }
}
