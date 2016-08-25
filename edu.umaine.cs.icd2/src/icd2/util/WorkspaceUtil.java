package icd2.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import icd2.model.Core;
import icd2.model.DatingProject;
import icd2.model.Plot;
import icd2.model.PlotValues;
import icd2.model.Workspace;

/**
 * @author Mark Royer
 *
 */
public class WorkspaceUtil {

	private static final Logger logger = LoggerFactory.getLogger(WorkspaceUtil.class);

	public final static String PROJECT_FILE = "project.xml";

	public static List<DatingProject> loadDatingProjects(Workspace workspace) {

		List<DatingProject> datingProjects = new ArrayList<>();

		File workspaceFolder = workspace.getFile();

		for (File f : workspaceFolder.listFiles()) {
			// Is it a project folder?
			if (!("cores".equals(f.getName()) || ".metadata".equals(f.getName()))) {
				try {
					datingProjects.add(loadDatingProject(workspace,
							new File(f.getAbsolutePath() + File.separatorChar + PROJECT_FILE)));
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return datingProjects;
	}

	public static DatingProject refreshDatingProject(Workspace workspace, DatingProject project) {

		DatingProject refreshedDatingProject = null;

		try {
			refreshedDatingProject = loadDatingProject(workspace, new File(workspace.getFile().getAbsolutePath()
					+ File.separatorChar + project.getName() + File.separatorChar + PROJECT_FILE));

			workspace.replaceProject(project, refreshedDatingProject);

		} catch (Exception e) {
			logger.error("Unable to refresh file.", e);
		}

		return refreshedDatingProject;
	}

	/**
	 * @param workspace
	 *            Workspace for dating project. If you pass null, then you
	 *            should probably specify the workspace after calling this
	 *            method. (Null allow)
	 * @param file
	 *            DatingProject object (Not null)
	 * @return May be null if class definition can't be found.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static DatingProject loadDatingProject(Workspace workspace, File file)
			throws FileNotFoundException, IOException {

		// XMLDecoder d = new XMLDecoder(new BufferedInputStream(
		// new FileInputStream(file)));
		//
		// DatingProject datingProject = (DatingProject) d.readObject();
		//
		// d.close();

		XStream xstream = new XStream(new StaxDriver());

		xstream.setClassLoader(WorkspaceUtil.class.getClassLoader());

		DatingProject datingProject = (DatingProject) xstream.fromXML(file);

		datingProject.setParent(workspace);

		return datingProject;
	}

	public static File saveDatingProject(DatingProject datingProject) {

		File file = new File(datingProject.getParent().getFile().getPath() + File.separatorChar
				+ datingProject.getName() + File.separatorChar + PROJECT_FILE);

		try {

			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

			XStream xstream = new XStream(new StaxDriver());

			xstream.toXML(datingProject, out);

			out.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return file;
	}

	public static void moveDatingProject(File prevDirectory, File newDirectory) throws IOException {

		Files.move(prevDirectory.toPath(), newDirectory.toPath(), StandardCopyOption.ATOMIC_MOVE);

	}

	/**
	 * Deletes the project from the workspace in the file system and removes the
	 * project from the workspace object.
	 * 
	 * @param workspace
	 * @param project
	 */
	public static void removeProject(Workspace workspace, DatingProject project) {

		File file = new File(workspace.getFile().getPath() + File.separatorChar + project.getName());

		try {

			logger.info("Attempting to delete {}", file.getAbsolutePath());

			Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});

			logger.info("File {} exists? {}", file.getAbsolutePath(), file.exists());


			Field fieldProjects = workspace.getClass().getDeclaredField("projects");
			fieldProjects.setAccessible(true);

			@SuppressWarnings("unchecked")
			List<DatingProject> projects = (List<DatingProject>) fieldProjects.get(workspace);

			projects.remove(project);

			logger.info("Deleted project {}", project.getName());
			
		} catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static DatingProject getProject(Workspace workspace, String label) {

		for (DatingProject dp : workspace.getProjects()) {
			if (dp.getName().equals(label)) {
				return dp;
			}
		}

		return null;
	}

	public static void removeCore(Workspace workspace, Core core) {

		if (HDF5Util.removeCoreHDF5(core))
			workspace.removeCore(core);

	}

	public static List<DatingProject> getReferencedProjects(Workspace workspace, Core core) {

		List<DatingProject> referencedProjects = new ArrayList<>();

		String coreName = core.getName();

		for (DatingProject dp : workspace.getProjects()) {
			plotSearch: for (Plot[] plots : dp.getChart().getPlots()) {
				for (Plot plot : plots) {
					for (PlotValues pv : plot.getRangeValues()) {
						if (pv.getValuesKey().getName().startsWith(coreName)) {
							referencedProjects.add(dp);
							break plotSearch; // Search next dating project
						}
					}
				}
			}
		}

		return referencedProjects;
	}

	/**
	 * Removes each project in the list from the workspace. Does not remove the
	 * projects from the given list.
	 * 
	 * @param workspace
	 *            The workspace to remove the projects from (Not null)
	 * @param projects
	 *            A list of projects to remove (Not null)
	 */
	public static void removeProjects(Workspace workspace, List<DatingProject> projects) {
		projects.forEach(p -> removeProject(workspace, p));
	}
}
