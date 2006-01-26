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

package net.sf.spindle.core.build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.build.templates.TemplateFinder;
import net.sf.spindle.core.namespace.ComponentSpecificationResolver;
import net.sf.spindle.core.namespace.CoreNamespace;
import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.namespace.NamespaceResourceLookup;
import net.sf.spindle.core.namespace.PageSpecificationResolver;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.PathUtils;
import net.sf.spindle.core.resources.TapestryResourceLocationAcceptor;
import net.sf.spindle.core.source.DefaultProblem;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.SourceLocation;
import net.sf.spindle.core.spec.PluginComponentSpecification;
import net.sf.spindle.core.spec.PluginLibrarySpecification;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.util.Assert;

import org.apache.hivemind.Resource;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;

/**
 * Resolver for a Namespace To resolve a namespace you need: Given, the
 * framework namespace, or null if this is the framework
 * <ul>
 * <li>As declared in the library specification</li>
 * <li>*.jwc in the same folder as the library specification</li>
 * </ul>
 * page resolve rules (ordinary library)
 * <ul>
 * <li>As declared in the library specification</li>
 * <li>*.page in the same folder as the library specification</li>
 * </ul>
 * template lookup rules:
 * <ul>
 * <li>If the component has a $template asset, use that</li>
 * <li>Look for a template in the same folder as the component</li>
 * <li>If a page in the application namespace, search in the application root</li>
 * </ul>
 * 
 * @author glongman@gmail.com
 */
public class NamespaceResolver {
	/**
	 * collector for any problems not handled by the AbstractBuild
	 */
	// private ProblemCollector fProblemCollector = new ProblemCollector();
	/**
	 * the instance of IBuild that instantiated the first Resolver
	 */
	protected AbstractBuild build;

	/**
	 * the result Namespace
	 */
	protected CoreNamespace namespace;

	protected IPropertySource fResultNamespacePropertySource;

	/**
	 * the Tapestry framwork Namespace
	 */
	protected ICoreNamespace frameworkNamespace;

	/**
	 * the location of the library spec that defines the Namespace being
	 * resolved
	 */
	protected ICoreResource namespaceSpecLocation;

	/**
	 * The id of the Namespace being resolved
	 */
	protected String namespaceId;

	/**
	 * A map of all component names -> locations in the Namespace being resolved
	 */
	protected Map<String, Resource> jwcFiles;

	/**
	 * a stack of the components being resolved its an error for a component to
	 * be in the stack more than once! If this happens, there is a circular
	 * dependency!
	 */
	protected Stack<ICoreResource> componentStack = new Stack<ICoreResource>();

	/**
	 * The resolver is not threadsafe
	 */
	protected boolean working;

	/**
	 * flag to indicate that this resolver is resolving the Tapestry Framework
	 * Namespace
	 */

	public NamespaceResolver(AbstractBuild build) {
		super();
		this.build = build;
	}

	public NamespaceResolver(AbstractBuild build, ICoreNamespace framework) {
		this(build);
		frameworkNamespace = framework;
	}

	public void resolve(CoreNamespace namespace) {
		this.namespace = namespace;
		doResolve();
	}

	protected void cleanup() {
		componentStack.clear();
		frameworkNamespace = null;
		namespaceId = null;
		namespaceSpecLocation = null;
		working = false;
		jwcFiles = null;
	}

	protected void doResolve() {
		if (working) {
			throw new RuntimeException("can't call resolve while resolving!");
		}

		try {
			working = true;
			componentStack.clear();
			// fProblemCollector.beginCollecting();
			if (namespace == null)
				throw new RuntimeException("Null namespace!");

			NamespaceResourceLookup lookup = create();

			namespace.setResourceLookup(lookup);

			// set a special component resolver that will prompt recursive
			// component/page resolution
			namespace.setComponentResolver(new BuilderComponentResolver(
					frameworkNamespace));

			// no special page resolver needed
			namespace.setPageResolver(new PageSpecificationResolver(
					frameworkNamespace, namespace));

			// do any work needed before we go ahead and resolve the pages and
			// components
			// for libraries other than the framework, child libraries are
			// resolved
			// here.
			namespaceConfigured();

			// resolve the pages/components in the Namespace
			resolveNamespaceContents();

			// now that we have resolved the pages/components, we need to
			// replace the
			// special ComponentResolver
			// with a regular one. Also gives subclasses the opportunity to do
			// some
			// final adjustments.
			namespaceResolved();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			working = false;
		}

	}

	/**
	 * Code that actually finds and resolves all the libraries, pages and
	 * components declared in this Namespace.
	 */
	protected void resolveNamespaceContents() {
		resolveComponents();
		resolvePages();
	}

	/**
	 * Called during a doResolve to setup the Namespace when it has been
	 * configures but before any of the contents are resolved.
	 */
	protected void namespaceConfigured() {
		resolveChildNamespaces();
	}

	/**
	 * Called in doResolve() right after the Namespace has been resolved.
	 * Usually this just entails setting the ComponentResolver for the
	 * Namespace.
	 */
	protected void namespaceResolved() {
		namespace.setComponentResolver(new ComponentSpecificationResolver(
				frameworkNamespace, namespace));
	}

	/**
	 * Every namespace has a Namespace resource lookup object for finding files
	 * according to Tapestry lookup rules.
	 * 
	 * @return a properly configured instance of NamespaceResourceLookup
	 */
	protected NamespaceResourceLookup create() {
		NamespaceResourceLookup lookup = new NamespaceResourceLookup();
		lookup.configure((PluginLibrarySpecification) namespace
				.getSpecification());
		return lookup;
	}

	/**
	 * @return List a list of all the templates for all page files in this
	 *         Namespace
	 */
	protected Set<Resource> getAllPageSpecTemplates() {
		Set<Resource> result = new HashSet<Resource>();
		List pageNames = namespace.getPageNames();
		int count = pageNames.size();
		for (int i = 0; i < count; i++) {
			PluginComponentSpecification spec = (PluginComponentSpecification) namespace
					.getPageSpecification((String) pageNames.get(i));

			result.addAll(spec.getTemplateLocations());
		}
		return result;
	}

	/**
	 * resolve/build all the child namespaces declared in the
	 * library/application specification
	 */
	protected void resolveChildNamespaces() {
		for (Iterator iter = namespace.getChildIds().iterator(); iter.hasNext();) {
			String childId = (String) iter.next();
			CoreNamespace nsChild = (CoreNamespace) namespace
					.getChildNamespace(childId);
			if (nsChild == null)
				continue;
			new NamespaceResolver(build, frameworkNamespace).resolve(nsChild);
		}
	}

	/**
	 * resolve all the components declared in the spec (or found in the
	 * appropriate locations!)
	 */
	protected void resolveComponents() {
		jwcFiles = getAllJWCFilesForNamespace();

		for (Iterator iter = jwcFiles.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			ICoreResource location = (ICoreResource) jwcFiles.get(name);
			resolveComponent(name, location);
		}

		Map<String, Resource> specless = resolveAllAnnotatedSpeclessComponentsForNamespace();
		for (Iterator iter = specless.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			jwcFiles.put(name, specless.get(name));
		}
	}

	/**
	 * resolve a single component. As this method is called recursively, we keep
	 * a stack recording the components being resolved. If we are asked to
	 * resolve a component that is already on the stack, we throw a runtime
	 * exception as this is an indication that there is a recursive dependancy
	 * between components.
	 */
	protected IComponentSpecification resolveComponent(String name,
			ICoreResource location) {

		IComponentSpecification result = namespace
				.getComponentSpecification(name);

		if (result != null)
			return result;

		Assert.isNotNull(location);

		result = null;

		if (componentStack.contains(location)) {
			throw new BuilderException(CoreMessages.format(
					"build-failed-circular-component-reference",
					getCircularErrorMessage(location)));
		}

		// we must test to ensure that this location is not already claimed by
		// another namespace!

		try {
			if (location.exists())
				build.clashDetector.claimResourceForNamespace(location,
						namespace, "key");

			componentStack.push(location);

			result = build.parseComponentSpecification(namespace, location,
					null);

			if (result != null) {
				namespace.installComponentSpecification(name, result);

				PluginComponentSpecification pluginComponentSpecification = ((PluginComponentSpecification) result);

				claimTemplates(location, pluginComponentSpecification
						.getTemplateLocations());
				build.parseTemplates(pluginComponentSpecification);
			}

			componentStack.pop();

		} catch (ClashException e) {
			build.problemPersister.recordProblem(location, clashProblem(e
					.getMessage()));
			e.printStackTrace();
		}
		return result;
	}

	private void claimTemplates(ICoreResource specLocation, List templates) {
		for (Iterator iter = templates.iterator(); iter.hasNext();) {
			ICoreResource template = (ICoreResource) iter.next();
			try {
				build.clashDetector.claimTemplateForComponent(specLocation,
						template);
			} catch (ClashException e) {

				// FIXME this is wrong, no message comes in the exception.
				// need to add markers with a message!
				build.problemPersister.recordProblem(template,
						clashProblem("PUT CLASH MESSAGE HERE"));
				e.printStackTrace();
				iter.remove();
			}
		}
	}

	private IProblem clashProblem(String message) {
		int severity = TapestryCore.getDefault().getNamespaceClashPriority();
		return new DefaultProblem(IProblem.TAPESTRY_CLASH_PROBLEM, severity,
				message, SourceLocation.FILE_LOCATION, false,
				IProblem.NOT_QUICK_FIXABLE);
	}

	/**
	 * build an error message for circular component references
	 * 
	 * @param location
	 * @return
	 */
	private String getCircularErrorMessage(ICoreResource location) {
		List<ICoreResource> result = new ArrayList<ICoreResource>();
		result.add(location);
		Stack clone = (Stack) componentStack.clone();

		ICoreResource sloc = (ICoreResource) clone.pop();

		if (sloc.equals(location))
			return location.toString() + " refers to itself";

		result.add(sloc);
		while (!sloc.equals(location)) {

			result.add(sloc);
			sloc = (ICoreResource) clone.pop();
		}

		return result.toString();

	}

	/**
	 * Scan the namespace looking for all jwc files that are in allowed places.
	 * 
	 * @return a Map of Component Type name -> location
	 */
	private Map<String, Resource> getAllJWCFilesForNamespace() {
		ICoreResource location = (ICoreResource) namespace
				.getSpecificationLocation();

		Map<String, Resource> result = new HashMap<String, Resource>();
		ILibrarySpecification spec = namespace.getSpecification();

		// pull the ones that are defined in the spec.
		List cTypes = spec.getComponentTypes();
		int count = cTypes.size();
		for (int i = 0; i < count; i++) {
			String type = (String) cTypes.get(i);
			Resource specLoc = location.getRelativeResource(spec
					.getComponentSpecificationPath(type));
			result.put(type, specLoc);
		}

		TapestryResourceLocationAcceptor acceptor = new TapestryResourceLocationAcceptor(
				"*", false, TapestryResourceLocationAcceptor.ACCEPT_JWC);
		Resource[] jwcs = namespace.getResourceLookup().lookup(acceptor);

		// remaining typed by thier filename
		for (int i = 0; i < jwcs.length; i++) {
			String type = new PathUtils(jwcs[i].getName())
					.removeFileExtension().toString();
			if (!result.containsKey(type))
				result.put(type, jwcs[i]);
			else if (!jwcs[i].equals(result.get(type)))

				build.problemPersister.recordProblem(jwcs[i],
						new DefaultProblem(IProblem.ERROR, CoreMessages.format(
								"builder-hidden-jwc-file", jwcs[i], result
										.get(type)),
								SourceLocation.FILE_LOCATION, false,
								IProblem.SPINDLE_BUILDER_HIDDEN_JWC_FILE));

		}
		return result;
	}

	private Map<String, Resource> resolveAllAnnotatedSpeclessComponentsForNamespace() {
		return Collections.emptyMap();
		// Map<String, Resource> result = new HashMap<String, Resource>();
		// if (build.infrastructure.projectSupportsAnnotations())
		// {
		// ILibrarySpecification spec = namespace.getSpecification();
		//
		// String packages = namespace
		// .getPropertyValue("org.apache.tapestry.component-class-packages");
		//
		// if (!HiveMind.isBlank(packages))
		// {
		// List annotatedComponentTypes = build.infrastructure
		// .getAllAnnotatedComponentTypes(packages);
		// for (Iterator iter = annotatedComponentTypes.iterator();
		// iter.hasNext();)
		// {
		// IJavaType type = (IJavaType) iter.next();
		// String simpleName = type.getSimpleName();
		//
		// Resource location = namespace.getSpecificationLocation()
		// .getRelativeResource(simpleName + ".jwc");
		//
		// resolveSpeclessComponent(location, type);
		//
		// result.put(simpleName, location);
		// }
		// }
		// }
		// return result;
	}

	protected void resolveSpeclessComponent(Resource location,
			IJavaType componentType) {
		PluginComponentSpecification specification = new PluginComponentSpecification();
		specification.setPageSpecification(false);
		specification.setSpecificationLocation(location);
		specification.setNamespace(namespace);
		specification.setComponentClassName(componentType
				.getFullyQualifiedName());

		build.scanComponentSpecificationAnnotations(specification);

		String templateExtension = build.getComponentTemplateExtension(
				namespace, specification);

		specification.setTemplateLocations(TemplateFinder.scanForTemplates(
				specification, templateExtension, build.tapestryProject, null));

		String name = location.getName();
		int dotx = name.lastIndexOf('.');
		if (dotx > 0)
			name = name.substring(0, dotx);

		namespace.installPageSpecification(name, specification);
		build.parseTemplates(specification);
		build.templateExtensionSeen(templateExtension);
		build.buildQueue.finished(specification.getTemplateLocations());
	}

	/**
	 * @return Map a map of the names and file locations of all the .page files
	 *         for the Namespace
	 */
	private Map<String, Resource> getAllPageFilesForNamespace() {
		ICoreResource location = (ICoreResource) namespace
				.getSpecificationLocation();

		Map<String, Resource> result = new HashMap<String, Resource>();
		ILibrarySpecification spec = namespace.getSpecification();

		// pull the ones that are defined in the spec.
		// They are named as defined in the spec.
		for (Iterator iter = spec.getPageNames().iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			Resource specLoc = location.getRelativeResource(spec
					.getPageSpecificationPath(type));
			result.put(type, specLoc);
		}

		TapestryResourceLocationAcceptor acceptor = new TapestryResourceLocationAcceptor(
				"*", false, TapestryResourceLocationAcceptor.ACCEPT_PAGE);
		Resource[] pages = namespace.getResourceLookup().lookup(acceptor);

		// remaining named by thier filename
		for (int i = 0; i < pages.length; i++) {
			String name = new PathUtils(pages[i].getName())
					.removeFileExtension().toString();
			if (!result.containsKey(name)) {
				result.put(name, pages[i]);

			} else if (!result.get(name).equals(pages[i]))
				build.problemPersister.recordProblem(pages[i],
						new DefaultProblem(IProblem.ERROR, CoreMessages.format(
								"builder-hidden-page-file", pages[i], result
										.get(name)),
								SourceLocation.FILE_LOCATION, false,
								IProblem.SPINDLE_BUILDER_HIDDEN_PAGE_FILE));

		}
		return result;
	}

	/**
	 * Resolve all the .page files in the namespace
	 */
	protected void resolvePages() {
		Map<String, Resource> dotPageFiles = getAllPageFilesForNamespace();
		for (String name : dotPageFiles.keySet()) {
			resolvePageFile(name, dotPageFiles.get(name));
		}
	}

	/**
	 * resolve a single .page file There could be recursive calls to
	 * resolveComponent() downstream from this method But this method will never
	 * be called recursively.
	 */
	protected IComponentSpecification resolvePageFile(String name,
			Resource location) {
		IComponentSpecification result = namespace.getPageSpecification(name);
		if (result != null || location == null)
			return result;

		result = null;

		result = build.parseComponentSpecification(namespace, location, null);

		if (result != null) {
			namespace.installPageSpecification(name, result);
			build.parseTemplates((PluginComponentSpecification) result);
		}
		return result;
	}

	class BuilderComponentResolver extends ComponentSpecificationResolver {

		public BuilderComponentResolver(INamespace framework) {
			super(framework, namespace);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see core.namespace.ComponentSpecificationResolver#resolve(java.lang.String,
		 *      java.lang.String)
		 */
		public IComponentSpecification resolve(String libraryId, String type) {
			if (libraryId != null
					&& !libraryId.equals(containerNamespace.getId()))
				return super.resolve(libraryId, type);

			IComponentSpecification result = null;
			result = containerNamespace.getComponentSpecification(type);

			if (result == null && jwcFiles.containsKey(type))
				result = resolveComponent(type, (ICoreResource) jwcFiles
						.get(type));

			if (result == null)
				result = resolveInFramework(type);

			return result;
		}

	}

}