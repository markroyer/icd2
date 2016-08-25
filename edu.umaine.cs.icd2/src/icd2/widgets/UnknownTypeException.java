/**
 * 
 */
package icd2.widgets;

/**
 * @author Mark Royer
 *
 */
public class UnknownTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownTypeException(Class<?> type) {
		super("Unknown type : " + type.getName());
	}
}
