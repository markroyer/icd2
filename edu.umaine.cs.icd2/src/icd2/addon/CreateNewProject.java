/**
 * 
 */
package icd2.addon;

import icd2.handlers.AddNewCoreHandler.NewProjectObject;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DatingProject;
import icd2.model.Workspace;
import icd2.util.WorkspaceUtil;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Royer
 *
 */
public class CreateNewProject {

	private static final Logger logger = LoggerFactory.getLogger(CreateNewProject.class);

	@Inject
	@Optional
	public void createNewProject(@UIEventTopic(CoreModelConstants.CREATE_NEW_DATING_PROJECT) NewProjectObject npo,
			Workspace workspace, IEventBroker eventBroker, EPartService partService, EModelService modelService,
			MApplication application) {
		DatingProject project = new DatingProject(workspace, npo.getProjectName(), npo.getSamples(),
				npo.getTopAndBottom(), npo.getPlotMethod());

		File pFile = new File(workspace.getFile().getPath() + File.separatorChar + project.getName());

		if (pFile.exists()) {
			logger.error("A project named {} already exists and will not be recreated.", pFile.getAbsolutePath());
		} else {
			pFile.mkdir();
		}

		DateSession ds = new DateSession(project, project.getName(), npo.getTopYear());
		project.addSession(ds);
		project.getChart().setActiveDateSession(ds);

		WorkspaceUtil.saveDatingProject(project);

		workspace.addProject(project);

		eventBroker.send(CoreModelConstants.ON_NEW_DATING_PROJECT, project);

		eventBroker.send(CoreModelConstants.DISPLAY_PROJECT_EVENT, project);

	}

}
