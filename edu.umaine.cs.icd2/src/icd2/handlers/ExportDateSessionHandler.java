
package icd2.handlers;

import static icd2.widgets.FileDialogExport.getExtension;
import static icd2.widgets.FileDialogExport.isRecognizedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umaine.cs.h5.H5ConnectorException;
import edu.umaine.cs.h5.H5Writer;
import edu.umaine.cs.h5.octave.H5OctaveWriter;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.Plot;
import icd2.widgets.FileDialogExport;

public class ExportDateSessionHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(ExportDateSessionHandler.class);

	@Execute
	public void execute(Shell shell,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession ds) {

		FileDialog saveDialog = FileDialogExport.getFileDialogExport(shell);

		logger.debug("Received {} date session.", ds);

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

		try {
			if (".csv".equalsIgnoreCase(fileExt))
				writeCSVFile(new File(filePath), ds);
			if (".h5".equalsIgnoreCase(fileExt)
					|| ".hdf".equalsIgnoreCase(fileExt))
				writeHDFFile(new File(filePath), ds);
		} catch (FileNotFoundException | H5ConnectorException e) {
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

	public void writeHDFFile(File file, DateSession dateSession)
			throws H5ConnectorException {

		Plot plot = dateSession.getParent().getChart().getPlots()[0][0];

		List<String> labels = new ArrayList<>();
		List<Object> objects = new ArrayList<>();

		labels.add("topyear");
		objects.add(dateSession.getYear(0));

		labels.add("plotmethod");
		objects.add(plot.getPlotMethod().toString());

		labels.add("depth");
		objects.add(dateSession.getDepthArray());

		labels.add("year");
		objects.add(Arrays.stream(dateSession.getYearArray())
				.mapToInt(e -> (int) e).toArray());

		H5Writer writer = new H5OctaveWriter();

		writer.writeHDF5File(file.getAbsolutePath(),
				labels.toArray(new String[labels.size()]), objects.toArray());

	}

}