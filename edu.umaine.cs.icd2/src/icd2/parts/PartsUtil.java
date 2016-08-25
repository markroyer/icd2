/**
 * 
 */
package icd2.parts;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Chart;
import icd2.model.CoreModelConstants;
import icd2.model.DatingProject;
import icd2.model.ModelObject;

/**
 * Tools for working with parts.
 * 
 * @author Mark Royer
 *
 */
public class PartsUtil {

	private static final Logger logger = LoggerFactory.getLogger(PartsUtil.class);

	@Inject
	@Optional
	public static void onDisplayProjectEvent(
			@UIEventTopic(CoreModelConstants.DISPLAY_PROJECT_EVENT) DatingProject project, EPartService partService,
			EModelService modelService, MApplication application, IEclipseContext iectx, IEventBroker eventBroker
	// ,@Named(value = PreferenceKeys.OPEN_PROJECTS) Set<String> openProjects
	) {

		Chart chart = project.getChart();
		if (chart.getActiveDateSession() == null && project.getSessions().size() > 0) {
			// If no active session and one available set it to the first
			chart.setActiveDateSession(project.getSessions().get(0));
		} else if (chart.getActiveDateSession() == null) { // Really nothing?
			// No active session found abort
			logger.error("No active date session found for chart {}", chart.getName());
			MessageDialog.openError(null, "Error",
					"Unable to find active date session.  Please create or select one first.");
			return;
		}

		List<MPart> projectPartsList = modelService.findElements(application, "icd2.partdescriptor.project",
				MPart.class, null);

		for (MPart mPart : projectPartsList) {

			if (mPart.getContext() != null) {

				if (mPart.getContext().get(DatingProject.class).equals(project)) {
					partService.bringToTop(mPart);
					logger.info("Bringing MPart to the front {}", mPart);
					return;
				}
			} else {
				logger.info("This MPart does not have a context {}", mPart);

				iectx.set(DatingProject.class, project);

				partService.bringToTop(mPart);

				return;
			}
		}

		// IEclipseContext newPartContext = E4Workbench.getServiceContext();
		//
		// newPartContext.set(DatingProject.class, project);

		MPart partToDisplay = partService.createPart("icd2.partdescriptor.project");

		// partToDisplay.setContext(iectx.createChild());

		iectx.set(DatingProject.class, project);

		partToDisplay.setCloseable(true);
		partToDisplay.setLabel(project.getName());

		List<MPartStack> partStackList = modelService.findElements(application, "icd2.partstack.center",
				MPartStack.class, null);

		if (partStackList != null) {

			MPartStack partStack = partStackList.get(0); // Only 1 partstack
															// named center

			partStack.setVisible(true);

			partStack.getChildren().add(partToDisplay);

			// We want to know when a project has been closed so that we can close it in other places.
			eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, new EventHandler() {

				@Override
				public void handleEvent(Event event) {
					Object part = event.getProperty(UIEvents.EventTags.ELEMENT);
					// boolean tbr = (Boolean) event
					// .getProperty(UIEvents.EventTags.NEW_VALUE);
					if (part instanceof MPart && ((MPart) part).getLabel().equals(project.getName())) {

						MPart mpart = (MPart) part;

						// @SuppressWarnings("unchecked")
						// Set<String> openedProjects = ((Set<String>)
						// iectx
						// .get(PreferenceKeys.OPEN_PROJECTS));
						// openedProjects.remove(mpart.getLabel());

						logger.info("Closed mpart named {}", mpart.getLabel());

						eventBroker.send(CoreModelConstants.CLOSE_PROJECT_EVENT, (DatingProject) project);
						eventBroker.unsubscribe(this);
					}
				}
			});

			partService.activate(partToDisplay, true);

		}
	}

	@Inject
	@Optional
	public void onNameChange(MApplication application, EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_MODELOBJECT_NAME_CHANGE) ModelObject<?, ?> mo) {

		List<MPart> projectPartsList = modelService.findElements(application, "icd2.partdescriptor.project",
				MPart.class, null);

		for (MPart mPart : projectPartsList) {
			if (mPart.getContext().get(DatingProject.class).equals(mo)) {
				mPart.setLabel(mo.getName());
			}
		}

	}

	@Inject
	@Optional
	public static void onDisplayProjectEvent(
			@UIEventTopic(CoreModelConstants.REMOVED_DATING_PROJECT) DatingProject project, MApplication application,
			EPartService partService, EModelService modelService) {

		List<MPart> projectPartsList = modelService.findElements(application, "icd2.partdescriptor.project",
				MPart.class, null);
		
		for (MPart mPart : projectPartsList) {
			if(mPart.getContext().get(DatingProject.class).equals(project)) {
				partService.hidePart(mPart, true);
				break;
			}
		}
	}

}
