
package icd2.handlers;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Core;
import icd2.model.CoreModelConstants;
import icd2.model.DatingProject;
import icd2.model.Workspace;
import icd2.util.WorkspaceUtil;

public class RemoveCoreHandler {

	private static final Logger logger = LoggerFactory.getLogger(RemoveCoreHandler.class);

	@Execute
	public void execute(Shell shell, IEventBroker eventBroker, Workspace workspace,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional Core core) {

		List<DatingProject> referencedProjects = WorkspaceUtil.getReferencedProjects(workspace, core);

		if (confirmDeleteReferencedCore(shell, workspace, core, referencedProjects)) {
			// If referencedWorkspaces is empty this is a no op
			WorkspaceUtil.removeProjects(workspace, referencedProjects);
			WorkspaceUtil.removeCore(workspace, core);
			
			referencedProjects.forEach(p -> eventBroker.post(CoreModelConstants.REMOVED_DATING_PROJECT, p));
			eventBroker.post(CoreModelConstants.REMOVED_CORE, core);

			logger.debug("Removed the {} core.", core.getName());
		} else {
			logger.error("Did not delete core {}.  User rejected action.", core.getName());
		}
	}

	private boolean confirmDeleteReferencedCore(Shell parent, Workspace workspace, Core core,
			List<DatingProject> referencedWorkspaces) {

		String message = String.format("Are you sure you want to delete the core data for '%s'?", core.getName());

		if (!referencedWorkspaces.isEmpty()) {
			message = String.format(
					"The core '%s' is referenced by the following projects: %s.  "
							+ "If deleted, the projects will be deleted too.  " + "Are you sure you want to continue?",
					core.getName(),
					referencedWorkspaces.stream().map(e -> "'" + e.getName() + "'").collect(Collectors.joining(", ")));
		}

		return MessageDialog.openConfirm(parent, "Confirm delete?", message);
	}

}