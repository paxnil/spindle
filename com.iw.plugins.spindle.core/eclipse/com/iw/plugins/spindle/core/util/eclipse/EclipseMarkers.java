package com.iw.plugins.spindle.core.util.eclipse;

import org.apache.hivemind.Resource;

import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.util.IProblemPeristManager;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EclipseMarkers implements IProblemPeristManager
{

    public void recordProblem(Resource resource, IProblem[] problems)
    {
        // TODO Auto-generated method stub
        
    }

    public void recordProblems(Resource resource, IProblem[] problems)
    {
        // TODO Auto-generated method stub
        
    }

    public boolean hasBrokenBuildProblems(ITapestryProject project)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeProblems(ITapestryProject project)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeAllProblems(ITapestryProject project)
    {
        // TODO Auto-generated method stub
        
    }

    public void recordProblem(ITapestryProject project, IProblem problem)
    {
        // TODO Auto-generated method stub
        
    }

    public void recordProblems(ITapestryProject project, IProblem[] problems)
    {
        // TODO Auto-generated method stub
        
    }

    public void recordProblem(Resource resource, IProblem problem)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeTemporaryProblemsForResource(Resource resource)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeAllProblemsFor(Resource resource)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeProblemsFor(Resource resource, String problemType)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeProblemsFor(ITapestryProject project, String problemType)
    {
        // TODO Auto-generated method stub
        
    }

}
