/**
 * 
 */
package icd2.util;

import icd2.model.Sample;
import icd2.model.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Mark Royer
 *
 */
public class CSVFileReader {

	/**
	 * Parse the given file and create a corrected version in the Files
	 * directory.
	 * 
	 * @param csvFile
	 *            The file to parse.
	 * @throws DataFileException
	 */
	public static List<Sample> read(File csvFile) throws DataFileException {

		int lineNumber = 0;
		String line = "";

		boolean minus99 = false;
		boolean emptyValues = false;
		boolean negatives = false;

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			double previousTop = -1, previousBottom = -1;

			List<Sample> samples;

			if ((line = br.readLine()) != null) {
				samples = createSamples(line);
			} else {
				throw new DataFileException("Unable to read header line.");
			}

			Value[] lineVals = new Value[samples.size()]; // For temporarily
															// holding line
															// values

			/*
			 * Read the file line by line
			 */
			while ((line = br.readLine()) != null) {

				lineNumber++;

				StringTokenizer st = new StringTokenizer(line, ",\t\n\r\f");
				int tokenNumber = 0;

				while (st.hasMoreTokens()) {
					String token = "";
					token = st.nextToken();

					Value val = null;

					if (token.trim().equals("")) {
						emptyValues = true;
					} else {
						double dval = Double.parseDouble(token);
						if (dval == -99) {
							minus99 = true;
						} else if (dval < 0 && dval != -99) {
							negatives = true;
						}

						val = new Value(dval);

					}

					lineVals[tokenNumber] = val;

					tokenNumber++;
				}

				if (lineVals.length < 3) {
					throw new DataFileException(
							"Not enough columns.  Requires at least three. tube, top, bottom.");
				}

				if (lineVals[1] == null || lineVals[2] == null) {
					throw new DataFileException(
							"There are missing top or bottom values.");
				}

				// top depth is second val
				double top = lineVals[1].doubleValue();

				// bottom depth is third val
				double bottom = lineVals[2].doubleValue();

				// Do some sanity checks with the data
				if (top > bottom) {
					throw new DataFileException(
							"The top value was greater than the "
									+ "bottom value on the same row. "
									+ DataFileException.lineSeparator
									+ "Are the first three columns "
									+ "tube, top depth, bottom depth?", line,
							lineNumber);
				}
				if (top < previousTop) {
					throw new DataFileException(
							"The top value was less than the "
									+ "top value on the previous row. "
									+ DataFileException.lineSeparator
									+ "Make sure the rows are ordered "
									+ "ascendingly by depth.", line, lineNumber);
				}
				if (bottom < previousBottom && lineNumber != 2) {
					throw new DataFileException(
							"The bottom value was less than the "
									+ "bottom value on the previous row. "
									+ DataFileException.lineSeparator
									+ "Make sure the rows are ordered "
									+ "ascendingly by depth.", line, lineNumber);
				}

				previousTop = top;
				previousBottom = bottom;

				for (int i = 0; i < lineVals.length; i++) {
					samples.get(i).addValue(lineVals[i]);
					lineVals[i] = null; // Reset the value to null for next line
				}
			}

			if (negatives || minus99 || emptyValues) {
				throw new DataFileException(
						"Contained negatives, -99, or empty values, but loaded.");
			}

			return samples;

		} catch (IOException e) {
			throw new DataFileException("Unable to read file data.");
		}
	}

	/**
	 * Read the header and generate the empty sample objects. An example header
	 * is "tube","top","bottom","length","Na","NH4","K","Mg","Ca","Cl","NO3",
	 * "SO4".
	 * 
	 * Currently using simple "," separation.
	 * 
	 * @param header
	 *            (Not null)
	 * @return The empty samples for the header ready to be populated with
	 *         actual data (Never null)
	 */
	private static List<Sample> createSamples(String header) {

		StringTokenizer st = new StringTokenizer(header, ",");
		List<Sample> result = new ArrayList<Sample>();
		// Pattern pattern = Pattern.compile("\\((.*?)\\)");

		while (st.hasMoreTokens()) {

			String colName = st.nextToken();
			// could do some pattern matching and extract the unit, but for
			// simplicity not now.
			// String unit = "";
			// Matcher matcher = pattern.matcher(colName);
			// if (matcher.find()) {
			// colName = colName.substring(0, colName.indexOf('('));
			// unit = matcher.group(1);
			// }

			result.add(new Sample(colName));
		}
		return result;
	}
}
