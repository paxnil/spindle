package net.sf.solareclipse.text.rules;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class ViewNode extends FlatNode {
	/** Inner view of the document */
	public InnerDocumentView view;

	public ViewNode(String type) {
		super(type);
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ViewNode[" + type + ", " + offset + ", " + length + "]";
	}
}
