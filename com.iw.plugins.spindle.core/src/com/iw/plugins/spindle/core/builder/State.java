package com.iw.plugins.spindle.core.builder;
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IClasspathEntry;

import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 * An object intended to store the state of the build between builds.
 * Normally, a builder's output is the result of compiling source files, and the State
 * is merely there to make things like incremental builds possible.
 * 
 * This is true for Tapestry but different in that the build state is *the* result
 * of the build!
 * 
 * TODO extend design so that States can be persisted!
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class State
{

    public static byte VERSION = 0x0001;

    String fProjectName;
    IResourceWorkspaceLocation fContextRoot;
    String fLibraryLocation;
    byte fVersion = VERSION;
    int fBuildNumber;
    Map fBinaryNamespaces = new HashMap();
    IClasspathEntry[] fLastKnownClasspath;
    
    // following are used to determine if an incremental Tapestry build is required at all.
    
    // list of IResources to java types in the project
    List fJavaDependencies;
    
    // list of fullyQualified names of types not found during a build
    List fMissingJavaTypes;
    
    // list of known template extensions
    List fSeenTemplateExtensions;
    
    // the results of parsing web.xml
    ServletInfo fApplicationServlet;
    
    // the main namespace result of the last build.
    ICoreNamespace fPrimaryNamespace;


    /**
     * Constructor for State.
     */
    State()
    {}

    /**
     * Constructor State.
     * @param builder
     */
    State(TapestryBuilder builder)
    {
        fProjectName = builder.getProject().getName();
        fContextRoot = builder.fTapestryProject.getWebContextLocation();
        fLibraryLocation = builder.fTapestryProject.getLibrarySpecPath();
        fBuildNumber = 0;
    }

    void markAsBrokenBuild()
    {
        fBuildNumber = -1;
    }

    void copyFrom(State lastState)
    {
        fProjectName = lastState.fProjectName;
        fContextRoot = lastState.fContextRoot;
        fLibraryLocation = lastState.fLibraryLocation;
        fBuildNumber = lastState.fBuildNumber + 1;
        fBinaryNamespaces.clear();       
        fLastKnownClasspath = new IClasspathEntry[lastState.fLastKnownClasspath.length];
        System.arraycopy(
            lastState.fLastKnownClasspath,
            0,
            fLastKnownClasspath,
            0,
            lastState.fLastKnownClasspath.length);
        fApplicationServlet = lastState.fApplicationServlet;
        fPrimaryNamespace = lastState.fPrimaryNamespace;

    }

    void write(DataOutputStream out) throws IOException
    {}

    static State read(DataInputStream in) throws IOException
    {
        return null;
    }

}
