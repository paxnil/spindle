package core.types;

public class TypeModelException extends Exception {

	private static final long serialVersionUID = 1L;

	Object problemChild;

	public TypeModelException() {
		super();
	}

	public TypeModelException(String message, Object problemChild) {
		super(message);
		this.problemChild = problemChild;
	}

	public TypeModelException(String message) {
		super(message);
	}

	public TypeModelException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
