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
 * The Original Code is Jetty Launcher Profile
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

package com.iw.plugins.jetty.profile;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaSourceLookupTab;

import ru.nlmk.eclipse.plugins.profiler.launch.ProfilerAdvTab;
import ru.nlmk.eclipse.plugins.profiler.launch.ProfilerTab;

import com.iw.plugins.jetty.launcher.JettyArgumentsTab;
import com.iw.plugins.jetty.launcher.JettyJspTab;
import com.iw.plugins.jetty.launcher.JettyMainTab;

/**
 *  Tab Group for Jetty Launching (with profiling)
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JettyProfileLaunchTabGroup extends AbstractLaunchConfigurationTabGroup
{
    ProfilerTab t1 = new ProfilerTab();

    /**
     * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
     */
    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {

        ILaunchConfigurationTab[] tabs =
            new ILaunchConfigurationTab[] {
                new JettyMainTab(),
                new JettyJspTab(),
                new JettyArgumentsTab(),
                new ProfilerTab(),
                new ProfilerAdvTab(),
                new JavaJRETab(),
                new JavaClasspathTab(),
                new JavaSourceLookupTab(),
                new CommonTab()};
        setTabs(tabs);
    }

}
