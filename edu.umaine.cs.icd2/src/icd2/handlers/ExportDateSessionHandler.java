
package icd2.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
import icd2.model.Plot;

public class ExportDateSessionHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(ExportDateSessionHandler.class);

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

	/**
	 * @param filePath
	 *            Fully qualified path to file (Not null)
	 * @return The extension (eg, .csv)
	 */
	protected String getExtension(String filePath) {
		return filePath.lastIndexOf(".") < 0 ? ""
				: filePath.substring(filePath.lastIndexOf("."));
	}

	protected boolean isRecognizedFile(String fileExt) {
		return Arrays.stream(FILTER_EXTS).anyMatch(e -> e.contains(fileExt));
	}

	@Execute
	public void execute(Shell shell,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession ds) {

		// Reuse dialog so we have the old directory info
		if (saveDialog == null) {
			saveDialog = createDiaolog(shell);
		}

		logger.debug("Received {} date session.", ds);

		String filePath = saveDialog.open();
		String fileExt = filePath == null ? null : getExtension(filePath);

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

		try {
			if (".csv".equals(fileExt))
				writeCSVFile(new File(filePath), ds);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("Exported file {}.", filePath);
	}

	public void writeCSVFile(File file, DateSession dateSession)
			throws FileNotFoundException {

		PrintWriter out = new PrintWriter(file);

		out.println(dateSession.getYear(0));
		Plot plot = dateSession.getParent().getChart().getPlots()[0][0];
		out.println(plot.getPlotMethod());
		out.println(plot.getRangeValues().stream()
				.map(e -> e.getName().split("/")[1])
				.reduce((e1, e2) -> e1 + ", " + e2).get());
		double[] depthArray = dateSession.getDepthArray();
		double[] yearArray = dateSession.getYearArray();
		out.println(depthArray.length);
		out.println("Depth, Year");
		for (int i = 0; i < yearArray.length; i++) {
			out.printf("%f,%.0f\n", depthArray[i], yearArray[i]);
		}

		out.flush();
		out.close();
	}

}