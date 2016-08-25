package icd2.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.util.WorkspaceUtil;

public class DatingProjectNameModifier extends AbstractModelModifier<DatingProject, String> {

	private static final Logger logger = LoggerFactory.getLogger(DatingProjectNameModifier.class);

	private String oldValue;

	@Override
	public void redo(Field f, DatingProject obj, String val) {
		f.setAccessible(true);

		File workspaceLocation = obj.getParent().getFile();

		try {

			oldValue = obj.getName();
			String oldPath = workspaceLocation.getPath() + File.separator + obj.getName();
			String newPath = workspaceLocation.getPath() + File.separator + val;

			logger.info("Setting dating project name to {}", val);

			f.set(obj, val);
			WorkspaceUtil.moveDatingProject(new File(oldPath), new File(newPath));

			WorkspaceUtil.saveDatingProject(obj);

		} catch (IOException | IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			try {
				f.set(obj, oldValue);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		logger.info("Set project name to {}", obj.getName());
	}

	@Override
	public void undo(Field f, DatingProject obj) {
		redo(f, obj, oldValue);
	}

}
