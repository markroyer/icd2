 
package icd2.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportHandler {
	
	private static final Logger logger = LoggerFactory
			.getLogger(ExportHandler.class);
	
	@Execute
	public void execute(Shell shell) {
		
		FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		
		saveDialog.setText("Export Data");
		
		saveDialog.setFilterExtensions(new String[]{"*.csv","*.h5","*.xls"});
		
		saveDialog.setOverwrite(true); // prompt, yes!
				
		saveDialog.setFilterIndex(0); // csv files
		
		String filePath = saveDialog.open();
	
		logger.info("Exported file {}.", filePath);
	}
		
}