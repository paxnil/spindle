package core.builder;

/**
 * If, by whatever plateform specific means, a user cancelled a build, this
 * exception must be thrown.
 * 
 * Clients usually override {@link core.builder.IBuildNotifier#checkCancel()}
 * and throw the exception there.
 * 
 */
public class BuildCancelledException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BuildCancelledException() {
		super();
	}
}
