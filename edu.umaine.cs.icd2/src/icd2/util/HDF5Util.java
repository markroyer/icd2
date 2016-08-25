/**
 * 
 */
package icd2.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.umaine.cs.h5.NameValuePair;
import edu.umaine.cs.h5.octave.H5OctaveReader;
import edu.umaine.cs.h5.octave.H5OctaveWriter;
import icd2.FileFormatException;
import icd2.model.Core;
import icd2.model.Sample;
import icd2.model.Value;

/**
 * A collection of methods for working with HDF5 files.
 * 
 * @author Mark Royer
 *
 */
public class HDF5Util {

	public static final String CORENAME = "core_name";
	public static final String CORETOPDATE = "core_topdate";
	public static final String COREHEADERS = "core_headers";

	/**
	 * The resulting core sample values are not loaded. These will be initialized (lazily) on a need to know basis.
	 * 
	 * This file must be HDF5 (Octave format) with the following additional meta data:
	 * 
	 * <ul>
	 * <li>core_name (String)</li>
	 * <li>core_topdate (float)</li>
	 * <li>core_headers (String[])</li>
	 * </ul>
	 * 
	 * @param file
	 *            (Not null)
	 * @return Initialized core for the given HDF5 (Octave format file)
	 * @throws Exception
	 */
	public static Core readCoreHDF5(File file) throws Exception {

		List<NameValuePair> data = new H5OctaveReader().readHDF5File(file,
				new String[] { CORENAME, CORETOPDATE, COREHEADERS });

		String coreName = null;
		float topDate = Float.NaN;
		String[] headers = null;

		for (NameValuePair p : data) {
			if ("core_name".equals(p.getName())) {
				coreName = (String) p.getValue();
			} else if ("core_topdate".equals(p.getName())) {
				topDate = (Float) p.getValue();
			} else if ("core_headers".equals(p.getName())) {
				headers = (String[]) p.getValue();
			} else {
				throw new FileFormatException("Unrecognized initialization value.");
			}
		}

		if (coreName == null) {
			throw new FileFormatException("%s missing from HDF5 core file %s.", CORENAME, file.getAbsoluteFile());
		} else if (topDate == Float.NaN) {
			throw new FileFormatException("%s missing from HDF5 core file %s.", CORENAME, file.getAbsoluteFile());
		} else if (headers == null) {
			throw new FileFormatException("%s missing from HDF5 core file %s.", COREHEADERS, file.getAbsoluteFile());
		}

		List<Sample> samples = new ArrayList<Sample>(headers.length);

		for (String s : headers) {
			// TODO Really shouldn't be storing Strings with padding in the file
			samples.add(new Sample(s.trim())); // The value will be initialized lazily...
		}

		return new Core(file, coreName, topDate, samples);
	}

	/**
	 * Saves a new core file in cores directory. The name of the file will be core.getName() + ".h5".
	 * 
	 * @param core
	 *            (Not null)
	 * @throws Exception
	 */
	public static void saveCoreHDF5(Core core) throws Exception {

		List<Sample> samples = core.getSamples();

		// + 3 for core name, topdate, and headers
		List<String> labels = new ArrayList<>(samples.size() + 3);
		List<Object> objs = new ArrayList<>(samples.size() + 3);
		labels.add(CORENAME);
		objs.add(core.getName());
		labels.add(CORETOPDATE);
		objs.add(core.getTopDate());
		labels.add(COREHEADERS);
		objs.add(core.getHeaders());
		for (Sample s : samples) {
			labels.add(s.getName());
			objs.add(s.getValuesAsDoubleArray());
		}

		String fileName = core.getFile().getAbsolutePath();

		new H5OctaveWriter().writeHDF5File(fileName, labels.toArray(new String[labels.size()]), objs.toArray());

	}

	public static <T extends Value> List<Value> readCoreSample(File file, String name, Class<T> clazz)
			throws Exception {

		List<NameValuePair> data = new H5OctaveReader().readHDF5File(file, new String[] { name });

		double[] theList = (double[]) data.get(0).getValue();

		List<Value> result = new ArrayList<Value>(theList.length);
		for (int i = 0; i < theList.length; i++) {
			result.add(new Value(theList[i]));
		}

		return result;
	}

	public static boolean removeCoreHDF5(Core core) {
		return core.getFile().delete();
	}
}
