
package icd2.handlers;

import static icd2.widgets.FileDialogExport.getExtension;
import static icd2.widgets.FileDialogExport.isRecognizedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umaine.cs.h5.H5ConnectorException;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.ObjectNotFound;
import icd2.model.Plot;
import icd2.widgets.FileDialogExport;

public class ExportSessionDataHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(ExportSessionDataHandler.class);

	@Execute
	public void execute(Shell shell,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession ds) {

		FileDialog exportDialog = FileDialogExport.getFileDialogExport(shell);

		logger.debug("Received {} date session.", ds);

		String filePath = exportDialog.open();
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
			filePath = exportDialog.open();
			fileExt = getExtension(filePath);
		}

		try {
			if (".csv".equalsIgnoreCase(fileExt))
				writeCSVFile(new File(filePath), ds);
			if (".h5".equalsIgnoreCase(fileExt)
					|| ".hdf".equalsIgnoreCase(fileExt))
				writeHDFFile(new File(filePath), ds);
		} catch (FileNotFoundException | H5ConnectorException
				| ObjectNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("Exported file {}.", filePath);

	}

	/**
	 * Interpolate the given depth data to produce year data based on the given
	 * date session.
	 * 
	 * @param ds
	 *            (Not null)
	 * @param depthData
	 *            (Not null)
	 * @return Interpolated year data
	 */
	private double[] interpolateYearData(DateSession ds,
			List<Double> depthData) {
		double[] dr = ds.getDepthArray();
		double[] yr = ds.getYearArray();
		SplineInterpolator si = new SplineInterpolator();
		PolynomialSplineFunction spf = si.interpolate(dr, yr);

		double[] result = new double[depthData.size()];

		for (int i = 0; i < depthData.size(); i++) {
			result[i] = spf.value(depthData.get(i));
		}

		return result;
	}

	public void writeCSVFile(File file, DateSession dateSession)
			throws FileNotFoundException, ObjectNotFound {

		logger.debug("Need to implement export CSV File");
		
		// Start of csv file code is below

		// PrintWriter out = new PrintWriter(file);
		//
		// Plot plot = dateSession.getParent().getChart().getPlots()[0][0];
		//
		// out.println(plot.getRangeValues().stream()
		// .map(e -> e.getName().split("/")[1])
		// .reduce((e1, e2) -> e1 + ", " + e2).get());
		//
		// double[] depthArray = dateSession.getDepthArray();
		// double[] yearArray = dateSession.getYearArray();
		//
		// double[] interpYearData = interpolateYearData(dateSession,
		// plot.getXData().stream().map(e -> e.doubleValue())
		// .collect(Collectors.toList()));
		//
		// out.println("Depth, Year");
		// for (int i = 0; i < yearArray.length; i++) {
		// out.printf("%f,%.0f\n", depthArray[i], yearArray[i]);
		// }
		//
		// out.flush();
		// out.close();
	}

	public void writeHDFFile(File file, DateSession dateSession)
			throws H5ConnectorException {

		logger.debug("Need to implement export HDF File");

		// Plot plot = dateSession.getParent().getChart().getPlots()[0][0];
		//
		// List<String> labels = new ArrayList<>();
		// List<Object> objects = new ArrayList<>();
		//
		// labels.add("topyear");
		// objects.add(dateSession.getYear(0));
		//
		// labels.add("plotmethod");
		// objects.add(plot.getPlotMethod().toString());
		//
		// labels.add("depth");
		// objects.add(dateSession.getDepthArray());
		//
		// labels.add("year");
		// objects.add(Arrays.stream(dateSession.getYearArray())
		// .mapToInt(e -> (int) e).toArray());
		//
		// H5Writer writer = new H5OctaveWriter();
		//
		// writer.writeHDF5File(file.getAbsolutePath(),
		// labels.toArray(new String[labels.size()]), objects.toArray());

	}

}