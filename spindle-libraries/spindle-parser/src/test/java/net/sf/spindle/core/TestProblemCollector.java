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
