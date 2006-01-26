package net.sf.spindle.core.parser;

import org.apache.hivemind.Resource;

/**
 * A wrapper around a parser. The plan in eclipse land is to abandon the custom
 * Xerces parser and wrap the WTP SSE model with this interface.
 * <p>
 * That way implementations outside of Eclipse can still use the custom Xerces
 * parser.
 * <p>
 * In WTP land these models are DOM adaptations of internal SEE structures and
 * they are reference counted. SEE models must be explicity released. Hence the
 * 'requestor' parameters and the 'release' method.
 * <p>
 * The thin
 * <p>
 * The custom Xerces parser ignores the requestor parameter and also ignores
 * calls to {@link #release(IDOMModel, Object)}
 * 
 */
public interface IDOMModelSource {
	/**
	 * Parse a {@link Resource} into a dom model.
	 * 
	 * @param resource
	 *            the resource
	 * @param validate
	 *            a hint to the implementation on whether validation should
	 *            occur during parsing.
	 * @param requestor
	 *            up to the implementation to use or ignore.
	 * @return an instance of {@link IDOMModel}
	 */
	IDOMModel parseDocument(Resource resource, boolean validate,
			Object requestor);

	/**
	 * Parse a {@link Resource} into a dom model.
	 * 
	 * @param resource
	 *            the resource
	 * @param encoding
	 *            the encoding of the contents of the resource
	 * @param validate
	 *            a hint to the implementation on whether validation should
	 *            occur during parsing.
	 * @param consumer
	 *            up to the implementation to use or ignore.
	 * @return an instance of {@link IDOMModel}
	 */
	IDOMModel parseDocument(Resource resource, String encoding,
			boolean validate, Object consumer);

	/**
	 * @param model
	 *            a model built by a previous call to
	 *            {@link #parseDocument(Resource, boolean, Object)} or
	 *            {@link #parseDocument(Resource, String, boolean, Object).
	 * @param requestor
	 *            up to the implementation to use or ignore.
	 */
	void release(IDOMModel model, Object requestor);
}
