package net.sf.spindle.core.namespace;
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
import java.util.List;

import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceAcceptor;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.spec.PluginApplicationSpecification;
import net.sf.spindle.core.spec.PluginLibrarySpecification;

import org.apache.hivemind.Resource;



/**
 * A Lookup that bases its searches on the namespace it is configured with. Does
 * not take into account any sub namespaces.
 * 
 * @author glongman@gmail.com
 */
public class NamespaceResourceLookup {

	private List<Resource> fLocations;

	public void configure(PluginLibrarySpecification specification) {
		fLocations = new ArrayList<Resource>();
		fLocations.add(specification.getSpecificationLocation());
	}

	public void configure(PluginApplicationSpecification specification,
			IResourceRoot contextRoot, String servletName) {
		fLocations = new ArrayList<Resource>();
		fLocations.add(specification.getSpecificationLocation());
		if (servletName != null)
			fLocations.add(contextRoot.getRelativeResource("/WEB-INF/"
					+ servletName));

		fLocations.add(contextRoot.getRelativeResource("/WEB-INF/"));
		fLocations.add(contextRoot.getRelativeResource("/"));
	}

	public Resource[] lookup(IResourceAcceptor acceptor) {
		if (fLocations == null)
			throw new Error("not initialized");

		for (Resource resource : fLocations) {
			ICoreResource location = (ICoreResource) resource;
			location.lookup(acceptor);
		}
		return acceptor.getResults();
	}

}