
package icd2.handlers;

import java.util.Arrays;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DatingProject;

public class ExportHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(ExportHandler.class);

	private static FileDialog saveDialog;

	private static final String[] FILTER_NAMES = {
			"Comma Separated Values Files (*.csv)",
			"Heirarchical Data Format (*.h5;*.hdf)",
			"Microsoft Excel Spreadsheet Files (*.xls)", "All Files (*.*)" };

	private static final String[] FILTER_EXTS = { "*.csv", "*.h5;*.hdf",
			"*.xls", "*.*" };

	private static FileDialog createDiaolog(Shell shell) {

		FileDialog resultDialog = new FileDialog(shell, SWT.SAVE);

		resultDialog.setText("Export Data");

		resultDialog.setFilterNames(FILTER_NAMES);

		resultDialog.setFilterExtensions(FILTER_EXTS);

		resultDialog.setOverwrite(true); // prompt, yes!

		resultDialog.setFilterIndex(0); // csv files

		return resultDialog;

	}

	protected String getExtension(String filePath) {
		return filePath == null ? null
				: filePath.lastIndexOf(".") < 0 ? ""
						: filePath.substring(filePath.lastIndexOf("."));
	}

	protected boolean isRecognizedFile(String fileExt) {
		return Arrays.stream(FILTER_EXTS).anyMatch(e -> e.contains(fileExt));
	}

	@Execute
	public void execute(Shell shell,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession ds,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DatingProject dp) {

		// If a project is selected default to the active session
		if (dp != null)
			ds = dp.getChart().getActiveDateSession();

		if (saveDialog == null) {
			saveDialog = createDiaolog(shell);
		}

		logger.debug("Received {} date session and {} date project", ds, dp);

		String filePath = saveDialog.open();
		String fileExt = getExtension(filePath);

		while (fileExt != null && !isRecognizedFile(fileExt)) {
			MessageBox unknownDialog = new MessageBox(shell,
					SWT.ICON_INFORMATION | SWT.OK);
			unknownDialog.setText("Unknown Extension");
			unknownDialog
					.setMessage(String.format(
							"The specified extension '%s' is not known.\n"
									+ "Please enter a valid extension.",
							fileExt));
			unknownDialog.open();
			filePath = saveDialog.open();
			fileExt = getExtension(filePath);
		}

		logger.info("Exported file {}.", filePath);
	}

}