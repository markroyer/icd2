/**
 * 
 */
package icd2.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Royer
 *
 */
public class CoreData implements ModelObject<CoreData, Workspace> {

	private static final Logger logger = LoggerFactory.getLogger(CoreData.class);

	public List<Core> cores;

	private Workspace parent;

	private File file;

	public CoreData(Workspace parent) {
		this.parent = parent;
		this.file = new File(parent.getFile().getAbsolutePath() + File.separatorChar + "cores");
		cores = new ArrayList<>();
	}

	@Override
	public void setParent(Workspace parent) {
		this.parent = parent;
	}

	@Override
	public Workspace getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return cores.toArray(new Core[cores.size()]);
	}

	@Override
	public String getName() {
		return "Cores";
	}

	public void addCore(Core newCore) {
		cores.add(newCore);
		newCore.setParent(this);
	}

	public List<Core> getCores() {
		return cores;
	}

	public File getFile() {
		return file;
	}

	/**
	 * @param coreName
	 *            (Not null)
	 * @return
	 */
	public File createCoreFile(String coreName) {
		File result = new File(file.getPath() + File.separatorChar + coreName + ".h5");
		logger.info("Created core file {}.", result.getAbsolutePath());
		return result;
	}

	public Sample lookupSample(ModelKey<String> sampleKey) throws ObjectNotFound {

		String[] array = sampleKey.getName().split("/");

		if (array.length != 2)
			throw new ObjectNotFound("Unable to find sample for key " + sampleKey);

		Core coreContainingSample = null;

		for (Core c : getCores()) {
			if (c.getName().equals(array[0])) {
				coreContainingSample = c;
				break;
			}
		}

		for (Sample s : coreContainingSample.getSamples()) {
			if (s.getName().equals(array[1]))
				return s;
		}

		throw new ObjectNotFound("Unable to find sample for key " + sampleKey);
	}

	public void removeCore(Core core) {
		int index = -1;
		for (int i = 0; i < cores.size(); i++) {
			if (cores.get(i).getName().equals(core.getName())) {
				index = i;
				break;
			}
		}
		cores.remove(index);
	}

}
