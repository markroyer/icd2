/**
 * 
 */
package icd2.util;

/**
 * An exception that is thrown when there is an error parsing a data file.
 * 
 * @author Mark Royer
 * 
 */
public class DataFileException extends Exception {

	/**
	 * For serializing.
	 */
	private static final long serialVersionUID = 637042222858259641L;

	public static final String lineSeparator = System
			.getProperty("line.separator");

	private String line;
	
	private Integer lineNumber;

	/**
	 * @param message
	 *            Message to user.
	 * @param lineOccurred
	 *            The line the error occurred.
	 */
	public DataFileException(String message, String line, int lineOccurred) {
		super(message);
		this.line = line;
		this.lineNumber = lineOccurred;
	}
	
	public DataFileException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		return "An error occurred while parsing the data file." + lineSeparator
				+ "Reason: " + this.getMessage() + (lineNumber != null ? lineSeparator
				+ "Occurred on line number: " + lineNumber : "") + (line != null ? lineSeparator
						+ "Line: " + line : "");
	}

}
