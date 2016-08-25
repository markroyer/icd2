package icd2.handlers;

import icd2.handlers.AddNewCoreHandler.NewProjectObject;
import icd2.model.CoreModelConstants;
import icd2.model.DatingProject;
import icd2.model.Workspace;

import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class NewProject {
	@Execute
	public void execute(Shell shell, Workspace workspace,
			IEventBroker eventBroker, IEclipseContext ctx,
			EPartService partService, EModelService modelService,
			MApplication application) {

		List<DatingProject> projects = workspace.getProjects();

		InputDialog dlg = new InputDialog(shell, "New Project", "Project Name",
				"Project " + (projects.size() + 1), new Validator(projects));

		if (dlg.open() == Window.OK) {
			eventBroker.post(CoreModelConstants.CREATE_NEW_DATING_PROJECT,
					new NewProjectObject(dlg.getValue()));
			// createNewProject(workspace, eventBroker, partService,
			// modelService,
			// application, dlg.getValue());
		}

	}

	class Validator implements IInputValidator {

		List<DatingProject> projects;

		Validator(List<DatingProject> projects) {
			this.projects = projects;
		}

		/**
		 * Validates the String. Returns null for no error, or an error message
		 * 
		 * @param newText
		 *            the String to validate
		 * @return String
		 */
		@Override
		public String isValid(String newText) {

			for (DatingProject datingProject : projects) {
				if (newText.equals(datingProject.getName())) {
					return "A project with this name already exists.";
				}
			}

			// Input must be OK
			return null;
		}
	}

}