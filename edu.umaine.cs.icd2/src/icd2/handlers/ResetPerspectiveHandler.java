
package icd2.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on
 * 
 * http://stackoverflow.com/questions/19717154/how-do-i-reset-perspective-for-eclipse-e4-rcp-application/20106360#
 * 20106360
 * 
 * 2/4/2016
 *
 */
public class ResetPerspectiveHandler {

	private static final Logger logger = LoggerFactory.getLogger(ResetPerspectiveHandler.class);

	@Execute
	public void execute(EModelService modelService, MApplication application) {
//		MPerspectiveStack perspectiveStack = (MPerspectiveStack) modelService
//				.find(CopyPerspectiveSnippetProcessor.MAIN_PERSPECTIVE_STACK_ID, application);
//
//		logger.debug("Selected perspective is {} and selected window is {}", perspectiveStack.getSelectedElement(),
//				application.getSelectedElement());

//		modelService.resetPerspectiveModel(perspectiveStack.getSelectedElement(), application.getSelectedElement());

//		 workbench.getActiveWorkbenchWindow().getActivePage().resetPerspective();

//		 PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();
//
//		perspectiveStack.getSelectedElement().setVisible(true);
//		perspectiveStack.setVisible(true);

		logger.debug("Not implemented...");
		
				
	}

}