
package icd2.handlers;

import static icd2.widgets.FileDialogExport.getExtension;
import static icd2.widgets.FileDialogExport.isRecognizedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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
import edu.umaine.cs.h5.H5Writer;
import edu.umaine.cs.h5.octave.H5OctaveWriter;
import icd2.model.Core;
import icd2.model.CoreData;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.ObjectNotFound;
import icd2.model.Plot;
import icd2.model.Sample;
import icd2.model.Value;
import icd2.util.DataFileException;
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
		} catch (FileNotFoundException | H5ConnectorException | ObjectNotFound
				| DataFileException e) {
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
	private List<Double> interpolateYearData(DateSession ds,
			List<Double> depthData) {
		double[] dr = ds.getDepthArray();
		double[] yr = ds.getYearArray();
		SplineInterpolator si = new SplineInterpolator();
		PolynomialSplineFunction spf = si.interpolate(dr, yr);

		List<Double> result = new ArrayList<Double>(depthData.size());

		int i = 0;

		for (; i < depthData.size() && (depthData.get(i) == null
				|| spf.isValidPoint(depthData.get(i))); i++) {
			result.add(depthData.get(i) == null ? null
					: spf.value(depthData.get(i)));
		}

		if (i < depthData.size()) {
			logger.warn("Not enough dates.  Did not interpolate {} values.",
					depthData.size() - i);
		}

		return result;
	}

	public void writeCSVFile(File file, DateSession dateSession)
			throws FileNotFoundException, ObjectNotFound, DataFileException {

		logger.debug("Need to implement export CSV File");

		// Start of csv file code is below

		PrintWriter out = new PrintWriter(file);

		Plot plot = dateSession.getParent().getChart().getPlots()[0][0];

		CoreData cd = dateSession.getParent().getParent().getCoreData();

		Core core = cd
				.lookupSample(plot.getDomainValues().get(0).getValuesKey())
				.getParent();

		List<Sample> samples = core.getSamples();

		List<Double> resampleYear = interpolateYearData(dateSession,
				plot.getXData());
		int sSize = resampleYear.size();

		// Write header info
		out.printf("%s", "Year");
		for (int i = 0; i < samples.size(); i++) {
			out.printf(", %s", samples.get(i).getName());
		}
		out.println();

		for (int i = 0; i < sSize; i++) {
			out.printf("%f", resampleYear.get(i));
			for (int j = 0; j < samples.size(); j++) {
				out.printf(", %f",
						samples.get(j).getValues().get(i).doubleValue());
			}
			out.println();
		}

		out.flush();
		out.close();
	}

	public void writeHDFFile(File file, DateSession dateSession)
			throws H5ConnectorException, ObjectNotFound {

		logger.debug("Need to implement export HDF File");

		Plot plot = dateSession.getParent().getChart().getPlots()[0][0];

		CoreData cd = dateSession.getParent().getParent().getCoreData();

		Core core = cd
				.lookupSample(plot.getDomainValues().get(0).getValuesKey())
				.getParent();

		List<Sample> samples = core.getSamples();

		List<Double> resampleYear = interpolateYearData(dateSession,
				plot.getXData());

		List<String> labels = new ArrayList<>();
		List<Object> objects = new ArrayList<>();

		labels.add("year");
		objects.add(resampleYear);

		samples.stream().forEachOrdered(e -> labels.add(e.getName()));

		samples.stream().forEachOrdered(e -> {
			List<Double> newVals = new ArrayList<>(resampleYear.size());
			List<Value> vals;
			try {
				vals = e.getValues();
			} catch (DataFileException e1) {
				e1.printStackTrace();
				throw new RuntimeException("Unable to get values.");
			}
			IntStream.range(0, resampleYear.size()).forEachOrdered(
					i -> newVals.add(vals.get(i).doubleValue()));
			objects.add(newVals);
		});

		H5Writer writer = new H5OctaveWriter();

		writer.writeHDF5File(file.getAbsolutePath(),
				labels.toArray(new String[labels.size()]), objects.toArray());

	}

}