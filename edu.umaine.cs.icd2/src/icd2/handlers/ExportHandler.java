
package icd2.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DatingProject;

public class ExportHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(ExportHandler.class);

	@Execute
	public void execute(Shell shell,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession ds,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DatingProject dp) {

		// If a project is selected default to the active session
		if (dp != null)
			ds = dp.getChart().getActiveDateSession();
		
		logger.debug("Received {} date session and {} date project", ds, dp);
		
		FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);

		saveDialog.setText("Export Data");

		saveDialog
				.setFilterExtensions(new String[] { "*.csv", "*.h5", "*.xls" });

		saveDialog.setOverwrite(true); // prompt, yes!

		saveDialog.setFilterIndex(0); // csv files

		String filePath = saveDialog.open();

		logger.info("Exported file {}.", filePath);
	}

}