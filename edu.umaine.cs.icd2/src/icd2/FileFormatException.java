/**
 * 
 */
package icd2;

/**
 * Used for files that are not properly formatted. For example, HDF5 core files.
 * 
 * @author Mark Royer
 *
 */
public class FileFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -486366707145014698L;

	/**
	 * Similar to {@link String#format(String, Object...)}.
	 * 
	 * @param format
	 * @param args
	 */
	public FileFormatException(String format, Object... args) {
		super(String.format(format, args));
	}

}
