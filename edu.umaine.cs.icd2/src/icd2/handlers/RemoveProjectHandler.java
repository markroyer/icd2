
package icd2.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.CoreModelConstants;
import icd2.model.DatingProject;
import icd2.model.Workspace;
import icd2.util.WorkspaceUtil;

public class RemoveProjectHandler {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(RemoveProjectHandler.class);

	@Execute
	public void execute(Shell shell, IEventBroker eventBroker, Workspace workspace,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DatingProject project) {

		if (project == null)
			return;

		boolean isDeleteProject = MessageDialog.openConfirm(shell, "Delete Project",
				"Are you sure that you want to delete " + project.getName()
						+ "?\nThis will perminantly delete the project from the system.");

		if (isDeleteProject) {
			WorkspaceUtil.removeProject(workspace, project);
			eventBroker.post(CoreModelConstants.REMOVED_DATING_PROJECT, project);
		}

	}

	@CanExecute
	public boolean canExecute() {

		return true;
	}

}