package net.sf.spindle.core.build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.spindle.core.IProblemPeristManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.util.Assert;

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
public class ProblemPersisterFixture implements IProblemPeristManager<Object>
{

    ITapestryProject project;

    List<IProblem> projectProblems = new ArrayList<IProblem>();

    List<IProblem> allProblems = new ArrayList<IProblem>();

    List<IProblem> allBrokenBuildProblems = new ArrayList<IProblem>();

    Map<Object, ArrayList<IProblem>> underlierProblems = new HashMap<Object, ArrayList<IProblem>>();

    /**
     * @return
     */
    public int getAllProblemCount()
    {
        return allProblems.size();
    }

    /**
     * @return
     */
    public int getBrokenBuildProblemCount()
    {
        return allBrokenBuildProblems.size();
    }

    /**
     * @return
     */
    public int getProjectProblemCount()
    {
        return projectProblems.size();
    }

    /**
     * @param resource
     * @return
     */
    public int getProblemCount(Object resource)
    {
        return getProblemsFor(resource).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#hasBrokenBuildProblems(net.sf.spindle.core.ITapestryProject)
     */
    public boolean hasBrokenBuildProblems(ITapestryProject project)
    {
        return !allBrokenBuildProblems.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblem(net.sf.spindle.core.ITapestryProject,
     *      net.sf.spindle.core.source.IProblem)
     */
    public void recordProblem(ITapestryProject project, IProblem problem)
    {
        checkProject(project);

        boolean isBrokenBuildProblem = problem.getType().equals(
                IProblem.TAPESTRY_BUILDBROKEN_MARKER);

        allProblems.add(problem);
        projectProblems.add(problem);

        if (isBrokenBuildProblem)
            allBrokenBuildProblems.add(problem);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblems(net.sf.spindle.core.ITapestryProject,
     *      net.sf.spindle.core.source.IProblem[])
     */
    public void recordProblems(ITapestryProject project, IProblem[] problems)
    {
        for (IProblem problem : problems)
        {
            recordProblem(project, problem);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblems(org.apache.hivemind.Resource,
     *      net.sf.spindle.core.source.IProblem[])
     */
    public void recordProblems(Object resource, IProblem[] problems)
    {
        for (IProblem problem : problems)
        {
            recordProblem(resource, problem);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#removeAllProblems(net.sf.spindle.core.ITapestryProject)
     */
    public void removeAllProblems(ITapestryProject project)
    {
        checkProject(project);

        allBrokenBuildProblems.clear();
        allProblems.clear();
        projectProblems.clear();
        underlierProblems.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#recordProblem(java.lang.Object,
     *      net.sf.spindle.core.source.IProblem)
     */
    public void recordProblem(Object underlier, IProblem problem)
    {
        if (underlier == null)
            return;
        getProblemsFor(underlier).add(problem);

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.spindle.core.IProblemPeristManager#removeAllProblemsFor(java.lang.Object)
     */
    public void removeAllProblemsFor(Object underlier)
    {
        if (underlier == null)
            return;
        ArrayList<IProblem> problemsForResource = getProblemsFor(underlier);
        allProblems.removeAll(problemsForResource);
        problemsForResource.clear();

    }

    public void removeTemporaryProblemsFor(Object underlier)
    {
        if (underlier == null)
            return;
        ArrayList<IProblem> problemsForResource = getProblemsFor(underlier);
        for (Iterator iter = problemsForResource.iterator(); iter.hasNext();)
        {
            IProblem element = (IProblem) iter.next();
            if (element.isTemporary())
                iter.remove();
        }
    }

    /**
     * @param project
     */
    private void checkProject(ITapestryProject project)
    {
        if (this.project == null)
            this.project = project;

        else
            Assert.isTrue(this.project == project);
    }

    /**
     * @param underlier
     * @return
     */
    private ArrayList<IProblem> getProblemsFor(Object underlier)
    {
        ArrayList<IProblem> result = underlierProblems.get(underlier);
        if (result == null)
        {
            result = new ArrayList<IProblem>();
            underlierProblems.put(underlier, result);
        }
        return result;
    }

    public void dump()
    {
        StringBuilder builder = new StringBuilder("++++++++ Recorded Problems ++++++++");
        builder.append("\n");
        builder.append("Project Problems.....+"
                + (projectProblems.isEmpty() ? "none" : projectProblems.size() + "\n"));

        for (IProblem problem : projectProblems)
        {
            builder.append(problem);
            builder.append("\n");
        }

        builder.append("\nResource Problems......"
                + (underlierProblems.isEmpty() ? "none" : underlierProblems.size()) + "\n");

        ArrayList<Object> keys = new ArrayList<Object>(underlierProblems.keySet());
        Collections.sort(keys, new Comparator<Object>()
        {

            public int compare(Object o1, Object o2)
            {

                return o1.toString().compareTo(o2.toString());
            }
        });

        for (Object underlier : keys)
        {
            builder.append(underlier);
            builder.append("\n");
            for (IProblem problem : underlierProblems.get(underlier))
            {
                builder.append("\t");
                builder.append(problem);
                builder.append("\n");
            }
        }

        System.err.println(builder.toString());

    }
}
