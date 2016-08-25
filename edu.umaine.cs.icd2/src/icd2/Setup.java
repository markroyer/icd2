/**
 * 
 */
package icd2;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Core;
import icd2.model.CoreData;
import icd2.model.DatingProject;
import icd2.model.Workspace;
import icd2.util.HDF5Util;
import icd2.util.WorkspaceUtil;

/**
 * The class has functions for making sure that program starts-up properly and
 * creates necessary workspace folders and files.
 * 
 * @author Mark Royer
 *
 */
public class Setup {

	private static final Logger logger = LoggerFactory.getLogger(Setup.class);

	public void ensureCoreDirectory(Workspace workspace) {

		File coreDir = new File(workspace.getFile().getAbsolutePath() + File.separator + "cores");

		if (!coreDir.exists()) {
			coreDir.mkdirs();
			logger.info("Created core directory {}", coreDir);
		}

		CoreData cd = workspace.getCoreData();

		for (File f : coreDir.listFiles()) {

			try {
				cd.addCore(createCoreObject(f));
			} catch (FileFormatException e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	/**
	 * Creates a {@link Core} object for the given file.
	 * 
	 * @param f
	 *            This must be a valid hdf5 file in the Octave format.
	 * @return The core created from the given file. (Never null)
	 * @throws FileFormatException
	 *             Thrown if something is not right in the format.
	 */
	public Core createCoreObject(File f) throws FileFormatException {

		if (f.isDirectory()) {
			throw new FileFormatException("%s is a directory and will be ignored as a core.", f.getName());
		} else if (!f.getName().endsWith(".h5")) {
			throw new FileFormatException(
					"%s is a file that does not end with the hdf5 extension and will be ignored as a core.",
					f.getName());
		} else {
			try {
				return HDF5Util.readCoreHDF5(f);
			} catch (Exception e) {
				throw new FileFormatException(e.getMessage());
			}
		}
	}

	public Workspace getWorkspace(File file) {

		Workspace result = new Workspace(file);

		ensureCoreDirectory(result);

		for (DatingProject dp : WorkspaceUtil.loadDatingProjects(result)) {
			result.addProject(dp);
		}

		return result;
	}
}
