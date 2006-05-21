package net.sf.spindle.core.build;

import java.util.ArrayList;
import java.util.Set;

import net.sf.spindle.core.IProblemPeristManager;
import net.sf.spindle.core.ITapestryProject;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.xerces.parser.XercesDOMModelSource;

import org.apache.hivemind.Resource;

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
public class InfrastructureFixture extends AbstractBuildInfrastructure
{

    private State stateObject;
    
    public InfrastructureFixture(ITapestryProject project,
            IProblemPeristManager<Object> problemPersistence, IBuildNotifier buildNotifier)
    {
        super();
        tapestryProject = project;      
        problemPersister = problemPersistence;
        notifier = buildNotifier;
    }

    @Override
    protected void initialize() throws BuilderException
    {
        contextRoot = tapestryProject.getWebContextLocation();
        classpathRoot = tapestryProject.getClasspathRoot();
        domModelSource = new XercesDOMModelSource();
    }   

    @Override
    protected AbstractBuild createIncrementalBuild()
    {
        // inc builds not supported, so return null
        return null;
    }

    @Override
    public WebXMLScanner createWebXMLScanner(FullBuild build)
    {
        // here is the easiest response.
        return new WebXMLScanner(build)
        {

            @Override
            protected String getApplicationPathFromServletSubclassOverride(IJavaType servletType)
                    throws ScannerException
            {
                return null;
            }
            
            
        };
    }

    @Override
    public void findAllTapestrySourceFiles(Set<String> knownTemplateExtensions,
            ArrayList<Resource> found)
    {
        // TODO Auto-generated method stub
        // for now do nothing!
    }

    @Override
    public Object getClasspathMemento()
    {
        // this is a platform dependent thing.
        return null;
    }

    @Override
    public Object copyClasspathMemento(Object memento)
    {
        return null;
    }
    
    @Override
    protected void clearLastState()
    {
        stateObject = null;
    }

    @Override
    public State getLastState()
    {        
        return stateObject;
    }

    @Override
    protected boolean isWorthBuilding() throws BuilderException, BrokenWebXMLException
    {
        return true;
    }

    @Override
    public void persistState(State state)
    {
        stateObject = state;
    }

    @Override
    public boolean projectSupportsAnnotations()
    {
        // we haven't figured out annotations yet.
        // besides, I think that api access to annotation source will only ocme
        // with 1.6 (Mustang).
        // What I'm saying here is that for these tests I will not have access to
        // the annotation info needed until Mustang arrives.
        return false;
    }

}
