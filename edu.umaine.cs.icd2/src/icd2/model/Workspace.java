/**
 * 
 */
package icd2.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mark Royer
 *
 */
public class Workspace implements ModelObject<Workspace, ModelObject<?, ?>> {

	File file;

	CoreData coreData;

	List<DatingProject> projects;

	/**
	 * Returns a completely empty workspace object. This object will need to be
	 * filled with proper data.
	 * 
	 * @param url
	 */
	public Workspace(File file) {
		this.file = file;
		coreData = new CoreData(this);
		projects = new ArrayList<DatingProject>();
	}

	@Override
	public void setParent(ModelObject<?, ?> parent) {
		// Do nothing. This should be the root.
	}

	@Override
	public ModelObject<?, ?> getParent() {
		return null; // This should be the root.
	}

	@Override
	public ModelObject<?, ?>[] children() {

		// + 1 for the coreData node
		ModelObject<?, ?>[] result = new ModelObject<?, ?>[projects.size() + 1];

		result[0] = coreData;
		System.arraycopy(projects.toArray(), 0, result, 1, projects.size());

		return result;
	}

	@Override
	public String getName() {
		return "Workspace";
	}

	public void addCore(Core newCore) {
		coreData.addCore(newCore);
	}

	public List<DatingProject> getProjects() {
		return Collections.unmodifiableList(projects);
	}

	public void addProject(DatingProject datingProject) {
		projects.add(datingProject);
	}

	public CoreData getCoreData() {
		return coreData;
	}

	public File getFile() {
		return file;
	}

	public void replaceProject(DatingProject project, DatingProject refreshedDatingProject) {
		projects.set(projects.indexOf(project), refreshedDatingProject);
	}

	public void removeCore(Core core) {
		coreData.removeCore(core);
	}

}
