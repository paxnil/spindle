package com.iw.plugins.spindle.core;

/**
 * Represents an entire Tapestry build unit 
 * <p>
 * <ul>
 * 	<li><code>.application</code> file;</li>
 * 	<li><code>.library</code> file;</li>
 * 	<li><code>.jwc</code> file;</li>
 * 	<li><code>.page</code> file;</li>
 * 	<li><code>.html</code> file;</li>
 * 	<li><code>.script</code> file;</li>
 * </ul>
 * </p>
 * Build unit elements need to be opened before they can be navigated or manipulated.
 * If a file cannot be parsed, its structure remains unknown.
 * Use <code>IJavaElement.isStructureKnown</code> to determine whether this is 
 * the case.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IBuildUnit extends ITapestryArtifact, IParent, IOpenable {

}
