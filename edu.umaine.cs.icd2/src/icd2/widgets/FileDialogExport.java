/**
 * 
 */
package icd2.widgets;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Class to help with managing and creating the export dialog.
 * 
 * @author Mark Royer
 *
 */
public class FileDialogExport {

	private static FileDialog exportDialog;

	private static final String[] FILTER_NAMES = {
			"Comma Separated Values Files (*.csv)",
			"Heirarchical Data Format (*.h5;*.hdf)",
			"Microsoft Excel Spreadsheet Files (*.xls)", "All Files (*.*)" };

	private static final String[] FILTER_EXTS = { "*.csv", "*.h5;*.hdf",
			"*.xls", "*.*" };

	public static FileDialog getFileDialogExport(Shell shell) {

		// Reuse dialog so we have the old directory info
		if (exportDialog == null)
			exportDialog = createDiaolog(shell);

		return exportDialog;
	}

	private static FileDialog createDiaolog(Shell shell) {

		FileDialog resultDialog = new FileDialog(shell, SWT.SAVE);

		resultDialog.setText("Export Data");

		resultDialog.setFilterNames(FILTER_NAMES);

		resultDialog.setFilterExtensions(FILTER_EXTS);

		resultDialog.setOverwrite(true); // prompt, yes!

		resultDialog.setFilterIndex(0); // csv files

		return resultDialog;

	}

	/**
	 * @param filePath
	 *            Fully qualified path to file (Null allowed)
	 * @return The extension (eg, .csv), an empty string if no . exists, or null
	 *         if filePath == null
	 */
	public static String getExtension(String filePath) {
		if (filePath == null)
			return null;
		else
			return filePath.lastIndexOf(".") < 0 ? ""
					: filePath.substring(filePath.lastIndexOf("."));
	}

	public static boolean isRecognizedFile(String fileExt) {
		return Arrays.stream(FILTER_EXTS).anyMatch(e -> e.contains(fileExt));
	}

}
