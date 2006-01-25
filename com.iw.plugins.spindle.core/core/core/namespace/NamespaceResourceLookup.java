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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package core.namespace;

import java.util.ArrayList;
import java.util.List;

import org.apache.hivemind.Resource;

import core.resources.ICoreResource;
import core.resources.IResourceAcceptor;
import core.resources.IResourceRoot;
import core.spec.PluginApplicationSpecification;
import core.spec.PluginLibrarySpecification;

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